package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.SalaDuplicadaException;
import com.gal.usc.roomify.exception.SalaNoEncontradaException;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.repository.SalaRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SalaService {
    private final SalaRepository salaRepository;

    @Autowired
    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    // Servicio para añadir una nueva sala a la base de datos
    public Sala addSala(@NonNull Sala sala) throws SalaDuplicadaException {
        if (!salaRepository.existsById(sala.id())) {
            return salaRepository.save(sala);
        } else {
            throw new SalaDuplicadaException(sala);
        }
    }

    // Servicio para obtener una sala de la base de datos
    public Sala getSala(@NonNull Integer id) throws SalaNoEncontradaException {
        if (salaRepository.existsById(id)) {
            return salaRepository.findById(id);
        } else {
            throw new SalaNoEncontradaException(id);
        }
    }

    // Servicio para eliminar una sala de la base de datos
    public void eliminarSala(@NonNull Integer id) throws SalaNoEncontradaException {
        if (salaRepository.existsById(id)) {
            salaRepository.deleteById(id);
        } else {
            throw new SalaNoEncontradaException(id);
        }
    }
}
