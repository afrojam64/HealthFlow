package com.healthflow.service;

import com.healthflow.domain.PacienteToken;
import com.healthflow.domain.Patient;
import com.healthflow.repo.PacienteTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PacienteTokenService {

    private final PacienteTokenRepository tokenRepository;

    public PacienteTokenService(PacienteTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Genera un nuevo token activo para el paciente (inactiva tokens anteriores del mismo paciente).
     */
    public String generarToken(Patient patient) {
        // Inactivar tokens anteriores del mismo paciente (opcional, para mantener solo uno activo)
        tokenRepository.findByPatientIdAndActiveTrue(patient.getId())
                .ifPresent(oldToken -> {
                    oldToken.setActive(false);
                    tokenRepository.save(oldToken);
                });

        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusYears(1);

        PacienteToken token = new PacienteToken(patient, tokenValue, expiresAt);
        tokenRepository.save(token);

        return tokenValue;
    }

    /**
     * Valida un token y retorna el paciente asociado si es válido y no expirado.
     */
    public Patient validarToken(String token) {
        PacienteToken pacienteToken = tokenRepository.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o inactivo"));

        if (pacienteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        return pacienteToken.getPatient();
    }

    /**
     * Renueva el token de un paciente (genera uno nuevo y desactiva el anterior).
     */
    public String renovarToken(Patient patient) {
        return generarToken(patient);
    }
}