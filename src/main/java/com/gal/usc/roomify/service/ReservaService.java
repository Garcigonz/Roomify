package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.ReservaNoEncontradaException;
import com.gal.usc.roomify.exception.ReservandoNoDisponibleException;
import com.gal.usc.roomify.exception.UsuarioCastigadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.FaltaRepository;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.repository.SalaRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.gal.usc.utils.patch.JsonPatch;
import com.gal.usc.utils.patch.JsonPatchOperation;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FaltaRepository faltaRepository;
    private final SalaRepository salaRepository;
    private final ObjectMapper mapper;

    @Autowired
    public ReservaService(ReservaRepository reservaRepository,
                          UsuarioRepository usuarioRepository,
                          FaltaRepository faltaRepository,
                          SalaRepository salaRepository,
                          ObjectMapper mapper) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.faltaRepository = faltaRepository;
        this.salaRepository = salaRepository;
        this.mapper = mapper;

        Reserva reserva = new Reserva("N1", salaRepository.findById(1), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1), usuarioRepository.findById("58456425D").get(), "Ninguna");

        reservaRepository.save(reserva);
    }


    // Servicio para añadir una nueva reserva a la base de datos
    public Reserva addReserva(Reserva nuevaReserva) throws ReservandoNoDisponibleException, UsuarioNoEncontradoException, UsuarioNoEncontradoException {
        // Buscar reservas que se solapen con la nueva reserva
        List<Reserva> reservasConflictivas = reservaRepository.findBySalaIdAndHoraInicioBeforeAndHoraFinAfter(
                nuevaReserva.sala().getId(),
                nuevaReserva.horaFin(),
                nuevaReserva.horaInicio()
        );

        Usuario usuario = usuarioRepository.findById(nuevaReserva.usuario().getId()).orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        if (!reservasConflictivas.isEmpty()) {
            throw new ReservandoNoDisponibleException(nuevaReserva);
        }

        // Contamos cuantas faltas tiene el Usuario
        long faltasCount = faltaRepository.countByCastigadoId(usuario.getId());

        try{
            // Si tiene 3 faltas o más, LANZAMOS EXCEPCIÓN
            if(faltasCount >= 3) {
                throw new UsuarioCastigadoException(usuario);
            }
        }catch (UsuarioCastigadoException castException){}

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
