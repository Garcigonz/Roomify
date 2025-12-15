package com.gal.usc.roomify.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CrearReservaRequest(
        @NotNull(message = "Debes especificar la sala")
        Integer salaId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @FutureOrPresent(message = "La reserva no puede ser en el pasado")
        LocalDateTime horaInicio,

        // horaFin es opcional, si es null pondrá +3 horas
        LocalDateTime horaFin,

        String observaciones
) {}