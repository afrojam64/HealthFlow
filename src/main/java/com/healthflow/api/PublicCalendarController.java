package com.healthflow.api;

import com.healthflow.domain.Appointment;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.service.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/appointments")
public class PublicCalendarController {

    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;
    private final String publicBaseUrl;

    public PublicCalendarController(AppointmentRepository appointmentRepository,
                                    @Value("${healthflow.timezone:America/Bogota}") String tz,
                                    @Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl) {
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(tz);
        this.publicBaseUrl = publicBaseUrl;
    }

    @GetMapping(value = "/{token}/calendar.ics", produces = "text/calendar")
    public ResponseEntity<byte[]> calendar(@PathVariable("token") UUID token) {
        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        // Fecha/hora en zona local
        ZonedDateTime start = appt.getDateTime().atZoneSameInstant(zoneId);
        ZonedDateTime end = start.plusMinutes(30); // MVP: duración fija 30 (luego lo hacemos configurable)

        // Formato iCalendar: YYYYMMDD'T'HHMMSS
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String dtStart = start.format(fmt);
        String dtEnd = end.format(fmt);

        String confirmUrl = publicBaseUrl + "/api/public/appointments/" + token + "/confirm";
        String cancelUrl  = publicBaseUrl + "/api/public/appointments/" + token + "/cancel";
        String statusUrl  = publicBaseUrl + "/api/public/appointments/" + token;

        String ics =
                "BEGIN:VCALENDAR\r\n" +
                        "VERSION:2.0\r\n" +
                        "PRODID:-//HealthFlow//ES\r\n" +
                        "CALSCALE:GREGORIAN\r\n" +
                        "METHOD:PUBLISH\r\n" +
                        "BEGIN:VEVENT\r\n" +
                        "UID:" + appt.getId() + "@healthflow\r\n" +
                        "DTSTAMP:" + ZonedDateTime.now(zoneId).format(fmt) + "\r\n" +
                        "DTSTART;TZID=" + zoneId + ":" + dtStart + "\r\n" +
                        "DTEND;TZID=" + zoneId + ":" + dtEnd + "\r\n" +
                        "SUMMARY:Cita médica (HealthFlow)\r\n" +
                        "DESCRIPTION:Gestiona tu cita\\n" +
                        "Ver estado: " + statusUrl + "\\n" +
                        "Confirmar: " + confirmUrl + "\\n" +
                        "Cancelar: " + cancelUrl + "\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR\r\n";

        byte[] bytes = ics.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cita.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .body(bytes);
    }
}