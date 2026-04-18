package com.fastory.fastorybackend.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom UserDetails que extiende el User de Spring Security
 * para exponer el idEmpresa (tenant) en el SecurityContext.
 *
 * Uso desde cualquier punto de la aplicación:
 * <pre>
 * TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
 *     .getContext()
 *     .getAuthentication()
 *     .getPrincipal();
 * Integer idEmpresa = userDetails.getIdEmpresa();
 * </pre>
 */
public class TenantUserDetails extends User {

    private final Integer idEmpresa;
    private final Integer idUsuario;

    public TenantUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                             Integer idEmpresa, Integer idUsuario) {
        super(username, password, authorities);
        this.idEmpresa = idEmpresa;
        this.idUsuario = idUsuario;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }
}
