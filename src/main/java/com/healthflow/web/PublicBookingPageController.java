package com.healthflow.web;

import com.healthflow.service.ProfessionalService;
import com.healthflow.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    public PublicBookingPageController(ProfessionalService professionalService,
                                       SchedulingService schedulingService) {
        this.professionalService = professionalService;
        this.schedulingService = schedulingService;
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
