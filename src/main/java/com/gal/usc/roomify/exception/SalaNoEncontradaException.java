package com.gal.usc.roomify.exception;

public class SalaNoEncontradaException extends Exception {
    private final int id;

    public SalaNoEncontradaException(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
