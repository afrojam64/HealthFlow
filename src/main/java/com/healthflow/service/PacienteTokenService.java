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

    private final PacienteTokenRepository tokenRepo;

    public PacienteTokenService(PacienteTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public String generarToken(Patient patient) {
        // Inactivar tokens anteriores del mismo paciente
        tokenRepo.findByPatientIdAndActiveTrue(patient.getId())
                .ifPresent(oldToken -> {
                    oldToken.setActive(false);
                    tokenRepo.save(oldToken);
                });

        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusYears(1);

        PacienteToken token = new PacienteToken(patient, tokenValue, expiresAt);
        tokenRepo.save(token);

        return tokenValue;
    }

    public Patient validarToken(String token) {
        PacienteToken pacienteToken = tokenRepo.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o inactivo"));

        if (pacienteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        return pacienteToken.getPatient();
    }

    public String renovarToken(Patient patient) {
        return generarToken(patient);
    }
}