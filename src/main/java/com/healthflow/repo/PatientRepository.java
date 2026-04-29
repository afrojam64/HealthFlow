package com.healthflow.repo;

import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByDocNumber(String docNumber);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt > :since")
    long countByCreatedAtAfter(@Param("since") OffsetDateTime since);

    // ========== NUEVOS MÉTODOS PARA EL MÓDULO DE PACIENTES ==========

    /**
     * Obtiene todos los pacientes que han tenido citas con un profesional específico,
     * aplicando filtros opcionales por nombre, documento y rango de fechas de la cita.
     * Los parámetros de fecha son opcionales; si se pasan null, se ignoran.
     */
    @Query("SELECT DISTINCT p FROM Patient p " +
            "JOIN Appointment a ON a.patient.id = p.id " +
            "WHERE a.professional.id = :professionalId " +
            "AND (:fechaDesde IS NULL OR a.dateTime >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR a.dateTime <= :fechaHasta)")
    List<Patient> findPatientsByProfessionalAndFilters(
            @Param("professionalId") UUID professionalId,
            @Param("fechaDesde") OffsetDateTime fechaDesde,
            @Param("fechaHasta") OffsetDateTime fechaHasta);

    /**
     * Versión simplificada sin filtros (útil para cargas iniciales o pruebas)
     */
    @Query("SELECT DISTINCT p FROM Patient p " +
            "JOIN Appointment a ON a.patient.id = p.id " +
            "WHERE a.professional.id = :professionalId")
    List<Patient> findPatientsByProfessionalId(@Param("professionalId") UUID professionalId);

    @Query("SELECT DISTINCT p FROM Patient p " +
            "JOIN Appointment a ON a.patient.id = p.id " +
            "WHERE a.professional.id = :professionalId " +
            "AND (:nombre IS NULL OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:documento IS NULL OR p.docNumber LIKE CONCAT('%', :documento, '%')) " +
            "AND (:fechaDesde IS NULL OR a.dateTime >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR a.dateTime <= :fechaHasta) " +
            "AND a.status = :estado")
    List<Patient> findPatientsByProfessionalAndStatus(
            @Param("professionalId") UUID professionalId,
            @Param("nombre") String nombre,
            @Param("documento") String documento,
            @Param("fechaDesde") OffsetDateTime fechaDesde,
            @Param("fechaHasta") OffsetDateTime fechaHasta,
            @Param("estado") AppointmentStatus estado);

    // NUEVO: contar pacientes creados entre dos fechas
    long countByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    Optional<Patient> findByEmail(String email);

    List<Patient> findByDocNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String docNumber, String firstName, String lastName);
}