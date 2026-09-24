package com.ejemplo.biblioteca.controller;

import jakarta.validation.constraints.NotNull;

public record AvailabilityRequest(
    @NotNull(message = "La disponibilidad es obligatoria")
    Boolean available
) {
}
