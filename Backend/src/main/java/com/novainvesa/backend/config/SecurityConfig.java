package com.novainvesa.backend.config;

import com.novainvesa.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST stateless — sin CSRF
            .csrf(AbstractHttpConfigurer::disable)

            // CORS configurado con el bean corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Sin estado de sesión — JWT en cada request
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Health check: público para Render.com
                .requestMatchers(new AntPathRequestMatcher("/api/health", HttpMethod.GET.name())).permitAll()
                // Autenticación de usuarios: rutas públicas
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/register", HttpMethod.POST.name())).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/login", HttpMethod.POST.name())).permitAll()
                // Admin auth: solo el login es público — el orden importa (va ANTES de la regla general admin)
                .requestMatchers(new AntPathRequestMatcher("/api/v1/admin/auth/login", HttpMethod.POST.name())).permitAll()
                // Admin: todas las demás rutas requieren rol ADMIN o SUPER_ADMIN
                .requestMatchers(new AntPathRequestMatcher("/api/v1/admin/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Productos: listado, búsqueda y detalle son públicos
                .requestMatchers(new AntPathRequestMatcher("/api/v1/products/**", HttpMethod.GET.name())).permitAll()
                // Categorías: listado público
                .requestMatchers(new AntPathRequestMatcher("/api/v1/categories/**", HttpMethod.GET.name())).permitAll()
                // Cobertura COD: público
                .requestMatchers(new AntPathRequestMatcher("/api/v1/coverage/**", HttpMethod.GET.name())).permitAll()
                // Pedidos: creación pública (guest + autenticado), consulta pública
                .requestMatchers(new AntPathRequestMatcher("/api/v1/orders", HttpMethod.POST.name())).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/orders/**", HttpMethod.GET.name())).permitAll()
                // Pagos: creación pública (el pedido ya valida la existencia)
                .requestMatchers(new AntPathRequestMatcher("/api/v1/payments/**", HttpMethod.POST.name())).permitAll()
                // Webhooks: llamados por proveedores externos, HMAC verifica la autenticidad
                .requestMatchers(new AntPathRequestMatcher("/api/v1/webhooks/**", HttpMethod.POST.name())).permitAll()
                // Meta Conversions API: publico (llamado desde el browser del cliente)
                .requestMatchers(new AntPathRequestMatcher("/api/v1/pixel/**", HttpMethod.POST.name())).permitAll()
                // Todo lo demás requiere autenticación JWT
                .anyRequest().authenticated()
            )

            // Registrar el filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** BCrypt con 12 rounds para contraseñas de usuarios y admins */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
