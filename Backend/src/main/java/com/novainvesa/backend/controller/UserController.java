package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.*;
import com.novainvesa.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para operaciones del usuario autenticado.
 *
 * GET    /api/v1/users/me                    — perfil
 * PUT    /api/v1/users/me                    — actualizar perfil
 * GET    /api/v1/users/me/orders             — historial de pedidos
 * GET    /api/v1/users/me/addresses          — direcciones guardadas
 * POST   /api/v1/users/me/addresses          — agregar direccion
 * GET    /api/v1/users/me/wishlist           — lista de deseos
 * POST   /api/v1/users/me/wishlist/{id}      — agregar a lista de deseos
 * DELETE /api/v1/users/me/wishlist/{id}      — eliminar de lista de deseos
 *
 * Todas las rutas requieren JWT valido (cubierto por .anyRequest().authenticated() en SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(email(auth))));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(email(auth), request)));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderSummaryResponse>>> getOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserOrders(email(auth), page, size)));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAddresses(email(auth))));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            Authentication auth,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.addAddress(email(auth), request)));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(userService.getWishlist(email(auth))));
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            Authentication auth,
            @PathVariable Long productId) {
        userService.addToWishlist(email(auth), productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            Authentication auth,
            @PathVariable Long productId) {
        userService.removeFromWishlist(email(auth), productId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String email(Authentication auth) {
        return auth.getName();
    }
}
