package com.healthflow.web;

import com.healthflow.domain.*;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.SchedulingService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/paciente")
public class PacienteAppointmentController {

    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final ProfessionalRepository professionalRepo;
    private final SchedulingService schedulingService;

    public PacienteAppointmentController(PatientRepository patientRepo,
                                         AppointmentRepository appointmentRepo,
                                         ProfessionalRepository professionalRepo,
                                         SchedulingService schedulingService) {
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
        this.professionalRepo = professionalRepo;
        this.schedulingService = schedulingService;
    }

    private UUID getAuthenticatedPatientId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        throw new DomainException("No autenticado");
    }

    @GetMapping("/agendar")
    public String mostrarAgendar(Model model, RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        Patient patient = patientRepo.findById(pacienteId).orElse(null);
        if (patient == null) return "redirect:/paciente/entrar";

        Appointment ultimaCita = appointmentRepo.findTopByPatientIdOrderByDateTimeDesc(pacienteId).orElse(null);
        UUID professionalId = null;
        if (ultimaCita != null) {
            professionalId = ultimaCita.getProfessional().getId();
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes citas previas. Agenda desde la página pública.");
            return "redirect:/public/";
        }
        Professional professional = professionalRepo.findById(professionalId).orElse(null);
        if (professional == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Médico no encontrado.");
            return "redirect:/paciente/dashboard";
        }
        model.addAttribute("professional", professional);
        model.addAttribute("professionalId", professionalId);
        model.addAttribute("patient", patient);
        model.addAttribute("title", "Agendar cita | HealthFlow");
        return "paciente/agendar";
    }

    @PostMapping("/agendar")
    public String agendarCita(@RequestParam("professionalId") UUID professionalId,
                              @RequestParam("dateTime") String dateTimeStr,
                              RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeStr);
            Appointment cita = schedulingService.bookForPatient(professionalId, pacienteId, dateTime);
            redirectAttributes.addFlashAttribute("successMessage", "Cita agendada correctamente.");
            return "redirect:/paciente/mis-citas";
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/paciente/agendar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado. Intenta de nuevo.");
            return "redirect:/paciente/agendar";
        }
    }

    @GetMapping("/mis-citas")
    public String misCitas(Model model) {
        UUID pacienteId = getAuthenticatedPatientId();
        List<Appointment> citas = appointmentRepo.findByPatientIdOrderByDateTimeDesc(pacienteId);
        ZoneId zone = ZoneId.of("America/Bogota");
        OffsetDateTime now = OffsetDateTime.now(zone);

        List<CitaDTO> futuras = new ArrayList<>();
        List<CitaDTO> pasadas = new ArrayList<>();

        for (Appointment cita : citas) {
            LocalDateTime localDateTime = cita.getDateTime().atZoneSameInstant(zone).toLocalDateTime();
            CitaDTO dto = new CitaDTO(cita, localDateTime);
            if (cita.getDateTime().isAfter(now)) {
                futuras.add(dto);
            } else {
                pasadas.add(dto);
            }
        }

        model.addAttribute("futuras", futuras);
        model.addAttribute("pasadas", pasadas);
        return "paciente/mis-citas";
    }

    @PostMapping("/cancelar/{appointmentId}")
    public String cancelarCita(@PathVariable("appointmentId") UUID appointmentId,
                               RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        Appointment cita = appointmentRepo.findById(appointmentId).orElse(null);
        if (cita == null || !cita.getPatient().getId().equals(pacienteId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cita no encontrada.");
            return "redirect:/paciente/mis-citas";
        }
        if (cita.getStatus() == AppointmentStatus.ATENDIDA || cita.getStatus() == AppointmentStatus.CANCELADA) {
            redirectAttributes.addFlashAttribute("errorMessage", "Esta cita no se puede cancelar.");
            return "redirect:/paciente/mis-citas";
        }
        cita.setStatus(AppointmentStatus.CANCELADA);
        appointmentRepo.save(cita);
        redirectAttributes.addFlashAttribute("successMessage", "Cita cancelada correctamente.");
        return "redirect:/paciente/mis-citas";
    }

    @GetMapping("/reprogramar/{appointmentId}")
    public String mostrarReprogramar(@PathVariable("appointmentId") UUID appointmentId,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        Appointment cita = appointmentRepo.findById(appointmentId).orElse(null);
        if (cita == null || !cita.getPatient().getId().equals(pacienteId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cita no encontrada.");
            return "redirect:/paciente/mis-citas";
        }
        model.addAttribute("cita", cita);
        model.addAttribute("professionalId", cita.getProfessional().getId());
        model.addAttribute("professional", cita.getProfessional());
        model.addAttribute("fechaActual", cita.getDateTime().toString());
        return "paciente/reprogramar";
    }

    @PostMapping("/reprogramar/{appointmentId}")
    public String reprogramarCita(@PathVariable("appointmentId") UUID appointmentId,
                                  @RequestParam("newDateTime") String newDateTimeStr,
                                  RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        Appointment cita = appointmentRepo.findById(appointmentId).orElse(null);
        if (cita == null || !cita.getPatient().getId().equals(pacienteId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cita no encontrada.");
            return "redirect:/paciente/mis-citas";
        }
        OffsetDateTime newDateTime = OffsetDateTime.parse(newDateTimeStr);
        try {
            schedulingService.rescheduleAppointment(appointmentId, newDateTime);
            redirectAttributes.addFlashAttribute("successMessage", "Cita reprogramada correctamente.");
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/paciente/mis-citas";
    }

    @GetMapping("/detalle-cita/{appointmentId}")
    public String detalleCita(@PathVariable("appointmentId") UUID appointmentId, Model model) {
        UUID pacienteId = getAuthenticatedPatientId();
        Appointment cita = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));
        if (!cita.getPatient().getId().equals(pacienteId)) {
            throw new DomainException("No tienes permiso para ver esta cita");
        }
        MedicalRecord record = cita.getMedicalRecord();
        model.addAttribute("cita", cita);
        model.addAttribute("record", record);
        model.addAttribute("title", "Detalle de cita - HealthFlow");
        return "paciente/detalle-cita";
    }

    public static class CitaDTO {
        private final Appointment cita;
        private final LocalDateTime fechaLocal;

        public CitaDTO(Appointment cita, LocalDateTime fechaLocal) {
            this.cita = cita;
            this.fechaLocal = fechaLocal;
        }
        public Appointment getCita() { return cita; }
        public LocalDateTime getFechaLocal() { return fechaLocal; }
        public String getStatus() { return cita.getStatus().name(); }
        public Professional getProfessional() { return cita.getProfessional(); }
    }
}