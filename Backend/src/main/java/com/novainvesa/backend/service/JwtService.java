package com.novainvesa.backend.service;

import com.novainvesa.backend.entity.AdminUser;
import com.novainvesa.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio de generación y validación de JWT.
 * Usa dos secretos separados: uno para usuarios públicos y otro para admins.
 * NUNCA loggear tokens ni contraseñas en este servicio.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String userSecret;

    @Value("${jwt.admin-secret}")
    private String adminSecret;

    @Value("${jwt.expiration}")
    private long userExpiration;

    @Value("${jwt.admin-expiration}")
    private long adminExpiration;

    // ─── Generación de tokens ───────────────────────────────────────────────

    /** Genera un JWT firmado con el secreto de usuarios (7 días). */
    public String generateUserToken(User user) {
        return buildToken(
                user.getEmail(),
                "ROLE_USER",
                user.getId(),
                userExpiration,
                getUserKey()
        );
    }

    /** Genera un JWT firmado con el secreto de admins (24h). */
    public String generateAdminToken(AdminUser admin) {
        return buildToken(
                admin.getEmail(),
                "ROLE_" + admin.getRole().name(),
                admin.getId(),
                adminExpiration,
                getAdminKey()
        );
    }

    // ─── Validación ────────────────────────────────────────────────────────

    /**
     * Valida el token con el secreto de usuarios.
     * Retorna false si el token es inválido o expirado — nunca lanza excepción.
     */
    public boolean validateUserToken(String token) {
        return validate(token, getUserKey());
    }

    /**
     * Valida el token con el secreto de admins.
     * Retorna false si el token es inválido o expirado — nunca lanza excepción.
     */
    public boolean validateAdminToken(String token) {
        return validate(token, getAdminKey());
    }

    // ─── Extracción de claims ──────────────────────────────────────────────

    /** Extrae el email (subject) del token de usuario. */
    public String extractEmailFromUserToken(String token) {
        return extractClaims(token, getUserKey()).getSubject();
    }

    /** Extrae el email (subject) del token de admin. */
    public String extractEmailFromAdminToken(String token) {
        return extractClaims(token, getAdminKey()).getSubject();
    }

    /** Extrae el rol del token (busca primero con secreto de usuario, luego admin). */
    public String extractRole(String token) {
        try {
            return extractClaims(token, getUserKey()).get("role", String.class);
        } catch (JwtException e) {
            return extractClaims(token, getAdminKey()).get("role", String.class);
        }
    }

    /** Extrae el email del token usando el secreto correcto (usuario o admin). */
    public String extractEmail(String token) {
        try {
            Claims claims = extractClaims(token, getUserKey());
            return claims.getSubject();
        } catch (JwtException e) {
            try {
                Claims claims = extractClaims(token, getAdminKey());
                return claims.getSubject();
            } catch (JwtException ex) {
                log.debug("Token no válido con ninguno de los secretos");
                return null;
            }
        }
    }

    /**
     * Determina si un token fue firmado con el secreto de admin.
     * Útil en el filtro JWT para saber qué repositorio consultar.
     */
    public boolean isAdminToken(String token) {
        try {
            extractClaims(token, getAdminKey());
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    private String buildToken(String subject, String role, Long userId,
                              long expirationMs, SecretKey key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private boolean validate(String token, SecretKey key) {
        try {
            extractClaims(token, key);
            return true;
        } catch (JwtException e) {
            log.debug("Token inválido o expirado: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getUserKey() {
        return Keys.hmacShaKeyFor(userSecret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getAdminKey() {
        return Keys.hmacShaKeyFor(adminSecret.getBytes(StandardCharsets.UTF_8));
    }
}
