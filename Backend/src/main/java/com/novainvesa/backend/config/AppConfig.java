package com.novainvesa.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Configuración general de beans de aplicación.
 * - RestTemplate con timeouts configurados para llamadas a APIs externas.
 * - @EnableAsync para que @Async funcione en N8nService.
 */
@Configuration
@EnableAsync
public class AppConfig {

    // Registrado explícitamente para garantizar que exista antes de que
    // WebSecurityConfiguration lo necesite durante su inicialización.
    // spring.main.allow-bean-definition-overriding=true permite que
    // WebMvcAutoConfiguration lo sobreescriba después con su versión completa.
    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);
        return new RestTemplate(factory);
    }
}
