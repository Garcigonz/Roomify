package com.gal.usc.roomify.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import com.gal.usc.roomify.exception.*;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.service.ReservaService;
import com.gal.usc.utils.patch.JsonPatchOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;


@RestController
@RequestMapping("reservas")
@Tag(name = "Reservas", description = "Endpoints para gestión de reservas")
public class ReservaController {
    private final ReservaRepository reservaRepository;
    ReservaService reservaService;

    @Autowired
    public ReservaController(ReservaService reservaService, ReservaRepository reservaRepository) {
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
    }




    @Operation(
            summary = "Obtener una reserva por ID",
            description = "Devuelve la información de una reserva a partir de su identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reserva.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No se tiene permiso para buscar las reservas"
            )

    })
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Reserva> getReserva(
            @Parameter(
                    description = "Identificador único de la reserva",
                    example = "R12345",
                    required = true
            )
            @PathVariable("id") String id
    ) {
        try {
            return ResponseEntity.ok(reservaService.getReserva(id.trim()));
        } catch (ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(
            summary = "Crear una nueva reserva",
            description = "Crea una nueva reserva en el sistema y devuelve la reserva creada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reserva creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reserva.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Location",
                                    description = "URI de la reserva creada",
                                    schema = @Schema(type = "string")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La sala que se intenta reservar no está disponible"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario/Sala asociado a la reserva no encontrado"
            )
    })
    @PostMapping()
    public ResponseEntity<@NonNull Reserva> addReserva(@RequestBody Reserva nuevaReserva) throws UsuarioNoEncontradoException {
        try {
            nuevaReserva = reservaService.addReserva(nuevaReserva);
            return ResponseEntity
                    .created(MvcUriComponentsBuilder.fromMethodName(ReservaController.class, "getReserva", nuevaReserva.id()).build().toUri())
                    .body(nuevaReserva);
        } catch (ReservandoNoDisponibleException | UsuarioCastigadoException e) {
            // Conflictos de lógica (sala ocupada o usuario castigado) -> 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
            // Opcional: Podrías devolver un body con el mensaje del error
        } catch (SalaNoEncontradaException e) {
            // La sala no existe -> 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Eliminar reserva por ID",
            description = "Elimina una reserva concre mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Reserva eliminada con éxito"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada"
            )
    })
    @DeleteMapping("/{idReserva}")
    public ResponseEntity<Void> deleteReserva(@PathVariable("idReserva") String idReserva) {
        try {
            reservaService.eliminarReserva(idReserva);
            return ResponseEntity.noContent().build();
        } catch(ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(
            summary = "Actualizar reserva por ID",
            description = """
        Actualiza parcialmente una reserva existente mediante operaciones JSON Patch.
        
        **Operaciones soportadas:**
        - `replace`: Reemplazar el valor de un campo
        - `add`: Añadir un nuevo campo o valor
        - `remove`: Eliminar un campo
        - `copy`: Copiar un valor de un campo a otro
        - `move`: Mover un valor de un campo a otro
        
        **Ejemplo de uso:**
        ```json
        [
          {
            "op": "replace",
            "path": "/estado",
            "value": "CONFIRMADA"
          },
          {
            "op": "replace",
            "path": "/observaciones",
            "value": "Requiere proyector"
          }
        ]
        ```
        
        **Campos comunes a modificar:** estado, observaciones, fechaInicio, fechaFin, salaId, etc.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva actualizada con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reserva.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Operación JSON Patch inválida o datos incorrectos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "La operación es válida pero no se puede procesar (ej: campo no existe)",
                    content = @Content
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<@NonNull Reserva> updateReserva(@PathVariable("id") String id, @RequestBody List<JsonPatchOperation> cambios) {
        try {
            return ResponseEntity.ok(reservaService.updateReserva(id, cambios));
        } catch (ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(
            summary = "Listar reservas de un usuario",
            description = "Devuelve una lista completa con todas las reservas asociadas a un ID de usuario específico."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de reservas obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            // Usamos @ArraySchema porque devolvemos una List<Reserva>, no una sola Reserva
                            array = @ArraySchema(schema = @Schema(implementation = Reserva.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para ver las reservas de este usuario"
            )
    })    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Reserva>> getReservasUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(reservaService.getReservasPorUsuario(usuarioId));
    }

    @Operation(summary = "Ampliar reserva", description = "Suma un número de horas a la hora de finalización actual.")
    @PutMapping("/{id}/ampliar")
    @PreAuthorize("@reservaRepository.findById(#id).get().usuario.id == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<Reserva> ampliarReserva(
            @PathVariable String id,
            @RequestParam int horas) {
        try {
            Reserva reserva = reservaService.ampliarReserva(id, horas);
            return ResponseEntity.ok(reserva);
        } catch (ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        } catch (SalaOcupadaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}
