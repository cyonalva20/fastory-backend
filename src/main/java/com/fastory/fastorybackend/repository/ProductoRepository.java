package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.entity.Producto;
import com.fastory.fastorybackend.entity.Ubicacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {
    Optional<Producto> findByNombreProducto(String nombreProducto);

    List<Producto> findByNombreProductoContainingIgnoreCase(String nombre);

    boolean existsByNombreProducto(String nombreProducto);

    List<Producto> findTop5ByOrderByNombreProductoAsc();

    // findByProveedorIdProveedor eliminado (Producto ya no tiene relación con Proveedor)

    Optional<Producto> findByUbicacion(Ubicacion ubicacion);

    // --- CONSULTA PARA ALERTAS REALES ---
    @Query("SELECT p FROM Producto p " +
            "LEFT JOIN p.lotes l " +
            "GROUP BY p " +
            "HAVING COALESCE(SUM(l.cantidad), 0) <= p.stockMinimo " +
            "ORDER BY COALESCE(SUM(l.cantidad), 0) ASC")
    List<Producto> findProductosConStockCriticoCalculado();
}