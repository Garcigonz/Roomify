package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.service.UsuarioService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestController
@RequestMapping("usuarios")
public class UsuarioController {
    UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    // POST: Crear un nuevo usuario
    @PostMapping()
    public ResponseEntity<@NonNull Usuario> addUsuario(@RequestBody Usuario usuario) {
        try {
            usuario = usuarioService.addUsuario(usuario);

            return ResponseEntity.created(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "getUsuario", usuario.id()).build().toUri())
                    .body(usuario);
        } catch(UsuarioDuplicadoException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "getUsuario", usuario.id()).build().toUri())
                    .build();
        }
    }

    // GET: Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Usuario> getUsuario(@PathVariable String id) {
        try {
            Usuario usuario = usuarioService.getUsuario(id);
            return ResponseEntity.ok(usuario);
        } catch(UsuarioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: Eliminar un usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch(UsuarioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
    //
}
