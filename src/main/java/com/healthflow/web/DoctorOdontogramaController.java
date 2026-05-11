package com.healthflow.web;

import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.PermisoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/doctor")
public class DoctorOdontogramaController {

    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final PermisoService permisoService;

    public DoctorOdontogramaController(PatientRepository patientRepository,
                                       ProfessionalRepository professionalRepository,
                                       UserRepository userRepository,
                                       PermisoService permisoService) {
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.permisoService = permisoService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    private void checkPeriodontogramaAccess() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            if (!permisoService.tienePermiso(user.getId(), "VER_PACIENTES") &&
                    !permisoService.tienePermiso(user.getId(), "GESTIONAR_PERIODONTOGRAMA")) {
                throw new AccessDeniedException("No tienes permiso para ver el periodontograma");
            }
        }
    }

    @GetMapping("/odontograma")
    public String periodontograma(@RequestParam("pacienteId") UUID pacienteId,
                                  @RequestParam(value = "citaId", required = false) UUID citaId,
                                  Model model)  {
        checkPeriodontogramaAccess();
        Patient patient = patientRepository.findById(pacienteId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));
        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("pacienteNombre", patient.getFirstName() + " " + patient.getLastName());
        model.addAttribute("contenido", "doctor/periodontograma");
        model.addAttribute("title", "Periodontograma - " + patient.getFirstName() + " " + patient.getLastName());
        if (citaId != null) model.addAttribute("citaId", citaId);
        return "fragments/layout";
    }
}