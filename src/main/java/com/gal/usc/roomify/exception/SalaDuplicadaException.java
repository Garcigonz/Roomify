package com.gal.usc.roomify.exception;

import com.gal.usc.roomify.model.Sala;

public class SalaDuplicadaException extends Throwable{
    public final Sala sala;

    public SalaDuplicadaException(Sala sala) {
        this.sala = sala;
    }

    public Sala getSala() {
        return sala;
    }
}
