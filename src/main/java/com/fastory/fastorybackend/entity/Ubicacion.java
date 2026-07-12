package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ubicacion", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_repisa", "fila", "columna" })
})
public class Ubicacion {

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

    @Column(name = "estado", length = 20)
    private String estado;

    public Ubicacion() {
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
