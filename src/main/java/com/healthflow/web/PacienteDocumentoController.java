package com.healthflow.web;

import com.healthflow.domain.Documento;
import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.service.DocumentoService;
import com.healthflow.service.DomainException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/paciente")
public class PacienteDocumentoController {

    private final DocumentoService documentoService;
    private final ProfessionalRepository professionalRepository;
    private final ZoneId zoneId = ZoneId.of("America/Bogota");

    public PacienteDocumentoController(DocumentoService documentoService, ProfessionalRepository professionalRepository) {
        this.documentoService = documentoService;
        this.professionalRepository = professionalRepository;
    }

    private UUID getAuthenticatedPatientId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        throw new DomainException("No autenticado");
    }

    // Endpoint para obtener los médicos que han atendido al paciente (para el select)
    @GetMapping("/documentos/medicos")
    @ResponseBody
    public List<Map<String, Object>> getMedicos() {
        UUID patientId = getAuthenticatedPatientId();
        List<Professional> profesionales = professionalRepository.findByPatientId(patientId);
        return profesionales.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("nombre", p.getFullName());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/subir-documento")
    public String mostrarFormulario(
            @RequestParam(value = "tipo", required = false) String tipoFiltro,
            @RequestParam(value = "origen", required = false) String origenFiltro,
            Model model) {

        UUID pacienteId = getAuthenticatedPatientId();
        List<Documento> todosDocumentos = documentoService.getDocumentsByPatient(pacienteId);

        // Aplicar filtros
        List<Documento> filtrados = todosDocumentos.stream()
                .filter(d -> {
                    if (tipoFiltro != null && !tipoFiltro.isEmpty() && !tipoFiltro.equals(d.getTipoDocumento()))
                        return false;
                    if (origenFiltro != null && !origenFiltro.isEmpty() && !origenFiltro.equals(d.getOrigen()))
                        return false;
                    return true;
                })
                .collect(Collectors.toList());

        // Separar por origen
        List<Documento> documentosMedico = filtrados.stream()
                .filter(d -> "MEDICO".equals(d.getOrigen()))
                .collect(Collectors.toList());
        List<Documento> documentosPaciente = filtrados.stream()
                .filter(d -> "PACIENTE".equals(d.getOrigen()))
                .collect(Collectors.toList());

        // Ordenar por fecha descendente
        documentosMedico.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        documentosPaciente.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        List<String> tiposDocumento = List.of("LABORATORIO", "RADIOGRAFIA", "OTRO");

        model.addAttribute("tiposDocumento", tiposDocumento);
        model.addAttribute("documentosMedico", documentosMedico);
        model.addAttribute("documentosPaciente", documentosPaciente);
        // Mantener los filtros seleccionados en la vista
        model.addAttribute("filtroTipo", tipoFiltro);
        model.addAttribute("filtroOrigen", origenFiltro);
        model.addAttribute("title", "Mis documentos - HealthFlow");

        return "paciente/subir-documento";
    }

    @PostMapping("/subir-documento")
    public String subirDocumento(@RequestParam("files") List<MultipartFile> files,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "expirationDays", required = false) Integer expirationDays,
                                 @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
                                 @RequestParam("medicoId") UUID medicoId,
                                 RedirectAttributes redirectAttributes) {
        UUID pacienteId = getAuthenticatedPatientId();
        try {
            int days = (expirationDays != null && expirationDays > 0) ? expirationDays : 30;
            documentoService.uploadMultipleDocuments(pacienteId, files, description, null, days, "PACIENTE", tipoDocumento, medicoId);
            redirectAttributes.addFlashAttribute("successMessage", "Documentos subidos correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/paciente/subir-documento";
    }
}