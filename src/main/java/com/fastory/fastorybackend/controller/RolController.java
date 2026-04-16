package com.fastory.fastorybackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastory.fastorybackend.entity.Rol;
import com.fastory.fastorybackend.service.RolService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@lombok.RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(rolService.listarRoles());
    }
}