package com.healthflow.repo;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // Módulo de pacientes
    Optional<Appointment> findTopByPatientIdAndProfessionalIdOrderByDateTimeDesc(UUID patientId, UUID professionalId);
    List<Appointment> findByPatientIdAndProfessionalIdOrderByDateTimeDesc(UUID patientId, UUID professionalId);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.medicalRecord mr " +
            "LEFT JOIN FETCH mr.finalidadConsulta " +
            "LEFT JOIN FETCH mr.causaExterna " +
            "WHERE a.professional.id = :professionalId " +
            "AND a.dateTime BETWEEN :start AND :end " +
            "AND a.status = :status")
    List<Appointment> findByProfessionalIdAndDateTimeBetweenAndStatus(
            @Param("professionalId") UUID professionalId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status AND a.dateTime BETWEEN :start AND :end")
    long countByStatusAndDateTimeBetween(@Param("status") AppointmentStatus status,
                                         @Param("start") OffsetDateTime start,
                                         @Param("end") OffsetDateTime end);

    List<Appointment> findByPatientIdOrderByDateTimeDesc(UUID patientId);
    Optional<Appointment> findTopByPatientIdOrderByDateTimeDesc(UUID patientId);

    // Métodos para verificar existencia de citas (evitar duplicados)
    boolean existsByProfessionalIdAndDateTime(UUID professionalId, OffsetDateTime dateTime);
    boolean existsByProfessionalIdAndDateTimeAndIdNot(UUID professionalId, OffsetDateTime dateTime, UUID id);

    /**
     * Actualiza a NO_ATENDIDA las citas con fecha anterior a la actual y estados PENDIENTE o CONFIRMADA.
     * @param now Fecha/hora actual (inicio del día)
     * @return número de registros actualizados
     */
    @Modifying
    @Query("UPDATE Appointment a SET a.status = com.healthflow.domain.AppointmentStatus.NO_ATENDIDA " +
            "WHERE a.dateTime < :hoy AND a.status IN (com.healthflow.domain.AppointmentStatus.PENDIENTE, com.healthflow.domain.AppointmentStatus.CONFIRMADA)")
    int updatePastAppointmentsToNotAttended(@Param("hoy") OffsetDateTime hoy);

    List<Appointment> findByProfessionalFullNameContainingIgnoreCase(String fullName);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);
}