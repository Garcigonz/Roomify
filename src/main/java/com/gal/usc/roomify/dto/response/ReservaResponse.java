package com.gal.usc.roomify.dto.response;

import java.time.LocalDateTime;

public record ReservaResponse(
        String id,
        String nombreSala,
        LocalDateTime horaInicio,
        LocalDateTime horaFin,
        String nombreUsuario,
        String observaciones
) {}