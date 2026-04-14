package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.AuthResponse;
import com.novainvesa.backend.dto.LoginRequest;
import com.novainvesa.backend.dto.RegisterRequest;
import com.novainvesa.backend.entity.AdminUser;
import com.novainvesa.backend.entity.User;
import com.novainvesa.backend.exception.AuthException;
import com.novainvesa.backend.repository.AdminUserRepository;
import com.novainvesa.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de autenticación para usuarios públicos y administradores.
 * NUNCA loggear tokens JWT ni contraseñas (ni hasheadas) en este servicio.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long userExpiration;

    @Value("${jwt.admin-expiration}")
    private long adminExpiration;

    public AuthService(UserRepository userRepository,
                       AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ─── Registro de usuario público ───────────────────────────────────────

    /**
     * Registra un nuevo usuario público.
     * Lanza AuthException si el email ya existe.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailAlreadyExists();
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFirstName().trim() + " " + request.getLastName().trim())
                .phone(request.getPhone())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("Usuario registrado con id={}", user.getId());

        String token = jwtService.generateUserToken(user);

        return buildUserAuthResponse(user, token);
    }

    // ─── Login de usuario público ──────────────────────────────────────────

    /**
     * Autentica un usuario público.
     * Lanza AuthException con AUTH_002 si las credenciales son inválidas,
     * AUTH_003 si la cuenta está inactiva.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(AuthException::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw AuthException.accountInactive();
        }

        // Actualizar último login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Login exitoso para usuario id={}", user.getId());
        String token = jwtService.generateUserToken(user);

        return buildUserAuthResponse(user, token);
    }

    // ─── Login de administrador ────────────────────────────────────────────

    /**
     * Autentica un administrador usando el secreto JWT de admins.
     */
    @Transactional
    public AuthResponse loginAdmin(LoginRequest request) {
        AdminUser admin = adminUserRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(AuthException::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        if (Boolean.FALSE.equals(admin.getIsActive())) {
            throw AuthException.accountInactive();
        }

        // Actualizar último login
        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);

        log.info("Login exitoso para admin id={}", admin.getId());
        String token = jwtService.generateAdminToken(admin);

        // Separar fullName en firstName y lastName para la respuesta
        String[] parts = splitFullName(admin.getFullName());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(adminExpiration)
                .id(admin.getId())
                .email(admin.getEmail())
                .firstName(parts[0])
                .lastName(parts[1])
                .role("ROLE_" + admin.getRole().name())
                .build();
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    private AuthResponse buildUserAuthResponse(User user, String token) {
        String[] parts = splitFullName(user.getFullName());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(userExpiration)
                .id(user.getId())
                .email(user.getEmail())
                .firstName(parts[0])
                .lastName(parts[1])
                .role("ROLE_USER")
                .build();
    }

    /** Divide "Nombre Apellido" en [Nombre, Apellido]. */
    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"", ""};
        }
        int idx = fullName.indexOf(' ');
        if (idx < 0) {
            return new String[]{fullName, ""};
        }
        return new String[]{fullName.substring(0, idx), fullName.substring(idx + 1)};
    }
}
