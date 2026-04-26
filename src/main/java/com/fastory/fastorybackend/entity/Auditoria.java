package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Entidad de auditoría para registrar cambios en movimientos de inventario.
 * NO hereda de BaseEntity — tiene su propio id_empresa como Integer directo.
 */
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Integer idAuditoria;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "tabla_afectada", nullable = false, length = 50)
    private String tablaAfectada;

    @Column(name = "id_registro", nullable = false)
    private Integer idRegistro;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    private String datosNuevos;

    @Column(name = "fecha_auditoria", nullable = false)
    private OffsetDateTime fechaAuditoria;

    @PrePersist
    protected void onCreate() {
        if (fechaAuditoria == null) {
            fechaAuditoria = OffsetDateTime.now();
        }
    }

    // --- Getters y Setters ---

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    public OffsetDateTime getFechaAuditoria() {
        return fechaAuditoria;
    }

    public void setFechaAuditoria(OffsetDateTime fechaAuditoria) {
        this.fechaAuditoria = fechaAuditoria;
    }
}
