package com.healthflow.repo;

import com.healthflow.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    Optional<MedicalRecord> findByAppointmentId(UUID appointmentId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.appointment.patientId = :patientId ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") UUID patientId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE DATE(mr.createdAt) = :date")
    List<MedicalRecord> findByDate(@Param("date") LocalDate date);
}