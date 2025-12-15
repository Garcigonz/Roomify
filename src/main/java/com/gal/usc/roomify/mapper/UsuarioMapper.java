package com.gal.usc.roomify.mapper;

import com.gal.usc.roomify.dto.request.RegistroUsuarioRequest;
import com.gal.usc.roomify.dto.response.UsuarioResponse;
import com.gal.usc.roomify.model.Usuario;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    // Request -> Entidad
    public Usuario toEntity(RegistroUsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setId(request.id());
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPassword(request.password());
        usuario.setHabitacion(request.habitacion());
        usuario.setNacimiento(request.nacimiento());
        usuario.setTelefono(request.telefono());
        return usuario;
    }

    // Entidad -> Response
    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getHabitacion(),
                usuario.getTelefono(),
                usuario.getNacimiento(),
                usuario.getRoles() != null
                        ? usuario.getRoles().stream().map(r -> r.getRolename()).collect(Collectors.toSet())
                        : null
        );
    }
}