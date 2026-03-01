package com.healthflow.api.dto;

import com.healthflow.domain.AppointmentStatus;

import java.util.UUID;

public record BookAppointmentResponse(
    UUID appointmentId,
    AppointmentStatus status,
    UUID accessToken
) {}
