package com.healthflow.web;

import com.healthflow.api.dto.OrdenExamenRequestDTO;
import com.healthflow.api.dto.OrdenExamenResponseDTO;
import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.OrdenExamenService;
import com.healthflow.service.PermisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/doctor/citas")
public class OrdenExamenController {

    private final OrdenExamenService ordenExamenService;
    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final PermisoService permisoService;

    public OrdenExamenController(OrdenExamenService ordenExamenService,
                                 UserRepository userRepository,
                                 ProfessionalRepository professionalRepository,
                                 PermisoService permisoService) {
        this.ordenExamenService = ordenExamenService;
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.permisoService = permisoService;
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

    @PostMapping("/{citaId}/orden-examen")
    public ResponseEntity<OrdenExamenResponseDTO> generarOrdenExamen(
            @PathVariable("citaId") UUID citaId,
            @RequestBody OrdenExamenRequestDTO request) {
        UUID profesionalId = getCurrentProfessionalId();
        OrdenExamenResponseDTO response = ordenExamenService.crearOrden(citaId, request, profesionalId);
        return ResponseEntity.ok(response);
    }
}