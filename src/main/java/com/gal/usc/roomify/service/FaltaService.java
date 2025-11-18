package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.FaltaDuplicadaException;
import com.gal.usc.roomify.exception.FaltaNoEncontradaException;
import com.gal.usc.roomify.model.Falta;
import com.gal.usc.roomify.model.Falta.*;
import com.gal.usc.roomify.repository.FaltaRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.gal.usc.roomify.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class FaltaService {

    private final FaltaRepository faltaRepository;

    @Autowired
    public FaltaService(FaltaRepository faltaRepository) {
        this.faltaRepository = faltaRepository;

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

    /**
     * Obtiene todas las faltas de forma paginada
     * @param page Configuración de paginación y ordenamiento
     * @return Página de faltas
     */
    public Page<Falta> getFaltasPaginadas(PageRequest page) {
        return faltaRepository.findAll(page);
    }

    /**
     * Obtiene todas las faltas de un usuario específico
     * @param usuarioId ID del usuario
     * @return Lista de faltas del usuario
     */
    public Page<Falta> getFaltasPorUsuario(String usuarioId, PageRequest page) {
        return faltaRepository.findByCastigadoId(usuarioId, page);
    }


    
}


