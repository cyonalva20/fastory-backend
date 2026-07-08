package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // <-- Import necesario
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

    List<Producto> findByProveedorIdProveedor(Integer idProveedor);

    Optional<Producto> findByUbicacion(Ubicacion ubicacion);

    // --- NUEVA CONSULTA PARA ALERTAS REALES ---
    /**
     * Obtiene productos donde la suma de la cantidad de sus lotes es menor o igual
     * al stock mínimo.
     * Utiliza LEFT JOIN para incluir productos que no tienen lotes (suma = 0).
     * COALESCE convierte el null (sin lotes) en 0.
     */
    @Query("SELECT p FROM Producto p " +
            "LEFT JOIN p.lotes l " +
            "GROUP BY p " +
            "HAVING COALESCE(SUM(l.cantidad), 0) <= p.stockMinimo " +
            "ORDER BY COALESCE(SUM(l.cantidad), 0) ASC")
    List<Producto> findProductosConStockCriticoCalculado();
}