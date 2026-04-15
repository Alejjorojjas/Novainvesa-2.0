package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.*;
import com.novainvesa.backend.entity.User;
import com.novainvesa.backend.entity.UserAddress;
import com.novainvesa.backend.entity.Wishlist;
import com.novainvesa.backend.exception.ResourceNotFoundException;
import com.novainvesa.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Logica de negocio para el perfil del usuario autenticado.
 * Maneja perfil, historial de pedidos, direcciones guardadas y lista de deseos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final WishlistRepository wishlistRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ─── Perfil ───────────────────────────────────────────────────────────────

    public UserProfileResponse getProfile(String email) {
        return toProfileResponse(findByEmail(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        return toProfileResponse(userRepository.save(user));
    }

    // ─── Historial de pedidos ─────────────────────────────────────────────────

    public PaginatedResponse<OrderSummaryResponse> getUserOrders(String email, int page, int size) {
        User user = findByEmail(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.novainvesa.backend.entity.Order> orders =
                orderRepository.findByUserId(user.getId(), pageable);
        List<OrderSummaryResponse> items = orders.getContent().stream()
                .map(o -> new OrderSummaryResponse(
                        o.getOrderCode(),
                        o.getTotal(),
                        o.getOrderStatus().name(),
                        o.getPaymentStatus().name(),
                        o.getCreatedAt()))
                .toList();
        return PaginatedResponse.<OrderSummaryResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .hasNext(orders.hasNext())
                .hasPrevious(orders.hasPrevious())
                .build();
    }

    // ─── Direcciones ──────────────────────────────────────────────────────────

    public List<AddressResponse> getAddresses(String email) {
        User user = findByEmail(email);
        return userAddressRepository.findByUserIdOrderByIsDefaultDesc(user.getId())
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = findByEmail(email);

        // Si se marca como default, quitar el default anterior
        if (request.isDefault()) {
            List<UserAddress> existing = userAddressRepository.findByUserIdOrderByIsDefaultDesc(user.getId());
            existing.forEach(a -> {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    userAddressRepository.save(a);
                }
            });
        }

        UserAddress addr = UserAddress.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .city(request.getCity())
                .address(request.getAddress())
                .neighborhood(request.getNeighborhood())
                .notes(request.getNotes())
                .isDefault(request.isDefault())
                .build();

        return toAddressResponse(userAddressRepository.save(addr));
    }

    // ─── Lista de deseos ──────────────────────────────────────────────────────

    public List<WishlistItemResponse> getWishlist(String email) {
        User user = findByEmail(email);
        // findByUserId retorna List<Wishlist> con el User embebido
        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(w -> {
                    if (w.getProduct() == null) return null;
                    var p = w.getProduct();
                    String image = (p.getImages() != null && !p.getImages().isEmpty())
                            ? p.getImages().get(0) : null;
                    return new WishlistItemResponse(
                            p.getId(), p.getSlug(), p.getName(), p.getPrice(),
                            image, Boolean.TRUE.equals(p.getInStock()), w.getCreatedAt());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public void addToWishlist(String email, Long productId) {
        User user = findByEmail(email);
        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            com.novainvesa.backend.entity.Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_001",
                            "Producto no encontrado: " + productId));
            Wishlist item = Wishlist.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishlistRepository.save(item);
            log.info("Producto {} agregado a wishlist de usuario {}", productId, user.getId());
        }
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = findByEmail(email);
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
        log.info("Producto {} eliminado de wishlist de usuario {}", productId, user.getId());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("USER_001", "Usuario no encontrado"));
    }

    private UserProfileResponse toProfileResponse(User u) {
        return new UserProfileResponse(
                u.getId(), u.getFullName(), u.getEmail(),
                u.getPhone(), u.getIsActive(), u.getCreatedAt());
    }

    private AddressResponse toAddressResponse(UserAddress a) {
        return new AddressResponse(
                a.getId(), a.getFullName(), a.getPhone(),
                a.getDepartment(), a.getCity(), a.getAddress(),
                a.getNeighborhood(), a.getNotes(), a.getIsDefault());
    }
}
