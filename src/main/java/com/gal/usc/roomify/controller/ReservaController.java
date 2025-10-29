package com.gal.usc.roomify.controller;


import com.gal.usc.roomify.exception.ReservaNoEncontradaException;
import com.gal.usc.roomify.exception.ReservandoNoDisponibleException;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.service.ReservaService;
import com.gal.usc.utils.patch.JsonPatchOperation;
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

    @GetMapping({"/{id}"})
    public ResponseEntity<@NonNull Reserva> getReserva(@PathVariable String id){
        try {
            return ResponseEntity.ok(reservaService.getReserva(id));
        } catch (ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping()
    public ResponseEntity<@NonNull Reserva> addReserva(@RequestBody Reserva nuevaReserva) {
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

    @DeleteMapping()
    public ResponseEntity<@NonNull Reserva> deleteReserva(@RequestBody String idReserva) {
        try {
            reservaService.eliminarReserva(idReserva);
            return ResponseEntity.noContent().build();
        } catch(ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<@NonNull Reserva> updateReserva(@PathVariable("id") String id, @RequestBody List<JsonPatchOperation> cambios) {
        try {
            return ResponseEntity.ok(reservaService.updateReserva(id, cambios));
        } catch (ReservaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
