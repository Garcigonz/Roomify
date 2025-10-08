package com.gal.usc.roomify.model;
import java.time.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "salas")
public record Sala  (
    @Id int id,
    String descripcion,
    int aforo,
    Usuario responsableActual // residente que tiene la sala reservada
) {}
