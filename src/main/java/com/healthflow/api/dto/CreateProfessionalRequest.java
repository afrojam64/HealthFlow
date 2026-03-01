package com.healthflow.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProfessionalRequest(
    @NotBlank String fullName,
    @NotBlank String medicalRegistry,
    @NotBlank String specialty
) {}
