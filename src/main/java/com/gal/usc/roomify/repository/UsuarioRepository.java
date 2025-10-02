package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Usuario;
import com.mongodb.lang.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UsuarioRepository extends MongoRepository<Usuario,String> {
    Usuario findById(@NonNull String id);

}