package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Superclase mapeada para todas las entidades multi-tenant.
 * Contiene el identificador de tenant (id_empresa) y la marca de auditoría (updated_at).
 * Todas las entidades excepto Empresa y Rol deben heredar de esta clase.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onBaseCreate() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onBaseUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // --- Getters y Setters ---

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
