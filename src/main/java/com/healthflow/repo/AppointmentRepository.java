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

    // CORREGIDO: Spring Data JPA generará la consulta correcta para professional.id
    List<Appointment> findByProfessional_IdAndDateTimeBetween(UUID professionalId, OffsetDateTime start, OffsetDateTime end);

    Optional<Appointment> findByAccessToken(UUID accessToken);

    // CORREGIDO: a.professionalId -> a.professional.id
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

    // CORREGIDO: a.professionalId -> a.professional.id
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.professional.id = :profId " +
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
