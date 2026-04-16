package com.fastory.fastorybackend.dto;

import java.time.LocalDateTime;

public class ReportesAnaliticosDto {

    // DTO para Productos Más Vendidos
    public interface ProductoMasVendidoProjection {
        String getNombreProducto();

        String getCategoria();

        Long getCantidadVendida();
    }

    // DTO para Baja Rotación
    public interface ProductoBajaRotacionProjection {
        String getNombreProducto();

        Integer getStockActual();

        Long getCantidadVendida();
    }

    // DTO para Entradas por Proveedor
    public interface EntradaProveedorProjection {
        String getNombreProducto();

        String getCodigoLote();

        Integer getCantidadRecibida();

        LocalDateTime getFechaEntrada();
    }
}