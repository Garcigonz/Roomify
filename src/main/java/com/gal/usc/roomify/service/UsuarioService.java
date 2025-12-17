package com.gal.usc.roomify.service;

import java.time.*;
import java.util.*;

import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.exception.UsuarioNoEncontradoException;
import com.gal.usc.roomify.model.Role;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.RoleRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Servicio para añadir un nuevo usuario a la base de datos
    public Usuario addUsuario(@NonNull Usuario usuario) throws UsuarioDuplicadoException {
        if (usuarioRepository.existsById(usuario.getId())) {
            // si ya existe
            throw new UsuarioDuplicadoException(usuario);
        }

        if (usuario.getRoles() == null) {
            usuario.setRoles(new HashSet<>());
        }

       // Role userRole = roleRepository.findByRolename("USER");
        //usuario.setRoles(Set.of(userRole));
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }


    @PreAuthorize("#id == authentication.name OR hasRole('ADMIN')")
    // Servicio para obtener un usuario de la base de datos
    public Usuario getUsuario(@NonNull String id) throws UsuarioNoEncontradoException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            return usuarioOpt.get();
        } else {
            throw new UsuarioNoEncontradoException(id);
        }
    }

    @PreAuthorize("#id == authentication.name OR hasRole('ADMIN')")
    // Servicio para elminar un usuario de la base de datos - return?
    public void eliminarUsuario(@NonNull String id) throws UsuarioNoEncontradoException {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        } else {
            throw new UsuarioNoEncontradoException(id);
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
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
