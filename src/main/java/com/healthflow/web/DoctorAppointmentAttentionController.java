package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.MedicalRecord;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.MedicalRecordRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.MedicalRecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/citas")
public class DoctorAppointmentAttentionController {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordRepository medicalRecordRepository;

    public DoctorAppointmentAttentionController(AppointmentRepository appointmentRepository,
                                                MedicalRecordService medicalRecordService,
                                                MedicalRecordRepository medicalRecordRepository) {
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @GetMapping("/{id}/atender")
    public String showAttentionForm(@PathVariable("id") UUID appointmentId, Model model) {
        // 1. Obtener el registro actual (nuevo o existente)
        MedicalRecord currentRecord = medicalRecordService.getOrCreateForAppointment(appointmentId);
        Appointment appointment = currentRecord.getAppointment();

        // 2. Obtener el historial de registros anteriores
        List<MedicalRecord> previousRecords = medicalRecordRepository
                .findByAppointmentPatientIdOrderByAppointmentDateTimeDesc(appointment.getPatient().getId())
                .stream()
                .filter(rec -> !rec.getId().equals(currentRecord.getId())) // Excluir el actual
                .toList();

        // 3. Pasar todo al modelo
        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", appointment.getPatient());
        model.addAttribute("medicalRecord", currentRecord);
        model.addAttribute("previousRecords", previousRecords);
        model.addAttribute("title", "Atención Clínica");

        return "doctor/atencion";
    }

    @PostMapping("/{id}/guardar")
    public String saveClinicalNote(@PathVariable("id") UUID appointmentId,
                                   @RequestParam("reason") String reason,
                                   @RequestParam("evolution") String evolution,
                                   @RequestParam(name = "prescription", required = false) String prescription,
                                   @RequestParam(name = "mainDiagnosis", required = false) String mainDiagnosis,
                                   RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.saveMedicalRecord(appointmentId, reason, evolution, prescription, mainDiagnosis);
            redirectAttributes.addFlashAttribute("successMessage", "Borrador guardado correctamente.");
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/citas/" + appointmentId + "/atender";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizeAndLock(@PathVariable("id") UUID appointmentId, RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.markAsAttendedAndLock(appointmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Consulta finalizada y cerrada correctamente.");
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
