package com.healthflow.repo;

import com.healthflow.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    /**
     * Recupera las historias clínicas previas de un paciente, ordenadas por fecha de la cita de forma descendente.
     * Esencial para la continuidad asistencial.
     *
     * @param patientId El ID del paciente.
     * @return Una lista de registros médicos ordenados.
     */
    List<MedicalRecord> findByAppointmentPatientIdOrderByAppointmentDateTimeDesc(UUID patientId);

    Optional<MedicalRecord> findByAppointmentId(UUID appointmentId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.appointment.patient.id = :patientId ORDER BY mr.appointment.dateTime DESC")
    List<MedicalRecord> findByPatientIdOrderByAppointmentDateTimeDesc(@Param("patientId") UUID patientId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE DATE(mr.createdAt) = :date")
    List<MedicalRecord> findByDate(@Param("date") LocalDate date);

    // ========== NUEVOS MÉTODOS PARA EL MÓDULO DE PACIENTES ==========

    /**
     * Verifica si un paciente tiene al menos un registro médico asociado (es decir, ha sido atendido).
     */
    boolean existsByAppointmentPatientId(UUID patientId);

}
