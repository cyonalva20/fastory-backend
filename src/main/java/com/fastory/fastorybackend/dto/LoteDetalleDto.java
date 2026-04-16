package com.fastory.fastorybackend.dto;

// DTO para la sub-tabla de lotes en el modal
public class LoteDetalleDto {
    private Integer idLote; // <--- NUEVO CAMPO AGREGADO
    private String codigoLote;
    private Integer cantidad;
    private String fechaVencimiento; // Enviamos como String formateado (ej: "2026-03-15")

    public LoteDetalleDto(Integer idLote, String codigoLote, Integer cantidad, String fechaVencimiento) {
        this.idLote = idLote;
        this.codigoLote = codigoLote;
        this.cantidad = cantidad;
        this.fechaVencimiento = fechaVencimiento;
    }

    // Getters y Setters
    public String getCodigoLote() {
        return codigoLote;
    }

    public void setCodigoLote(String codigoLote) {
        this.codigoLote = codigoLote;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }
}