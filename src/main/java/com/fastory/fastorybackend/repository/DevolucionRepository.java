package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.entity.Devolucion;

import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {

    // Buscar devoluciones por estado, ordenadas por fecha de solicitud
    List<Devolucion> findByEstadoOrderByFechaSolicitudAsc(String estado);
}