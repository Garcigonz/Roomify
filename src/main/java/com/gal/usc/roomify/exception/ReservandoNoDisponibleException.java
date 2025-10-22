package com.gal.usc.roomify.exception;
import com.gal.usc.roomify.model.Reserva;

public class ReservandoNoDisponibleException extends Exception {
    private final Reserva reserva;

    public ReservandoNoDisponibleException(Reserva reserva) {
        this.reserva = reserva;
    }

    public Reserva getReserva() {
        return reserva;
    }
}