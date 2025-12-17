package com.gal.usc.roomify.service;

import com.gal.usc.roomify.exception.*;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.FaltaRepository;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.repository.SalaRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.gal.usc.utils.patch.JsonPatch;
import com.gal.usc.utils.patch.JsonPatchOperation;
import org.apache.tomcat.util.http.parser.Authorization;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
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


    }


    @PreAuthorize("#nuevaReserva.usuario().id == authentication.name OR hasRole('ADMIN')")
    // Servicio para añadir una nueva reserva a la base de datos
    public Reserva addReserva(Reserva nuevaReserva) throws ReservandoNoDisponibleException, UsuarioNoEncontradoException, UsuarioCastigadoException, SalaNoEncontradaException {

        Sala salaReal = salaRepository.findById(nuevaReserva.sala().getId())
                .orElseThrow(() -> new SalaNoEncontradaException(nuevaReserva.sala().getId()));

        List<Reserva> reservasConflictivas = reservaRepository.findBySalaIdAndHoraInicioBeforeAndHoraFinAfter(
                salaReal.getId(),
                nuevaReserva.horaFin(),
                nuevaReserva.horaInicio()
        );

        Usuario usuario = usuarioRepository.findById(nuevaReserva.usuario().getId()).orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        if (!reservasConflictivas.isEmpty()) {
            throw new ReservandoNoDisponibleException(nuevaReserva);
        }

        Usuario usuarioReal = usuarioRepository.findById(nuevaReserva.usuario().getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        long faltasCount = faltaRepository.countByCastigadoId(usuarioReal.getId());
        if(faltasCount >= 3) {
            throw new UsuarioCastigadoException(usuarioReal);
        }

        Reserva reservaA_Guardar = new Reserva(
                nuevaReserva.id(),
                salaReal,
                nuevaReserva.horaInicio(),
                nuevaReserva.horaFin(),
                usuarioReal,
                nuevaReserva.observaciones()
        );

        return reservaRepository.save(reservaA_Guardar);
    }

    // Servicio para obtener una reserva de la base de datos
    public Reserva getReserva(@NonNull String id) throws ReservaNoEncontradaException {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));
    }



    @PreAuthorize("@reservaService.getResponsableReserva(#id).id == authentication.name OR hasRole('ADMIN')")
    // Servicio para eliminar una reserva de la base de datos
    public void eliminarReserva(@NonNull String id) throws ReservaNoEncontradaException {
        if (reservaRepository.existsById(id)) {
            reservaRepository.deleteById(id);
        } else {
            throw new ReservaNoEncontradaException(id);
        }
    }

    @PreAuthorize("@reservaService.getResponsableReserva(#id).id == authentication.name OR hasRole('ADMIN')")
    public Reserva updateReserva(String id, List<JsonPatchOperation> cambios) throws ReservaNoEncontradaException {
        Reserva reserva = reservaRepository.findById(id).orElseThrow(() -> new ReservaNoEncontradaException(id));
        JsonNode patched = JsonPatch.apply(cambios, mapper.convertValue(reserva, JsonNode.class));
        Reserva updated = mapper.convertValue(patched, Reserva.class);
        return reservaRepository.save(updated);
    }

    public Usuario getResponsableReserva(String idReserva) throws ReservaNoEncontradaException {
        if(reservaRepository.existsById(idReserva)){
            return reservaRepository.findById(idReserva).get().usuario();
        }else{
            throw new ReservaNoEncontradaException(idReserva);
        }
    }

    public List<Reserva> getReservasPorUsuario(String usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    public Reserva ampliarReserva(String id, int horasExtra) throws ReservaNoEncontradaException {
        Reserva reservaActual = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));

        LocalDateTime inicioAmpliacion = reservaActual.horaFin();
        LocalDateTime finAmpliacion = reservaActual.horaFin().plusHours(horasExtra);

        List<Reserva> conflictos = reservaRepository.findBySalaIdAndHoraInicioBeforeAndHoraFinAfter(
                reservaActual.sala().getId(),
                finAmpliacion,
                inicioAmpliacion
        );

        if (!conflictos.isEmpty()) {
            throw new SalaOcupadaException("No se puede ampliar: La sala está reservada en ese horario.");
        }

        Reserva reservaAmpliada = new Reserva(
                reservaActual.id(),
                reservaActual.sala(),
                reservaActual.horaInicio(),
                finAmpliacion,
                reservaActual.usuario(),
                reservaActual.observaciones()
        );

        return reservaRepository.save(reservaAmpliada);
    }

}
