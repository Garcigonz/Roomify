package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.model.Usuario;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends MongoRepository<@NonNull Usuario, @NonNull String> {


}