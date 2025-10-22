package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.ReservaNoEncontradaException;
import com.gal.usc.roomify.exception.ReservandoNoDisponibleException;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.repository.SalaRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final SalaRepository salaRepository;

    public ReservaService(ReservaRepository reservaRepository, SalaRepository salaRepository) {
        this.reservaRepository = reservaRepository;
        this.salaRepository = salaRepository;
    }

    // Servicio para añadir una nueva reserva a la base de datos
    public Reserva addReserva(Reserva nuevaReserva) throws ReservandoNoDisponibleException {
        // Buscar reservas que se solapen con la nueva reserva
        List<Reserva> reservasConflictivas = reservaRepository.findBySalaIdAndHoraInicioBeforeAndHoraFinAfter(
                nuevaReserva.getSala().id(),
                nuevaReserva.getHoraFin(),
                nuevaReserva.getHoraInicio()
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
            salaRepository.deleteById(id);
        } else {
            throw new ReservaNoEncontradaException(id);
        }
    }
}
