package com.healthflow.web;

import com.healthflow.domain.Receta;
import com.healthflow.domain.RecetaMedicamento;
import com.healthflow.repo.RecetaMedicamentoRepository;
import com.healthflow.repo.RecetaRepository;
import com.healthflow.service.DomainException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/public/verify")
public class RecetaVerificacionController {

    private final RecetaRepository recetaRepository;
    private final RecetaMedicamentoRepository recetaMedicamentoRepository;

    public RecetaVerificacionController(RecetaRepository recetaRepository,
                                        RecetaMedicamentoRepository recetaMedicamentoRepository) {
        this.recetaRepository = recetaRepository;
        this.recetaMedicamentoRepository = recetaMedicamentoRepository;
    }

    @GetMapping("/prescription")
    public String verifyReceta(@RequestParam("token") UUID token, Model model) {
        try {
            Receta receta = recetaRepository.findByToken(token)
                    .orElseThrow(() -> new DomainException("Receta no encontrada"));

            if (!"ACTIVA".equals(receta.getEstado())) {
                model.addAttribute("valid", false);
                model.addAttribute("message", "Esta receta no está activa (estado: " + receta.getEstado() + ")");
                return "public/verify-prescription";
            }

            List<RecetaMedicamento> medicamentos = recetaMedicamentoRepository.findByRecetaId(receta.getId());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            model.addAttribute("valid", true);
            model.addAttribute("numero", receta.getNumero());
            model.addAttribute("pacienteNombre", receta.getPatient().getFirstName() + " " + receta.getPatient().getLastName());
            model.addAttribute("pacienteDoc", receta.getPatient().getDocNumber());
            model.addAttribute("medicoNombre", receta.getProfessional().getFullName());
            model.addAttribute("especialidad", receta.getProfessional().getSpecialty());
            model.addAttribute("fechaEmision", receta.getFechaEmision().format(formatter));
            model.addAttribute("fechaExpiracion", receta.getFechaExpiracion().format(formatter));
            model.addAttribute("estado", receta.getEstado());
            model.addAttribute("observaciones", receta.getObservaciones());
            model.addAttribute("medicamentos", medicamentos);

        } catch (Exception e) {
            model.addAttribute("valid", false);
            model.addAttribute("message", e.getMessage());
        }
        return "public/verify-prescription";
    }
}