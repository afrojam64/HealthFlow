package com.healthflow.web;

import com.healthflow.domain.RipsGeneration;
import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.RipsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
    public void generarRips(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "numFactura", required = false) String numFactura,
            HttpServletResponse response) throws IOException {

        UUID professionalId = getCurrentProfessionalId();
        RipsGeneration generation = ripsService.generarRips(professionalId, fechaInicio, fechaFin, numFactura, false);

        Path filePath = Paths.get(generation.getArchivoPath());
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath.getFileName().toString() + "\"");
        Files.copy(filePath, response.getOutputStream());
        response.flushBuffer();
    }
}