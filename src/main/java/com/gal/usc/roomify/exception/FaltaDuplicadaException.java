package com.gal.usc.roomify.exception;

import com.gal.usc.roomify.model.Falta;


public class FaltaDuplicadaException extends Throwable {
    public Falta falta;

    public FaltaDuplicadaException(Falta falta) {
        this.falta = falta;
    }

    public Falta getFalta() { return falta; }

}
