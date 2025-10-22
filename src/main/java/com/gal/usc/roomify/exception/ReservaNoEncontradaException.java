package com.gal.usc.roomify.exception;

public class ReservaNoEncontradaException extends Exception {
    private final String id;

    public ReservaNoEncontradaException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
