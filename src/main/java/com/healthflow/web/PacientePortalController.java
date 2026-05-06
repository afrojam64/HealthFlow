package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.service.JwtService;
import com.healthflow.service.NotificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/paciente")
public class PacientePortalController {

    private final PatientRepository patientRepo;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;

    public PacientePortalController(PatientRepository patientRepo,
                                    JwtService jwtService,
                                    NotificationService notificationService,
                                    AppointmentRepository appointmentRepository,
                                    ProfessionalRepository professionalRepository) {
        this.patientRepo = patientRepo;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.appointmentRepository = appointmentRepository;
        this.professionalRepository = professionalRepository;
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
        Cookie cookie = new Cookie("pacienteToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        return "redirect:/paciente/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@CookieValue(value = "pacienteToken", required = false) String token,
                            Model model,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        if (token == null) {
            return "redirect:/paciente/entrar";
        }

        try {
            // Obtener ID del paciente desde el token
            UUID patientId = jwtService.getPatientIdFromToken(token);
            Patient patient = patientRepo.findById(patientId).orElse(null);
            if (patient == null) {
                return "redirect:/paciente/entrar";
            }

            // Poner nombre del paciente en el modelo y sesión
            String nombreCompleto = patient.getFirstName() + " " + patient.getLastName();
            model.addAttribute("pacienteNombre", nombreCompleto);
            request.getSession().setAttribute("pacienteNombre", nombreCompleto);

            // Obtener el médico del paciente (última cita)
            Optional<Appointment> ultimaCita = appointmentRepository.findTopByPatientIdOrderByDateTimeDesc(patient.getId());
            if (ultimaCita.isPresent()) {
                Professional medico = ultimaCita.get().getProfessional();
                if (medico != null) {
                    model.addAttribute("medicoNombre", medico.getFullName());
                    model.addAttribute("medicoAvatar", medico.getAvatarUrl());
                    model.addAttribute("medicoEspecialidad", medico.getSpecialty());
                    model.addAttribute("medicoBiografia", medico.getBiografia() != null ? medico.getBiografia() : "");
                } else {
                    model.addAttribute("medicoNombre", "No asignado");
                    model.addAttribute("medicoAvatar", null);
                    model.addAttribute("medicoEspecialidad", "No asignada");
                    model.addAttribute("medicoBiografia", "");
                }
            } else {
                model.addAttribute("medicoNombre", "Aún sin médico asignado");
                model.addAttribute("medicoAvatar", null);
                model.addAttribute("medicoEspecialidad", "No asignada");
                model.addAttribute("medicoBiografia", "");
            }

            // Opcional: pasar el token a la vista si se necesita en los enlaces
            model.addAttribute("token", token);

            return "paciente/dashboard";
        } catch (Exception e) {
            // Token inválido o expirado
            Cookie cookie = new Cookie("pacienteToken", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return "redirect:/paciente/entrar";
        }
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