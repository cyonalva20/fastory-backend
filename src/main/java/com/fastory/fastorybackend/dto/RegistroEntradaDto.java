package com.fastory.fastorybackend.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class RegistroEntradaDto {
    private Integer idProveedor;
    private OffsetDateTime fechaEntrada;
    private List<DetalleEntradaDto> detalles;

    // Getters y Setters
    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public OffsetDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(OffsetDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public List<DetalleEntradaDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleEntradaDto> detalles) {
        this.detalles = detalles;
    }
}
