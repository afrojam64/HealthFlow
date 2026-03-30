package com.healthflow.service;

import com.healthflow.domain.Appointment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final String publicBaseUrl;
    private final String fromEmail;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl,
                               @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.publicBaseUrl = publicBaseUrl;
        this.fromEmail = fromEmail;
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
            System.out.println("✅ CORREO ENVIADO a: " + to + " - Asunto: " + subject);
        } catch (MessagingException e) {
            System.err.println("❌ ERROR al enviar correo a " + to + ": " + e.getMessage());
        }
    }

    public void sendBookingEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String confirmUrl = publicBaseUrl + "/public/booking/confirm?token=" + token;
        String cancelUrl = publicBaseUrl + "/public/booking/cancel?token=" + token;
        String statusUrl = publicBaseUrl + "/public/booking/status?token=" + token;

        String subject = "Tu cita en HealthFlow";
        String body = "Tu cita fue creada. Confirma o cancela:\n\n" +
                "Confirmar: " + confirmUrl + "\n" +
                "Cancelar:  " + cancelUrl + "\n" +
                "Estado:    " + statusUrl + "\n\n" +
                "Saludos,\nHealthFlow";

        sendEmail(email, subject, body);
    }

    public void sendStatusEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String statusUrl = publicBaseUrl + "/public/booking/status?token=" + token;

        String subject = "Estado de tu cita - HealthFlow";
        String body = "Estado actualizado: " + appt.getStatus() + "\n\n" +
                "Ver estado: " + statusUrl + "\n\n" +
                "Saludos,\nHealthFlow";

        sendEmail(email, subject, body);
    }

    public void sendReminderEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String statusUrl = publicBaseUrl + "/public/booking/status?token=" + token;
        String confirmUrl = publicBaseUrl + "/public/booking/confirm?token=" + token;
        String cancelUrl = publicBaseUrl + "/public/booking/cancel?token=" + token;

        String subject = "Recordatorio de cita - HealthFlow";
        String body = "Recordatorio: tienes una cita programada para: " + appt.getDateTime() + "\n\n" +
                "Ver estado: " + statusUrl + "\n" +
                "Confirmar:  " + confirmUrl + "\n" +
                "Cancelar:   " + cancelUrl + "\n\n" +
                "Saludos,\nHealthFlow";

        sendEmail(email, subject, body);
    }

    public void sendRescheduleEmail(String email, Appointment appt) {
        String token = appt.getAccessToken().toString();
        String statusUrl = publicBaseUrl + "/public/booking/status?token=" + token;
        String confirmUrl = publicBaseUrl + "/public/booking/confirm?token=" + token;
        String cancelUrl = publicBaseUrl + "/public/booking/cancel?token=" + token;

        String subject = "Cita reprogramada - HealthFlow";
        String body = "Tu cita fue reprogramada para: " + appt.getDateTime() + "\n\n" +
                "Ver estado: " + statusUrl + "\n" +
                "Confirmar:  " + confirmUrl + "\n" +
                "Cancelar:   " + cancelUrl + "\n\n" +
                "Saludos,\nHealthFlow";

        sendEmail(email, subject, body);
    }

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

        sendEmail(email, subject, body);
    }
}