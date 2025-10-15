package com.gal.usc.roomify.model;
import java.time.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "faltas")
public record Falta(
    @Id String id,      // Identificador de la falta
    String descripcion, // Descripción de la falta
    LocalDate fecha,    // Fecha en la que se interpuso
    LocalDate fechaCaducidad, // Fecha en la que caduca la falta
    Usuario castigado // Usuario al que se le interpone una falta)
) {}
