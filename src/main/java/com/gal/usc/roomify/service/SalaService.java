package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.SalaDuplicadaException;
import com.gal.usc.roomify.exception.SalaNoEncontradaException;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.FaltaRepository;
import com.gal.usc.roomify.repository.SalaRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SalaService {
    private final SalaRepository salaRepository;

    @Autowired
    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;


        salaRepository.save(new Sala(1, "Sala de reuniones pequeña", 20, null));
        salaRepository.save(new Sala(2, "Sala de conferencias principal", 120, null));
        salaRepository.save(new Sala(3, "Aula de informática 1", 30, null));
        salaRepository.save(new Sala(4, "Aula de informática 2", 35, null));
        salaRepository.save(new Sala(5, "Laboratorio de electrónica", 25, null));
        salaRepository.save(new Sala(6, "Laboratorio de química", 20, null));
        salaRepository.save(new Sala(7, "Sala multiuso", 50, null));
        salaRepository.save(new Sala(8, "Auditorio", 200, null));
        salaRepository.save(new Sala(9, "Oficina compartida", 12, null));
        salaRepository.save(new Sala(10, "Sala de creatividad", 15, null));

    }


    @PreAuthorize("hasRole('ADMIN')")
    // Servicio para añadir una nueva sala a la base de datos
    public Sala addSala(@NonNull Sala sala) throws SalaDuplicadaException {
        if (!salaRepository.existsById(sala.getId())) {
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

    @PreAuthorize("hasRole('ADMIN')")
    // Servicio para eliminar una sala de la base de datos
    public void eliminarSala(@NonNull Integer id) throws SalaNoEncontradaException {
        if (salaRepository.existsById(id)) {
            salaRepository.deleteById(id);
        } else {
            throw new SalaNoEncontradaException(id);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    // Servicio para asignar Sala a un residente
    public void asignarUsuario(@NonNull Usuario usuario, @NonNull Sala sala) {
        if(salaRepository.existsById(sala.getId())) {
            salaRepository.findById(sala.getId()).setResponsableActual(usuario);
        }
    }
}
