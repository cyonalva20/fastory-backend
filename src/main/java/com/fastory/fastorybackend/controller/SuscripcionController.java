package com.fastory.fastorybackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastory.fastorybackend.config.TenantUserDetails;
import com.fastory.fastorybackend.dto.SuscripcionEstadoDto;
import com.fastory.fastorybackend.service.SuscripcionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/suscripcion")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @GetMapping("/estado")
    public ResponseEntity<SuscripcionEstadoDto> obtenerEstado() {
        Integer idEmpresa = obtenerIdEmpresaActual();
        return ResponseEntity.ok(suscripcionService.obtenerEstado(idEmpresa));
    }

    @PostMapping("/renovar")
    public ResponseEntity<SuscripcionEstadoDto> renovar() {
        Integer idEmpresa = obtenerIdEmpresaActual();
        return ResponseEntity.ok(suscripcionService.renovar(idEmpresa));
    }

    private Integer obtenerIdEmpresaActual() {
        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getIdEmpresa();
    }
}
