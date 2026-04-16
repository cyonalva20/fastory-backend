package com.fastory.fastorybackend.dto;

public class DetalleHistorialDto {
    private String nombreProducto;
    private Integer idProducto;
    private Integer cantidad;
    private Double precioVenta;
    private Double precioCompra;
    private Double subtotal;

    // --- NUEVOS CAMPOS ---
    private Integer stockAnterior;
    private Integer stockNuevo;

    // Constructor COMPLETO (Usado por Ajustes)
    public DetalleHistorialDto(Integer idProducto, String nombreProducto, Integer cantidad, Double precioVenta,
            Double precioCompra, Integer stockAnterior, Integer stockNuevo) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioVenta = (precioVenta != null) ? precioVenta : 0.0;
        this.precioCompra = (precioCompra != null) ? precioCompra : 0.0;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;

        calcularSubtotal(cantidad);
    }

    // Constructor LEGACY (Para compatibilidad con servicios existentes que no
    // envían stock)
    public DetalleHistorialDto(Integer idProducto, String nombreProducto, Integer cantidad, Double precioVenta,
            Double precioCompra) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioVenta = (precioVenta != null) ? precioVenta : 0.0;
        this.precioCompra = (precioCompra != null) ? precioCompra : 0.0;
        this.stockAnterior = null; // Opcional
        this.stockNuevo = null; // Opcional

        calcularSubtotal(cantidad);
    }

    private void calcularSubtotal(Integer cantidad) {
        double precioCalculo = 0.0;
        if (this.precioVenta != null && this.precioVenta > 0) {
            precioCalculo = this.precioVenta;
        } else if (this.precioCompra != null) {
            precioCalculo = this.precioCompra;
        }

        if (cantidad != null) {
            this.subtotal = cantidad * precioCalculo;
        } else {
            this.subtotal = 0.0;
        }
    }

    // Getters y Setters
    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    // --- NUEVOS GETTERS Y SETTERS ---
    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }
}