package com.gal.usc.roomify.service;

import com.gal.usc.roomify.repository.UsuarioRepository;
import com.gal.usc.roomify.repository.RoleRepository;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.model.Permission;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // <--- Importante
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
// 1. CAMBIO: Debe implementar UserDetailsService para que SecurityConfig lo reconozca
public class AuthenticationService implements UserDetailsService {

    private final AuthenticationManager authenticationManager;
    private final KeyPair keyPair;
    private final UsuarioRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${jwt.ttl:PT15M}")
    private Duration tokenTTL;

    @Autowired
    public AuthenticationService(
            @Lazy AuthenticationManager authenticationManager,
            KeyPair keyPair,
            UsuarioRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.keyPair = keyPair;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // En Mongo, el findById busca por la clave primaria (@Id), que en tu caso es el username
        return userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public Authentication login(String username, String password) throws AuthenticationException {
        // 3. CAMBIO: Corrección de sintaxis de getters (getId y getPassword)
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password)
        );
    }

    public String generateJWT(Authentication auth) {
        List<String> roles = auth.getAuthorities()
                .stream()
                .filter(authority -> authority instanceof SimpleGrantedAuthority)
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(auth.getName())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(tokenTTL)))
                .notBefore(Date.from(Instant.now()))
                .claim("roles", roles)
                .signWith(keyPair.getPrivate())
                .compact();
    }

    public Authentication parseJWT(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build  ()
                .parseSignedClaims(token)
                .getPayload();

        String id = claims.getSubject();
        Optional<Usuario> user = userRepository.findById(id);

        if (user.isPresent()) {
            return UsernamePasswordAuthenticationToken.authenticated(id, token, user.get().getAuthorities());
        } else {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    public RoleHierarchy loadRoleHierarchy() {
        RoleHierarchyImpl.Builder builder = RoleHierarchyImpl.withRolePrefix("");

        roleRepository.findAll().forEach(role -> {
            if (role.getIncludes() != null && !role.getIncludes().isEmpty()) {
                builder.role("ROLE_"+role.getRolename()).implies(
                        role.getIncludes().stream().map(i -> "ROLE_"+i.getRolename()).toArray(String[]::new)
                );
            }
            if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
                builder.role("ROLE_"+role.getRolename()).implies(
                        // 4. CAMBIO: Usar getName() en lugar de toString() para evitar errores
                        role.getPermissions().stream().map(Permission::getName).toArray(String[]::new)
                );
            }
        });

        return builder.build();
    }
}