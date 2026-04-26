package com.healthflow.service;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.RipsGenerationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class RipsReminderService {

    private static final Logger log = LoggerFactory.getLogger(RipsReminderService.class);

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private RipsGenerationRepository ripsGenerationRepository;

    @Value("${healthflow.timezone:America/Bogota}")
    private String zoneIdStr;

    @Value("${rips.reminder.enabled:true}")
    private boolean reminderEnabled;

    private ZoneId getZoneId() {
        return ZoneId.of(zoneIdStr);
    }

    @Transactional(readOnly = true)
    public void enviarRecordatoriosRipsPendientes() {
        if (!reminderEnabled) {
            log.debug("Recordatorios RIPS desactivados");
            return;
        }

        List<Professional> profesionales = professionalRepository.findAll();
        LocalDate hoy = LocalDate.now(getZoneId());
        LocalDate fechaDesde = hoy.minusMonths(1).withDayOfMonth(1);
        LocalDate fechaHasta = fechaDesde.withDayOfMonth(fechaDesde.lengthOfMonth());

        for (Professional prof : profesionales) {
            if (prof.getTipoFacturacion() == Professional.TipoFacturacion.SOLO_HC) {
                continue;
            }

            long generaciones = ripsGenerationRepository.countByProfessionalIdAndFechaDesdeAndFechaHasta(
                    prof.getId(), fechaDesde, fechaHasta);

            if (generaciones == 0) {
                // Simular envío de correo mediante log
                String email = prof.getUser() != null ? prof.getUser().getEmail() : "sin-email@healthflow.local";
                log.info("🔔 RECORDATORIO RIPS (SIMULADO) -> Para: {} ({})", prof.getFullName(), email);
                log.info("   Mensaje: Aún no has generado el reporte RIPS del período {} - {}.", fechaDesde, fechaHasta);
                log.info("   Enlace: http://localhost:8080/doctor/reportes/rips");
            }
        }
    }
}