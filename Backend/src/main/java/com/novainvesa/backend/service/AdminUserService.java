package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.UserDetailResponse;
import com.novainvesa.backend.dto.UserSummaryResponse;
import com.novainvesa.backend.entity.User;
import com.novainvesa.backend.exception.ResourceNotFoundException;
import com.novainvesa.backend.repository.OrderRepository;
import com.novainvesa.backend.repository.UserAddressRepository;
import com.novainvesa.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de gestión de usuarios compradores para el panel de administración.
 * NUNCA retorna passwordHash.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserAddressRepository userAddressRepository;

    // ─── Listado ───────────────────────────────────────────────────────────

    public PaginatedResponse<UserSummaryResponse> listUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> usersPage = userRepository.findAll(pageable);

        List<UserSummaryResponse> items = usersPage.getContent()
                .stream()
                .map(this::toSummaryResponse)
                .toList();

        return PaginatedResponse.<UserSummaryResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .hasNext(usersPage.hasNext())
                .hasPrevious(usersPage.hasPrevious())
                .build();
    }

    // ─── Detalle ───────────────────────────────────────────────────────────

    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NOT_FOUND", "Usuario no encontrado: " + id));

        long ordersCount = orderRepository.findByUserId(id,
                PageRequest.of(0, 1)).getTotalElements();
        int addressesCount = userAddressRepository.findByUserIdOrderByIsDefaultDesc(id).size();

        return UserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .ordersCount((int) ordersCount)
                .addressesCount(addressesCount)
                .build();
    }

    // ─── Mapeo ─────────────────────────────────────────────────────────────

    private UserSummaryResponse toSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
