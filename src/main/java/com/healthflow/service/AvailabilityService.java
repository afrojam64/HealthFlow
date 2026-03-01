package com.healthflow.service;

import com.healthflow.domain.AvailabilityBase;
import com.healthflow.repo.AvailabilityBaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {
  private final AvailabilityBaseRepository repo;

  public AvailabilityService(AvailabilityBaseRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public AvailabilityBase addAvailability(AvailabilityBase ab) {
    if (!ab.getEndTime().isAfter(ab.getStartTime())) {
      throw new DomainException("hora_fin debe ser mayor que hora_inicio");
    }

    // HU01: sin traslapes en el mismo día
    List<AvailabilityBase> existing = repo.findByProfessionalIdAndDayOfWeek(ab.getProfessionalId(), ab.getDayOfWeek());
    for (AvailabilityBase e : existing) {
      if (overlaps(ab.getStartTime(), ab.getEndTime(), e.getStartTime(), e.getEndTime())) {
        throw new DomainException("Traslape detectado con disponibilidad existente en el mismo día");
      }
    }

    return repo.save(ab);
  }

  public List<AvailabilityBase> list(UUID professionalId, int dayOfWeek) {
    return repo.findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek);
  }

  private static boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
    // [aStart, aEnd) y [bStart, bEnd) se traslapan si aStart < bEnd y bStart < aEnd
    return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
  }
}
