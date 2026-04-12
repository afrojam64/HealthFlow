package com.healthflow.service;

import com.healthflow.domain.PacienteToken;
import com.healthflow.domain.Patient;
import com.healthflow.repo.PacienteTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PacienteTokenService {

    private final PacienteTokenRepository tokenRepo;
    private final int expirationHours;

    public PacienteTokenService(PacienteTokenRepository tokenRepo,
                                @Value("${healthflow.paciente.token-expiration-hours:24}") int expirationHours) {
        this.tokenRepo = tokenRepo;
        this.expirationHours = expirationHours;
    }

    public String generarToken(Patient patient) {
        // Inactivar tokens anteriores del mismo paciente
        tokenRepo.findByPatientIdAndActiveTrue(patient.getId())
                .ifPresent(oldToken -> {
                    oldToken.setActive(false);
                    tokenRepo.save(oldToken);
                });

        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        PacienteToken token = new PacienteToken(patient, tokenValue, expiresAt);
        tokenRepo.save(token);

        return tokenValue;
    }

    public Patient validarToken(String token) {
        PacienteToken pacienteToken = tokenRepo.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o inactivo"));

        if (pacienteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace ha expirado. Solicite uno nuevo.");
        }

        return pacienteToken.getPatient();
    }

    public String renovarToken(Patient patient) {
        return generarToken(patient);
    }
}