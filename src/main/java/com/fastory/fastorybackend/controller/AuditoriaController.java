package com.fastory.fastorybackend.controller;

import com.fastory.fastorybackend.config.TenantUserDetails;
import com.fastory.fastorybackend.entity.Auditoria;
import com.fastory.fastorybackend.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@lombok.RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    /**
     * Obtiene todos los registros de auditoría de la empresa.
     * Solo ADMINISTRADOR.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Auditoria>> obtenerTodas() {
        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(auditoriaService.obtenerTodas(userDetails.getIdEmpresa()));
    }

    /**
     * Filtra registros de auditoría por usuario.
     * Solo ADMINISTRADOR.
     */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Auditoria>> obtenerPorUsuario(@PathVariable Integer idUsuario) {
        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(auditoriaService.obtenerPorUsuario(userDetails.getIdEmpresa(), idUsuario));
    }

    /**
     * Filtra registros de auditoría por tabla afectada.
     * Solo ADMINISTRADOR.
     */
    @GetMapping("/tabla/{tabla}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Auditoria>> obtenerPorTabla(@PathVariable String tabla) {
        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(auditoriaService.obtenerPorTabla(userDetails.getIdEmpresa(), tabla));
    }
}
