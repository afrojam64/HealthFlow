package com.healthflow.repo;

import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {
    // CORREGIDO: Buscar por la entidad User directamente, en lugar de por su ID.
    Optional<Professional> findByUser(User user);

    long count();
}
