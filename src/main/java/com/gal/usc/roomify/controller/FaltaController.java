package com.gal.usc.roomify.controller;


import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.model.Falta;
import com.gal.usc.roomify.service.FaltaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;


@RestController
@RequestMapping("faltas")
public class FaltaController {

    FaltaService faltaService;

    public FaltaController(FaltaService faltaService) { this.faltaService = faltaService; }

    // POST: Poner falta a un usuario
    @PostMapping()
    public ResponseEntity<@NonNull Falta> addFalta(@RequestBody Falta falta) {
        try {
            falta = faltaService.addfalta(falta);

            return ResponseEntity.created(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "getUsuario", usuario.id()).build().toUri())
                    .body(usuario);
        } catch(UsuarioDuplicadoException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "getUsuario", usuario.id()).build().toUri())
                    .build();
        }
    }
}
