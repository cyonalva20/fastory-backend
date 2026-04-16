package com.fastory.fastorybackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.DevolucionCreateDto;
import com.fastory.fastorybackend.dto.DevolucionListDto;
import com.fastory.fastorybackend.service.impl.DevolucionServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/devoluciones")
@lombok.RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionServiceImpl devolucionService;

    @PostMapping
    public ResponseEntity<Object> registrarDevolucion(@Valid @RequestBody DevolucionCreateDto dto) {
        try {
            devolucionService.registrarSolicitud(dto);
            return ResponseEntity.ok(Map.of("message",
                    "Solicitud de devolución registrada. El stock se repondrá automáticamente en la fecha indicada."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<DevolucionListDto>> listarPendientes() {
        return ResponseEntity.ok(devolucionService.listarPendientes());
    }
}