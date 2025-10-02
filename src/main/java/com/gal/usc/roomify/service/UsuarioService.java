package com.gal.usc.roomify.service;

import java.time.*;
import java.util.*;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        // Prueba ejemplo
        usuarioRepository.save(
                new Usuario("58456425D",
                        "Pedro Mosque" +
                                "ra Cerqueiro",
                        007,
                        LocalDate.parse("2003-07-24"),
                        625900947)
        );
    }

    // Servicio para añadir un nuevo usuario a la base de datos
    public Usuario addUsuario(@NonNull Usuario usuario) throws UsuarioDuplicadoException {
        if (!usuarioRepository.existsById(usuario.id())) {
            return usuarioRepository.save(usuario);
        } else {
            throw new UsuarioDuplicadoException(usuario);
        }
    }


    // Servicio para obtener un usuario de la base de datos
    public Usuario getUsuario(@NonNull String id) throws UsuarioNoEncontradoException {
        if (usuarioRepository.existsById(id)) {
            return usuarioRepository.findById(id);
        } else {
            throw new UsuarioNoEncontradoException(id);
        }
    }

    // Servicio para elminar un usuario de la base de datos - return?
    public void eliminarUsuario(@NonNull String id) throws UsuarioNoEncontradoException {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        } else {
            throw new UsuarioNoEncontradoException(id);
        }
    }
}
