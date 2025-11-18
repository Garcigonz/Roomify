package com.gal.usc.roomify.exception;

import com.gal.usc.roomify.model.Usuario;

import static java.lang.System.*;

public class UsuarioCastigadoException extends RuntimeException {

    public UsuarioCastigadoException(Usuario usuario) {
        System.err.println("El usuario " + usuario.id() + " No puede realizar la reserva porque esta castigado sin sala\n");
    }
}
