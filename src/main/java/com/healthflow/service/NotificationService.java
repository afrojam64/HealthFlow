package com.healthflow.service;

import com.healthflow.domain.Appointment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private final String publicBaseUrl;

    public NotificationService(@Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public void sendBookingEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        // URLs amigables (vistas web)
        String confirmUrl = publicBaseUrl + "/public/booking/confirm?token=" + token;
        String cancelUrl  = publicBaseUrl + "/public/booking/cancel?token=" + token;
        String statusUrl  = publicBaseUrl + "/public/booking/status?token=" + token;

        System.out.println("=== EMAIL (MOCK) ===");
        System.out.println("Para: " + email);
        System.out.println("Tu cita fue creada. Confirma o cancela:");
        System.out.println("Confirmar: " + confirmUrl);
        System.out.println("Cancelar:  " + cancelUrl);
        System.out.println("Estado:    " + statusUrl);
        System.out.println("====================");
    }

    public void sendStatusEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String statusUrl = publicBaseUrl + "/public/booking/status?token=" + token;

        System.out.println("=== EMAIL (MOCK) ===");
        System.out.println("Para: " + email);
        System.out.println("Estado actualizado: " + appt.getStatus());
        System.out.println("Ver estado: " + statusUrl);
        System.out.println("====================");
    }

    public void sendReminderEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String statusUrl  = publicBaseUrl + "/public/booking/status?token=" + token;
        String confirmUrl = publicBaseUrl + "/public/booking/confirm?token=" + token;
        String cancelUrl  = publicBaseUrl + "/public/booking/cancel?token=" + token;

        System.out.println("=== EMAIL (MOCK) ===");
        System.out.println("Para: " + email);
        System.out.println("Recordatorio: tienes una cita programada para: " + appt.getDateTime());
        System.out.println("Ver estado: " + statusUrl);
        System.out.println("Confirmar:  " + confirmUrl);
        System.out.println("Cancelar:   " + cancelUrl);
        System.out.println("====================");
    }

    public void sendRescheduleEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String statusUrl  = publicBaseUrl + "/public/booking/status?token=" + token;
        String confirmUrl = publicBaseUrl + "/public/booking/confirm?token=" + token;
        String cancelUrl  = publicBaseUrl + "/public/booking/cancel?token=" + token;

        System.out.println("=== EMAIL (MOCK) ===");
        System.out.println("Para: " + email);
        System.out.println("Tu cita fue reprogramada para: " + appt.getDateTime());
        System.out.println("Ver estado: " + statusUrl);
        System.out.println("Confirmar:  " + confirmUrl);
        System.out.println("Cancelar:   " + cancelUrl);
        System.out.println("====================");
    }

    // Nuevo método para enviar enlace de documento
    public void sendDocumentLinkEmail(String email, String patientName, String fileName,
                                      String downloadUrl, LocalDate expirationDate) {
        String subject = "Documento disponible - HealthFlow";
        String body = String.format(
                "Hola %s,\n\nSe ha compartido un documento con usted: %s\n\n" +
                        "Puede descargarlo usando el siguiente enlace (válido hasta el %s):\n%s\n\n" +
                        "Si no solicitó este documento, ignore este mensaje.\n\nSaludos,\nHealthFlow",
                patientName, fileName,
                expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                downloadUrl
        );

        System.out.println("=== EMAIL (MOCK) ===");
        System.out.println("Para: " + email);
        System.out.println("Asunto: " + subject);
        System.out.println(body);
        System.out.println("====================");
    }
}