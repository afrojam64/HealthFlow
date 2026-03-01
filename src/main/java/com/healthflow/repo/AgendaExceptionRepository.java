package com.healthflow.repo;

import com.healthflow.domain.AgendaException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AgendaExceptionRepository extends JpaRepository<AgendaException, UUID> {
  List<AgendaException> findByProfessionalIdAndDate(UUID professionalId, LocalDate date);
}
