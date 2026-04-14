package com.healthflow.web;

import com.healthflow.domain.Patient;
import com.healthflow.repo.PatientRepository;
import com.healthflow.service.JwtService;
import com.healthflow.service.NotificationService;
import com.healthflow.service.PacienteTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/paciente")
public class PacientePortalController {

    private final PatientRepository patientRepo;
    private final PacienteTokenService tokenService;
    private final NotificationService notificationService;
    private final JwtService jwtService;

    public PacientePortalController(PatientRepository patientRepo,
                                    PacienteTokenService tokenService,
                                    NotificationService notificationService, JwtService jwtService) {
        this.patientRepo = patientRepo;
        this.tokenService = tokenService;
        this.notificationService = notificationService;
        this.jwtService = jwtService;
    }

    @GetMapping("/entrar")
    public String mostrarFormularioEntrada() {
        return "paciente/entrar";
    }

    @PostMapping("/enviar-enlace")
    public String enviarEnlace(@RequestParam("email") String email,
                               RedirectAttributes redirectAttributes) {
        Patient patient = patientRepo.findByEmail(email).orElse(null);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("error", "No existe un paciente con ese correo electrónico.");
            return "redirect:/paciente/entrar";
        }

        String token = jwtService.generateToken(patient);
        String portalUrl = "http://localhost:8080/paciente/dashboard?token=" + token;
        notificationService.sendPortalAccessEmail(patient.getEmail(),
                patient.getFirstName() + " " + patient.getLastName(),
                portalUrl);

        // Redirigir directamente al dashboard con el token
        return "redirect:/paciente/dashboard?token=" + token;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "token", required = false) String token,
                            HttpServletResponse response,
                            Model model) {
        // Si ya hay autenticación en el contexto (por el filtro), redirigir sin token
        if (token == null && SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UUID) {
            return "paciente/dashboard";
        }
        // Si hay token en URL, validarlo y redirigir a la misma URL sin token (se guardará en localStorage por JS)
        if (token != null) {
            // El filtro ya validará el token y establecerá la autenticación.
            // La vista se encargará de guardar el token en localStorage y eliminar de la URL.
            model.addAttribute("token", token);
            return "paciente/dashboard";
        }
        return "redirect:/paciente/entrar";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/paciente/entrar";
    }
}