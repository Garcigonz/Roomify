package com.gal.usc.roomify.controller;


import com.gal.usc.roomify.exception.FaltaDuplicadaException;
import com.gal.usc.roomify.exception.FaltaNoEncontradaException;
import com.gal.usc.roomify.model.Falta;
import com.gal.usc.roomify.service.FaltaService;
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
public class FaltaController {

    FaltaService faltaService;

    @Autowired
    public FaltaController(FaltaService faltaService) { this.faltaService = faltaService; }

    // POST: Poner falta a un usuario
    @PostMapping()
    public ResponseEntity<@NonNull Falta> addFalta(@RequestBody Falta falta) {
        try {
            falta = faltaService.addFalta(falta);

            return ResponseEntity.created(MvcUriComponentsBuilder.fromMethodName(FaltaController.class, "getFalta",falta.id()).build().toUri()).body(falta);
        } catch(FaltaDuplicadaException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(FaltaController.class, "getFalta", falta.id()).build().toUri())
                    .build();

        }
    }

    // GET: Obtener una falta por ID
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull Falta> getFalta(@PathVariable String id) {
        try {
            return ResponseEntity.ok(faltaService.getFalta(id));
        } catch (FaltaNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Obtener lista de faltas paginada y ordenada
    @GetMapping()
    public ResponseEntity<@NonNull Page<@NonNull Falta>> getFaltas(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
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
