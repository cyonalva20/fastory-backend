package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.entity.Repisa;
import com.fastory.fastorybackend.entity.Ubicacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {

    // Buscar una ubicación específica dentro de una repisa
    Optional<Ubicacion> findByRepisaAndFilaAndColumna(Repisa repisa, Integer fila, Integer columna);

    // Verificar si existe una ubicación específica
    boolean existsByRepisaAndFilaAndColumna(Repisa repisa, Integer fila, Integer columna);

    // Listar todas las ubicaciones de una repisa
    List<Ubicacion> findByRepisa(Repisa repisa);

    // Métodos nuevos para calcular dinámicamente las dimensiones de una repisa
    @Query("SELECT MAX(u.fila) FROM Ubicacion u WHERE u.repisa = :repisa")
    Integer findMaxFilaByRepisa(@Param("repisa") Repisa repisa);

    @Query("SELECT MAX(u.columna) FROM Ubicacion u WHERE u.repisa = :repisa")
    Integer findMaxColumnaByRepisa(@Param("repisa") Repisa repisa);
}
