package com.healthflow.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorAgendaController {

    @GetMapping("/agenda")
    public String agenda(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("username", username);
        model.addAttribute("title", "Agenda - HealthFlow");
        return "doctor/agenda"; // Esto busca templates/doctor/agenda.html
    }
}