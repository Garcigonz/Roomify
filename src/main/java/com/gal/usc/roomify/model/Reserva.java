package com.gal.usc.roomify.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reservas")
public record Reserva(
        @Id String id,
        Sala sala,
        LocalDateTime horaInicio,
        LocalDateTime horaFin,
        Usuario usuario,
        String observaciones
) {
    // Constructor compacto - valida y ajusta horaFin automáticamente
    public Reserva {
        // Si horaFin es null, la calculamos automáticamente
        if (horaFin == null && horaInicio != null) {
            horaFin = horaInicio.plusHours(3);
        }
    }
    
}