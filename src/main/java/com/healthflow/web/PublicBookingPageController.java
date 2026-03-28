package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.ProfessionalService;
import com.healthflow.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.UUID;

@Controller
@RequestMapping("/public")
public class PublicBookingPageController {

    private static final Logger logger = LoggerFactory.getLogger(PublicBookingPageController.class);

    private final ProfessionalService professionalService;
    private final SchedulingService schedulingService;
    private final ProfessionalRepository professionalRepository;

    public PublicBookingPageController(ProfessionalService professionalService,
                                       SchedulingService schedulingService,
                                       ProfessionalRepository professionalRepository) {
        this.professionalService = professionalService;
        this.schedulingService = schedulingService;
        this.professionalRepository = professionalRepository;
    }

    // Página de agendamiento (con slug)
    @GetMapping("/{slug}")
    public String showBookingPageBySlug(@PathVariable("slug") String slug, Model model) {
        Professional professional = professionalRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        model.addAttribute("professional", professional);
        model.addAttribute("professionalId", professional.getId());
        model.addAttribute("slug", professional.getSlug());
        model.addAttribute("title", "Agenda tu cita | HealthFlow");
        model.addAttribute("contenido", "public/public-book");
        return "fragments/layout-public";
    }

    // Procesar el agendamiento (POST)
    @PostMapping("/{slug}/book")
    public String bookAppointment(@PathVariable("slug") String slug,
                                  @RequestParam("dateTime") String dateTimeStr,
                                  @RequestParam("docType") String docType,
                                  @RequestParam("docNumber") String docNumber,
                                  @RequestParam("firstName") String firstName,
                                  @RequestParam(value = "middleName", required = false) String middleName,
                                  @RequestParam("lastName") String lastName,
                                  @RequestParam(value = "secondLastName", required = false) String secondLastName,
                                  @RequestParam("birthDate") String birthDateStr,
                                  @RequestParam("sex") String sex,
                                  @RequestParam("municipalityCode") String municipalityCode,
                                  @RequestParam("email") String email,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Parsear fechas
            OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeStr);
            LocalDate birthDate = LocalDate.parse(birthDateStr);

            // Obtener profesional
            Professional professional = professionalRepository.findBySlug(slug)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));

            // Crear paciente
            Patient patient = new Patient();
            patient.setDocType(docType);
            patient.setDocNumber(docNumber);
            patient.setFirstName(firstName);
            patient.setMiddleName(middleName);
            patient.setLastName(lastName);
            patient.setSecondLastName(secondLastName);
            patient.setBirthDate(birthDate);
            patient.setSex(sex);
            patient.setMunicipalityCode(municipalityCode);
            patient.setEmail(email);
            patient.setPhone(phone);

            // Reservar cita
            Appointment appointment = schedulingService.book(professional.getId(), patient, dateTime);
            // Redirigir a la página de confirmación con el token
            return "redirect:/public/booking/confirm?token=" + appointment.getAccessToken();
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Fecha u hora inválida.");
            return "redirect:/public/" + slug;
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/public/" + slug;
        } catch (Exception e) {
            logger.error("Error al agendar cita", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado. Inténtalo de nuevo.");
            return "redirect:/public/" + slug;
        }
    }

    // Redirección desde UUID antiguo (compatibilidad)
    @GetMapping("/book/{id}")
    public String showBookingPageById(@PathVariable UUID id, Model model) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        if (professional.getSlug() != null) {
            return "redirect:/public/" + professional.getSlug();
        }
        model.addAttribute("professional", professional);
        model.addAttribute("professionalId", professional.getId());
        return "public-book-legacy";
    }

    // Métodos legacy (pueden eliminarse si no se usan)
    @GetMapping("/book/{professionalId}")
    public String bookPage(@PathVariable("professionalId") UUID professionalId,
                           @RequestParam(value = "date", required = false) LocalDate date,
                           Model model) {
        try {
            var prof = professionalService.get(professionalId);
            model.addAttribute("professional", prof);
            LocalDate selected = (date != null) ? date : LocalDate.now();
            model.addAttribute("date", selected);
            logger.info("Buscando slots para profesional {} en fecha {}", professionalId, selected);
            var slots = schedulingService.getFreeSlots(professionalId, selected);
            model.addAttribute("slots", slots);
            logger.info("Encontrados {} slots.", slots.size());
        } catch (Exception e) {
            logger.error("ERROR CATASTRÓFICO AL CARGAR LA PÁGINA DE AGENDAMIENTO", e);
            model.addAttribute("slots", Collections.emptyList());
            model.addAttribute("errorMessage", "Ocurrió un error inesperado al cargar los horarios. Por favor, contacte a soporte.");
        }
        return "public/public-book";
    }

    @GetMapping("/book/{professionalId}/details")
    public String details(@PathVariable("professionalId") UUID professionalId,
                          @RequestParam("dateTime") OffsetDateTime dateTime,
                          Model model) {
        var prof = professionalService.get(professionalId);
        model.addAttribute("professional", prof);
        model.addAttribute("dateTime", dateTime);
        return "public/public-book-details";
    }
}