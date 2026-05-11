package com.healthflow.web;

import com.healthflow.domain.Patient;
import com.healthflow.domain.Periodontograma;
import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.api.dto.PeriodontalIndicatorsDTO;
import com.healthflow.api.dto.PeriodontogramaRequest;
import com.healthflow.api.dto.PeriodontogramaResponse;
import com.healthflow.api.dto.SuggestedDiagnosisResponse;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.PeriodontogramaRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.PeriodontogramaService;
import com.healthflow.service.PermisoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/periodontograma")
public class PeriodontogramaController {

    private final PeriodontogramaService periodontogramaService;
    private final PeriodontogramaRepository periodontogramaRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final PermisoService permisoService;

    public PeriodontogramaController(PeriodontogramaService periodontogramaService,
                                     PeriodontogramaRepository periodontogramaRepository,
                                     PatientRepository patientRepository,
                                     ProfessionalRepository professionalRepository,
                                     UserRepository userRepository,
                                     PermisoService permisoService) {
        this.periodontogramaService = periodontogramaService;
        this.periodontogramaRepository = periodontogramaRepository;
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

    private Professional getCurrentProfessional() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            UUID medicoId = permisoService.getMedicoIdByAsistente(user.getId());
            if (medicoId == null) {
                throw new AccessDeniedException("No tienes un médico asociado");
            }
            return professionalRepository.findById(medicoId)
                    .orElseThrow(() -> new DomainException("Médico no encontrado"));
        } else {
            return professionalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DomainException("Profesional no encontrado"));
        }
    }

    private void checkPeriodontogramaPermission() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            if (!permisoService.tienePermiso(user.getId(), "VER_PACIENTES") &&
                    !permisoService.tienePermiso(user.getId(), "GESTIONAR_PERIODONTOGRAMA")) {
                throw new AccessDeniedException("No tienes permiso para acceder al periodontograma");
            }
        }
    }

    @GetMapping("/paciente/{pacienteId}/ultimo")
    public ResponseEntity<PeriodontogramaResponse> getUltimoPeriodontograma(@PathVariable("pacienteId") UUID pacienteId) {
        checkPeriodontogramaPermission();
        patientRepository.findById(pacienteId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        var periodontogramaOpt = periodontogramaService.getLastByPatient(pacienteId);
        if (periodontogramaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(periodontogramaOpt.get()));
    }

    @PostMapping("/calcular")
    public ResponseEntity<SuggestedDiagnosisResponse> calcularDiagnostico(@RequestBody String measurementsJson) {
        checkPeriodontogramaPermission();
        var indicators = periodontogramaService.calculateIndicators(measurementsJson);
        var suggested = periodontogramaService.suggestDiagnosis(indicators);

        SuggestedDiagnosisResponse response = new SuggestedDiagnosisResponse();
        response.setDiagnosisBase(suggested.getDiagnosisBase());
        response.setSubcategory(suggested.getSubcategory());
        response.setStage(suggested.getStage());
        response.setGrade(suggested.getGrade());
        response.setExtent(suggested.getExtent());
        response.setStability(suggested.getStability());
        response.setFullText(suggested.getFullText());

        PeriodontalIndicatorsDTO indicatorsDTO = new PeriodontalIndicatorsDTO();
        indicatorsDTO.setBopPercent(indicators.getBopPercent());
        indicatorsDTO.setMaxCAL(indicators.getMaxCAL());
        indicatorsDTO.setAffectedTeethCAL(indicators.getAffectedTeethCAL());
        indicatorsDTO.setMaxMobility(indicators.getMaxMobility());
        indicatorsDTO.setMaxFurcation(indicators.getMaxFurcation());
        indicatorsDTO.setLostTeeth(indicators.getLostTeeth());
        indicatorsDTO.setTotalTeeth(indicators.getTotalTeeth());
        response.setIndicators(indicatorsDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/guardar")
    public ResponseEntity<PeriodontogramaResponse> guardarPeriodontograma(@RequestBody PeriodontogramaRequest request) {
        checkPeriodontogramaPermission();
        Professional professional = getCurrentProfessional();
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        Periodontograma entity = new Periodontograma();
        entity.setPatient(patient);
        entity.setProfessional(professional);
        entity.setExamDate(request.getExamDate() != null ? request.getExamDate() : java.time.LocalDate.now());
        entity.setObservations(request.getObservations());
        entity.setMeasurementsJson(request.getMeasurementsJson());
        if (request.getFinalDiagnosisText() != null) {
            entity.setFinalDiagnosisText(request.getFinalDiagnosisText());
        }

        Periodontograma saved = periodontogramaService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/paciente/{pacienteId}/todos")
    public ResponseEntity<List<PeriodontogramaResponse>> listarPeriodontogramas(@PathVariable("pacienteId") UUID pacienteId) {
        checkPeriodontogramaPermission();
        patientRepository.findById(pacienteId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));
        List<Periodontograma> list = periodontogramaRepository.findByPatientIdOrderByExamDateDesc(pacienteId);
        return ResponseEntity.ok(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    private PeriodontogramaResponse toResponse(Periodontograma p) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new PeriodontogramaResponse(
                p.getId(),
                p.getPatient().getId(),
                p.getAppointment() != null ? p.getAppointment().getId() : null,
                p.getProfessional().getId(),
                p.getExamDate(),
                p.getObservations(),
                p.getMeasurementsJson(),
                p.getDiagnosisBase(),
                p.getSubcategory(),
                p.getStage(),
                p.getGrade(),
                p.getExtent(),
                p.getStability(),
                p.getFinalDiagnosisText(),
                p.getCreatedAt() != null ? p.getCreatedAt().format(fmt) : null,
                p.getUpdatedAt() != null ? p.getUpdatedAt().format(fmt) : null
        );
    }
}