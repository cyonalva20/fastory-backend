package com.fastory.fastorybackend.dto;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) utilizado para transportar los datos de
 * la tabla de reporte de stock desde el servicio al controlador.
 */
public class ReporteDto implements Serializable {

    private Integer idProducto;
    private String nombreProducto;
    private String categoria;
    private String marca; // Campo simple (String)
    private String ubicacion; // Combinación de repisa, fila y columna
    private Integer stockDisponible;
    private Integer stockMinimo;

    /**
     * Constructor para inicializar todos los campos del reporte.
     */
    public ReporteDto(Integer idProducto, String nombreProducto, String categoria, String marca, String ubicacion,
            Integer stockDisponible, Integer stockMinimo) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.marca = marca;
        this.ubicacion = ubicacion;
        this.stockDisponible = stockDisponible;
        this.stockMinimo = stockMinimo;
    }

    // Constructor vacío requerido por algunos frameworks (e.g., Jackson/Spring)
    public ReporteDto() {
    }

    // --- Getters y Setters ---

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}