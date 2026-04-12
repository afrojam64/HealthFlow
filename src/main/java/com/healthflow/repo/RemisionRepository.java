package com.healthflow.repo;

import com.healthflow.domain.Remision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RemisionRepository extends JpaRepository<Remision, UUID> {
    Optional<Remision> findByToken(UUID token);
}