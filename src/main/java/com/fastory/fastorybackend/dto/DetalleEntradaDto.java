package com.fastory.fastorybackend.dto;

import java.time.LocalDateTime;

public class DetalleEntradaDto {
    private Integer idProducto;
    private Integer cantidad;
    private Double precioCompra;
    private Double precioVenta;
    private LocalDateTime fechaVencimiento;
    private String observacionDetalle; // <-- AÑADIDO

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getObservacionDetalle() {
        return observacionDetalle;
    } // <-- AÑADIDO (CORRIGE ERROR)

    public void setObservacionDetalle(String observacionDetalle) {
        this.observacionDetalle = observacionDetalle;
    }
}
