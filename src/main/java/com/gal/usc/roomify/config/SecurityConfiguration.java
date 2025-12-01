package com.gal.usc.roomify.config;

import com.gal.usc.roomify.filter.JWTFilter;
import com.gal.usc.roomify.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.security.*;

@Configuration
@EnableMethodSecurity()
public class SecurityConfiguration {
    JWTFilter jwtFilter;
    AuthenticationService authenticationService;

    @Autowired
    public SecurityConfiguration(JWTFilter jwtFilter,  AuthenticationService authenticationService) {
        this.jwtFilter = jwtFilter;
        this.authenticationService = authenticationService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize ->
                        authorize
                                // 1. Rutas de autenticación
                                .requestMatchers("/auth/**").permitAll()
                                // 2. Rutas de documentación (Scalar)
                                .requestMatchers("/scalar/**", "/v3/api-docs/**", "/docs/**").permitAll()
                                // 3. El resto requiere login
                                .anyRequest().authenticated()

                )
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(jwtFilter, BasicAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return authenticationService.loadRoleHierarchy();
    }
}
