package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.entity.PasswordResetToken;
import com.fastory.fastorybackend.entity.Usuario;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario); // Para limpiar tokens viejos
}