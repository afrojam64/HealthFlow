package com.healthflow.repo;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByProfessional_IdAndDateTimeBetween(UUID professionalId, OffsetDateTime start, OffsetDateTime end);

    Optional<Appointment> findByAccessToken(UUID accessToken);

    @Query("SELECT a FROM Appointment a WHERE a.professional.id = :profId " +
            "AND a.dateTime BETWEEN :start AND :end " +
            "AND a.status != 'CANCELADA'")
    List<Appointment> findActiveByProfessionalIdAndDateTimeBetween(
            @Param("profId") UUID professionalId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("SELECT a FROM Appointment a WHERE a.dateTime BETWEEN :start AND :end " +
            "AND a.status != 'CANCELADA' AND a.reminderSentAt IS NULL")
    List<Appointment> findPendingReminders(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.professional.id = :profId " +
            "AND a.dateTime >= :startOfDay AND a.dateTime < :endOfDay " +
            "AND a.status != 'CANCELADA'")
    long countTodayAppointments(
            @Param("profId") UUID professionalId,
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );

    long countByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);

    long countByDateTimeAfter(OffsetDateTime since);

    long countByStatus(AppointmentStatus status);

    List<Appointment> findByDateTimeBetweenOrderByDateTimeAsc(OffsetDateTime start, OffsetDateTime end);

    // ========== NUEVOS MÉTODOS PARA EL MÓDULO DE PACIENTES ==========

    /**
     * Encuentra la última cita (más reciente) de un paciente con un profesional específico.
     */
    Optional<Appointment> findTopByPatientIdAndProfessionalIdOrderByDateTimeDesc(UUID patientId, UUID professionalId);

    /**
     * Encuentra todas las citas de un paciente con un profesional específico, ordenadas por fecha descendente.
     */
    List<Appointment> findByPatientIdAndProfessionalIdOrderByDateTimeDesc(UUID patientId, UUID professionalId);

    /**
     * Busca todas las citas de un profesional en un rango de fechas que tengan un estado específico.
     * Incluye las relaciones con paciente y registro médico para evitar lazy loading.
     */
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.medicalRecord " +
            "WHERE a.professional.id = :professionalId " +
            "AND a.dateTime BETWEEN :start AND :end " +
            "AND a.status = :status")
    List<Appointment> findByProfessionalIdAndDateTimeBetweenAndStatus(
            @Param("professionalId") UUID professionalId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("status") AppointmentStatus status);

}
