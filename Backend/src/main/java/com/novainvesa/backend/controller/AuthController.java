package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.AuthResponse;
import com.novainvesa.backend.dto.LoginRequest;
import com.novainvesa.backend.dto.RegisterRequest;
import com.novainvesa.backend.exception.AuthException;
import com.novainvesa.backend.service.AuthService;
import com.novainvesa.backend.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación.
 *
 * Endpoints públicos:
 *   POST /api/v1/auth/register      — registro de usuario
 *   POST /api/v1/auth/login         — login de usuario
 */
@RestController
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    public AuthController(AuthService authService, RateLimiterService rateLimiterService) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }

    // ─── Registro de usuario público ───────────────────────────────────────

    /**
     * POST /api/v1/auth/register
     * Registra un nuevo usuario y devuelve un token JWT.
     * Rate limit: 10 por IP en 15 minutos.
     */
    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        if (!rateLimiterService.allowRegister(getClientIp(httpRequest))) {
            throw AuthException.rateLimitExceeded();
        }

        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authResponse));
    }

    // ─── Login de usuario público ──────────────────────────────────────────

    /**
     * POST /api/v1/auth/login
     * Autentica un usuario público y devuelve un token JWT.
     * Rate limit: 5 por IP en 15 minutos.
     */
    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        if (!rateLimiterService.allowLogin(getClientIp(httpRequest))) {
            throw AuthException.rateLimitExceeded();
        }

        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    /**
     * Obtiene la IP real del cliente, considerando proxies (X-Forwarded-For).
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
