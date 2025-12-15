package com.gal.usc.roomify.controller;

import com.gal.usc.roomify.dto.request.RegistroUsuarioRequest;
import com.gal.usc.roomify.dto.response.UsuarioResponse;
import com.gal.usc.roomify.exception.RefreshTokenInvalidoException;
import com.gal.usc.roomify.exception.UsuarioDuplicadoException;
import com.gal.usc.roomify.mapper.UsuarioMapper;
import com.gal.usc.roomify.model.*;
import com.gal.usc.roomify.service.AuthenticationService;
import com.gal.usc.roomify.service.UsuarioService;
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

    /* ???
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {

        Authentication auth = authenticationService.login(request.id(), request.password());
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
                .headers(h -> h.setBearerAuth(token))
                .build();
    } */

    /*@PostMapping("login")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<Void> login(@RequestBody Usuario usuario) {
        Authentication auth = authenticationService.login(usuario);
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
                .headers(headers -> headers.setBearerAuth(token))
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }*/

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        Usuario usuarioLogin = new Usuario();
        usuarioLogin.setId(request.id());
        usuarioLogin.setPassword(request.password());

        Authentication auth = authenticationService.login(usuarioLogin);

        return generarRespuestaConTokens(auth);
    }


    /*@PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario) {
        try {
            Usuario createdUser = usuarioService.addUsuario(usuario);
            return ResponseEntity.created(null).body(createdUser);
        } catch (UsuarioDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .location(MvcUriComponentsBuilder.fromMethodName(
                                    UsuarioController.class,
                                    "getUsuario",
                                    usuario.getId())
                            .build().toUri())
                    .build();
        }
    }*/

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegistroUsuarioRequest request) throws UsuarioDuplicadoException {

        // convertir dto a entidad
        Usuario usuarioParaGuardar = usuarioMapper.toEntity(request);
        Usuario usuarioGuardado = usuarioService.addUsuario(usuarioParaGuardar);
        // convertir a response
        UsuarioResponse response = usuarioMapper.toResponse(usuarioGuardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {

        Authentication auth = authenticationService.login(refreshToken);

        if (auth.getPrincipal() != null) {
            return generarRespuestaConTokens(auth);
        }

        throw new RefreshTokenInvalidoException(refreshToken);
    }


    @PostMapping("logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
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