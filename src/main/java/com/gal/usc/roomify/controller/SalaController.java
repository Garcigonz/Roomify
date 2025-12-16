package com.gal.usc.roomify.controller;
import com.gal.usc.roomify.exception.SalaDuplicadaException;
import com.gal.usc.roomify.exception.SalaNoEncontradaException;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.service.SalaService;
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
@RequestMapping("salas")
public class SalaController {
    SalaService salaService;

    @Autowired
    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @Operation(
            summary = "Obtener una sala",
            description = "Respondemos a un GET sobre una sala con el contenido de una."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Devolvemos el objeto Sala"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La sala no existe"
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
            summary = "Anhadimos una nueva sala",
            description = "Se crea una nueva sala en el repositorio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Devolvemos la sala que se ha creado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permisos para insertar en el repositorio"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La sala que se quiere crear ya existe"
            )
    })
    @PostMapping()
    public ResponseEntity<@NonNull Sala> addSala(@RequestBody Sala sala) {
        try {
            // ?¿
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
