package com.healthflow.repo;

import com.healthflow.domain.Periodontograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodontogramaRepository extends JpaRepository<Periodontograma, UUID> {

    // Obtener todos los periodontogramas de un paciente, ordenados por fecha descendente
    List<Periodontograma> findByPatientIdOrderByExamDateDesc(UUID patientId);

    // Obtener el periodontograma más reciente de un paciente
    Optional<Periodontograma> findTopByPatientIdOrderByExamDateDesc(UUID patientId);

    // Obtener periodontogramas asociados a una cita específica
    Optional<Periodontograma> findByAppointmentId(UUID appointmentId);

    // Obtener periodontogramas de un profesional (para reportes)
    List<Periodontograma> findByProfessionalIdOrderByExamDateDesc(UUID professionalId);

    // Verificar si un paciente tiene al menos un periodontograma
    boolean existsByPatientId(UUID patientId);

    // Obtener periodontogramas entre dos fechas (para estadísticas)
    List<Periodontograma> findByExamDateBetween(LocalDate startDate, LocalDate endDate);

    // Obtener periodontogramas de un paciente en un rango de fechas
    List<Periodontograma> findByPatientIdAndExamDateBetweenOrderByExamDateDesc(UUID patientId, LocalDate startDate, LocalDate endDate);

    // Eliminar todos los periodontogramas de un paciente (útil para borrado lógico)
    void deleteByPatientId(UUID patientId);
}