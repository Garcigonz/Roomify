package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.ReservaNoEncontradaException;
import com.gal.usc.roomify.exception.ReservandoNoDisponibleException;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.repository.SalaRepository;
import com.gal.usc.utils.patch.JsonPatch;
import com.gal.usc.utils.patch.JsonPatchOperation;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final ObjectMapper mapper;

    public ReservaService(ReservaRepository reservaRepository, ObjectMapper mapper) {
        this.reservaRepository = reservaRepository;
        this.mapper = mapper;
    }

    // Servicio para añadir una nueva reserva a la base de datos
    public Reserva addReserva(Reserva nuevaReserva) throws ReservandoNoDisponibleException {
        // Buscar reservas que se solapen con la nueva reserva
        List<Reserva> reservasConflictivas = reservaRepository.findBySalaIdAndHoraInicioBeforeAndHoraFinAfter(
                nuevaReserva.sala().id(),
                nuevaReserva.horaFin(),
                nuevaReserva.horaInicio()
        );

        if (!reservasConflictivas.isEmpty()) {
            throw new ReservandoNoDisponibleException(nuevaReserva);
        }

        return reservaRepository.save(nuevaReserva);
    }

    // Servicio para obtener una reserva de la base de datos
    public Reserva getReserva(@NonNull String id) throws ReservaNoEncontradaException {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));
    }

    // Servicio para eliminar una reserva de la base de datos
    public void eliminarReserva(@NonNull String id) throws ReservaNoEncontradaException {
        if (reservaRepository.existsById(id)) {
            reservaRepository.deleteById(id);
        } else {
            throw new ReservaNoEncontradaException(id);
        }
    }

    //
    public Reserva updateReserva(String id, List<JsonPatchOperation> cambios) throws ReservaNoEncontradaException {
        Reserva reserva = reservaRepository.findById(id).orElseThrow(() -> new ReservaNoEncontradaException(id));
        JsonNode patched = JsonPatch.apply(cambios, mapper.convertValue(reserva, JsonNode.class));
        Reserva updated = mapper.convertValue(patched, Reserva.class);
        return reservaRepository.save(updated);
    }


}
