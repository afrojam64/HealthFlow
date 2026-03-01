package com.healthflow.service;

import com.healthflow.domain.AvailabilityBase;
import com.healthflow.repo.AvailabilityBaseRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AvailabilityServiceTest {

  @Test
  void shouldRejectOverlaps() {
    AvailabilityBaseRepository repo = mock(AvailabilityBaseRepository.class);
    AvailabilityService svc = new AvailabilityService(repo);

    UUID prof = UUID.randomUUID();
    AvailabilityBase existing = new AvailabilityBase();
    existing.setProfessionalId(prof);
    existing.setDayOfWeek(1);
    existing.setStartTime(LocalTime.of(9,0));
    existing.setEndTime(LocalTime.of(12,0));

    when(repo.findByProfessionalIdAndDayOfWeek(prof, 1)).thenReturn(List.of(existing));

    AvailabilityBase incoming = new AvailabilityBase();
    incoming.setProfessionalId(prof);
    incoming.setDayOfWeek(1);
    incoming.setStartTime(LocalTime.of(11,0));
    incoming.setEndTime(LocalTime.of(13,0));

    DomainException ex = assertThrows(DomainException.class, () -> svc.addAvailability(incoming));
    assertTrue(ex.getMessage().toLowerCase().contains("traslape"));
  }
}
