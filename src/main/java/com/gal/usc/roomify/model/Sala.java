package com.gal.usc.roomify.model;

import java.time.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "salas")
public class Sala {

    @Id
    private int id;
    private String descripcion;
    private int aforo;
    private Usuario responsableActual; // residente que tiene la sala reservada

    public Sala() {
    }

    public Sala(int id, String descripcion, int aforo, Usuario responsableActual) {
        this.id = id;
        this.descripcion = descripcion;
        this.aforo = aforo;
        this.responsableActual = responsableActual;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getAforo() {
        return aforo;
    }

    public void setAforo(int aforo) {
        this.aforo = aforo;
    }

    public Usuario getResponsableActual() {
        return responsableActual;
    }

    public void setResponsableActual(Usuario responsableActual) {
        this.responsableActual = responsableActual;
    }

    @Override
    public String toString() {
        return "Sala{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", aforo=" + aforo +
                ", responsableActual=" + responsableActual +
                '}';
    }
}
