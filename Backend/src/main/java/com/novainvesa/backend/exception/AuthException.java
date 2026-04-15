package com.novainvesa.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de autenticación y autorización.
 * Cada código de error mapea a un status HTTP específico.
 */
@Getter
public class AuthException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public AuthException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    // ─── Fábrica de errores estándar ────────────────────────────────────────

    /** AUTH_001 — Email ya registrado */
    public static AuthException emailAlreadyExists() {
        return new AuthException("AUTH_001", "El email ya está registrado", HttpStatus.CONFLICT);
    }

    /** AUTH_002 — Credenciales inválidas */
    public static AuthException invalidCredentials() {
        return new AuthException("AUTH_002", "Email o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
    }

    /** AUTH_003 — Usuario inactivo o bloqueado */
    public static AuthException accountInactive() {
        return new AuthException("AUTH_003", "La cuenta está inactiva o bloqueada", HttpStatus.FORBIDDEN);
    }

    /** AUTH_004 — Rate limit excedido */
    public static AuthException rateLimitExceeded() {
        return new AuthException("AUTH_004", "Demasiados intentos. Intenta de nuevo más tarde", HttpStatus.TOO_MANY_REQUESTS);
    }
}
