package com.gal.usc.roomify.controller;
// ddd
import com.gal.usc.roomify.exception.SalaDuplicadaException;
import com.gal.usc.roomify.exception.SalaNoEncontradaException;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.service.SalaService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestController
@RequestMapping("salas")
public class SalaController {
    SalaService salaService;

    @Autowired
    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Sala> getSala(@PathVariable int id) {
        try {
            return ResponseEntity.ok(salaService.getSala(id));
        } catch (SalaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping()
    public ResponseEntity<@NonNull Sala> addSala(@RequestBody Sala sala) {
        try {
            sala = salaService.addSala(sala);
            return ResponseEntity
                    .created(MvcUriComponentsBuilder.fromMethodName(SalaController.class, "getSala", sala.id()).build().toUri())
                    .body(sala);
        } catch (SalaDuplicadaException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(SalaController.class, "getSala", sala.id()).build().toUri())
                    .build();

        }
    }

}
