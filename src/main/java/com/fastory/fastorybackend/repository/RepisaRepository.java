package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.entity.Repisa;

import java.util.Optional;

@Repository
public interface RepisaRepository extends JpaRepository<Repisa, Integer> {

    Optional<Repisa> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
