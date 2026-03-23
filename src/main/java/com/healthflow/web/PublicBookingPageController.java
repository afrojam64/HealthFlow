package com.healthflow.web;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.service.ProfessionalService;
import com.healthflow.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
                                       SchedulingService schedulingService, ProfessionalRepository professionalRepository) {
        this.professionalService = professionalService;
        this.schedulingService = schedulingService;
        this.professionalRepository = professionalRepository;
    }

    // Endpoint con slug (nuevo)
    @GetMapping("/{slug}")
    public String showBookingPageBySlug(@PathVariable("slug") String slug, Model model) {
        Professional professional = professionalRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        model.addAttribute("professional", professional);
        model.addAttribute("professionalId", professional.getId());
        model.addAttribute("slug", professional.getSlug());
        model.addAttribute("title", "Agenda tu cita | HealthFlow");
        model.addAttribute("contenido", "public/public-book"); // ← ajusta según ubicación real
        return "fragments/layout-public";
    }

    // Endpoint legacy con UUID (redirige al slug si existe, para compatibilidad)
    @GetMapping("/book/{id}")
    public String showBookingPageById(@PathVariable UUID id, Model model) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        // Si tiene slug, redirigimos a la nueva URL amigable
        if (professional.getSlug() != null) {
            return "redirect:/public/" + professional.getSlug();
        }
        // Si no tiene slug, usamos el antiguo (por compatibilidad)
        model.addAttribute("professional", professional);
        model.addAttribute("professionalId", professional.getId());
        return "public-book-legacy"; // o puedes seguir con la misma vista, pero pasando el id
    }

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
