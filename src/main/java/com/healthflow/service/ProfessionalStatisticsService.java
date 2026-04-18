package com.healthflow.service;

import com.healthflow.domain.Professional;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.ProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ProfessionalStatisticsService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    private final ZoneId zoneId = ZoneId.of("America/Bogota");

    private OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(zoneId).toOffsetDateTime();
    }

    private OffsetDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
    }

    public Map<String, List<Object[]>> getGlobalProfessionalStats(LocalDate desde, LocalDate hasta) {
        OffsetDateTime start = (desde != null) ? startOfDay(desde) : startOfDay(LocalDate.of(2000, 1, 1));
        OffsetDateTime end = (hasta != null) ? endOfDay(hasta) : OffsetDateTime.now(zoneId);

        List<Object[]> pacientesPorProf = appointmentRepository.countDistinctPatientsPerProfessionalNative(start, end);
        List<Object[]> citasPorProf = appointmentRepository.countAppointmentsPerProfessionalNative(start, end);

        Map<String, List<Object[]>> result = new HashMap<>();
        result.put("pacientesPorProfesional", pacientesPorProf);
        result.put("citasPorProfesional", citasPorProf);
        return result;
    }

    public ProfessionalStatsDTO getProfessionalStats(String professionalSlug, LocalDate desde, LocalDate hasta) {
        Professional professional = professionalRepository.findBySlug(professionalSlug)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado: " + professionalSlug));

        UUID profId = professional.getId();
        OffsetDateTime start = (desde != null) ? startOfDay(desde) : startOfDay(LocalDate.of(2000, 1, 1));
        OffsetDateTime end = (hasta != null) ? endOfDay(hasta) : OffsetDateTime.now(zoneId);

        ProfessionalStatsDTO dto = new ProfessionalStatsDTO();
        dto.setProfessionalId(profId);
        dto.setProfessionalName(professional.getFullName());
        dto.setSpecialty(professional.getSpecialty());
        dto.setSlug(professionalSlug);

        long totalPacientes = appointmentRepository.countDistinctPatientsByProfessional(profId, start, end);
        long totalCitas = appointmentRepository.countByProfessionalIdAndDateTimeBetween(profId, start, end);
        dto.setTotalPacientes(totalPacientes);
        dto.setTotalCitas(totalCitas);

        // Pacientes por antigüedad (usando fechas reales)
        List<Object[]> firstAppointments = appointmentRepository.findFirstAppointmentDatePerPatient(profId, start, end);
        Map<String, Integer> antiguedadMap = new LinkedHashMap<>();
        antiguedadMap.put("<6 meses", 0);
        antiguedadMap.put("6-12 meses", 0);
        antiguedadMap.put("1-2 años", 0);
        antiguedadMap.put(">2 años", 0);

        OffsetDateTime now = OffsetDateTime.now(zoneId);
        for (Object[] row : firstAppointments) {
            OffsetDateTime firstDate = (OffsetDateTime) row[1];
            long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(firstDate, now);
            if (monthsBetween < 6) antiguedadMap.put("<6 meses", antiguedadMap.get("<6 meses") + 1);
            else if (monthsBetween < 12) antiguedadMap.put("6-12 meses", antiguedadMap.get("6-12 meses") + 1);
            else if (monthsBetween < 24) antiguedadMap.put("1-2 años", antiguedadMap.get("1-2 años") + 1);
            else antiguedadMap.put(">2 años", antiguedadMap.get(">2 años") + 1);
        }
        dto.setPacientesPorAntiguedad(antiguedadMap);

        // Citas por día (solo si rango ≤ 31 días)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                (desde != null ? desde : start.toLocalDate()),
                (hasta != null ? hasta : end.toLocalDate()));
        if (daysBetween <= 31) {
            List<Object[]> citasPorDiaRaw = appointmentRepository.countAppointmentsByProfessionalGroupByDayNative(profId, start, end);
            List<Map<String, Object>> citasPorDiaList = new ArrayList<>();
            for (Object[] row : citasPorDiaRaw) {
                Map<String, Object> item = new HashMap<>();
                item.put("fecha", row[0].toString());
                item.put("conteo", ((Number) row[1]).longValue());
                citasPorDiaList.add(item);
            }
            dto.setCitasPorDia(citasPorDiaList);
        } else {
            dto.setCitasPorDia(new ArrayList<>()); // vacío
        }

        // Citas por mes (usando nativo)
        List<Object[]> citasPorMesRaw = appointmentRepository.countAppointmentsByProfessionalGroupByMonthNative(profId, start, end);
        List<Map<String, Object>> citasPorMesList = new ArrayList<>();
        for (Object[] row : citasPorMesRaw) {
            Map<String, Object> item = new HashMap<>();
            int mes = ((Number) row[0]).intValue();
            int anio = ((Number) row[1]).intValue();
            long conteo = ((Number) row[2]).longValue();
            String label = String.format("%d-%02d", anio, mes);
            item.put("mes", label);
            item.put("conteo", conteo);
            citasPorMesList.add(item);
        }
        dto.setCitasPorMes(citasPorMesList);

        // Citas por año (usando nativo)
        List<Object[]> citasPorAnioRaw = appointmentRepository.countAppointmentsByProfessionalGroupByYearNative(profId, start, end);
        List<Map<String, Object>> citasPorAnioList = new ArrayList<>();
        for (Object[] row : citasPorAnioRaw) {
            Map<String, Object> item = new HashMap<>();
            int anio = ((Number) row[0]).intValue();
            long conteo = ((Number) row[1]).longValue();
            item.put("anio", anio);
            item.put("conteo", conteo);
            citasPorAnioList.add(item);
        }
        dto.setCitasPorAnio(citasPorAnioList);

        return dto;
    }

    // DTO (mismo que tenías)
    public static class ProfessionalStatsDTO {
        private UUID professionalId;
        private String professionalName;
        private String specialty;
        private String slug;
        private long totalPacientes;
        private long totalCitas;
        private Map<String, Integer> pacientesPorAntiguedad;
        private List<Map<String, Object>> citasPorDia;
        private List<Map<String, Object>> citasPorMes;
        private List<Map<String, Object>> citasPorAnio;

        // getters y setters (todos)
        public UUID getProfessionalId() { return professionalId; }
        public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
        public String getProfessionalName() { return professionalName; }
        public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }
        public String getSpecialty() { return specialty; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public long getTotalPacientes() { return totalPacientes; }
        public void setTotalPacientes(long totalPacientes) { this.totalPacientes = totalPacientes; }
        public long getTotalCitas() { return totalCitas; }
        public void setTotalCitas(long totalCitas) { this.totalCitas = totalCitas; }
        public Map<String, Integer> getPacientesPorAntiguedad() { return pacientesPorAntiguedad; }
        public void setPacientesPorAntiguedad(Map<String, Integer> pacientesPorAntiguedad) { this.pacientesPorAntiguedad = pacientesPorAntiguedad; }
        public List<Map<String, Object>> getCitasPorDia() { return citasPorDia; }
        public void setCitasPorDia(List<Map<String, Object>> citasPorDia) { this.citasPorDia = citasPorDia; }
        public List<Map<String, Object>> getCitasPorMes() { return citasPorMes; }
        public void setCitasPorMes(List<Map<String, Object>> citasPorMes) { this.citasPorMes = citasPorMes; }
        public List<Map<String, Object>> getCitasPorAnio() { return citasPorAnio; }
        public void setCitasPorAnio(List<Map<String, Object>> citasPorAnio) { this.citasPorAnio = citasPorAnio; }
    }
}