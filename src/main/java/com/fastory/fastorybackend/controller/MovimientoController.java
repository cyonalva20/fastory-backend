package com.fastory.fastorybackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.*;
import com.fastory.fastorybackend.service.MovimientoService;

import org.springframework.security.core.Authentication; // 🔹 Import necesario
import jakarta.validation.Valid;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movimientos")
@lombok.RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    // -------------------------------------------------------------------------
    // --- ENDPOINTS DE BÚSQUEDA Y SALIDAS ---
    // -------------------------------------------------------------------------

    /**
     * Busca productos por nombre.
     * URL: GET /api/v1/movimientos/productos/buscar?nombre=...
     */
    @GetMapping("/productos/buscar")
    public ResponseEntity<List<ProductoBusquedaDto>> buscarProductos(
            @RequestParam(required = false, defaultValue = "") String nombre) {
        return ResponseEntity.ok(movimientoService.buscarProductosPorNombre(nombre));
    }

    /**
     * Registra una nueva salida.
     * URL: POST /api/v1/movimientos/salidas
     */
    @PostMapping("/salidas")
    public ResponseEntity<Object> registrarSalida(@RequestBody RegistroSalidaDto salidaDto,
            Authentication authentication) {
        try {
            // 🔹 Obtenemos el username del contexto de seguridad
            String username = authentication.getName();

            // 🔹 Llamamos al servicio pasando el username
            movimientoService.registrarSalida(salidaDto, username);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Salida registrada exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene el historial de salidas.
     * URL: GET /api/v1/movimientos/salidas/historial
     */
    @GetMapping("/salidas/historial")
    public ResponseEntity<List<MovimientoHistorialDto>> obtenerHistorialDeSalidas() {
        return ResponseEntity.ok(movimientoService.obtenerHistorialDeSalidas());
    }

    // -------------------------------------------------------------------------
    // --- ENDPOINTS DE ENTRADAS (NUEVOS) ---
    // -------------------------------------------------------------------------


    /**
     * Registra una nueva entrada de mercadería.
     * URL: POST /api/v1/movimientos/entradas
     */
    @PostMapping("/entradas")
    public ResponseEntity<Object> registrarEntrada(@RequestBody RegistroEntradaDto entradaDto,
            Authentication authentication) {
        try {
            // 🔹 Obtenemos el username del contexto de seguridad
            String username = authentication.getName();

            // 🔹 Llamamos al servicio pasando el username
            movimientoService.registrarEntrada(entradaDto, username);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Entrada registrada exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene el historial de entradas.
     * URL: GET /api/v1/movimientos/entradas/historial
     */
    @GetMapping("/entradas/historial")
    public ResponseEntity<List<MovimientoHistorialDto>> obtenerHistorialDeEntradas() {
        return ResponseEntity.ok(movimientoService.obtenerHistorialDeEntradas());
    }

    /**
     * Obtiene lista de movimientos con filtros unificados.
     * URL: GET /api/v1/movimientos?fechaInicio=...&fechaFin=...&tipo=...
     */
    @GetMapping
    public ResponseEntity<List<MovimientoHistorialDto>> listarMovimientos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(movimientoService.listarMovimientos(fechaInicio, fechaFin, tipo));
    }

    /**
     * Actualiza un movimiento existente.
     * Requiere rol ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Object> actualizarMovimiento(@PathVariable Integer id,
            @Valid @RequestBody MovimientoUpdateDto dto) {
        try {
            movimientoService.actualizarMovimiento(id, dto);
            return ResponseEntity.ok(Map.of("message", "Movimiento actualizado correctamente"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Elimina un movimiento.
     * Requiere rol ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Object> eliminarMovimiento(@PathVariable Integer id) {
        try {
            movimientoService.eliminarMovimiento(id);
            return ResponseEntity.ok(Map.of("message", "Movimiento eliminado correctamente"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // --- NUEVOS ENDPOINTS PARA AJUSTE DE INVENTARIO ---
    // -------------------------------------------------------------------------

    @PostMapping("/ajustes")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Generalmente restringido a admins
    public ResponseEntity<Object> registrarAjuste(@Valid @RequestBody AjusteInventarioDto ajusteDto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            movimientoService.registrarAjusteInventario(ajusteDto, username);
            return ResponseEntity.ok(Map.of("message", "Ajuste de inventario realizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ajustes/historial")
    public ResponseEntity<List<MovimientoHistorialDto>> obtenerHistorialAjustes() {
        return ResponseEntity.ok(movimientoService.obtenerHistorialDeAjustes());
    }
}