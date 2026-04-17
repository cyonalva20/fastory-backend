package com.fastory.fastorybackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.DevolucionCreateDto;
import com.fastory.fastorybackend.dto.DevolucionListDto;
import com.fastory.fastorybackend.entity.*;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class DevolucionServiceImpl {

    private final DevolucionRepository devolucionRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final ProveedorRepository proveedorRepository;

    @Transactional
    public void registrarSolicitud(DevolucionCreateDto dto) {
        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Lote lote = loteRepository.findById(dto.getIdLote())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        // 1. Validar cantidad
        if (lote.getCantidad() < dto.getCantidad()) {
            throw new IllegalArgumentException("La cantidad a devolver supera el stock del lote.");
        }

        // 2. Reducir el stock del lote inmediatamente (se aparta para devolución)
        lote.setCantidad(lote.getCantidad() - dto.getCantidad());
        loteRepository.save(lote);

        producto.setStock(producto.getStock() - dto.getCantidad());
        productoRepository.save(producto);

        // 3. Crear registro de Devolución (nuevo esquema)
        // TODO: Obtener el proveedor correcto — ahora Producto no tiene relación con Proveedor.
        // Por ahora se requiere que el DTO incluya idProveedor, o se busca el primero disponible.
        Proveedor proveedor = proveedorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No hay proveedores registrados"));

        Devolucion devolucion = new Devolucion();
        devolucion.setProducto(producto);
        devolucion.setProveedor(proveedor);
        devolucion.setCantidad(dto.getCantidad());
        devolucion.setMotivo("Devolución de lote: " + lote.getCodigoLote());
        devolucion.setEstado("PENDIENTE");

        devolucionRepository.save(devolucion);
    }

    @Transactional(readOnly = true)
    public List<DevolucionListDto> listarPendientes() {
        return devolucionRepository.findByEstadoOrderByFechaSolicitudAsc("PENDIENTE").stream()
                .map(d -> new DevolucionListDto(
                        d.getIdDevolucion(),
                        d.getProducto().getNombreProducto(),
                        null, // codigoLote - ya no se almacena en Devolucion
                        d.getCantidad(),
                        d.getProveedor() != null ? d.getProveedor().getNombreProveedor() : "Sin Proveedor",
                        d.getFechaSolicitud() != null ? d.getFechaSolicitud().toLocalDate() : null,
                        d.getFechaSolicitud() != null ? d.getFechaSolicitud().toLocalTime() : null,
                        d.getEstado()))
                .collect(Collectors.toList());
    }

    // Tarea automática de procesamiento eliminada temporalmente.
    // El nuevo esquema no tiene fechaRecepcionProgramada/horaRecepcionProgramada.
    // TODO: Redefinir lógica de procesamiento automático de devoluciones con el nuevo esquema.
}