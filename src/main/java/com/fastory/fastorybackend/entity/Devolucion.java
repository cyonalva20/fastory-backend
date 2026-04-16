package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "devolucion")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Integer idDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // Guardamos el código del lote vencido como referencia histórica
    // No relacionamos directamente la entidad Lote porque el lote podría ser
    // eliminado o vaciado
    @Column(name = "codigo_lote_vencido")
    private String codigoLoteVencido;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_recepcion_programada", nullable = false)
    private LocalDate fechaRecepcionProgramada;

    @Column(name = "hora_recepcion_programada", nullable = false)
    private LocalTime horaRecepcionProgramada;

    @Column(name = "estado", nullable = false) // PENDIENTE, COMPLETADA
    private String estado;

    // Getters y Setters
    public Integer getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(Integer idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String getCodigoLoteVencido() {
        return codigoLoteVencido;
    }

    public void setCodigoLoteVencido(String codigoLoteVencido) {
        this.codigoLoteVencido = codigoLoteVencido;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaRecepcionProgramada() {
        return fechaRecepcionProgramada;
    }

    public void setFechaRecepcionProgramada(LocalDate fechaRecepcionProgramada) {
        this.fechaRecepcionProgramada = fechaRecepcionProgramada;
    }

    public LocalTime getHoraRecepcionProgramada() {
        return horaRecepcionProgramada;
    }

    public void setHoraRecepcionProgramada(LocalTime horaRecepcionProgramada) {
        this.horaRecepcionProgramada = horaRecepcionProgramada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}