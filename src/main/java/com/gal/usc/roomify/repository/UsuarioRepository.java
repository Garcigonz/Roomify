package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Usuario;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<@NonNull Usuario, @NonNull String> {

    Optional<@NonNull Usuario> findById(String id);

}