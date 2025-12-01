package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.service.AuthenticationService;
import com.gal.usc.roomify.service.UsuarioService; // <--- Necesitarás crear este servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UsuarioService usuarioService) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody Usuario usuario) {
        // 1. Autenticamos usando el servicio que ya configuramos
        Authentication auth = authenticationService.login(usuario);

        // 2. Generamos el token JWT
        String token = authenticationService.generateJWT(auth);

        // 3. Devolvemos una respuesta vacía (204 No Content) pero con el token en la cabecera
        return ResponseEntity.noContent()
                .headers(headers -> headers.setBearerAuth(token))
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