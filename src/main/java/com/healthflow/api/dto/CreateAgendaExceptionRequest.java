package com.healthflow.api.dto;

import com.healthflow.domain.ExceptionType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAgendaExceptionRequest(
    @NotNull LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    @NotNull ExceptionType type
) {}
