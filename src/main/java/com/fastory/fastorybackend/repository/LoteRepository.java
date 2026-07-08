package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.entity.Lote;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer> {
    List<Lote> findByProductoIdProducto(Integer idProducto);

    @Query("SELECT l FROM Lote l WHERE l.producto.idProducto = :idProducto AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC, l.fechaRegistro ASC")
    List<Lote> findLotesDisponiblesParaSalida(Integer idProducto);

    // Para No Perecibles: Lote con mayor cantidad primero (según requerimiento de
    // negocio)
    @Query("SELECT l FROM Lote l WHERE l.producto.idProducto = :idProducto AND l.cantidad > 0 ORDER BY l.cantidad DESC")
    List<Lote> findLotesPorCantidadDesc(@Param("idProducto") Integer idProducto);
}
