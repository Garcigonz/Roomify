package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Sala;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends MongoRepository<@NonNull Sala, @NonNull String> {

    // Verificar si existe una sala por ID
    boolean existsById(int id);

    // Encontrar sala por Id
    Sala findById(int id);

    // Eliminar una sala por ID
    void deleteById(int id);

}