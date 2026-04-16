package com.fastory.fastorybackend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class DevolucionListDto {
    private Integer idDevolucion;
    private String nombreProducto;
    private String codigoLote;
    private Integer cantidad;
    private String nombreProveedor;
    private String fechaRecepcion; // Formateado
    private String estado;

    public DevolucionListDto(Integer idDevolucion, String nombreProducto, String codigoLote, Integer cantidad,
            String nombreProveedor, LocalDate fecha, LocalTime hora, String estado) {
        this.idDevolucion = idDevolucion;
        this.nombreProducto = nombreProducto;
        this.codigoLote = codigoLote;
        this.cantidad = cantidad;
        this.nombreProveedor = nombreProveedor;
        this.fechaRecepcion = fecha + " " + hora;
        this.estado = estado;
    }

    // Getters y Setters generados
    public Integer getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(Integer idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

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

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(String fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}