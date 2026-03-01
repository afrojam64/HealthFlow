package com.healthflow.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CreateAvailabilityRequest(
    @Min(0) @Max(6) int dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime
) {}
