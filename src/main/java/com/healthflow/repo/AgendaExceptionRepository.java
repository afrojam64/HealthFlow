package com.healthflow.repo;

import com.healthflow.domain.AgendaException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AgendaExceptionRepository extends JpaRepository<AgendaException, UUID> {

    List<AgendaException> findByProfessional_IdAndDate(UUID professionalId, LocalDate date);

    //List<AgendaException> findByProfessionalIdAndFechaBetween(UUID professionalId, LocalDate start, LocalDate end);

    List<AgendaException> findByProfessional_IdAndDateBetween(UUID professionalId, LocalDate start, LocalDate end);
}
