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

import com.fastory.fastorybackend.entity.Usuario;
import com.fastory.fastorybackend.repository.RolRepository;
import com.fastory.fastorybackend.repository.UsuarioRepository;
import com.fastory.fastorybackend.service.UsuarioService;
import com.fastory.fastorybackend.dto.RegistroRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

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

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1Autenticar credenciales
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String token = jwtUtil.generarToken(usuario.getUsername(), usuario.getRol().getNombreRol(),
                    usuario.getEmpresa().getIdEmpresa(), usuario.getEmpresa().getNombreComercial());

            LoginResponse response = new LoginResponse(
                    "Inicio de sesión correcto",
                    usuario.getRol().getNombreRol(),
                    token,
                    usuario.getIdUsuario(),
                    usuario.getEmpresa().getIdEmpresa());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    new LoginResponse("Credenciales inválidas", null, null, null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroRequest request) {
        try {
            // Validar datos obligatorios básicos
            if (request.getUsername() == null || request.getPassword() == null || request.getNombreEmpresa() == null) {
                return ResponseEntity.badRequest().body("Username, password y nombre de empresa son obligatorios");
            }

            usuarioService.registrarOnboarding(request);

            return ResponseEntity.ok("Empresa y usuario administrador registrados correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar usuario: " + e.getMessage());
        }
    }

}
