package com.fastory.fastorybackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.UsuarioDto;
import com.fastory.fastorybackend.dto.UsuarioRequestDto;
import com.fastory.fastorybackend.service.UsuarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@lombok.RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDto>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuariosDto());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Solo admins pueden crear
    public ResponseEntity<Object> crearUsuario(@Valid @RequestBody UsuarioRequestDto request) {
        try {
            UsuarioDto nuevoUsuario = usuarioService.crearUsuario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Object> actualizarUsuario(@PathVariable Integer id,
            @Valid @RequestBody UsuarioRequestDto request) {
        try {
            UsuarioDto actualizado = usuarioService.actualizarUsuario(id, request);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Object> eliminarUsuario(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo eliminar el usuario"));
        }
    }
}