package com.healthflow.web;

import com.healthflow.domain.Patient;
import com.healthflow.repo.PatientRepository;
import com.healthflow.service.NotificationService;
import com.healthflow.service.PacienteTokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/paciente")
public class PacientePortalController {

    private final PatientRepository patientRepo;
    private final PacienteTokenService tokenService;
    private final NotificationService notificationService;

    public PacientePortalController(PatientRepository patientRepo,
                                    PacienteTokenService tokenService,
                                    NotificationService notificationService) {
        this.patientRepo = patientRepo;
        this.tokenService = tokenService;
        this.notificationService = notificationService;
    }

    @GetMapping("/entrar")
    public String mostrarFormularioEntrada() {
        return "paciente/entrar";
    }

    @PostMapping("/enviar-enlace")
    public String enviarEnlace(@RequestParam("email") String email, Model model) {
        Patient patient = patientRepo.findByEmail(email).orElse(null);
        if (patient == null) {
            model.addAttribute("error", "No existe un paciente con ese correo electrónico.");
            return "paciente/entrar";
        }
        String token = tokenService.generarToken(patient);
        String portalUrl = "http://localhost:8080/paciente/dashboard?token=" + token;
        notificationService.sendPortalAccessEmail(patient.getEmail(),
                patient.getFirstName() + " " + patient.getLastName(),
                portalUrl);
        model.addAttribute("mensaje", "Hemos enviado un enlace de acceso a tu correo electrónico.");
        return "paciente/entrar";
    }

    // Dashboard con manejo de sesión y redirección limpia
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "token", required = false) String token,
                            HttpSession session,
                            Model model) {
        // Si ya hay sesión, mostrar dashboard directamente
        if (session.getAttribute("pacienteId") != null) {
            UUID pacienteId = (UUID) session.getAttribute("pacienteId");
            Patient patient = patientRepo.findById(pacienteId).orElse(null);
            model.addAttribute("patient", patient);
            return "paciente/dashboard";
        }
        // Si no hay sesión pero hay token, validar y crear sesión
        if (token != null) {
            try {
                Patient patient = tokenService.validarToken(token);
                session.setAttribute("pacienteId", patient.getId());
                session.setAttribute("pacienteNombre", patient.getFirstName() + " " + patient.getLastName());
                return "redirect:/paciente/dashboard"; // Redirige sin token
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
                return "paciente/entrar";
            }
        }
        return "redirect:/paciente/entrar";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/paciente/entrar";
    }
}