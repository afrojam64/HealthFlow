package com.healthflow.web;

import com.healthflow.domain.Documento;
import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DocumentoService;
import com.healthflow.service.DomainException;
import com.healthflow.service.PermisoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final PermisoService permisoService;
    private final int defaultExpirationDays;
    private final PatientRepository patientRepository;

    public DocumentoController(DocumentoService documentoService,
                               ProfessionalRepository professionalRepository,
                               UserRepository userRepository,
                               PermisoService permisoService,
                               @Value("${healthflow.document.expiration-days:7}") int defaultExpirationDays, PatientRepository patientRepository) {
        this.documentoService = documentoService;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.permisoService = permisoService;
        this.defaultExpirationDays = defaultExpirationDays;
        this.patientRepository = patientRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    private UUID getCurrentProfessionalId() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            UUID medicoId = permisoService.getMedicoIdByAsistente(user.getId());
            if (medicoId == null) {
                throw new AccessDeniedException("No tienes un médico asociado");
            }
            return medicoId;
        } else {
            Professional professional = professionalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
            return professional.getId();
        }
    }

    private void checkVerPacientesPermission() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            if (!permisoService.tienePermiso(user.getId(), "VER_PACIENTES")) {
                throw new AccessDeniedException("No tienes permiso para ver documentos de pacientes");
            }
        }
    }

    private void checkSubirDocumentosPermission() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            List<String> permisos = permisoService.getPermisosDeAsistente(user.getId());
            if (permisos == null || !permisos.contains("SUBIR_DOCUMENTOS")) {
                throw new AccessDeniedException("No tienes permiso para subir documentos");
            }
        }
    }

    private void checkEliminarDocumentosPermission() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            if (!permisoService.tienePermiso(user.getId(), "ELIMINAR_DOCUMENTOS")) {
                throw new AccessDeniedException("No tienes permiso para eliminar documentos");
            }
        }
    }

    @GetMapping("/paciente/{patientId}")
    public String listDocuments(@PathVariable("patientId") UUID patientId, Model model) {
        checkVerPacientesPermission();
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
                                 @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
                                 RedirectAttributes redirectAttributes) {
        checkSubirDocumentosPermission();
        try {
            int days = (expirationDays != null && expirationDays > 0) ? expirationDays : defaultExpirationDays;
            UUID professionalId = getCurrentProfessionalId();
            List<Documento> docs = documentoService.uploadMultipleDocuments(patientId, files, description,
                    appointmentId, days, "MEDICO", tipoDocumento, professionalId);
            redirectAttributes.addFlashAttribute("successMessage", "Documentos subidos correctamente (" + docs.size() + ").");
        } catch (IOException | DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al subir: " + e.getMessage());
        }
        return "redirect:/doctor/pacientes/" + patientId + "/historial";
    }

    @PostMapping("/{documentId}/delete")
    public String deleteDocument(@PathVariable("documentId") UUID documentId,
                                 RedirectAttributes redirectAttributes) {
        checkEliminarDocumentosPermission(); // Opcional: si no quieres este permiso, elimina esta línea
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

    @GetMapping("/{token}/preview")
    public ResponseEntity<Resource> previewDocument(@PathVariable("token") UUID token) {
        // Este endpoint es público o requiere autenticación, pero normalmente se usa token único
        // No se añade verificación de rol aquí porque el token ya encapsula el acceso.
        Documento doc = documentoService.getByToken(token);
        Path filePath = Paths.get(doc.getFilePath());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() && !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = null;
            String filename = doc.getFileName().toLowerCase();
            if (filename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else {
                contentType = Files.probeContentType(filePath);
                if (contentType == null) contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/subir")
    public String mostrarFormularioSubida(Model model) {
        checkSubirDocumentosPermission();
        model.addAttribute("tiposDocumento", List.of("FORMULA", "REMISION", "LABORATORIO", "RADIOGRAFIA", "OTRO"));
        model.addAttribute("title", "Subir Documento - HealthFlow");
        model.addAttribute("contenido", "doctor/subir-documento");
        return "fragments/layout";
    }

    @PostMapping("/subir")
    public String subirDocumento(@RequestParam("pacienteId") UUID patientId,
                                 @RequestParam("tipoDocumento") String tipoDocumento,
                                 @RequestParam("descripcion") String descripcion,
                                 @RequestParam("files") List<MultipartFile> files,
                                 RedirectAttributes redirectAttributes) {
        checkSubirDocumentosPermission();
        try {
            UUID professionalId = getCurrentProfessionalId();
            // Validar que el paciente pertenezca al médico asociado
            List<Patient> pacientesValidos = patientRepository.findPatientsByProfessionalId(professionalId);
            boolean pacientePertenece = pacientesValidos.stream().anyMatch(p -> p.getId().equals(patientId));
            if (!pacientePertenece) {
                throw new AccessDeniedException("El paciente no pertenece a tu médico asociado");
            }
            int expirationDays = defaultExpirationDays; // usar valor por defecto
            List<Documento> docs = documentoService.uploadMultipleDocuments(patientId, files, descripcion,
                    null, expirationDays, "ASISTENTE", tipoDocumento, professionalId);
            redirectAttributes.addFlashAttribute("successMessage", "Documento(s) subido(s) correctamente (" + docs.size() + ").");
        } catch (IOException | DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al subir: " + e.getMessage());
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/documentos/subir";
    }

    @GetMapping("/api/buscar-pacientes")
    @ResponseBody
    public List<Map<String, String>> buscarPacientes(@RequestParam("q") String query) {
        checkSubirDocumentosPermission(); // solo asistentes con permiso pueden buscar
        UUID professionalId = getCurrentProfessionalId();
        List<Patient> pacientes = patientRepository.findByProfessionalIdAndSearchText(professionalId, query);
        return pacientes.stream().limit(10).map(p -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", p.getId().toString());
            map.put("nombre", p.getFirstName() + " " + p.getLastName());
            map.put("documento", p.getDocNumber());
            return map;
        }).collect(Collectors.toList());
    }
}