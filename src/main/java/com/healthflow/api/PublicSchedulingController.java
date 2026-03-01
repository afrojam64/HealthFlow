package com.healthflow.api;

import com.healthflow.api.dto.BookAppointmentRequest;
import com.healthflow.api.dto.BookAppointmentResponse;
import com.healthflow.domain.Patient;
import com.healthflow.service.ProfessionalService;
import com.healthflow.service.SchedulingService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
public class PublicSchedulingController {
  private final SchedulingService schedulingService;
  private final ProfessionalService professionalService;

  public PublicSchedulingController(SchedulingService schedulingService, ProfessionalService professionalService) {
    this.schedulingService = schedulingService;
    this.professionalService = professionalService;
  }

  @GetMapping("/professionals/{id}/slots")
  public List<OffsetDateTime> getSlots(@PathVariable("id") UUID id, @RequestParam("date") LocalDate date) {
    professionalService.get(id);
    return schedulingService.getFreeSlots(id, date);
  }

  @PostMapping("/professionals/{id}/book")
  @ResponseStatus(HttpStatus.CREATED)
  public BookAppointmentResponse book(@PathVariable("id") UUID id, @Valid @RequestBody BookAppointmentRequest req) {
    professionalService.get(id);

    Patient p = new Patient();
    p.setDocType(req.docType());
    p.setDocNumber(req.docNumber());
    p.setFirstName(req.firstName());
    p.setMiddleName(req.middleName());
    p.setLastName(req.lastName());
    p.setSecondLastName(req.secondLastName());
    p.setBirthDate(req.birthDate());
    p.setSex(req.sex());
    p.setMunicipalityCode(req.municipalityCode());
    p.setEmail(req.email());
    p.setPhone(req.phone());

    var appt = schedulingService.book(id, p, req.dateTime());
    return new BookAppointmentResponse(appt.getId(), appt.getStatus(), appt.getAccessToken());
  }
}
