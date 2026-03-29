package com.healthflow.web;

import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DocumentoService;
import com.healthflow.service.DomainException;
import com.healthflow.service.PatientService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/doctor")
public class PatientController {

    private final PatientService patientService;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final ZoneId zoneId;
    private final DocumentoService documentoService;

    public PatientController(PatientService patientService,
                             ProfessionalRepository professionalRepository,
                             UserRepository userRepository,
                             PatientRepository patientRepository,
                             @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz, DocumentoService documentoService) {
        this.patientService = patientService;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.zoneId = ZoneId.of(tz);
        this.documentoService = documentoService;
    }

    @GetMapping("/pacientes")
    public String listarPacientes(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "documento", required = false) String documento,
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {

        UUID professionalId = getCurrentProfessionalId();

        PatientService.PacientesConEstado pacientes = patientService.obtenerPacientesConEstado(
                professionalId, nombre, documento, fechaDesde, fechaHasta);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate today = LocalDate.now(zoneId);
        String fechaActual = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " de " +
                today.getYear();

        String fechaTablaCitas = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        DashboardStats stats = new DashboardStats(0, 0, 0, 0);
        List<Object> proximasCitas = new ArrayList<>();

        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("fechaActual", fechaActual);
        model.addAttribute("fechaTablaCitas", fechaTablaCitas);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("prevDate", today.minusDays(1));
        model.addAttribute("nextDate", today.plusDays(1));
        model.addAttribute("today", today);

        model.addAttribute("pacientesAtendidos", pacientes.getAtendidos());
        model.addAttribute("pacientesPendientes", pacientes.getPendientes());
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("filtroDocumento", documento);
        model.addAttribute("filtroFechaDesde", fechaDesde);
        model.addAttribute("filtroFechaHasta", fechaHasta);
        model.addAttribute("title", "Mis Pacientes - HealthFlow");
        model.addAttribute("contenido", "doctor/pacientes"); // ← CORREGIDO: añadido ":: content"

        return "fragments/layout";
    }

    @GetMapping("/pacientes/{id}/historial")
    public String verHistorial(@PathVariable("id") UUID patientId, Model model) {

        UUID professionalId = getCurrentProfessionalId();

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        var historial = patientService.obtenerHistorialPaciente(patientId, professionalId);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate today = LocalDate.now(zoneId);
        String fechaActual = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " de " +
                today.getYear();

        String fechaTablaCitas = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        DashboardStats stats = new DashboardStats(0, 0, 0, 0);
        List<Object> proximasCitas = new ArrayList<>();

        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("fechaActual", fechaActual);
        model.addAttribute("fechaTablaCitas", fechaTablaCitas);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("prevDate", today.minusDays(1));
        model.addAttribute("nextDate", today.plusDays(1));
        model.addAttribute("today", today);

        model.addAttribute("patient", patient);
        model.addAttribute("historial", historial);
        model.addAttribute("documentos", documentoService.getDocumentsByPatient(patientId));
        model.addAttribute("title", "Historial de " + patient.getFirstName() + " - HealthFlow");
        model.addAttribute("contenido", "doctor/historial-paciente"); // ← CORREGIDO: añadido ":: content"

        return "fragments/layout";
    }

    private UUID getCurrentProfessionalId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }

    public static class DashboardStats {
        public final long citasHoy;
        public final long citasSemana;
        public final long pacientesNuevos;
        public final int ocupacion;

        public DashboardStats(long citasHoy, long citasSemana, long pacientesNuevos, int ocupacion) {
            this.citasHoy = citasHoy;
            this.citasSemana = citasSemana;
            this.pacientesNuevos = pacientesNuevos;
            this.ocupacion = ocupacion;
        }

        public long getCitasHoy() { return citasHoy; }
        public long getCitasSemana() { return citasSemana; }
        public long getPacientesNuevos() { return pacientesNuevos; }
        public int getOcupacion() { return ocupacion; }
    }
}