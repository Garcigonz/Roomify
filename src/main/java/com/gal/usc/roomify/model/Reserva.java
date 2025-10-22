package com.gal.usc.roomify.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reservas")
public class Reserva {
    @Id
    private String id;
    private Sala sala;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private Usuario usuario;
    private String observaciones;
    private String estado;

    // Constructor para crear nuevas reservas
    public Reserva(Sala sala, LocalDateTime horaInicio, Usuario usuario, String observaciones) {
        this.sala = sala;
        this.horaInicio = horaInicio;
        this.horaFin = horaInicio.plusHours(3); // Calcula automáticamente 3 horas después
        this.usuario = usuario;
        this.observaciones = observaciones;
    }

    public String getId() {
        return id;
    }

    public Sala getSala() {
        return sala;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getObservaciones() {
        return observaciones;
    }

}