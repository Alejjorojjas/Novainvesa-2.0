package com.novainvesa.backend.security;

import com.novainvesa.backend.repository.AdminUserRepository;
import com.novainvesa.backend.repository.UserRepository;
import com.novainvesa.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que se ejecuta una vez por petición.
 * Lee el header Authorization: Bearer <token>, lo valida y establece
 * el contexto de seguridad. Si el token es inválido, no lanza excepción
 * — simplemente no establece el contexto y Spring Security devuelve 401.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

    public JwtAuthFilter(JwtService jwtService,
                         UserRepository userRepository,
                         AdminUserRepository adminUserRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Sin header Authorization → continuar sin autenticar (Security devolverá 401 si el endpoint lo requiere)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Determinar si es token de admin o de usuario
            if (jwtService.isAdminToken(token)) {
                processAdminToken(token);
            } else {
                processUserToken(token);
            }
        } catch (Exception e) {
            // Token inválido — no setear contexto, Spring Security devolverá 401
            log.debug("No se pudo procesar el token JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    private void processUserToken(String token) {
        if (!jwtService.validateUserToken(token)) {
            return;
        }
        String email = jwtService.extractEmailFromUserToken(token);
        if (email == null) return;

        userRepository.findByEmail(email).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getIsActive())) {
                setAuthentication(email, "ROLE_USER");
            }
        });
    }

    private void processAdminToken(String token) {
        if (!jwtService.validateAdminToken(token)) {
            return;
        }
        String email = jwtService.extractEmailFromAdminToken(token);
        if (email == null) return;

        adminUserRepository.findByEmail(email).ifPresent(admin -> {
            if (Boolean.TRUE.equals(admin.getIsActive())) {
                String role = "ROLE_" + admin.getRole().name();
                setAuthentication(email, role);
            }
        });
    }

    private void setAuthentication(String email, String role) {
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
