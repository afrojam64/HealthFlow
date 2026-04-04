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
    private final DiagnosticoCIE10Repository diagnosticoRepo;

    public DoctorAppointmentAttentionController(AppointmentRepository appointmentRepository,
                                                MedicalRecordService medicalRecordService,
                                                MedicalRecordRepository medicalRecordRepository,
                                                CatalogoFinalidadConsultaRepository finalidadRepo,
                                                CatalogoCausaExternaRepository causaExternaRepo,
                                                DiagnosticoCIE10Repository diagnosticoRepo) {
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.finalidadRepo = finalidadRepo;
        this.causaExternaRepo = causaExternaRepo;
        this.diagnosticoRepo = diagnosticoRepo;
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
        List<DiagnosticoCIE10> diagnosticos = diagnosticoRepo.findAll();

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", appointment.getPatient());
        model.addAttribute("medicalRecord", currentRecord);
        model.addAttribute("previousRecords", previousRecords);
        model.addAttribute("finalidades", finalidades);
        model.addAttribute("causas", causas);
        model.addAttribute("diagnosticos", diagnosticos);
        model.addAttribute("title", "Atención Clínica");

        // 👇 LO QUE FALTA: indicar el fragmento y retornar el layout
        model.addAttribute("contenido", "doctor/atencion");
        return "fragments/layout";
    }

    /**
     * Guarda o actualiza la historia clínica de una cita.
     * Recibe los datos del formulario de atención clínica, incluyendo los nuevos campos
     * enfermedad actual, examen físico y concepto, y delega el guardado al servicio.
     */
    @PostMapping("/{id}/guardar")
    public String saveClinicalNote(@PathVariable("id") UUID appointmentId,
                                   @RequestParam("reason") String reason,
                                   @RequestParam(name = "enfermedadActual", required = false) String enfermedadActual,
                                   @RequestParam(name = "examenFisico", required = false) String examenFisico,
                                   @RequestParam(name = "concepto", required = false) String concepto,
                                   @RequestParam(name = "prescription", required = false) String prescription,
                                   @RequestParam(name = "mainDiagnosis", required = false) String mainDiagnosis,
                                   @RequestParam(name = "finalidadId", required = false) Long finalidadId,
                                   @RequestParam(name = "causaExternaId", required = false) Long causaExternaId,
                                   @RequestParam(name = "valorServicio", required = false) BigDecimal valorServicio,
                                   @RequestParam(name = "cuotaModeradora", required = false) BigDecimal cuotaModeradora,
                                   @RequestParam(name = "copago", required = false) BigDecimal copago,
                                   @RequestParam(name = "codigoCups", required = false) String codigoCups,
                                   @RequestParam(name = "relatedDiagnosis1", required = false) String relatedDiagnosis1,
                                   @RequestParam(name = "relatedDiagnosis2", required = false) String relatedDiagnosis2,
                                   @RequestParam(name = "complicationDiagnosis", required = false) String complicationDiagnosis,
                                   @RequestParam(name = "accion", required = false) String accion,
                                   RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.saveMedicalRecord(appointmentId, reason, enfermedadActual, examenFisico, concepto,
                    prescription, mainDiagnosis, finalidadId, causaExternaId, valorServicio, cuotaModeradora,
                    copago, codigoCups, relatedDiagnosis1, relatedDiagnosis2, complicationDiagnosis);

            if ("finalizar".equals(accion)) {
                medicalRecordService.markAsAttendedAndLock(appointmentId);
                redirectAttributes.addFlashAttribute("successMessage", "Consulta finalizada y cerrada correctamente.");
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Borrador guardado correctamente.");
                return "redirect:/doctor/citas/" + appointmentId + "/atender";
            }
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/citas/" + appointmentId + "/atender";
        }
    }
}