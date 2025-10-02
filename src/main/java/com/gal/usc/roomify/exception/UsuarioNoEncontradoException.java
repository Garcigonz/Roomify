package com.gal.usc.roomify.exception;

public class UsuarioNoEncontradoException extends Exception {
    private final String id;

    public UsuarioNoEncontradoException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
