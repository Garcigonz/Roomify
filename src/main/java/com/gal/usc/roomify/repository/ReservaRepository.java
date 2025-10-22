package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Reserva;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends MongoRepository<@NonNull Reserva, @NonNull String> {
    // Buscar por sala
    //Reserva findById(String reservaId);

    // Buscar por estudiante
    List<Reserva> findByUsuarioId(String usuarioId);

    List<Reserva> findBySalaIdAndHoraInicioBeforeAndHoraFinAfter(
            int salaId,
            LocalDateTime horaFin,
            LocalDateTime horaInicio
    );
}
