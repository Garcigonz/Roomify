package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.dto.request.RegistroUsuarioRequest;
import com.gal.usc.roomify.dto.response.UsuarioResponse;
import com.gal.usc.roomify.exception.RefreshTokenInvalidoException;
import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.mapper.UsuarioMapper;
import com.gal.usc.roomify.model.*;
import com.gal.usc.roomify.service.AuthenticationService;
import com.gal.usc.roomify.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para gestión de autenticación y registro de usuarios")
public class AuthenticationController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "__Secure-RefreshToken";
    private final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UsuarioService usuarioService, UsuarioMapper usuarioMapper) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = """
            Autentica a un usuario con sus credenciales (ID y contraseña).
            
            En caso de éxito, devuelve:
            - Un JWT en el header `Authorization` (formato: Bearer token)
            - Un refresh token en una cookie HTTP-only segura
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Inicio de sesión exitoso. Los tokens se envían en los headers",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de login inválidos",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @RequestBody LoginRequest request) {
        Usuario usuarioLogin = new Usuario();
        usuarioLogin.setId(request.id());
        usuarioLogin.setPassword(request.password());

        Authentication auth = authenticationService.login(usuarioLogin);

        return generarRespuestaConTokens(auth);
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = """
            Registra un nuevo usuario en el sistema con validación completa de datos.
            
            **Validaciones aplicadas:**
            - **ID de usuario**: Obligatorio, no puede estar vacío (será el username)
            - **Nombre**: Obligatorio, no puede estar vacío
            - **Email**: Obligatorio, debe tener formato válido de email
            - **Contraseña**: Obligatoria, mínimo 6 caracteres
            - **Habitación**: Obligatorio, debe ser un número positivo (≥1)
            - **Fecha de nacimiento**: Obligatoria, debe ser una fecha en el pasado
            - **Teléfono**: Opcional
            
            Si el ID de usuario ya existe en el sistema, se devuelve un error 409 (Conflict).
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El ID de usuario ya existe en el sistema",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                    Datos de registro inválidos. Posibles errores:
                    - ID, nombre, email o contraseña vacíos
                    - Email con formato inválido
                    - Contraseña con menos de 6 caracteres
                    - Número de habitación menor a 1
                    - Fecha de nacimiento en el futuro o nula
                    """,
                    content = @Content
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del usuario a registrar con todas las validaciones requeridas",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegistroUsuarioRequest.class),
                            examples = @ExampleObject(
                                    name = "Usuario válido",
                                    value = """
                                    {
                                      "id": "jgarcia",
                                      "nombre": "Juan García",
                                      "email": "juan.garcia@example.com",
                                      "password": "contraseña123",
                                      "habitacion": 305,
                                      "nacimiento": "2000-05-15",
                                      "telefono": 612345678
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody RegistroUsuarioRequest request) throws UsuarioDuplicadoException {

        // convertir dto a entidad
        Usuario usuarioParaGuardar = usuarioMapper.toEntity(request);

        Usuario usuarioGuardado = usuarioService.addUsuario(usuarioParaGuardar);
        // convertir a response
        UsuarioResponse response = usuarioMapper.toResponse(usuarioGuardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Refrescar tokens de autenticación",
            description = """
            Genera un nuevo par de tokens (JWT y refresh token) usando un refresh token válido.
            
            El refresh token debe enviarse en una cookie con el nombre `__Secure-RefreshToken`.
            Requiere que el usuario esté autenticado.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Tokens renovados con éxito. Los nuevos tokens se envían en los headers",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario no autenticado",
                    content = @Content
            )
    })
    @PostMapping("refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> refresh(
            @Parameter(
                    description = "Refresh token enviado en cookie",
                    required = true
            )
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {

        Authentication auth = authenticationService.login(refreshToken);

        if (auth.getPrincipal() != null) {
            return generarRespuestaConTokens(auth);
        }

        throw new RefreshTokenInvalidoException(refreshToken);
    }

    @Operation(
            summary = "Cerrar sesión",
            description = """
            Invalida los tokens del usuario actual y cierra su sesión.
            
            El JWT debe enviarse en el header `Authorization` (formato: Bearer token).
            Esto invalidará tanto el JWT como el refresh token asociado.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Sesión cerrada con éxito. La cookie del refresh token se elimina",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @PostMapping("logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @Parameter(
                    description = "JWT token en formato Bearer",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        Authentication auth = authenticationService.parseJWT(token);

        if (auth.getPrincipal() != null) {
            Usuario user = (Usuario)auth.getPrincipal();
            authenticationService.invalidateTokens(user);
            ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, null).build();

            return ResponseEntity.noContent()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .build();
        }

        throw new RuntimeException("Internal Error");
    }

    /**
     * Genera una respuesta HTTP con los tokens de autenticación.
     *
     * Crea un JWT y un refresh token a partir de la autenticación proporcionada,
     * y los incluye en la respuesta: el JWT en el header Authorization y el
     * refresh token en una cookie HTTP-only segura.
     *
     * @param auth Objeto de autenticación del usuario
     * @return ResponseEntity con código 204 y los tokens en los headers correspondientes
     */
    private ResponseEntity<Void> generarRespuestaConTokens(Authentication auth) {
        String token = authenticationService.generateJWT(auth);
        String refreshToken = authenticationService.regenerateRefreshToken(auth);

        String refreshPath = MvcUriComponentsBuilder.fromMethodName(AuthenticationController.class, "refresh", "").build().toUri().getPath();

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .secure(true)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.STRICT.toString())
                .path(refreshPath)
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

}