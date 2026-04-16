package com.fastory.fastorybackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.ProveedorCreateDto;
import com.fastory.fastorybackend.dto.ProveedorDto;
import com.fastory.fastorybackend.entity.Proveedor;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.service.ProveedorService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/proveedores")
@lombok.RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * Endpoint para obtener la lista SIMPLE de Proveedores
     * Es el endpoint principal que ya tienes en tu frontend.
     * URL: GET /api/v1/proveedores
     */
    @GetMapping
    public List<Proveedor> listarProveedores() {
        return proveedorService.obtenerTodas();
    }

    /**
     * Endpoint para obtener la lista COMPLETA de proveedores con el conteo de
     * productos.
     * URL: GET /api/v1/proveedores/con-conteo
     */
    @GetMapping("/con-conteo")
    public ResponseEntity<List<ProveedorDto>> listarProveedoresConConteo() {
        List<ProveedorDto> proveedores = proveedorService.obtenerTodasConConteo();
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Endpoint para crear un nuevo proveedor.
     * URL: POST /api/v1/proveedores
     */
    @PostMapping
    public ResponseEntity<Object> crearProveedor(@Valid @RequestBody ProveedorCreateDto createDto) {
        try {
            ProveedorDto nuevoProveedor = proveedorService.crearProveedor(createDto);
            return new ResponseEntity<>(nuevoProveedor, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar un proveedor existente.
     * URL: PUT /api/v1/proveedores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizarProveedor(@PathVariable Integer id,
            @Valid @RequestBody ProveedorCreateDto updateDto) {
        try {
            ProveedorDto proveedorActualizado = proveedorService.actualizarProveedor(id, updateDto);
            return ResponseEntity.ok(proveedorActualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un proveedor.
     * URL: DELETE /api/v1/proveedores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminarProveedor(@PathVariable Integer id) {
        try {
            proveedorService.eliminarProveedor(id);
            return ResponseEntity.noContent().build(); // HTTP 204
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            // Se usa 409 Conflict cuando una regla de negocio impide la acción
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}
