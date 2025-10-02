package com.gal.usc.roomify.exception;

import com.gal.usc.roomify.model.Usuario;

public class UsuarioDuplicadoException extends Throwable{
    public final Usuario usuario;

    public UsuarioDuplicadoException(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
