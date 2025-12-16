package com.gal.usc.roomify.controller;
import com.gal.usc.roomify.exception.SalaDuplicadaException;
import com.gal.usc.roomify.exception.SalaNoEncontradaException;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.service.SalaService;
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
@RequestMapping("salas")
@Tag(name = "Salas", description = "Endpoints para gestión de salas")
public class SalaController {
    SalaService salaService;

    @Autowired
    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @Operation(
            summary = "Obtener sala por ID",
            description = "Recupera los detalles de una sala concreta mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sala encontrada con éxito"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sala no encontrada"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Sala> getSala(@PathVariable int id) {
        try {
            return ResponseEntity.ok(salaService.getSala(id));
        } catch (SalaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Crear nueva sala",
            description = "Registra una nueva sala. Si la sala ya existe, devuelve un error (409)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Sala registrada con éxito"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permisos para insertar en el repositorio"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La sala ya existe"
            )
    })
    @PostMapping()
    public ResponseEntity<@NonNull Sala> addSala(@RequestBody Sala sala) {
        try {
            sala = salaService.addSala(sala);
            return ResponseEntity
                    .created(MvcUriComponentsBuilder.fromMethodName(SalaController.class, "getSala", sala.getId()).build().toUri())
                    .body(sala);
        } catch (SalaDuplicadaException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(SalaController.class, "getSala", sala.getId()).build().toUri())
                    .build();

        }
    }

    @Operation(
            summary = "Obtener salas",
            description = "Recupera una página de salas con soporte para paginación, tamaño de página y ordenación. " +
                    "La ordenación se puede especificar usando el prefijo `-` para orden descendente (ej: -capacidad)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de salas obtenida con éxito"
            )
    })
    @GetMapping()
    public ResponseEntity<@NonNull Page<@NonNull Sala>> getSalas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") List<String> sort
    ) {
        // Configuramos la ordenación (ej: sort=-capacidad para descendente)
        Sort sorting = Sort.by(sort.stream()
                .map(key -> key.startsWith("-")
                        ? Sort.Order.desc(key.substring(1))
                        : Sort.Order.asc(key))
                .toList());

        PageRequest pageable = PageRequest.of(page, size, sorting);

        return ResponseEntity.ok(salaService.getSalas(pageable));
    }

}
