package com.healthflow.repo;

import com.healthflow.domain.Professional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    Optional<Professional> findByUserId(UUID id);

    long count();

    Optional<Professional> findBySlug(String slug);

    @EntityGraph(attributePaths = "especialidades")
    Optional<Professional> findById(UUID id);

    @Query("SELECT DISTINCT p FROM Professional p JOIN Appointment a ON a.professional.id = p.id WHERE a.patient.id = :patientId")
    List<Professional> findByPatientId(@Param("patientId") UUID patientId);
}
