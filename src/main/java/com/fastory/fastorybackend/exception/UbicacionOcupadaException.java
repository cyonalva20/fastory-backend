package com.fastory.fastorybackend.exception;

/**
 * Excepción lanzada cuando se intenta asignar una ubicación que está ocupada.
 * Contiene información del producto que actualmente ocupa la ubicación.
 */
public class UbicacionOcupadaException extends RuntimeException {

    private Integer idProductoOcupante;
    private String nombreProductoOcupante;

    public UbicacionOcupadaException(String message, Integer idProductoOcupante, String nombreProductoOcupante) {
        super(message);
        this.idProductoOcupante = idProductoOcupante;
        this.nombreProductoOcupante = nombreProductoOcupante;
    }

    public Integer getIdProductoOcupante() {
        return idProductoOcupante;
    }

    public String getNombreProductoOcupante() {
        return nombreProductoOcupante;
    }
}