package com.fastory.fastorybackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MovimientoHistorialDto {
    private Integer idMovimiento;
    private Integer idUsuario;
    private String tipoMovimiento;
    private String motivo;
    private LocalDateTime fechaMovimiento;
    private String nombreUsuario;
    private List<DetalleHistorialDto> detalles;
    private Double totalGeneral; // <-- AÑADIDO

    // Getters y Setters
    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public List<DetalleHistorialDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleHistorialDto> detalles) {
        this.detalles = detalles;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    // --- NUEVO GETTER Y SETTER ---
    public Double getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(Double totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}