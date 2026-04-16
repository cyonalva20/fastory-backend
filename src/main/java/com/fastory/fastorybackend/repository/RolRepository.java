package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fastory.fastorybackend.entity.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByidRol(Integer idRol);

    Optional<Rol> findByNombreRol(String nombreRol);
}
