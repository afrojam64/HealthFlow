package com.healthflow.web;

import com.healthflow.domain.Documento;
import com.healthflow.service.DocumentoService;
import com.healthflow.service.DomainException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/paciente")
public class PacienteDocumentoController {

    private final DocumentoService documentoService;

    public PacienteDocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    private UUID getAuthenticatedPatientId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        throw new DomainException("No autenticado");
    }

    @GetMapping("/subir-documento")
    public String mostrarFormulario(Model model) {
        UUID pacienteId = getAuthenticatedPatientId();
        List<Documento> documentos = documentoService.getDocumentsByPatient(pacienteId);
        // Separar por origen (MEDICO / PACIENTE) igual que antes
        model.addAttribute("documentos", documentos);
        return "paciente/subir-documento";
    }

    @PostMapping("/subir-documento")
    public String subirDocumento(@RequestParam("files") List<MultipartFile> files,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "expirationDays", required = false) Integer expirationDays,
                                 @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
                                 RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        try {
            int days = (expirationDays != null && expirationDays > 0) ? expirationDays : 30;
            documentoService.uploadMultipleDocuments(pacienteId, files, description, null, days, "PACIENTE", tipoDocumento);
            redirectAttributes.addFlashAttribute("successMessage", "Documentos subidos correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/paciente/subir-documento";
    }
}