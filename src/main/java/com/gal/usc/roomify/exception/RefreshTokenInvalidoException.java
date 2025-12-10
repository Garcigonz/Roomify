package com.gal.usc.roomify.exception;

import org.springframework.security.core.AuthenticationException;

public class RefreshTokenInvalidoException extends AuthenticationException  {
    private final String token;

    public RefreshTokenInvalidoException(String token) {
        super("Invalid refresh token ");
        this.token = token;
    }

    public String getToken() {
        return token;
    }

}
