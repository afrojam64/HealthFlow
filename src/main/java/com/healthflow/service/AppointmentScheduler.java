package com.healthflow.service;

import com.healthflow.repo.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class AppointmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentScheduler.class);

    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;

    public AppointmentScheduler(AppointmentRepository appointmentRepository,
                                @Value("${healthflow.timezone:America/Bogota}") String timezone) {
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(timezone);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Bogota")
    @Transactional
    public void marcarCitasNoAtendidas() {
        OffsetDateTime hoyInicio = LocalDate.now(zoneId).atStartOfDay(zoneId).toOffsetDateTime();
        int actualizadas = appointmentRepository.updatePastAppointmentsToNotAttended(hoyInicio);
        if (actualizadas > 0) {
            log.info("Se marcaron {} citas como NO_ATENDIDA (fecha anterior a {}).", actualizadas, hoyInicio);
        }
    }
}