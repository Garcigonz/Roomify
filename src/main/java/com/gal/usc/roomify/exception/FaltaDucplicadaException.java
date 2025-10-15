package com.gal.usc.roomify.exception;

import com.gal.usc.roomify.model.Falta;


public class FaltaDucplicadaException extends Throwable {
    public Falta falta;

    public FaltaDucplicadaException(Falta falta) {
        this.falta = falta;
    }

    public Falta getFalta() { return falta; }

}
