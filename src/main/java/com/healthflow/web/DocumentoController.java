package com.healthflow.web;

import com.healthflow.domain.Documento;
import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DocumentoService;
import com.healthflow.service.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final int defaultExpirationDays;

    public DocumentoController(DocumentoService documentoService,
                               ProfessionalRepository professionalRepository,
                               UserRepository userRepository,
                               @Value("${healthflow.document.expiration-days:7}") int defaultExpirationDays) {
        this.documentoService = documentoService;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.defaultExpirationDays = defaultExpirationDays;
    }

    private UUID getCurrentProfessionalId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }

    @GetMapping("/paciente/{patientId}")
    public String listDocuments(@PathVariable("patientId") UUID patientId, Model model) {
        var documentos = documentoService.getDocumentsByPatient(patientId);
        model.addAttribute("documentos", documentos);
        model.addAttribute("patientId", patientId);
        model.addAttribute("title", "Documentos del paciente - HealthFlow");
        model.addAttribute("contenido", "doctor/documentos");
        return "fragments/layout";
    }

    @PostMapping("/paciente/{patientId}/upload")
    public String uploadDocument(@PathVariable("patientId") UUID patientId,
                                 @RequestParam("files") List<MultipartFile> files,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "appointmentId", required = false) UUID appointmentId,
                                 @RequestParam(value = "expirationDays", required = false) Integer expirationDays,
                                 RedirectAttributes redirectAttributes) {
        try {
            int days = (expirationDays != null && expirationDays > 0) ? expirationDays : defaultExpirationDays;
            List<Documento> docs = documentoService.uploadMultipleDocuments(patientId, files, description, appointmentId, days);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Documentos subidos correctamente (" + docs.size() + "). Se ha enviado un correo al paciente con los enlaces.");
        } catch (IOException | DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al subir: " + e.getMessage());
        }
        return "redirect:/doctor/pacientes/" + patientId + "/historial";
    }

    @PostMapping("/{documentId}/delete")
    public String deleteDocument(@PathVariable("documentId") UUID documentId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Documento doc = documentoService.getDocumentById(documentId);
            UUID patientId = doc.getPatient().getId();
            documentoService.deleteDocument(documentId, getCurrentProfessionalId());
            redirectAttributes.addFlashAttribute("successMessage", "Documento eliminado");
            return "redirect:/doctor/pacientes/" + patientId + "/historial";
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/pacientes";
        }
    }
}