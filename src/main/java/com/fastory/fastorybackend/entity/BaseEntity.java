package com.fastory.fastorybackend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.OffsetDateTime;

/**
 * Superclase mapeada para todas las entidades multi-tenant.
 * Contiene la relación con Empresa (tenant) y la marca de auditoría (updated_at).
 * Todas las entidades excepto Empresa y Rol deben heredar de esta clase.
 *
 * @FilterDef declarado aquí para que esté disponible globalmente.
 * La activación del filtro se realiza en el interceptor AOP de cada request.
 */
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "empresaId", type = Integer.class))
public abstract class BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onBaseCreate() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
        if (empresa == null) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.fastory.fastorybackend.config.TenantUserDetails userDetails) {
                Empresa e = new Empresa();
                e.setIdEmpresa(userDetails.getIdEmpresa());
                this.empresa = e;
            }
        }
    }

    @PreUpdate
    protected void onBaseUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // --- Getters y Setters ---

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    /**
     * Helper de conveniencia — devuelve el ID de la empresa sin forzar carga completa.
     * REGLA DE ORO: No eliminar hasta que todos los servicios migren a getEmpresa().
     */
    public Integer getIdEmpresa() {
        return empresa != null ? empresa.getIdEmpresa() : null;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
