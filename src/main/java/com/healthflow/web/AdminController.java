package com.healthflow.web;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;

    public AdminController(UserRepository userRepository,
                           ProfessionalRepository professionalRepository,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository,
                           @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(tz);
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsuarios = userRepository.count();
        long totalProfesionales = professionalRepository.count();
        long totalPacientes = patientRepository.count();
        long citasHoy = appointmentRepository.countByDateTimeBetween(
                LocalDate.now(zoneId).atStartOfDay(zoneId).toOffsetDateTime(),
                LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toOffsetDateTime());
        long totalCitas = appointmentRepository.count();  // <--- NUEVA LÍNEA: total de citas

        model.addAttribute("title", "Dashboard Administrativo");
        model.addAttribute("username", getUsername());
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalProfesionales", totalProfesionales);
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("totalCitas", totalCitas);     // <--- NUEVA LÍNEA: enviar al modelo
        model.addAttribute("active", "dashboard");
        model.addAttribute("contenido", "admin/dashboard");
        return "fragments/layout-admin";
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<User> usuarios = userRepository.findAll();
        model.addAttribute("title", "Gestión de Usuarios");
        model.addAttribute("username", getUsername());
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("active", "usuarios");
        model.addAttribute("contenido", "admin/usuarios");
        return "fragments/layout-admin";
    }

    @GetMapping("/profesionales")
    public String listarProfesionales(Model model) {
        List<Professional> profesionales = professionalRepository.findAll();
        model.addAttribute("title", "Gestión de Profesionales");
        model.addAttribute("username", getUsername());
        model.addAttribute("profesionales", profesionales);
        model.addAttribute("active", "profesionales");
        model.addAttribute("contenido", "admin/profesionales");
        return "fragments/layout-admin";
    }

    @GetMapping("/pacientes")
    public String listarPacientes(@RequestParam(name = "search", required = false) String search, Model model) {
        List<Patient> pacientes;
        if (search != null && !search.isEmpty()) {
            pacientes = patientRepository.findByDocNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search, search);
        } else {
            pacientes = patientRepository.findAll();
        }
        model.addAttribute("title", "Gestión de Pacientes");
        model.addAttribute("username", getUsername());
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("search", search);
        model.addAttribute("active", "pacientes");
        model.addAttribute("contenido", "admin/pacientes");
        return "fragments/layout-admin";
    }

    @GetMapping("/citas")
    public String listarCitas(@RequestParam(name = "medico", required = false) String medico,
                              @RequestParam(name = "estado", required = false) String estado,
                              @RequestParam(name = "fecha", required = false) String fecha,
                              Model model) {
        List<Appointment> citas;

        if (medico != null && !medico.isEmpty()) {
            citas = appointmentRepository.findByProfessionalFullNameContainingIgnoreCase(medico);
        } else if (estado != null && !estado.isEmpty()) {
            try {
                AppointmentStatus status = AppointmentStatus.valueOf(estado.toUpperCase());
                citas = appointmentRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                citas = appointmentRepository.findAll();
            }
        } else if (fecha != null && !fecha.isEmpty()) {
            LocalDate date = LocalDate.parse(fecha);
            citas = appointmentRepository.findByDateTimeBetween(
                    date.atStartOfDay(zoneId).toOffsetDateTime(),
                    date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime());
        } else {
            citas = appointmentRepository.findAll();
        }

        model.addAttribute("title", "Gestión de Citas");
        model.addAttribute("username", getUsername());
        model.addAttribute("citas", citas);
        model.addAttribute("active", "citas");
        model.addAttribute("contenido", "admin/citas");
        return "fragments/layout-admin";
    }
}