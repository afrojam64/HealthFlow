package com.healthflow.web;

import com.healthflow.api.dto.rips.RipsValidationResult;
import com.healthflow.domain.CatalogoCUPS;
import com.healthflow.domain.RipsGeneration;
import com.healthflow.domain.Professional;
import com.healthflow.repo.CatalogoCUPSRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.RipsGenerationRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.RipsReminderService;
import com.healthflow.service.RipsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/reportes")
public class RipsController {

    @Autowired
    private RipsService ripsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private RipsGenerationRepository ripsGenerationRepository;

    @Autowired
    private RipsReminderService reminderService;

    @Autowired
    private CatalogoCUPSRepository cupsRepository;

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UUID getCurrentProfessionalId() {
        String username = getUsername();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }

    @GetMapping("/rips")
    public String mostrarFormulario(Model model) {
        String username = getUsername();
        LocalDate today = LocalDate.now();

        model.addAttribute("username", username);
        model.addAttribute("today", today);
        model.addAttribute("title", "Reportes RIPS - HealthFlow");
        model.addAttribute("contenido", "doctor/reportes/rips");

        return "fragments/layout";
    }

    @PostMapping("/rips/generar")
    public String generarRips(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "numFactura", required = false) String numFactura,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        UUID professionalId = getCurrentProfessionalId();

        try {
            RipsGeneration generation = ripsService.generarRips(professionalId, fechaInicio, fechaFin, numFactura, false);
            Path filePath = Paths.get(generation.getArchivoPath());
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath.getFileName().toString() + "\"");
            Files.copy(filePath, response.getOutputStream());
            response.flushBuffer();
            return null; // Éxito, ya se envió el archivo
        } catch (RuntimeException e) {
            // Capturamos la excepción de servicio (incluye "No hay citas atendidas")
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/doctor/reportes/rips";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al generar el archivo: " + e.getMessage());
            return "redirect:/doctor/reportes/rips";
        }
    }

    @GetMapping("/rips/historial")
    public String verHistorial(Model model) {
        String username = getUsername();
        UUID professionalId = getCurrentProfessionalId();
        List<RipsGeneration> generaciones = ripsGenerationRepository.findByProfessionalIdOrderByFechaGeneracionDesc(professionalId);

        model.addAttribute("username", username);
        model.addAttribute("title", "Historial RIPS - HealthFlow");
        model.addAttribute("generaciones", generaciones);
        model.addAttribute("contenido", "doctor/reportes/historial"); // nuevo fragmento
        return "fragments/layout";
    }

    @PostMapping("/rips/actualizar-cuv")
    public String actualizarCuv(@RequestParam("id") UUID id, @RequestParam("cuv") String cuv) {
        RipsGeneration gen = ripsGenerationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generación no encontrada"));

        // Verificar que el profesional sea el dueño
        if (!gen.getProfessionalId().equals(getCurrentProfessionalId())) {
            throw new RuntimeException("No autorizado");
        }

        gen.setCuv(cuv);
        ripsGenerationRepository.save(gen);
        return "redirect:/doctor/reportes/rips/historial";
    }

    @GetMapping("/rips/descargar/{id}")
    public void descargarJson(@PathVariable("id") UUID id, HttpServletResponse response) throws IOException {
        RipsGeneration gen = ripsGenerationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generación no encontrada"));

        if (!gen.getProfessionalId().equals(getCurrentProfessionalId())) {
            throw new RuntimeException("No autorizado");
        }

        Path filePath = Paths.get(gen.getArchivoPath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("El archivo no existe en el servidor");
        }

        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath.getFileName().toString() + "\"");
        Files.copy(filePath, response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/rips/validar")
    @ResponseBody
    public RipsValidationResult validarRips(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        UUID professionalId = getCurrentProfessionalId();
        return ripsService.validarCitas(professionalId, fechaInicio, fechaFin);
    }

    @GetMapping("/test/recordatorios")
    @ResponseBody
    public String probarRecordatorios() {
        try {
            reminderService.enviarRecordatoriosRipsPendientes();
            return "Recordatorios ejecutados. Revisa los logs del servidor.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/api/cups/search")
    @ResponseBody
    public List<CatalogoCUPS> buscarCups(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) return List.of();
        Pageable pageable = PageRequest.of(0, 20);
        return cupsRepository.searchByCodeOrDescription(query.trim(), pageable);
    }
}