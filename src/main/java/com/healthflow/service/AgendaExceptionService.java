package com.healthflow.service;

import com.healthflow.domain.AgendaException;
import com.healthflow.repo.AgendaExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgendaExceptionService {
  private final AgendaExceptionRepository repo;

  public AgendaExceptionService(AgendaExceptionRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public AgendaException add(AgendaException ex) {
    // Bloqueo todo el día => start/end null permitido
    if (ex.getStartTime() != null || ex.getEndTime() != null) {
      if (ex.getStartTime() == null || ex.getEndTime() == null) {
        throw new DomainException("Si defines horas, debes definir hora_inicio y hora_fin");
      }
      if (!ex.getEndTime().isAfter(ex.getStartTime())) {
        throw new DomainException("hora_fin debe ser mayor que hora_inicio");
      }
    }
    return repo.save(ex);
  }

  public List<AgendaException> list(UUID professionalId, java.time.LocalDate date) {
    return repo.findByProfessionalIdAndDate(professionalId, date);
  }

  public static boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
    return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
  }
}
