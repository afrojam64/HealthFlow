package com.healthflow.web;

import com.healthflow.domain.Patient;
import com.healthflow.repo.PatientRepository;
import com.healthflow.service.JwtService;
import com.healthflow.service.NotificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paciente")
public class PacientePortalController {

    private final PatientRepository patientRepo;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public PacientePortalController(PatientRepository patientRepo,
                                    JwtService jwtService,
                                    NotificationService notificationService) {
        this.patientRepo = patientRepo;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    @GetMapping("/entrar")
    public String mostrarFormularioEntrada() {
        return "paciente/entrar";
    }

    @PostMapping("/enviar-enlace")
    public String enviarEnlace(@RequestParam("email") String email,
                               RedirectAttributes redirectAttributes,
                               HttpServletResponse response) {
        Patient patient = patientRepo.findByEmail(email).orElse(null);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("error", "No existe un paciente con ese correo electrónico.");
            return "redirect:/paciente/entrar";
        }
        String token = jwtService.generateToken(patient);
        // Crear cookie HttpOnly (segura, no accesible desde JavaScript)
        Cookie cookie = new Cookie("pacienteToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 horas
        // En producción, activar setSecure(true) si usas HTTPS
        // cookie.setSecure(true);
        response.addCookie(cookie);
        return "redirect:/paciente/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "paciente/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("pacienteToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/paciente/entrar";
    }
}