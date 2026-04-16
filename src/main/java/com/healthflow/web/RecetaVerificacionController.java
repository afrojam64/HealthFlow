package com.healthflow.web;

import com.healthflow.domain.Receta;
import com.healthflow.service.RecetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/public/verify")
public class RecetaVerificacionController {

    private final RecetaService recetaService;

    public RecetaVerificacionController(RecetaService recetaService) {
        this.recetaService = recetaService;
    }

    @GetMapping("/prescription")
    public ResponseEntity<?> verifyReceta(@RequestParam("token") UUID token) {
        try {
            Receta receta = recetaService.getRecetaByToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("valida", true);
            response.put("numero", receta.getNumero());
            response.put("paciente", receta.getPatient().getFirstName() + " " + receta.getPatient().getLastName());
            response.put("medico", receta.getProfessional().getFullName());
            response.put("fechaEmision", receta.getFechaEmision());
            response.put("fechaExpiracion", receta.getFechaExpiracion());
            response.put("estado", receta.getEstado());
            response.put("observaciones", receta.getObservaciones());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valida", false, "error", e.getMessage()));
        }
    }
}