package com.fastory.fastorybackend.dto;

/**
 * DTO para asignar un producto a una ubicación específica.
 */
public class AsignarUbicacionDto {

    private Integer productoId;
    private Integer repisaId;
    private Integer fila;
    private Integer columna;

    // Getters y Setters

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getRepisaId() {
        return repisaId;
    }

    public void setRepisaId(Integer repisaId) {
        this.repisaId = repisaId;
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
}
