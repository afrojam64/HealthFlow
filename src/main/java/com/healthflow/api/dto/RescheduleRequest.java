package com.healthflow.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record RescheduleRequest(
        @NotNull OffsetDateTime newDateTime
) {}