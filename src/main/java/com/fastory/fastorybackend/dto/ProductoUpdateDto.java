package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO específico para actualizar un producto.
 * Solo incluye los campos que se pueden modificar desde la tabla principal.
 */
public class ProductoUpdateDto {

    @NotBlank(message = "Debe ingresar un nombre para el producto")
    @Size(max = 100, message = "El nombre del producto no debe superar los 100 caracteres")
    private String nombreProducto;

    @NotNull(message = "El precio de compra es obligatorio")
    @Positive(message = "El precio de compra debe ser mayor a cero")
    private Double precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor al precio de compra")
    private Double precioVenta;

    @NotNull(message = "Debe seleccionar una categoría")
    private Integer idCategoria;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Positive(message = "El stock mínimo debe ser mayor a cero")
    private Integer stockMinimo;

    @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
    private String descripcion;

    // --- CAMPO AÑADIDO ---
    @Size(max = 100, message = "La marca no debe superar los 100 caracteres.")
    private String marca;
    // --------------------

    private Integer idRepisa;

    @Positive(message = "La fila debe ser un número positivo")
    private Integer fila;

    @Positive(message = "La columna debe ser un número positivo")
    private Integer columna;

    // --- NUEVO CAMPO ---
    private Boolean forzarCambioUbicacion; // Flag para desasignar ubicación del producto anterior

    // Getters y Setters

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
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

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // --- GETTER Y SETTER AÑADIDOS ---
    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getIdRepisa() {
        return idRepisa;
    }

    public void setIdRepisa(Integer idRepisa) {
        this.idRepisa = idRepisa;
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

    // --- NUEVO GETTER Y SETTER ---
    public Boolean getForzarCambioUbicacion() {
        return forzarCambioUbicacion;
    }

    public void setForzarCambioUbicacion(Boolean forzarCambioUbicacion) {
        this.forzarCambioUbicacion = forzarCambioUbicacion;
    }
}