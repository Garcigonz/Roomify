package com.gal.usc.roomify.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record UsuarioResponse(
        String id,
        String nombre,
        String email,
        int habitacion,
        int telefono,
        LocalDate nacimiento,
        Set<String> roles
) {}