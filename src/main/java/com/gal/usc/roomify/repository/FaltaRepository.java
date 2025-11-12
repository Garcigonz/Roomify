package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Falta;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaltaRepository extends MongoRepository <@NonNull Falta, @NonNull String> {

    boolean existsById(String id);

    void deleteById(String id);

    @Query("{ 'castigado.id': ?0 }")
    Page<Falta> findByCastigadoId(String usuarioId, Pageable pageable);

    


}
