package com.healthflow.repo;

import com.healthflow.domain.DocumentoFirmado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoFirmadoRepository extends JpaRepository<DocumentoFirmado, UUID> {
    Optional<DocumentoFirmado> findByToken(UUID token);
}