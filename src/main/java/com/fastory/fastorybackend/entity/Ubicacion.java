package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ubicacion", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ubicacion_tenant", columnNames = {"id_repisa", "fila", "columna"})
})
public class Ubicacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer idUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repisa", nullable = false)
    private Repisa repisa;

    @Column(name = "fila", nullable = false)
    private Integer fila;

    @Column(name = "columna", nullable = false)
    private Integer columna;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUbicacion estado = EstadoUbicacion.LIBRE;

    public Ubicacion() {
    }

    // --- Getters y Setters ---

    public Integer getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Integer idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public Repisa getRepisa() {
        return repisa;
    }

    public void setRepisa(Repisa repisa) {
        this.repisa = repisa;
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

    public EstadoUbicacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoUbicacion estado) {
        this.estado = estado;
    }
}
