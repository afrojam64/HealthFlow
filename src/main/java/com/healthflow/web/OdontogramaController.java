package com.healthflow.web;

import com.healthflow.api.dto.odontograma.OdontogramaHallazgoDTO;
import com.healthflow.domain.Patient;
import com.healthflow.repo.PatientRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.OdontogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctor/odontograma")
public class OdontogramaController {

    @Autowired
    private OdontogramaService odontogramaService;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/cita/{citaId}")
    public List<OdontogramaHallazgoDTO> getHallazgosPorCita(@PathVariable("citaId") UUID citaId) {
        return odontogramaService.getHallazgosPorCita(citaId);
    }

    @PostMapping("/cita/{citaId}")
    public ResponseEntity<?> saveHallazgos(@PathVariable("citaId") UUID citaId,
                                           @RequestBody List<OdontogramaHallazgoDTO> hallazgos) {
        odontogramaService.guardarHallazgos(citaId, hallazgos);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/paciente/{pacienteId}/inicial")
    public List<OdontogramaHallazgoDTO> getOdontogramaInicial(@PathVariable("pacienteId") UUID pacienteId) {
        return odontogramaService.getInicialesPorPaciente(pacienteId);
    }

    @GetMapping("/paciente/{pacienteId}/etapa")
    public String getEtapaDental(@PathVariable("pacienteId") UUID pacienteId) {
        Patient patient = patientRepository.findById(pacienteId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        int edad = (int) ChronoUnit.YEARS.between(patient.getBirthDate(), LocalDate.now(ZoneId.of("America/Bogota")));

        if (edad < 6) {
            return "TEMPORAL";
        } else if (edad <= 12) {
            return "MIXTA";
        } else {
            return "PERMANENTE";
        }
    }
}