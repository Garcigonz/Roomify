package com.gal.usc.roomify.model;

import java.time.*;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "faltas")
public record Falta(
        @Id String id,
        String descripcion,
        LocalDate fecha,
        Severidad severidad,
        LocalDate fechaCaducidad,
        Usuario castigado
) {}

