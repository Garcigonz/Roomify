package com.gal.usc.roomify.exception;

public class FaltaNoEncontradaException extends Exception {
    private final int id;

    public FaltaNoEncontradaException(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
