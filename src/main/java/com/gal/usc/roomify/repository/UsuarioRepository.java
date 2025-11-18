package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Sala;
import com.gal.usc.roomify.model.Usuario;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<@NonNull Usuario, @NonNull String> {
    Page<Usuario> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    Page<Usuario> findByRol(String rol, Pageable pageable);
    Page<Usuario> findByNombreContainingIgnoreCaseAndRol(String nombre, String rol, Pageable pageable);
    Optional<@NonNull Usuario> findById(String id);

}