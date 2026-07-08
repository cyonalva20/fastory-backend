package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fastory.fastorybackend.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);
}
