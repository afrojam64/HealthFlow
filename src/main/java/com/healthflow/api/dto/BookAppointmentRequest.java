package com.healthflow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record BookAppointmentRequest(
    // Datos paciente (mínimos según HU03)
    @NotBlank String docType,
    @NotBlank String docNumber,
    @NotBlank String firstName,
    String middleName,
    @NotBlank String lastName,
    String secondLastName,
    @NotNull LocalDate birthDate,
    @NotBlank String sex,
    @NotBlank String municipalityCode,
    @Email @NotBlank String email,
    @NotBlank String phone,

    // Datos cita
    @NotNull OffsetDateTime dateTime
) {}
