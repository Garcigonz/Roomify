package com.gal.usc.roomify.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearSalaRequest(
        @NotNull(message = "El ID de la sala es obligatorio")
        Integer id,

        @NotBlank(message = "La descripción no puede estar vacía")
        String descripcion,

        @Min(value = 1, message = "El aforo debe ser al menos 1 persona")
        int aforo
) {}