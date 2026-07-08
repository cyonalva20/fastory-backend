package com.fastory.fastorybackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RegistroEntradaDto {
    private Integer idProveedor;
    private LocalDateTime fechaEntrada;
    private List<DetalleEntradaDto> detalles;

    // Getters y Setters
    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public List<DetalleEntradaDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleEntradaDto> detalles) {
        this.detalles = detalles;
    }
}
