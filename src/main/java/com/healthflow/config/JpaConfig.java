package com.healthflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

    // Le dice a Spring que use este método para obtener la fecha y hora actual para la auditoría.
    // Esto resuelve el error de conversión de LocalDateTime a OffsetDateTime.
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

    // Le dice a Spring cómo encontrar el "auditor" (el usuario que realiza la acción).
    // Aunque no estamos usando @CreatedBy/@LastModifiedBy todavía, es bueno tenerlo.
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system"); // Para acciones realizadas por el sistema
            }
            return Optional.of(authentication.getName());
        };
    }
}
