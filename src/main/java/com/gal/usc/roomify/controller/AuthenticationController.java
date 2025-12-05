package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.model.*;
import com.gal.usc.roomify.service.AuthenticationService;
import com.gal.usc.roomify.service.UsuarioService; // <--- Necesitarás crear este servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
// import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder; // Descomenta esto cuando tengas UsuarioController

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UsuarioService usuarioService) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {

        Authentication auth = authenticationService.login(
                request.id(),
                request.password()
        );

        String token = authenticationService.generateJWT(auth);

        return ResponseEntity.noContent()
                .headers(h -> h.setBearerAuth(token))
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario) {
        // 1. Creamos el usuario en base de datos (Mongo)
        try {
            Usuario createdUser = usuarioService.addUsuario(usuario);
            return ResponseEntity.created(null).body(createdUser);
        } catch (UsuarioDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(
                                    UsuarioController.class,
                                    "getUsuario",
                                    usuario.getId())
                            .build().toUri())
                    .build();
        }
    }
}