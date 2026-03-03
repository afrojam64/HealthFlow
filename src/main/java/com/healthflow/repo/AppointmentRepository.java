package com.healthflow.repo;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByProfessionalIdAndDateTimeBetween(UUID professionalId, OffsetDateTime start, OffsetDateTime end);

    Optional<Appointment> findByAccessToken(UUID accessToken);

    @Query("SELECT a FROM Appointment a WHERE a.professionalId = :profId " +
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

    // ✅ VERSIÓN CORREGIDA: Usar BETWEEN con fechas en lugar de DATE()
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.professionalId = :profId " +
            "AND a.dateTime >= :startOfDay AND a.dateTime < :endOfDay " +
            "AND a.status != 'CANCELADA'")
    long countTodayAppointments(
            @Param("profId") UUID professionalId,
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dateTime BETWEEN :start AND :end")
    long countByFechaHoraBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dateTime > :since")
    long countByFechaHoraAfter(@Param("since") OffsetDateTime since);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :estado")
    long countByEstado(@Param("estado") AppointmentStatus estado);

}