package com.healthflow.web;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DoctorAgendaController {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public DoctorAgendaController(ProfessionalRepository professionalRepository, UserRepository userRepository) {
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/agenda")
    public String myAgenda(Authentication authentication, Model model) {
        String username = authentication.getName();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado en BD."));

        Professional prof = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado (userId)."));

        model.addAttribute("username", username);
        model.addAttribute("professional", prof);

        return "admin/admin-agenda";
    }
}