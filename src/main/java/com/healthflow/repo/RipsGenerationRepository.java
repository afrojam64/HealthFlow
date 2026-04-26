package com.healthflow.repo;

import com.healthflow.domain.RipsGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RipsGenerationRepository extends JpaRepository<RipsGeneration, UUID> {

    List<RipsGeneration> findByProfessionalIdOrderByFechaGeneracionDesc(UUID professionalId);

    @Query("SELECT MAX(CAST(SUBSTRING(r.numFactura, 5) AS int)) FROM RipsGeneration r " +
            "WHERE r.professionalId = :professionalId AND r.numFactura LIKE 'REC-%'")
    Integer findMaxConsecutivoReciboInterno(@Param("professionalId") UUID professionalId);
}