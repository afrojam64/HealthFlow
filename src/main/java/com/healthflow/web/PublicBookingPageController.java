package com.healthflow.web;

import com.healthflow.service.ProfessionalService;
import com.healthflow.service.SchedulingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/public")
public class PublicBookingPageController {

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

        var prof = professionalService.get(professionalId);
        model.addAttribute("professional", prof);

        // por defecto: hoy (puedes cambiar a "mañana" si prefieres)
        LocalDate selected = (date != null) ? date : LocalDate.now();
        model.addAttribute("date", selected);

        var slots = schedulingService.getFreeSlots(professionalId, selected);
        model.addAttribute("slots", slots);

        return "public/public-book";
    }

    @GetMapping("/book/{professionalId}/details")
    public String details(@PathVariable("professionalId") UUID professionalId,
                          @RequestParam("dateTime") OffsetDateTime dateTime,
                          Model model) {

        var prof = professionalService.get(professionalId);
        model.addAttribute("professional", prof);
        model.addAttribute("dateTime", dateTime);

        // En el siguiente paso mostraremos el formulario de paciente en un template
        return "public/public-book-details";
    }
}