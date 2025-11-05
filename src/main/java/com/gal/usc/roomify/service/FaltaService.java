package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.FaltaDuplicadaException;
import com.gal.usc.roomify.exception.FaltaNoEncontradaException;
import com.gal.usc.roomify.model.Falta;
import com.gal.usc.roomify.model.Falta.*;
import com.gal.usc.roomify.repository.FaltaRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gal.usc.roomify.model.*;

import java.time.LocalDate;
import java.util.Optional;


@Service
public class FaltaService {

    private final FaltaRepository faltaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public FaltaService(FaltaRepository faltaRepository, UsuarioRepository usuarioRepository) {
        this.faltaRepository = faltaRepository;
        this.usuarioRepository = usuarioRepository;
        Falta f1 = new Falta(
                "f1",
                "Llegó tarde a una reunión",
                LocalDate.of(2025, 11, 1),
                Severidad.LEVE,
                LocalDate.of(2025, 12, 1),
                usuarioRepository.findById("58456425D")
        );

    }

    // Servicio de añadir una nueva falta a la BBDD
    public Falta addFalta(@NonNull Falta falta) throws FaltaDuplicadaException {
        if(!faltaRepository.existsById(falta.id()))
            return faltaRepository.save(falta);
        else{
            throw new FaltaDuplicadaException(falta);
        }
    }

    // Servicio para obtener una Falta de la BBDD
    public Falta getFalta(@NonNull String id) throws FaltaNoEncontradaException {
        Optional<Falta> faltaOpt = faltaRepository.findById(id);

        if(faltaOpt.isPresent()) {
            return faltaOpt.get();
        }
        else{
            throw new FaltaNoEncontradaException(id);
        }
    }
}
