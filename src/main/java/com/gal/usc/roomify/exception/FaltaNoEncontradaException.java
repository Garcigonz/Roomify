package com.gal.usc.roomify.exception;

public class FaltaNoEncontradaException extends Exception {
    private final String id;

    public FaltaNoEncontradaException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
