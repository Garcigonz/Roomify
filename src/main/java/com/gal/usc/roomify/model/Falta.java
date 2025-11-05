package com.gal.usc.roomify.model;

import java.time.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "faltas")
public record Falta(
        @Id String id,
        String descripcion,
        LocalDate fecha,
        Severidad severidad,
        LocalDate fechaCaducidad,
        java.util.Optional<@org.jspecify.annotations.NonNull Usuario> castigado
) {}
