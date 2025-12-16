package com.gal.usc.roomify.model;

import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.mapping.FieldType;import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reservas")
public record Reserva(
        @MongoId(FieldType.OBJECT_ID) String id,
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