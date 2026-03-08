package com.healthflow.repo;

import com.healthflow.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
  Optional<Patient> findByDocNumber(String docNumber);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt > :since")
    long countByCreatedAtAfter(@Param("since") OffsetDateTime since);
}
