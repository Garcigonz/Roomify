package com.gal.usc.roomify.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegistroUsuarioRequest(
        @NotBlank(message = "El ID de usuario es obligatorio")
        String id, // username

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @Min(value = 1, message = "Número de habitación inválido")
        int habitacion,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        LocalDate nacimiento,

        int telefono
) {}