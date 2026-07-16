package com.fastory.fastorybackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.config.JwtUtil;
import com.fastory.fastorybackend.dto.LoginRequest;
import com.fastory.fastorybackend.dto.LoginResponse;
import com.fastory.fastorybackend.entity.Rol;
import com.fastory.fastorybackend.entity.Usuario;
import com.fastory.fastorybackend.repository.RolRepository;
import com.fastory.fastorybackend.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1Autenticar credenciales
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String token = jwtUtil.generarToken(usuario.getUsername(), usuario.getRol().getNombreRol());

            LoginResponse response = new LoginResponse(
                    "Inicio de sesión correcto",
                    usuario.getRol().getNombreRol(),
                    token,
                    usuario.getIdUsuario() // <-- AÑADIDO
            );

            // Log estructurado para Grafana (Ingreso exitoso)
            MDC.put("job", "login");
            MDC.put("user", request.getUsername());
            MDC.put("action", "User login");
            log.info("Usuario autenticado exitosamente");
            MDC.clear();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log estructurado para Grafana (Ingreso fallido)
            MDC.put("job", "login");
            MDC.put("user", request.getUsername());
            MDC.put("action", "Login failed");
            log.warn("Fallo de autenticación");
            MDC.clear();

            return ResponseEntity.status(401).body(
                    new LoginResponse("Credenciales inválidas", null, null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        try {
            // Validar datos obligatorios
            if (usuario.getUsername() == null || usuario.getPassword() == null) {
                return ResponseEntity.badRequest().body("Username y password son obligatorios");
            }

            // Validar rol
            Rol rol;
            if (usuario.getRol() != null && usuario.getRol().getIdRol() != null) {
                // Usar el idRol proporcionado en el JSON
                rol = rolRepository.findById(usuario.getRol().getIdRol())
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            } else {
                // Usar rol por defecto "USER"
                rol = rolRepository.findByNombreRol("USER")
                        .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
            }

            // Encriptar la contraseña antes de guardar
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            // Establecer relación bidireccional de manera segura
            usuario.setRol(rol);
            if (rol.getUsuarios() != null) {
                rol.getUsuarios().add(usuario);
            }

            // Guardar usuario
            usuarioRepository.save(usuario);

            return ResponseEntity.ok("Usuario registrado correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar usuario: " + e.getMessage());
        }
    }

}
