package com.fastory.fastorybackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.PasswordResetDto;
import com.fastory.fastorybackend.service.PasswordResetService;


import java.util.Map;

@RestController
@RequestMapping("/auth/recovery")
@CrossOrigin
@lombok.RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService resetService;

    // Paso 1: Enviar Código
    @PostMapping("/send-code")
    public ResponseEntity<Object> sendCode(@RequestBody @Valid PasswordResetDto.ForgotRequest request) {
        resetService.solicitarRecuperacion(request.getEmail());
        // Siempre retornamos OK por seguridad para no revelar emails existentes
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, se ha enviado un código de verificación."));
    }

    // Paso 2: Verificar Código
    @PostMapping("/verify-code")
    public ResponseEntity<Object> verifyCode(@RequestBody @Valid PasswordResetDto.VerifyCodeRequest request) {
        boolean isValid = resetService.validarCodigo(request.getEmail(), request.getCodigo());
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "Código válido", "valid", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Código inválido o expirado", "valid", false));
        }
    }

    // Paso 3: Resetear Contraseña
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody @Valid PasswordResetDto.ChangePasswordRequest request) {
        try {
            resetService.cambiarContrasena(request.getEmail(), request.getCodigo(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}