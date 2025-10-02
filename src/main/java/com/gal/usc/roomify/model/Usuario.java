package com.gal.usc.roomify.model;
import java.time.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public record Usuario  (
    @Id String id,
    String nombre,
    int habitacion,
    LocalDate nacimiento,
    int telefono
) {}
