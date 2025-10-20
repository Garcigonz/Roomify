package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Falta;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaltaRepository extends MongoRepository <@NonNull Falta, @NonNull String> {

    Falta findById(@NonNull int id);

    boolean existsById(int id);

    void deleteById(int id);
}
