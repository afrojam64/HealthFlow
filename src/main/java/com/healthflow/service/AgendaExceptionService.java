package com.healthflow.service;

import com.healthflow.domain.AgendaException;
import com.healthflow.repo.AgendaExceptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class AgendaExceptionService {
  private final AgendaExceptionRepository repo;
  private final ZoneId zoneId;

  public AgendaExceptionService(AgendaExceptionRepository repo,
                                @Value("${healthflow.timezone:America/Bogota}") String tz) {
    this.repo = repo;
    this.zoneId = ZoneId.of(tz);
  }

  @Transactional
  public AgendaException add(AgendaException ex) {
    // REGLA: No se pueden añadir excepciones en el pasado.
    if (ex.getDate().isBefore(LocalDate.now(zoneId))) {
        throw new DomainException("No se puede gestionar la agenda de fechas pasadas.");
    }

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
    return repo.findByProfessional_IdAndDate(professionalId, date);
  }

  public static boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
    return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
  }
}
