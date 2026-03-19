package com.healthflow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthflow.api.dto.rips.RipsDTO;
import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.RipsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/reportes")
public class RipsController {

    private final RipsService ripsService;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public RipsController(RipsService ripsService,
                          ProfessionalRepository professionalRepository,
                          UserRepository userRepository,
                          ObjectMapper objectMapper) {
        this.ripsService = ripsService;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/rips")
    public String mostrarFormulario(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate today = LocalDate.now();

        // Variables necesarias para el layout
        model.addAttribute("username", username);
        model.addAttribute("today", today);
        model.addAttribute("title", "Reportes RIPS - HealthFlow");
        model.addAttribute("contenido", "doctor/reportes/rips");

        return "fragments/layout";
    }

    @PostMapping("/rips/generar")
    public ResponseEntity<byte[]> generarRips(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        UUID professionalId = getCurrentProfessionalId();

        RipsDTO rips;
        try {
            rips = ripsService.generarRips(professionalId, fechaInicio, fechaFin);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el reporte RIPS: " + e.getMessage(), e);
        }

        // Convertir a JSON usando el ObjectMapper inyectado (que ya tiene el módulo JSR310)
        String json;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rips);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al serializar JSON", e);
        }

        // Preparar respuesta para descarga
        String filename = String.format("rips_%s_%s.json", fechaInicio, fechaFin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.getBytes());
    }

    private UUID getCurrentProfessionalId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }
}