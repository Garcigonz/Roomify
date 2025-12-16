package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.exception.FaltaDuplicadaException;
import com.gal.usc.roomify.exception.FaltaNoEncontradaException;
import com.gal.usc.roomify.model.Falta;
import com.gal.usc.roomify.service.FaltaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("faltas")
@Tag(name = "Faltas", description = "Endpoints para gestión de faltas de usuarios")
public class FaltaController {

    FaltaService faltaService;

    @Autowired
    public FaltaController(FaltaService faltaService) {
        this.faltaService = faltaService;
    }

    @Operation(
            summary = "Crear nueva falta",
            description = "Registra una nueva falta para un usuario. Si la falta ya existe, devuelve un error (409)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Falta registrada con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Falta.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La falta ya existe en el sistema",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la falta inválidos",
                    content = @Content
            )
    })
    @PostMapping()
    public ResponseEntity<@NonNull Falta> addFalta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la falta a registrar",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Falta.class))
            )
            @RequestBody Falta falta) {
        try {
            falta = faltaService.addFalta(falta);

            return ResponseEntity.created(
                    MvcUriComponentsBuilder.fromMethodName(
                            FaltaController.class,
                            "getFalta",
                            falta.id()
                    ).build().toUri()
            ).body(falta);
        } catch(FaltaDuplicadaException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(
                            FaltaController.class,
                            "getFalta",
                            falta.id()
                    ).build().toUri())
                    .build();
        }
    }

    @Operation(
            summary = "Obtener falta por ID",
            description = "Recupera los detalles de una falta específica mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Falta encontrada con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Falta.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Falta no encontrada",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Falta> getFalta(
            @Parameter(
                    description = "ID único de la falta",
                    required = true,
                    example = "falta-123"
            )
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(faltaService.getFalta(id));
        } catch (FaltaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Obtener faltas",
            description = """
            Obtiene una lista paginada de faltas con opciones de ordenación.
            
            **Ordenación:** Los campos se pueden ordenar de forma ascendente o descendente.
            - Ascendente: `sort=fecha` o `sort=usuarioId`
            - Descendente: `sort=-fecha` o `sort=-usuarioId`
            - Múltiples criterios: `sort=fecha&sort=-usuarioId`
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de faltas obtenida con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    @GetMapping()
    public ResponseEntity<@NonNull Page<@NonNull Falta>> getFaltas(
            @Parameter(
                    description = "Número de página (comienza en 0)",
                    example = "0"
            )
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(
                    description = "Cantidad de elementos por página",
                    example = "10"
            )
            @RequestParam(value = "size", defaultValue = "10") int size,

            @Parameter(
                    description = "Campo(s) de ordenación. Prefijo '-' para orden descendente",
                    example = "fecha"
            )
            @RequestParam(value = "sort", defaultValue = "fecha") List<String> sort
    ) {
        // Genera las reglas de ordenación (asc o desc)
        Sort sorting = Sort.by(sort.stream()
                .map(key -> key.startsWith("-")
                        ? Sort.Order.desc(key.substring(1))
                        : Sort.Order.asc(key))
                .toList());

        PageRequest pageable = PageRequest.of(page, size, sorting);
        Page<Falta> faltas = faltaService.getFaltasPaginadas(pageable);

        return ResponseEntity.ok(faltas);
    }
}