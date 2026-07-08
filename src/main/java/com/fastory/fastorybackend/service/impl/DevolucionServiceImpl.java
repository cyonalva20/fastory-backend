package com.fastory.fastorybackend.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.DevolucionCreateDto;
import com.fastory.fastorybackend.dto.DevolucionListDto;
import com.fastory.fastorybackend.entity.*;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class DevolucionServiceImpl {

    private final DevolucionRepository devolucionRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;

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

        // 2. Reducir el stock del lote "malo" inmediatamente (se aparta para
        // devolución)
        // Y reducir stock global del producto
        lote.setCantidad(lote.getCantidad() - dto.getCantidad());
        loteRepository.save(lote);

        producto.setStock(producto.getStock() - dto.getCantidad());
        productoRepository.save(producto);

        // 3. Crear registro de Devolución
        Devolucion devolucion = new Devolucion();
        devolucion.setProducto(producto);
        devolucion.setProveedor(producto.getProveedor()); // Asumimos proveedor actual del producto
        devolucion.setCodigoLoteVencido(lote.getCodigoLote());
        devolucion.setCantidad(dto.getCantidad());
        devolucion.setFechaRecepcionProgramada(dto.getFechaRecepcion());
        devolucion.setHoraRecepcionProgramada(dto.getHoraRecepcion());
        devolucion.setEstado("PENDIENTE");

        devolucionRepository.save(devolucion);
    }

    @Transactional(readOnly = true)
    public List<DevolucionListDto> listarPendientes() {
        return devolucionRepository.findByEstadoOrderByFechaRecepcionProgramadaAsc("PENDIENTE").stream()
                .map(d -> new DevolucionListDto(
                        d.getIdDevolucion(),
                        d.getProducto().getNombreProducto(),
                        d.getCodigoLoteVencido(),
                        d.getCantidad(),
                        d.getProveedor() != null ? d.getProveedor().getNombreProveedor() : "Sin Proveedor",
                        d.getFechaRecepcionProgramada(),
                        d.getHoraRecepcionProgramada(),
                        d.getEstado()))
                .collect(Collectors.toList());
    }

    /**
     * TAREA AUTOMÁTICA:
     * Se ejecuta cada minuto (o el intervalo que prefieras) para verificar
     * si ya pasamos la fecha de recepción. Si es así, procesa el canje.
     */
    @Scheduled(fixedRate = 60000) // Cada 60 segundos
    @Transactional
    public void procesarDevolucionesAutomaticas() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Devolucion> devolucionesAProcesar = devolucionRepository.findDevolucionesParaProcesar(hoy, ahora);

        for (Devolucion devolucion : devolucionesAProcesar) {
            System.out.println("Procesando canje automático para devolución ID: " + devolucion.getIdDevolucion());

            // 1. Crear nuevo lote de reemplazo (Canje)
            Producto producto = devolucion.getProducto();
            Lote nuevoLote = new Lote();
            nuevoLote.setProducto(producto);
            nuevoLote.setCantidad(devolucion.getCantidad());

            // REGLA DE NEGOCIO: Fecha vencimiento = Actual + 1 mes
            nuevoLote.setFechaVencimiento(LocalDateTime.now().plusMonths(1));

            nuevoLote.setCodigoLote("CANJE-" + devolucion.getCodigoLoteVencido() + "-R");
            nuevoLote.setFechaRegistro(LocalDateTime.now());

            // Opcional: Crear un movimiento de entrada tipo "CANJE" para historial
            // (Omitido aquí para brevedad, pero recomendado en producción)

            loteRepository.save(nuevoLote);

            // 2. Reponer stock al producto
            producto.setStock(producto.getStock() + devolucion.getCantidad());
            productoRepository.save(producto);

            // 3. Marcar devolución como completada
            devolucion.setEstado("COMPLETADA");
            devolucionRepository.save(devolucion);
        }
    }
}