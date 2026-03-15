package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/citas")
public class DoctorClinicalNoteController {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordService medicalRecordService;

    public DoctorClinicalNoteController(AppointmentRepository appointmentRepository, MedicalRecordService medicalRecordService) {
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/{id}/atender")
    public String showClinicalNoteForm(@PathVariable("id") UUID appointmentId, Model model) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", appointment.getPatient());
        model.addAttribute("title", "Atención Clínica");
        if (appointment.getMedicalRecord() != null) {
            model.addAttribute("medicalRecord", appointment.getMedicalRecord());
        }

        return "doctor/atencion";
    }

    @PostMapping("/atencion/guardar")
    public String saveClinicalNote(@RequestParam("appointmentId") UUID appointmentId,
                                   @RequestParam("reason") String reason,
                                   @RequestParam("evolution") String evolution,
                                   @RequestParam(value = "prescription", required = false) String prescription,
                                   @RequestParam(value = "mainDiagnosis", required = false) String mainDiagnosis,
                                   RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.saveMedicalRecord(appointmentId, reason, evolution, prescription, mainDiagnosis);
            redirectAttributes.addFlashAttribute("successMessage", "Historia clínica guardada y cerrada correctamente.");
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/citas/" + appointmentId + "/atender";
        }
        return "redirect:/dashboard";
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
