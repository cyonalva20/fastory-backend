package com.fastory.fastorybackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.FiltrosDto;
import com.fastory.fastorybackend.dto.ProductoDTO;
import com.fastory.fastorybackend.dto.ProductoDetalleDto;
import com.fastory.fastorybackend.dto.ProductoInventarioDto;
import com.fastory.fastorybackend.dto.ProductoUpdateDto;
import com.fastory.fastorybackend.entity.Categoria;
import com.fastory.fastorybackend.entity.Producto;
import com.fastory.fastorybackend.entity.Ubicacion;
import com.fastory.fastorybackend.entity.Empresa;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.exception.UbicacionOcupadaException;
import com.fastory.fastorybackend.service.ProductoService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.fastory.fastorybackend.config.TenantUserDetails;
import com.fastory.fastorybackend.repository.EmpresaRepository;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@lombok.RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final EmpresaRepository empresaRepository;

    @PostMapping("/registrar")
    public ResponseEntity<Object> registrarProducto(@Valid @RequestBody ProductoDTO productoDTO, Authentication authentication) {
        try {
            // Validación de duplicado
            boolean existe = productoService.existePorNombre(productoDTO.getNombreProducto());
            if (existe) {
                return ResponseEntity.badRequest()
                        .body("Ya existe un producto con ese nombre");
            }
            if (productoDTO.getPrecioCompra().compareTo(productoDTO.getPrecioVenta()) >= 0) {
                return ResponseEntity.badRequest()
                        .body("El precio de venta debe ser mayor al precio de compra");
            }

            // Mapeo manual del DTO → Entidad
            Producto producto = new Producto();
            producto.setNombreProducto(productoDTO.getNombreProducto());
            // descripcionProducto eliminada del esquema
            producto.setUnidadMedida(productoDTO.getUnidadMedida());
            producto.setPrecioCompra(productoDTO.getPrecioCompra());
            producto.setPrecioVenta(productoDTO.getPrecioVenta());
            producto.setStock(productoDTO.getStock() != null ? productoDTO.getStock() : 0);
            producto.setStockMinimo(productoDTO.getStockMinimo());
            producto.setEsPerecible(productoDTO.isPerecible());
            // fechaVencimiento se maneja a nivel de lote, no de producto
            // marca eliminada del esquema

            // Set categoría (solo ID)
            Categoria categoria = new Categoria();
            categoria.setIdCategoria(productoDTO.getIdCategoria());
            producto.setCategoria(categoria);
            // Proveedor eliminado de Producto en el nuevo esquema

            // Asignar ubicación (opcional según esquema)
            if (productoDTO.getIdUbicacion() != null) {
                Ubicacion ubicacion = new Ubicacion();
                ubicacion.setIdUbicacion(productoDTO.getIdUbicacion());
                producto.setUbicacion(ubicacion);
            }

            TenantUserDetails userDetails = (TenantUserDetails) authentication.getPrincipal();
            Empresa empresa = empresaRepository.findById(userDetails.getIdEmpresa())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            producto.setEmpresa(empresa);

            productoService.guardar(producto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Producto registrado correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo registrar el producto, intente nuevamente");
        }
    }

    // --- ENDPOINTS PARA EL PANEL PRINCIPAL (Index.tsx) ---

    @GetMapping("/inventario")
    public ResponseEntity<List<ProductoInventarioDto>> getInventario(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String repisa,
            @RequestParam(required = false) Integer fila,
            @RequestParam(required = false) Integer columna,
            @RequestParam(defaultValue = "nombreProducto") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<ProductoInventarioDto> productos = productoService.getInventario(
                nombre, categoriaId, repisa, fila, columna, sortBy, sortDir);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<ProductoInventarioDto>> getAlertasStock() {
        List<ProductoInventarioDto> alertas = productoService.obtenerAlertasStock();
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/filtros")
    public ResponseEntity<FiltrosDto> getFiltrosInventario() {
        FiltrosDto filtros = productoService.getFiltrosInventario();
        return ResponseEntity.ok(filtros);
    }

    @GetMapping("/detalles/{id}")
    public ResponseEntity<ProductoDetalleDto> getProductoDetalle(@PathVariable Integer id) {
        ProductoDetalleDto detalle = productoService.getProductoDetalle(id);
        return ResponseEntity.ok(detalle);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Object> actualizarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoUpdateDto dto,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado.");
        }

        try {
            String username = authentication.getName();
            ProductoDetalleDto productoActualizado = productoService.actualizarProductoParcial(id, dto, username);
            return ResponseEntity.ok(productoActualizado);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (UbicacionOcupadaException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "UBICACION_OCUPADA");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("idProductoOcupante", e.getIdProductoOcupante());
            errorResponse.put("nombreProductoOcupante", e.getNombreProductoOcupante());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el producto: " + e.getMessage());
        }
    }
}
