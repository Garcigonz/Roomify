package com.gal.usc.roomify.service;

import java.time.*;
import java.util.*;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        // Prueba ejemplo
        usuarioRepository.save(new Usuario("58456425D","Pedro Mosquera Cerqueiro", 007, LocalDate.parse("2003-07-24"), 625900947, "residente"));
        usuarioRepository.save(new Usuario("52348961F", "Xenxo Fernandez Rodriguez", 101, LocalDate.parse("1998-03-12"), 612345678, "residente"));
        usuarioRepository.save(new Usuario("74215689K", "Andrés García López", 102, LocalDate.parse("2001-11-03"), 634567890, "visitante"));
        usuarioRepository.save(new Usuario("61547892M", "María Rodríguez Díaz", 103, LocalDate.parse("1995-05-28"), 698745632, "residente"));
        usuarioRepository.save(new Usuario("78451236J", "David Sánchez Torres", 104, LocalDate.parse("2000-02-15"), 677889900, "residente"));
        usuarioRepository.save(new Usuario("49632158P", "Sofía González Castro", 105, LocalDate.parse("2004-09-09"), 689321456, "visitante"));
        usuarioRepository.save(new Usuario("85479362Q", "Carlos Ruiz Méndez", 106, LocalDate.parse("1999-12-30"), 655432198, "residente"));
        usuarioRepository.save(new Usuario("71564829T", "Marta López Varela", 107, LocalDate.parse("2002-08-05"), 622198743, "residente"));
        usuarioRepository.save(new Usuario("58974326H", "Javier Iglesias Suárez", 108, LocalDate.parse("1997-04-17"), 644998877, "residente"));
        usuarioRepository.save(new Usuario("65214897B", "Lucía Romero Fernández", 109, LocalDate.parse("2003-01-25"), 677112233, "visitante"));
        usuarioRepository.save(new Usuario("73458912L", "Alberto Fernández Vázquez", 110, LocalDate.parse("1996-06-19"), 633556677, "residente"));
        usuarioRepository.save(new Usuario("82596431R", "Nerea Castro Martínez", 111, LocalDate.parse("2005-10-02"), 699887766, "residente"));
        usuarioRepository.save(new Usuario("59421687S", "Pablo Rodríguez Iglesias", 112, LocalDate.parse("1994-07-07"), 655334422, "visitante"));

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
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            return usuarioOpt.get();
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

    public Page<Usuario> getUsuarios(String nombre, String rol, Pageable pageable) {
        if (nombre != null && rol != null) {
            return usuarioRepository.findByNombreContainingIgnoreCaseAndRol(nombre, rol, pageable);
        } else if (nombre != null) {
            return usuarioRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (rol != null) {
            return usuarioRepository.findByRol(rol, pageable);
        } else {
            return usuarioRepository.findAll(pageable);
        }
    }
}
