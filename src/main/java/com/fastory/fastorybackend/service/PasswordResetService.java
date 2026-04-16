package com.fastory.fastorybackend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.entity.PasswordResetToken;
import com.fastory.fastorybackend.entity.Usuario;
import com.fastory.fastorybackend.repository.PasswordResetTokenRepository;
import com.fastory.fastorybackend.repository.UsuarioRepository;

import java.util.Optional;
import java.util.Random;

@Service
@lombok.RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Paso 1: Generar y enviar código
    @Transactional
    public void solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            // Por seguridad, no revelamos si el email existe o no,
            // pero internamente retornamos.
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // Eliminar token anterior si existe
        tokenRepository.findByUsuario(usuario).ifPresent(tokenRepository::delete);

        // Generar código 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Guardar token (15 min expiración)
        PasswordResetToken token = new PasswordResetToken(codigo, usuario, 15);
        tokenRepository.save(token);

        // Enviar email
        emailService.enviarCodigoRecuperacion(email, codigo);
    }

    // Paso 2: Validar código
    public boolean validarCodigo(String email, String codigo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty())
            return false;

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByUsuario(usuarioOpt.get());

        if (tokenOpt.isEmpty())
            return false;

        PasswordResetToken token = tokenOpt.get();

        if (!token.getToken().equals(codigo))
            return false;
        if (token.estaExpirado())
            return false;

        return true;
    }

    // Paso 3: Cambiar contraseña
    @Transactional
    public void cambiarContrasena(String email, String codigo, String nuevaPassword) {
        if (!validarCodigo(email, codigo)) {
            throw new IllegalArgumentException("Código inválido o expirado");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        // Eliminar el token usado
        tokenRepository.deleteByUsuario(usuario);
    }
}