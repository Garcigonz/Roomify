package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Role;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.RoleRepository;
import com.gal.usc.roomify.service.UsuarioService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RoleRepository roleRepository;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, RoleRepository roleRepository) {
        this.usuarioService = usuarioService;
        this.roleRepository = roleRepository;
    }

    // 🟢 GET: Obtener lista de usuarios (paginada, filtrada y ordenada)
    @GetMapping()
    public ResponseEntity<@NonNull Page<@NonNull Usuario>> getUsuarios(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "rol", required = false) String rol,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "nombre") List<String> sort
    ) {
        // Genera las reglas de ordenación (asc o desc)
        Sort sorting = Sort.by(sort.stream()
                .map(key -> key.startsWith("-")
                        ? Sort.Order.desc(key.substring(1))
                        : Sort.Order.asc(key))
                .toList());

        PageRequest pageable = PageRequest.of(page, size, sorting);

        // Si el servicio soporta filtros, se pasan aquí
        Page<Usuario> usuarios = usuarioService.getUsuarios(nombre, rol, pageable);

        return ResponseEntity.ok(usuarios);
    }

    // 🟢 GET: Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Usuario> getUsuario(@PathVariable String id) {
        try {
            Usuario usuario = usuarioService.getUsuario(id);
            return ResponseEntity.ok(usuario);
        } catch (UsuarioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 POST: Crear un nuevo usuario
    @PostMapping()
    public ResponseEntity<@NonNull Usuario> addUsuario(@RequestBody Usuario usuario) {
        try {
            usuario = usuarioService.addUsuario(usuario);

            return ResponseEntity.created(
                            MvcUriComponentsBuilder.fromMethodName(
                                    UsuarioController.class,
                                    "getUsuario",
                                    usuario.getId()
                            ).build().toUri())
                    .body(usuario);
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

    // 🟢 DELETE: Eliminar un usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (UsuarioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
