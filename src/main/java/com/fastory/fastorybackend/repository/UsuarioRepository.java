package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fastory.fastorybackend.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol JOIN FETCH u.empresa WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    Optional<Usuario> findByEmail(String email);
}