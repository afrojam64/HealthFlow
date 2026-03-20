package com.healthflow.web;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import com.healthflow.service.DomainException;
import com.healthflow.service.MedicalRecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/citas")
public class DoctorAppointmentAttentionController {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final CatalogoFinalidadConsultaRepository finalidadRepo;
    private final CatalogoCausaExternaRepository causaExternaRepo;

    public DoctorAppointmentAttentionController(AppointmentRepository appointmentRepository,
                                                MedicalRecordService medicalRecordService,
                                                MedicalRecordRepository medicalRecordRepository,
                                                CatalogoFinalidadConsultaRepository finalidadRepo,
                                                CatalogoCausaExternaRepository causaExternaRepo) {
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.finalidadRepo = finalidadRepo;
        this.causaExternaRepo = causaExternaRepo;
    }

    @GetMapping("/{id}/atender")
    public String showAttentionForm(@PathVariable("id") UUID appointmentId, Model model) {
        MedicalRecord currentRecord = medicalRecordService.getOrCreateForAppointment(appointmentId);
        Appointment appointment = currentRecord.getAppointment();

        List<MedicalRecord> previousRecords = medicalRecordRepository
                .findByAppointmentPatientIdOrderByAppointmentDateTimeDesc(appointment.getPatient().getId())
                .stream()
                .filter(rec -> !rec.getId().equals(currentRecord.getId()))
                .toList();

        List<CatalogoFinalidadConsulta> finalidades = finalidadRepo.findAll();
        List<CatalogoCausaExterna> causas = causaExternaRepo.findAll();

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", appointment.getPatient());
        model.addAttribute("medicalRecord", currentRecord);
        model.addAttribute("previousRecords", previousRecords);
        model.addAttribute("finalidades", finalidades);
        model.addAttribute("causas", causas);
        model.addAttribute("title", "Atención Clínica");
        model.addAttribute("contenido", "doctor/atencion");

        return "fragments/layout";
    }

    @PostMapping("/{id}/guardar")
    public String saveClinicalNote(@PathVariable("id") UUID appointmentId,
                                   @RequestParam("reason") String reason,
                                   @RequestParam("evolution") String evolution,
                                   @RequestParam(name = "prescription", required = false) String prescription,
                                   @RequestParam(name = "mainDiagnosis", required = false) String mainDiagnosis,
                                   @RequestParam(name = "finalidadId", required = false) Long finalidadId,
                                   @RequestParam(name = "causaExternaId", required = false) Long causaExternaId,
                                   @RequestParam(name = "valorServicio", required = false) BigDecimal valorServicio,
                                   @RequestParam(name = "cuotaModeradora", required = false) BigDecimal cuotaModeradora,
                                   @RequestParam(name = "copago", required = false) BigDecimal copago,
                                   @RequestParam(name = "codigoCups", required = false) String codigoCups,
                                   RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.saveMedicalRecord(appointmentId, reason, evolution, prescription, mainDiagnosis,
                    finalidadId, causaExternaId, valorServicio, cuotaModeradora, copago, codigoCups);
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