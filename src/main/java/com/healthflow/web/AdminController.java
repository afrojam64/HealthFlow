package com.healthflow.web;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import com.healthflow.service.DomainException;
import com.healthflow.service.ProfessionalStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalStatisticsService statisticsService;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;
    private final ZoneId zoneId;

    public AdminController(UserRepository userRepository,
                           ProfessionalRepository professionalRepository,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository,
                           ProfessionalStatisticsService statisticsService, EspecialidadRepository especialidadRepository, PasswordEncoder passwordEncoder,
                           @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.statisticsService = statisticsService;
        this.especialidadRepository = especialidadRepository;
        this.passwordEncoder = passwordEncoder;
        this.zoneId = ZoneId.of(tz);
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        if (desde == null) {
            desde = LocalDate.now(zoneId).minusMonths(12);
        }
        if (hasta == null) {
            hasta = LocalDate.now(zoneId);
        }

        long totalUsuarios = userRepository.count();
        long totalProfesionales = professionalRepository.count();
        long totalPacientes = patientRepository.count();
        long totalCitas = appointmentRepository.count();
        long citasHoy = appointmentRepository.countByDateTimeBetween(
                LocalDate.now(zoneId).atStartOfDay(zoneId).toOffsetDateTime(),
                LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toOffsetDateTime());

        List<Professional> profesionales = professionalRepository.findAll();
        List<String> profNombres = profesionales.stream()
                .map(Professional::getFullName)
                .collect(Collectors.toList());

        Map<String, List<Object[]>> globalStats = statisticsService.getGlobalProfessionalStats(desde, hasta);
        List<Object[]> pacientesPorProf = globalStats.get("pacientesPorProfesional");
        List<Object[]> citasPorProf = globalStats.get("citasPorProfesional");

        Map<UUID, Long> pacientesMap = new HashMap<>();
        for (Object[] row : pacientesPorProf) {
            UUID id = (UUID) row[0];
            Long count = ((Number) row[1]).longValue();
            pacientesMap.put(id, count);
        }
        Map<UUID, Long> citasMap = new HashMap<>();
        for (Object[] row : citasPorProf) {
            UUID id = (UUID) row[0];
            Long count = ((Number) row[1]).longValue();
            citasMap.put(id, count);
        }

        List<Long> pacientesData = new ArrayList<>();
        List<Long> citasData = new ArrayList<>();
        for (Professional prof : profesionales) {
            pacientesData.add(pacientesMap.getOrDefault(prof.getId(), 0L));
            citasData.add(citasMap.getOrDefault(prof.getId(), 0L));
        }

        List<Object[]> citasPorMesRaw = appointmentRepository.countAppointmentsGroupByMonthNative(
                desde.atStartOfDay(zoneId).toOffsetDateTime(),
                hasta.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime());
        System.out.println("=== DEBUG DASHBOARD ===");
        System.out.println("desde: " + desde + " hasta: " + hasta);
        System.out.println("citasPorMesRaw size: " + citasPorMesRaw.size());
        for (Object[] row : citasPorMesRaw) {
            System.out.println("mes: " + row[0] + " año: " + row[1] + " conteo: " + row[2]);
        }
        System.out.println("pacientesPorProf size: " + pacientesPorProf.size());
        System.out.println("citasPorProf size: " + citasPorProf.size());
        System.out.println("profesionalesNombres: " + profNombres);
        System.out.println("pacientesData: " + pacientesData);
        System.out.println("citasData: " + citasData);
        List<Long> citasPorMesData = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) citasPorMesData.add(0L);
        for (Object[] row : citasPorMesRaw) {
            int mes = ((Number) row[0]).intValue();
            long conteo = ((Number) row[2]).longValue();
            citasPorMesData.set(mes - 1, citasPorMesData.get(mes - 1) + conteo);
        }

        model.addAttribute("title", "Dashboard Administrativo");
        model.addAttribute("username", getUsername());
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalProfesionales", totalProfesionales);
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("profesionalesNombres", profNombres);
        model.addAttribute("pacientesPorProfesionalData", pacientesData);
        model.addAttribute("citasPorProfesionalData", citasData);
        model.addAttribute("citasPorMesData", citasPorMesData);
        model.addAttribute("fechaDesde", desde);
        model.addAttribute("fechaHasta", hasta);
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

    @GetMapping("/profesionales/{slug}/estadisticas")
    public String estadisticasProfesional(
            @PathVariable(name = "slug") String slug,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {
        ProfessionalStatisticsService.ProfessionalStatsDTO stats = statisticsService.getProfessionalStats(slug, desde, hasta);
        model.addAttribute("title", "Estadísticas de " + stats.getProfessionalName());
        model.addAttribute("username", getUsername());
        model.addAttribute("stats", stats);
        model.addAttribute("fechaDesde", desde);
        model.addAttribute("fechaHasta", hasta);
        model.addAttribute("active", "profesionales");
        model.addAttribute("contenido", "admin/profesional-estadisticas");
        return "fragments/layout-admin";
    }

    // Mostrar formulario de creación de usuario
    @GetMapping("/usuarios/crear")
    public String mostrarFormularioCrearUsuario(Model model) {
        List<Especialidad> especialidades = especialidadRepository.findAll();
        model.addAttribute("especialidades", especialidades);
        model.addAttribute("title", "Crear nuevo usuario - HealthFlow");
        model.addAttribute("contenido", "admin/crear-usuario");
        model.addAttribute("active", "usuarios");
        return "fragments/layout";
    }

    @PostMapping("/usuarios/crear")
    public String crearUsuario(@RequestParam("rol") String rol,
                               @RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "fullName", required = false) String fullName,
                               @RequestParam(value = "specialty", required = false) String specialty,
                               @RequestParam(value = "medicalRegistry", required = false) String medicalRegistry,
                               @RequestParam(value = "nit", required = false) String nit,
                               @RequestParam(value = "providerCode", required = false) String providerCode,
                               @RequestParam(value = "tipoFacturacion", required = false) String tipoFacturacion,
                               @RequestParam(value = "especialidadId", required = false) Long especialidadId,
                               RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new DomainException("El nombre de usuario ya está en uso.");
            }
            if (userRepository.findByEmail(email).isPresent()) {
                throw new DomainException("El correo electrónico ya está registrado.");
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));   // ✅ setPasswordHash
            user.setActive(true);                                   // ✅ setActive
            user.setRole(rol);                                      // ✅ setRole
            user.setCreatedAt(OffsetDateTime.now());
            user.setUpdatedAt(OffsetDateTime.now());
            user = userRepository.save(user);

            if ("MEDICO".equals(rol)) {
                Professional professional = new Professional();
                professional.setUser(user);
                professional.setFullName(fullName);
                professional.setSpecialty(specialty);
                professional.setMedicalRegistry(medicalRegistry);
                professional.setNit(nit);
                professional.setProviderCode(providerCode);
                professional.setSlug(generateSlug(fullName));
                if (tipoFacturacion != null) {
                    professional.setTipoFacturacion(Professional.TipoFacturacion.valueOf(tipoFacturacion));
                }
                professionalRepository.save(professional);

                if (especialidadId != null) {
                    Especialidad esp = especialidadRepository.findById(especialidadId).orElse(null);
                    if (esp != null) {
                        professional.getEspecialidades().add(esp);
                        professionalRepository.save(professional);
                    }
                }
                redirectAttributes.addFlashAttribute("successMessage", "Profesional creado exitosamente.");
            } else if ("ASISTENTE".equals(rol)) {
                redirectAttributes.addFlashAttribute("successMessage", "Asistente creado exitosamente.");
            } else {
                throw new DomainException("Rol no válido.");
            }
            return "redirect:/admin/usuarios";
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/usuarios/crear";
        }
    }

    private String generateSlug(String fullName) {
        return fullName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}