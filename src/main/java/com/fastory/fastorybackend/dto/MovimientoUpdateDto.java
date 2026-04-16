package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MovimientoUpdateDto {
    @NotNull
    private String tipoMovimiento; // "ENTRADA" o "SALIDA"

    @NotNull
    private Integer idResponsable; // ID del usuario seleccionado

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    private List<DetalleMovimientoUpdateDto> detalles;

    // Getters y Setters
    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Integer idResponsable) {
        this.idResponsable = idResponsable;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public List<DetalleMovimientoUpdateDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleMovimientoUpdateDto> detalles) {
        this.detalles = detalles;
    }
}