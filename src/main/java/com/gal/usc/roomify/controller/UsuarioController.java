package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.service.UsuarioService;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestController
@RequestMapping("Usuarios")
public class UsuarioController {
    UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("{id}")
    public ResponseEntity<@Usuario> addUsuario(@RequestBody Usuario usuario) {
        try {
            usuario = usuarioService.addUsuario(usuario);

            return ResponseEntity.created(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "getUsuario", usuario.id()).build().toUri())
                    .body(usuario);
        } catch(UsuarioDuplicadoException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "getBook", usuario.id()).build().toUri())
                    .build();
        }
    }
}
