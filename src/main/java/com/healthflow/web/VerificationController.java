package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.DocumentoFirmado;
import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.DocumentoFirmadoRepository;
import com.healthflow.repo.RemisionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Controller
@RequestMapping("/public/verify")
public class VerificationController {

    private final DocumentoFirmadoRepository documentoFirmadoRepository;
    private final AppointmentRepository appointmentRepository;
    private final RemisionRepository remisionRepository;

    public VerificationController(DocumentoFirmadoRepository documentoFirmadoRepository,
                                  AppointmentRepository appointmentRepository, RemisionRepository remisionRepository) {
        this.documentoFirmadoRepository = documentoFirmadoRepository;
        this.appointmentRepository = appointmentRepository;
        this.remisionRepository = remisionRepository;
    }

    @GetMapping("/document")
    public String verifyDocument(@RequestParam("token") UUID token, Model model) {
        var docFirmado = documentoFirmadoRepository.findByToken(token).orElse(null);
        if (docFirmado == null) {
            model.addAttribute("valid", false);
            model.addAttribute("message", "Token no válido o documento no encontrado.");
            return "public/verify";
        }

        if ("FORMULA_MEDICA".equals(docFirmado.getTipoDocumento())) {
            UUID appointmentId = docFirmado.getReferenciaId();
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment != null) {
                Patient patient = appointment.getPatient();
                Professional professional = appointment.getProfessional();
                model.addAttribute("valid", true);
                model.addAttribute("patientName", patient.getFirstName() + " " + patient.getLastName());
                model.addAttribute("doctorName", professional.getFullName());
                model.addAttribute("specialty", professional.getSpecialty());
                model.addAttribute("date", docFirmado.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                model.addAttribute("documentType", "Fórmula Médica");
                return "public/verify";
            }
        }

        model.addAttribute("valid", false);
        model.addAttribute("message", "No se pudieron recuperar los datos del documento.");
        return "public/verify";
    }

    @GetMapping("/remision")
    public String verifyRemision(@RequestParam("token") UUID token, Model model) {
        var remision = remisionRepository.findByToken(token).orElse(null);
        if (remision == null) {
            model.addAttribute("valid", false);
            model.addAttribute("message", "Token no válido o remisión no encontrada.");
            return "public/verify";
        }
        // Obtener datos de la cita
        Appointment appointment = appointmentRepository.findById(remision.getCitaId()).orElse(null);
        if (appointment == null) {
            model.addAttribute("valid", false);
            model.addAttribute("message", "Datos de la cita no encontrados.");
            return "public/verify";
        }
        Patient patient = appointment.getPatient();
        Professional professional = appointment.getProfessional();
        model.addAttribute("valid", true);
        model.addAttribute("patientName", patient.getFirstName() + " " + patient.getLastName());
        model.addAttribute("doctorName", professional.getFullName());
        model.addAttribute("specialty", remision.getEspecialidad());
        model.addAttribute("date", remision.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        model.addAttribute("documentType", "Remisión a " + remision.getEspecialidad());
        model.addAttribute("motivo", remision.getMotivo());
        model.addAttribute("prioridad", remision.getPrioridad());
        return "public/verify";
    }
}