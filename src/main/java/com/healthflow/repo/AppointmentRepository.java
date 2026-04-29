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

    boolean existsByProfessionalIdAndDateTime(UUID professionalId, OffsetDateTime dateTime);
    boolean existsByProfessionalIdAndDateTimeAndIdNot(UUID professionalId, OffsetDateTime dateTime, UUID id);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = com.healthflow.domain.AppointmentStatus.NO_ATENDIDA " +
            "WHERE a.dateTime < :hoy AND a.status IN (com.healthflow.domain.AppointmentStatus.PENDIENTE, com.healthflow.domain.AppointmentStatus.CONFIRMADA)")
    int updatePastAppointmentsToNotAttended(@Param("hoy") OffsetDateTime hoy);

    List<Appointment> findByProfessionalFullNameContainingIgnoreCase(String fullName);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.professional.id = :professionalId AND a.dateTime BETWEEN :start AND :end")
    long countByProfessionalIdAndDateTimeBetween(@Param("professionalId") UUID professionalId,
                                                 @Param("start") OffsetDateTime start,
                                                 @Param("end") OffsetDateTime end);

    // --- Consultas JPQL originales (funcionales para algunos casos) ---
    @Query("SELECT EXTRACT(MONTH FROM a.dateTime), EXTRACT(YEAR FROM a.dateTime), COUNT(a) " +
            "FROM Appointment a WHERE a.dateTime BETWEEN :start AND :end " +
            "GROUP BY EXTRACT(YEAR FROM a.dateTime), EXTRACT(MONTH FROM a.dateTime) " +
            "ORDER BY EXTRACT(YEAR FROM a.dateTime), EXTRACT(MONTH FROM a.dateTime)")
    List<Object[]> countAppointmentsGroupByMonth(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("SELECT EXTRACT(MONTH FROM a.dateTime), EXTRACT(YEAR FROM a.dateTime), COUNT(a) " +
            "FROM Appointment a WHERE a.professional.id = :professionalId AND a.dateTime BETWEEN :start AND :end " +
            "GROUP BY EXTRACT(YEAR FROM a.dateTime), EXTRACT(MONTH FROM a.dateTime) " +
            "ORDER BY EXTRACT(YEAR FROM a.dateTime), EXTRACT(MONTH FROM a.dateTime)")
    List<Object[]> countAppointmentsByProfessionalGroupByMonth(@Param("professionalId") UUID professionalId,
                                                               @Param("start") OffsetDateTime start,
                                                               @Param("end") OffsetDateTime end);

    @Query("SELECT FUNCTION('DATE', a.dateTime), COUNT(a) " +
            "FROM Appointment a WHERE a.professional.id = :professionalId AND a.dateTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', a.dateTime) ORDER BY FUNCTION('DATE', a.dateTime)")
    List<Object[]> countAppointmentsByProfessionalGroupByDay(@Param("professionalId") UUID professionalId,
                                                             @Param("start") OffsetDateTime start,
                                                             @Param("end") OffsetDateTime end);

    @Query("SELECT EXTRACT(YEAR FROM a.dateTime), COUNT(a) " +
            "FROM Appointment a WHERE a.professional.id = :professionalId AND a.dateTime BETWEEN :start AND :end " +
            "GROUP BY EXTRACT(YEAR FROM a.dateTime) ORDER BY EXTRACT(YEAR FROM a.dateTime)")
    List<Object[]> countAppointmentsByProfessionalGroupByYear(@Param("professionalId") UUID professionalId,
                                                              @Param("start") OffsetDateTime start,
                                                              @Param("end") OffsetDateTime end);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a " +
            "WHERE a.professional.id = :professionalId AND a.dateTime BETWEEN :start AND :end")
    long countDistinctPatientsByProfessional(@Param("professionalId") UUID professionalId,
                                             @Param("start") OffsetDateTime start,
                                             @Param("end") OffsetDateTime end);

    @Query("SELECT a.patient.id, MIN(a.dateTime) FROM Appointment a " +
            "WHERE a.professional.id = :professionalId AND a.dateTime BETWEEN :start AND :end " +
            "GROUP BY a.patient.id")
    List<Object[]> findFirstAppointmentDatePerPatient(@Param("professionalId") UUID professionalId,
                                                      @Param("start") OffsetDateTime start,
                                                      @Param("end") OffsetDateTime end);

    @Query("SELECT a.professional.id, COUNT(DISTINCT a.patient.id) FROM Appointment a " +
            "WHERE a.dateTime BETWEEN :start AND :end " +
            "GROUP BY a.professional.id")
    List<Object[]> countDistinctPatientsPerProfessional(@Param("start") OffsetDateTime start,
                                                        @Param("end") OffsetDateTime end);

    @Query("SELECT a.professional.id, COUNT(a) FROM Appointment a " +
            "WHERE a.dateTime BETWEEN :start AND :end " +
            "GROUP BY a.professional.id")
    List<Object[]> countAppointmentsPerProfessional(@Param("start") OffsetDateTime start,
                                                    @Param("end") OffsetDateTime end);

    // --- NUEVAS CONSULTAS NATIVAS (PostgreSQL) para evitar problemas de tipos ---
    // --- CONSULTAS NATIVAS CORREGIDAS (con nombres de columnas reales) ---

    @Query(value = "SELECT EXTRACT(MONTH FROM fecha_hora) AS mes, EXTRACT(YEAR FROM fecha_hora) AS anio, COUNT(*) AS total " +
            "FROM citas WHERE fecha_hora BETWEEN :start AND :end " +
            "GROUP BY anio, mes ORDER BY anio, mes", nativeQuery = true)
    List<Object[]> countAppointmentsGroupByMonthNative(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query(value = "SELECT EXTRACT(MONTH FROM fecha_hora) AS mes, EXTRACT(YEAR FROM fecha_hora) AS anio, COUNT(*) AS total " +
            "FROM citas WHERE profesional_id = :professionalId AND fecha_hora BETWEEN :start AND :end " +
            "GROUP BY anio, mes ORDER BY anio, mes", nativeQuery = true)
    List<Object[]> countAppointmentsByProfessionalGroupByMonthNative(@Param("professionalId") UUID professionalId,
                                                                     @Param("start") OffsetDateTime start,
                                                                     @Param("end") OffsetDateTime end);

    @Query(value = "SELECT DATE(fecha_hora) AS dia, COUNT(*) AS total " +
            "FROM citas WHERE profesional_id = :professionalId AND fecha_hora BETWEEN :start AND :end " +
            "GROUP BY dia ORDER BY dia", nativeQuery = true)
    List<Object[]> countAppointmentsByProfessionalGroupByDayNative(@Param("professionalId") UUID professionalId,
                                                                   @Param("start") OffsetDateTime start,
                                                                   @Param("end") OffsetDateTime end);

    @Query(value = "SELECT EXTRACT(YEAR FROM fecha_hora) AS anio, COUNT(*) AS total " +
            "FROM citas WHERE profesional_id = :professionalId AND fecha_hora BETWEEN :start AND :end " +
            "GROUP BY anio ORDER BY anio", nativeQuery = true)
    List<Object[]> countAppointmentsByProfessionalGroupByYearNative(@Param("professionalId") UUID professionalId,
                                                                    @Param("start") OffsetDateTime start,
                                                                    @Param("end") OffsetDateTime end);

    @Query(value = "SELECT profesional_id, COUNT(DISTINCT paciente_id) FROM citas " +
            "WHERE fecha_hora BETWEEN :start AND :end GROUP BY profesional_id", nativeQuery = true)
    List<Object[]> countDistinctPatientsPerProfessionalNative(@Param("start") OffsetDateTime start,
                                                              @Param("end") OffsetDateTime end);

    @Query(value = "SELECT profesional_id, COUNT(*) FROM citas " +
            "WHERE fecha_hora BETWEEN :start AND :end GROUP BY profesional_id", nativeQuery = true)
    List<Object[]> countAppointmentsPerProfessionalNative(@Param("start") OffsetDateTime start,
                                                          @Param("end") OffsetDateTime end);

    boolean existsByPatientIdAndProfessionalIdAndStatus(UUID patientId, UUID professionalId, AppointmentStatus status);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.patient.id = :patientId " +
            "AND a.professional.id = :professionalId " +
            "AND a.status IN :estados " +
            "AND a.dateTime >= :hoy")
    boolean existsFutureActiveAppointment(@Param("patientId") UUID patientId,
                                          @Param("professionalId") UUID professionalId,
                                          @Param("estados") List<AppointmentStatus> estados,
                                          @Param("hoy") OffsetDateTime hoy);
}