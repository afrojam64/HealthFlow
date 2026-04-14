package com.healthflow.repo;

import com.healthflow.domain.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

    Optional<Documento> findByToken(UUID token);

    List<Documento> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    @Query("DELETE FROM Documento d WHERE d.expirationDate < :now")
    void deleteExpired(@Param("now") OffsetDateTime now);

    // Listar documentos de un paciente por origen (MEDICO/PACIENTE)
    List<Documento> findByPatientIdAndOrigenOrderByCreatedAtDesc(UUID patientId, String origen);

    // Filtrar por tipo de documento adicionalmente
    List<Documento> findByPatientIdAndOrigenAndTipoDocumentoOrderByCreatedAtDesc(UUID patientId, String origen, String tipoDocumento);

    // Filtrar por rango de fechas
    List<Documento> findByPatientIdAndOrigenAndCreatedAtBetweenOrderByCreatedAtDesc(UUID patientId, String origen, OffsetDateTime start, OffsetDateTime end);

    // Filtro combinado (tipo + fechas)
    List<Documento> findByPatientIdAndOrigenAndTipoDocumentoAndCreatedAtBetweenOrderByCreatedAtDesc(UUID patientId, String origen, String tipoDocumento, OffsetDateTime start, OffsetDateTime end);
}