package com.healthflow.service;

import com.healthflow.domain.Patient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationHours;

    public JwtService(@Value("${healthflow.jwt.secret:miClaveSecretaSuperSegura1234567890}") String secret,
                      @Value("${healthflow.jwt.expiration-hours:24}") long expirationHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    public String generateToken(Patient patient) {
        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("Paciente o ID nulo");
        }
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationHours, ChronoUnit.HOURS);
        String token = Jwts.builder()
                .subject(patient.getId().toString())
                .claim("name", patient.getFirstName() + " " + patient.getLastName())
                .claim("email", patient.getEmail())
                .claim("role", "PACIENTE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
        System.out.println("Token generado: " + token); // Para depuración
        return token;
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getPatientIdFromToken(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }
}