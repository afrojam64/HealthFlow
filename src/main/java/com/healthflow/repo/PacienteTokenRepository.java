package com.healthflow.repo;

import com.healthflow.domain.PacienteToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PacienteTokenRepository extends JpaRepository<PacienteToken, UUID> {
    Optional<PacienteToken> findByTokenAndActiveTrue(String token);
    Optional<PacienteToken> findByPatientIdAndActiveTrue(UUID patientId);
}