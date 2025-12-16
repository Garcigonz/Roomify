package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Role;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.RoleRepository;
import com.gal.usc.roomify.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RoleRepository roleRepository;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, RoleRepository roleRepository) {
        this.usuarioService = usuarioService;
        this.roleRepository = roleRepository;
    }

    @Operation(
            summary = "Obtener usuarios",
            description = "Recupera la lista de usuarios."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Lista de usuarios obtenida con éxito"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permiso para acceder a todos los Usuarios"
            ),
    })
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


    @Operation(
            summary = "Obtener usuario por ID",
            description = "Recupera los detalles de un usuario concreto mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario encontrado con éxito"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permiso para obtener información de este usuario"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Usuario> getUsuario(@PathVariable String id) {
        try {
            Usuario usuario = usuarioService.getUsuario(id);
            return ResponseEntity.ok(usuario);
        } catch (UsuarioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Registra un nuevo usuario. Por defecto se le asigna el Rol: USER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado con éxito"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permiso para añadir un usuario"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El usuario ya existe"
            )
    })
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

    @Operation(
            summary = "Eliminar usuario por ID",
            description = "Elimina a un usuario concreto mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario eliminado con éxito"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el Usuario"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tienen permisos para hacer este borrado"
            )
    })
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
