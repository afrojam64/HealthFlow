package com.healthflow.repo;

import com.healthflow.domain.Professional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    Optional<Professional> findByUserId(UUID id);

    long count();

    Optional<Professional> findBySlug(String slug);

    @EntityGraph(attributePaths = "especialidades")
    Optional<Professional> findById(UUID id);
}
