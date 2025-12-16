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

    @Operation(
            summary = "Devolvemos todos los usuarios",
            description = "Obtenemos todos los usuarios de la BBDD"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Devolvemos los usuarios de la BBDD"
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
            summary = "Obtemeos un usuario",
            description = "Devuelve un usuario concreto con el id pasado como argumento"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "El usuario existe y se devuelve"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario no existe"
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
            summary = "Anhadimos un usuario",
            description = "Se anhade un usuario a la BBDD, por defecto en el Servicio se le asigna el Rol: USER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Se crea y se devuelve el usuario, omitiendo campos sensibles como la contraseña"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permiso para añadir un usuario"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Se está intentando crear un usuario que ya existe"
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
            summary = "Borrar un usuario de la colección",
            description = "Eliminamos a un usuario de la colección"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Devolvemos un OK de que se borró el usuario correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el Usuario"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permisos para hacer este borrado (por ej. un USER intenta borrar a otro USER)"
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
