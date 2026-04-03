package com.healthflow.web;

import com.healthflow.domain.Documento;
import com.healthflow.service.DocumentoService;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/subir-documento")
    public String mostrarFormulario(HttpSession session, Model model) {
        UUID pacienteId = (UUID) session.getAttribute("pacienteId");
        if (pacienteId == null) return "redirect:/paciente/entrar";
        List<Documento> documentos = documentoService.getDocumentsByPatient(pacienteId);
        model.addAttribute("documentos", documentos);
        return "paciente/subir-documento";
    }

    @PostMapping("/subir-documento")
    public String subirDocumento(@RequestParam("files") List<MultipartFile> files,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "expirationDays", required = false) Integer expirationDays,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        UUID pacienteId = (UUID) session.getAttribute("pacienteId");
        if (pacienteId == null) return "redirect:/paciente/entrar";
        try {
            int days = (expirationDays != null && expirationDays > 0) ? expirationDays : 30;
            documentoService.uploadMultipleDocuments(pacienteId, files, description, null, days);
            redirectAttributes.addFlashAttribute("successMessage", "Documentos subidos correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/paciente/subir-documento";
    }
}