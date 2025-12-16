package com.gal.usc.roomify.controller;


import com.gal.usc.roomify.exception.ReservaNoEncontradaException;
import com.gal.usc.roomify.exception.ReservandoNoDisponibleException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
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
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;


@RestController
@RequestMapping("reservas")
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
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(reservaService.getReserva(id));
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
        } catch (ReservandoNoDisponibleException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(ReservaController.class, "getReserva", nuevaReserva.id()).build().toUri())
                    .build();
        }
    }

    @Operation(
            summary = "Elimina una reserva",
            description = "Eliminamos una reserva de la colección"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "La reserva fue eliminada perfectamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La reserva que se intenta borrar no existe"
            )
    })
    @DeleteMapping("/{idReserva}")
    public ResponseEntity<@NonNull Reserva> deleteReserva(@PathVariable String idReserva) {
        try {
            reservaService.eliminarReserva(idReserva);
            return ResponseEntity.noContent().build();
        } catch(ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(
            summary = "Actualizamos una reserva",
            description = "Se le envía un JSON para actualizar la reserva"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Se editó correctamente la reserva"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La reserva a la que se quiere acceder no existe"
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

}
