package com.gal.usc.roomify.filter;

import com.gal.usc.roomify.service.AuthenticationService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull; // O la anotación que uses
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final AuthenticationService authenticationService;

    @Autowired
    public JWTFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException { // Quitamos JwtException de aquí

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null || !token.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Intentamos parsear el token
            Authentication authentication = authenticationService.parseJWT(token.replaceFirst("^Bearer ", ""));

            // Si funciona, autenticamos al usuario
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            // Si el token está mal o expirado, NO lanzamos error.
            // Simplemente no autenticamos (limpiamos el contexto) y dejamos pasar.
            // Spring Security bloqueará la petición más adelante si la ruta es privada.
            SecurityContextHolder.clearContext();

            // System.out.println("Token inválido: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }
}