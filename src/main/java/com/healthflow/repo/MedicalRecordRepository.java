package com.healthflow.repo;

import com.healthflow.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
