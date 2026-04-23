package com.fastory.fastorybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fastory.fastorybackend.dto.ReportesAnaliticosDto;
import com.fastory.fastorybackend.entity.DetalleMovimiento;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Integer> {

        // 1. Productos más vendidos
        @Query("SELECT p.nombreProducto as nombreProducto, c.nombreCategoria as categoria, SUM(d.cantidad) as cantidadVendida "
                        +
                        "FROM DetalleMovimiento d " +
                        "JOIN d.producto p " +
                        "JOIN p.categoria c " +
                        "JOIN d.movimientoInventario m " +
                        "WHERE m.tipoMovimiento = 'SALIDA' " +
                        "AND m.fechaMovimiento BETWEEN :inicio AND :fin " +
                        "GROUP BY p.idProducto, p.nombreProducto, c.nombreCategoria " +
                        "ORDER BY cantidadVendida DESC")
        List<ReportesAnaliticosDto.ProductoMasVendidoProjection> findProductosMasVendidos(
                        @Param("inicio") OffsetDateTime inicio,
                        @Param("fin") OffsetDateTime fin);

        // 2. Productos con baja rotación (Vendidos poco o nada, pero con stock > 0)
        @Query("SELECT p.nombreProducto as nombreProducto, p.stock as stockActual, COALESCE(SUM(d.cantidad), 0) as cantidadVendida "
                        +
                        "FROM Producto p " +
                        "LEFT JOIN DetalleMovimiento d ON d.producto.idProducto = p.idProducto " +
                        "LEFT JOIN d.movimientoInventario m ON m.idMovimiento = d.movimientoInventario.idMovimiento " +
                        "AND m.tipoMovimiento = 'SALIDA' " +
                        "AND m.fechaMovimiento BETWEEN :inicio AND :fin " +
                        "WHERE p.stock > 0 " +
                        "GROUP BY p.idProducto, p.nombreProducto, p.stock " +
                        "HAVING COALESCE(SUM(d.cantidad), 0) <= :umbral " +
                        "ORDER BY cantidadVendida ASC")
        List<ReportesAnaliticosDto.ProductoBajaRotacionProjection> findProductosBajaRotacion(
                        @Param("inicio") OffsetDateTime inicio,
                        @Param("fin") OffsetDateTime fin,
                        @Param("umbral") Long umbral);

        // 3. Entradas por proveedor (usando el motivo que guarda el nombre)
        @Query("SELECT p.nombreProducto as nombreProducto, l.codigoLote as codigoLote, d.cantidad as cantidadRecibida, m.fechaMovimiento as fechaEntrada "
                        +
                        "FROM DetalleMovimiento d " +
                        "JOIN d.movimientoInventario m " +
                        "JOIN d.producto p " +
                        "LEFT JOIN d.lote l " +
                        "WHERE m.tipoMovimiento = 'ENTRADA' " +
                        "AND m.motivo LIKE CONCAT('%', :proveedorMotivo, '%') " +
                        "AND m.fechaMovimiento BETWEEN :inicio AND :fin " +
                        "ORDER BY m.fechaMovimiento DESC")
        List<ReportesAnaliticosDto.EntradaProveedorProjection> findEntradasPorProveedor(
                        @Param("proveedorMotivo") String proveedorMotivo,
                        @Param("inicio") OffsetDateTime inicio,
                        @Param("fin") OffsetDateTime fin);

        @Query("SELECT COUNT(DISTINCT d.producto.idProducto) " +
                        "FROM DetalleMovimiento d " +
                        "JOIN d.movimientoInventario m " +
                        "WHERE m.tipoMovimiento = 'ENTRADA' " +
                        "AND m.motivo LIKE CONCAT('%', :proveedorMotivo, '%')")
        Integer countDistinctProductosByProveedorMotivo(@Param("proveedorMotivo") String proveedorMotivo);

}
