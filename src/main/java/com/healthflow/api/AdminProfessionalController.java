package com.healthflow.api;

import com.healthflow.api.dto.*;
import com.healthflow.domain.*;
import com.healthflow.service.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminProfessionalController {
  private final ProfessionalService professionalService;
  private final AvailabilityService availabilityService;
  private final AgendaExceptionService exceptionService;

  public AdminProfessionalController(
      ProfessionalService professionalService,
      AvailabilityService availabilityService,
      AgendaExceptionService exceptionService
  ) {
    this.professionalService = professionalService;
    this.availabilityService = availabilityService;
    this.exceptionService = exceptionService;
  }

  @PostMapping("/professionals")
  @ResponseStatus(HttpStatus.CREATED)
  public Professional createProfessional(@Valid @RequestBody CreateProfessionalRequest req) {
    Professional p = new Professional();
    p.setFullName(req.fullName());
    p.setMedicalRegistry(req.medicalRegistry());
    p.setSpecialty(req.specialty());
    return professionalService.create(p);
  }

  @PostMapping("/professionals/{id}/availability")
  @ResponseStatus(HttpStatus.CREATED)
  public AvailabilityBase addAvailability(@PathVariable("id") UUID id, @Valid @RequestBody CreateAvailabilityRequest req) {
    professionalService.get(id); // valida existencia
    AvailabilityBase ab = new AvailabilityBase();
    ab.setProfessionalId(id);
    ab.setDayOfWeek(req.dayOfWeek());
    ab.setStartTime(req.startTime());
    ab.setEndTime(req.endTime());
    return availabilityService.addAvailability(ab);
  }

  @PostMapping("/professionals/{id}/exceptions")
  @ResponseStatus(HttpStatus.CREATED)
  public AgendaException addException(@PathVariable("id") UUID id, @Valid @RequestBody CreateAgendaExceptionRequest req) {
    professionalService.get(id);
    AgendaException ex = new AgendaException();
    ex.setProfessionalId(id);
    ex.setDate(req.date());
    ex.setStartTime(req.startTime());
    ex.setEndTime(req.endTime());
    ex.setType(req.type());
    return exceptionService.add(ex);
  }
}
