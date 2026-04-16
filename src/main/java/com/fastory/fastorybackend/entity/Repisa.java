package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "repisa")
public class Repisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repisa")
    private Integer idRepisa;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @OneToMany(mappedBy = "repisa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ubicacion> ubicaciones;

    public Integer getIdRepisa() {
        return idRepisa;
    }

    public void setIdRepisa(Integer idRepisa) {
        this.idRepisa = idRepisa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Ubicacion> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<Ubicacion> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }
}
