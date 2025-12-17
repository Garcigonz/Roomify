package com.gal.usc.roomify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class SalaOcupadaException extends RuntimeException {

    public SalaOcupadaException(String message) {
        super(message);
    }

    public SalaOcupadaException() {
        super("La sala no está disponible en el horario seleccionado.");
    }
}