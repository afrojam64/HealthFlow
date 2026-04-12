package com.healthflow.web;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;

    public DoctorProfileController(UserRepository userRepository, ProfessionalRepository professionalRepository) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
    }

    private Professional getCurrentProfessional() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        return professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpServletRequest request) {
        Professional professional = getCurrentProfessional();
        model.addAttribute("nombre", professional.getFullName());
        model.addAttribute("especialidad", professional.getSpecialty());
        // Agregar token CSRF al modelo
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            model.addAttribute("csrfToken", token.getToken());
            model.addAttribute("csrfHeader", token.getHeaderName());
        }
        model.addAttribute("contenido", "doctor/perfil");
        return "fragments/layout";
    }
}