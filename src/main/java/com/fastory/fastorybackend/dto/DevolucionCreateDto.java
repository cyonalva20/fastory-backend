package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class DevolucionCreateDto {

    @NotNull
    private Integer idProducto;

    @NotNull
    private Integer idLote; // ID del lote que se va a devolver

    @NotNull
    private Integer cantidad;

    @NotNull
    private LocalDate fechaRecepcion;

    @NotNull
    private LocalTime horaRecepcion;

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public LocalTime getHoraRecepcion() {
        return horaRecepcion;
    }

    public void setHoraRecepcion(LocalTime horaRecepcion) {
        this.horaRecepcion = horaRecepcion;
    }
}