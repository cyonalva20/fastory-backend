package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import java.util.List;

@Entity
@Table(name = "repisa", uniqueConstraints = {
        @UniqueConstraint(name = "uq_repisa_tenant", columnNames = {"id_empresa", "codigo"})
})
@Filter(name = "tenantFilter", condition = "id_empresa = :empresaId")
public class Repisa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repisa")
    private Integer idRepisa;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @OneToMany(mappedBy = "repisa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ubicacion> ubicaciones;

    // --- Getters y Setters ---

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

    public List<Ubicacion> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<Ubicacion> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }
}
