package com.fastory.fastorybackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@lombok.RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SuscripcionFilter suscripcionFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 🔸 OPTIONS PRIMERO
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔸 Autenticación y Recuperación
                        .requestMatchers("/auth/**").permitAll() // Incluye /auth/recovery/**

                        // 🔸 Endpoints de API
                        .requestMatchers("/api/v1/categorias/**").authenticated()
                        .requestMatchers("/api/v1/proveedores/**").authenticated()
                        .requestMatchers("/api/v1/ubicaciones/**").authenticated()
                        .requestMatchers("/api/v1/movimientos/**").authenticated()
                        .requestMatchers("/api/v1/devoluciones/pendientes").authenticated()
                        .requestMatchers("/api/v1/devoluciones").authenticated()
                        .requestMatchers("/api/v1/reportes/**").authenticated()
                        .requestMatchers("/api/v1/suscripcion/**").authenticated()

                        // 🔸 Usuarios y Roles
                        .requestMatchers("/api/v1/usuarios/**").authenticated()
                        .requestMatchers("/api/v1/roles/**").authenticated()
                        .requestMatchers("/api/v1/devoluciones/**").authenticated()
                        .requestMatchers("/api/productos/**").authenticated()

                        // 🔸 Todo lo demás requiere autenticación
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(suscripcionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://fastory.com"));
        cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cors.setAllowedHeaders(Arrays.asList("*"));
        cors.setAllowCredentials(true);
        cors.setExposedHeaders(Arrays.asList("Authorization"));
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}