package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.User;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.PermisoService;
import com.healthflow.service.SchedulingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor/citas")
public class AsistenteCitaController {

    private final SchedulingService schedulingService;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PermisoService permisoService;
    private final ZoneId zoneId;

    public AsistenteCitaController(SchedulingService schedulingService,
                                   PatientRepository patientRepository,
                                   UserRepository userRepository,
                                   PermisoService permisoService,
                                   @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.schedulingService = schedulingService;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.permisoService = permisoService;
        this.zoneId = ZoneId.of(tz);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    private UUID getCurrentProfessionalId() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            UUID medicoId = permisoService.getMedicoIdByAsistente(user.getId());
            if (medicoId == null) {
                throw new AccessDeniedException("No tienes un médico asociado");
            }
            return medicoId;
        } else {
            // Para médicos (opcional, si se permite)
            throw new AccessDeniedException("Solo los asistentes pueden usar esta funcionalidad");
        }
    }

    private void checkCrearCitasPermission() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            List<String> permisos = permisoService.getPermisosDeAsistente(user.getId());
            if (permisos == null || !permisos.contains("CREAR_CITAS")) {
                throw new AccessDeniedException("No tienes permiso para crear citas");
            }
        } else {
            throw new AccessDeniedException("Solo los asistentes pueden crear citas");
        }
    }

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        checkCrearCitasPermission();
        model.addAttribute("title", "Nueva Cita - HealthFlow");
        model.addAttribute("contenido", "doctor/nueva-cita");
        return "fragments/layout";
    }

    @PostMapping("/crear")
    public String crearCita(@RequestParam("pacienteId") UUID patientId,
                            @RequestParam("fecha") String fechaStr,
                            @RequestParam("hora") String horaStr,
                            RedirectAttributes redirectAttributes) {
        checkCrearCitasPermission();
        try {
            UUID professionalId = getCurrentProfessionalId();

            // Validar que el paciente pertenezca al médico asociado
            boolean pacientePertenece = patientRepository.existsByProfessionalIdAndPatientId(professionalId, patientId);
            if (!pacientePertenece) {
                throw new AccessDeniedException("El paciente no pertenece a tu médico asociado");
            }

            // Construir OffsetDateTime
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr);
            OffsetDateTime dateTime = fecha.atTime(hora).atZone(zoneId).toOffsetDateTime();

            // Crear cita usando SchedulingService
            Appointment cita = schedulingService.bookForPatient(professionalId, patientId, dateTime);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Cita agendada exitosamente para " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " a las " + horaStr);
            return "redirect:/doctor/calendario";
        } catch (DomainException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/citas/nueva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado: " + e.getMessage());
            return "redirect:/doctor/citas/nueva";
        }
    }

    @GetMapping("/disponibilidad")
    @ResponseBody
    public List<String> getHorariosDisponibles(@RequestParam("fecha") String fechaStr,
                                               @RequestParam("pacienteId") UUID patientId) {
        checkCrearCitasPermission(); // Validar permiso
        UUID professionalId = getCurrentProfessionalId();
        LocalDate fecha = LocalDate.parse(fechaStr);

        // Opcional: validar que el paciente pertenezca al médico
        boolean pacientePertenece = patientRepository.existsByProfessionalIdAndPatientId(professionalId, patientId);
        if (!pacientePertenece) {
            throw new AccessDeniedException("Paciente no válido");
        }

        List<OffsetDateTime> freeSlots = schedulingService.getFreeSlots(professionalId, fecha);
        return freeSlots.stream()
                .map(slot -> slot.atZoneSameInstant(zoneId).toLocalTime().toString())
                .collect(Collectors.toList());
    }
}