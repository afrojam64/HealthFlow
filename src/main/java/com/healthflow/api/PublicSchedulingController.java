package com.healthflow.api;

import com.healthflow.api.dto.BookAppointmentRequest;
import com.healthflow.api.dto.BookAppointmentResponse;
import com.healthflow.domain.*;
import com.healthflow.repo.AgendaExceptionRepository;
import com.healthflow.repo.AvailabilityBaseRepository;
import com.healthflow.repo.WeeklyAvailabilityRepository;
import com.healthflow.service.ProfessionalService;
import com.healthflow.service.SchedulingService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicSchedulingController {

    private final SchedulingService schedulingService;
    private final ProfessionalService professionalService;
    private final AvailabilityBaseRepository availabilityBaseRepository;
    private final WeeklyAvailabilityRepository weeklyAvailabilityRepository;
    private final AgendaExceptionRepository agendaExceptionRepository;

  public PublicSchedulingController(SchedulingService schedulingService, ProfessionalService professionalService, AvailabilityBaseRepository availabilityBaseRepository, WeeklyAvailabilityRepository weeklyAvailabilityRepository, AgendaExceptionRepository agendaExceptionRepository) {
    this.schedulingService = schedulingService;
    this.professionalService = professionalService;
      this.availabilityBaseRepository = availabilityBaseRepository;
      this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
      this.agendaExceptionRepository = agendaExceptionRepository;
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

    @GetMapping("/professionals/{id}/monthly-availability")
    public Map<String, Object> getMonthlyAvailability(@PathVariable("id") UUID id,
                                                      @RequestParam("year") int year,
                                                      @RequestParam("month") int month) {
        // Obtener el profesional (para validar que existe)
        professionalService.get(id);

        // 1. Obtener todos los días del mes
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        // 2. Obtener disponibilidad base (recurrente) para todos los días de la semana
        List<AvailabilityBase> baseList = availabilityBaseRepository
                .findByProfessionalIdOrderByDayOfWeekAscStartTimeAsc(id);
        Map<Integer, List<AvailabilityBase>> baseByDay = baseList.stream()
                .collect(Collectors.groupingBy(AvailabilityBase::getDayOfWeek));

        // 3. Obtener excepciones semanales (weekly_availability)
        List<WeeklyAvailability> weeklyExceptions = weeklyAvailabilityRepository
                .findByProfessionalIdAndWeekStartDateBetween(id, firstDay, lastDay);
        Map<LocalDate, List<WeeklyAvailability>> weeklyExceptionsByDate = weeklyExceptions.stream()
                .collect(Collectors.groupingBy(wa -> wa.getWeekStartDate().plusDays(wa.getDayOfWeek() - 1)));

        // 4. Obtener excepciones puntuales (excepciones_agenda)
        List<AgendaException> puntualExceptions = agendaExceptionRepository
                .findByProfessional_IdAndDateBetween(id, firstDay, lastDay);
        Map<LocalDate, List<AgendaException>> puntualExceptionsByDate = puntualExceptions.stream()
                .collect(Collectors.groupingBy(AgendaException::getDate));

        // 5. Construir respuesta
        List<Map<String, Object>> daysData = new ArrayList<>();
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", date.toString());

            // Prioridad: puntual > semanal > base
            List<AgendaException> puntual = puntualExceptionsByDate.get(date);
            if (puntual != null && !puntual.isEmpty()) {
                AgendaException ae = puntual.get(0);
                dayInfo.put("hasAvailability", ae.getType() != ExceptionType.BLOQUEO);
            } else {
                List<WeeklyAvailability> weekly = weeklyExceptionsByDate.get(date);
                if (weekly != null && !weekly.isEmpty()) {
                    WeeklyAvailability wa = weekly.get(0);
                    dayInfo.put("hasAvailability", wa.getActive() && wa.getStartTime() != null);
                } else {
                    int dow = date.getDayOfWeek().getValue();
                    List<AvailabilityBase> baseForDay = baseByDay.getOrDefault(dow, List.of());
                    dayInfo.put("hasAvailability", !baseForDay.isEmpty());
                }
            }
            daysData.add(dayInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("days", daysData);
        return response;
    }
}
