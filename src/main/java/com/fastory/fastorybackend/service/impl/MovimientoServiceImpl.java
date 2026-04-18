package com.fastory.fastorybackend.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.*;
import com.fastory.fastorybackend.entity.*;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.*;
import com.fastory.fastorybackend.service.MovimientoService;
import com.fastory.fastorybackend.specification.ProductoSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProveedorRepository proveedorRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    // -------------------------------------------------------------------------
    // --- MÉTODOS DE BÚSQUEDA Y MOVIMIENTOS (Entradas y Salidas) ---
    // -------------------------------------------------------------------------

    @Override
    public List<ProductoBusquedaDto> buscarProductosPorNombre(String nombre) {
        List<Producto> productos;

        if (nombre == null || nombre.trim().isEmpty()) {
            productos = productoRepository.findAll();
        } else {
            productos = productoRepository.findByNombreProductoContainingIgnoreCase(nombre);
        }

        return productos.stream()
                .map(p -> {
                    List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto());
                    int stockReal = lotes.stream().mapToInt(Lote::getCantidad).sum();

                    return new ProductoBusquedaDto(
                            p.getEsPerecible(),
                            p.getIdProducto(),
                            p.getNombreProducto(),
                            null, // descripcionProducto eliminada del esquema
                            p.getPrecioVenta() != null ? p.getPrecioVenta().doubleValue() : 0.0,
                            stockReal
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registrarSalida(RegistroSalidaDto salidaDto, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("SALIDA");
        movimiento.setUsuario(usuario);
        movimiento.setEmpresa(usuario.getEmpresa());

        String motivoFinal = "otro".equalsIgnoreCase(salidaDto.getMotivo()) ? salidaDto.getObservacion()
                : salidaDto.getMotivo();
        movimiento.setMotivo(motivoFinal);

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        List<DetalleMovimiento> detallesAGuardar = new ArrayList<>();

        for (DetalleSalidaDto detalleDto : salidaDto.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + detalleDto.getIdProducto()));

            List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());
            int stockDisponibleEnLotes = lotes.stream().mapToInt(Lote::getCantidad).sum();

            if (stockDisponibleEnLotes < detalleDto.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente en lotes para el producto: "
                        + producto.getNombreProducto() + ". Disponible: " + stockDisponibleEnLotes);
            }

            int cantidadARetirar = detalleDto.getCantidad();
            for (Lote lote : lotes) {
                if (cantidadARetirar <= 0)
                    break;
                int cantidadDelLote = lote.getCantidad();
                int cantidadDescontada = Math.min(cantidadARetirar, cantidadDelLote);
                lote.setCantidad(cantidadDelLote - cantidadDescontada);
                cantidadARetirar -= cantidadDescontada;
                loteRepository.save(lote);
            }

            DetalleMovimiento detalleMovimiento = new DetalleMovimiento();
            detalleMovimiento.setMovimientoInventario(movimientoGuardado);
            detalleMovimiento.setProducto(producto);
            detalleMovimiento.setCantidad(detalleDto.getCantidad());
            detalleMovimiento.setPrecioVenta(producto.getPrecioVenta() != null ? producto.getPrecioVenta() : BigDecimal.ZERO);
            detalleMovimiento.setEmpresa(usuario.getEmpresa());
            detallesAGuardar.add(detalleMovimiento);
        }
        movimientoGuardado.setDetalles(detallesAGuardar);
    }

    @Override
    public List<MovimientoHistorialDto> obtenerHistorialDeSalidas() {
        return movimientoRepository.findByTipoMovimientoOrderByFechaMovimientoDesc("SALIDA").stream()
                .map(this::convertirAMovimientoHistorialDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoPorProveedorDto> obtenerProductosPorProveedor(Integer idProveedor) {
        // Relación Producto→Proveedor eliminada en el nuevo esquema.
        // TODO: Redefinir esta lógica de negocio cuando se diseñe la relación productos-proveedores.
        return List.of();
    }

    @Override
    @Transactional
    public void registrarEntrada(RegistroEntradaDto entradaDto, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // Proveedor ya no se vincula al movimiento en el nuevo esquema
        // Se mantiene la búsqueda solo para validar que existe
        Proveedor proveedor = proveedorRepository.findById(entradaDto.getIdProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor no encontrado con ID: " + entradaDto.getIdProveedor()));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("ENTRADA");
        movimiento.setUsuario(usuario);
        movimiento.setEmpresa(usuario.getEmpresa());
        movimiento.setMotivo("Entrada de proveedor: " + proveedor.getNombreProveedor());
        movimiento.setFechaMovimiento(
                entradaDto.getFechaEntrada() != null
                        ? entradaDto.getFechaEntrada().atOffset(ZoneOffset.UTC)
                        : OffsetDateTime.now());

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

        List<DetalleMovimiento> detallesAGuardar = new ArrayList<>();

        for (DetalleEntradaDto detalleDto : entradaDto.getDetalles()) {
            if (detalleDto.getCantidad() == null || detalleDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser un entero positivo.");
            }

            Producto producto = productoRepository.findById(detalleDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + detalleDto.getIdProducto()));

            if (Boolean.TRUE.equals(producto.getEsPerecible()) && detalleDto.getFechaVencimiento() == null) {
                throw new IllegalArgumentException("El producto '" + producto.getNombreProducto()
                        + "' es perecible y requiere fecha de vencimiento.");
            }

            // 1. CREACIÓN Y GUARDADO DEL LOTE
            Lote nuevoLote = new Lote();
            nuevoLote.setProducto(producto);
            nuevoLote.setCantidad(detalleDto.getCantidad());
            nuevoLote.setFechaVencimiento(
                    detalleDto.getFechaVencimiento() != null
                            ? detalleDto.getFechaVencimiento().atOffset(ZoneOffset.UTC)
                            : null);
            nuevoLote.setCodigoLote("LOTE-" + producto.getIdProducto() + "-" + System.currentTimeMillis());
            nuevoLote.setEmpresa(usuario.getEmpresa());

            Lote loteGuardado = loteRepository.save(nuevoLote);

            // 2. CREACIÓN DEL DETALLE DEL MOVIMIENTO
            DetalleMovimiento detalleMovimiento = new DetalleMovimiento();
            detalleMovimiento.setMovimientoInventario(movimientoGuardado);
            detalleMovimiento.setProducto(producto);
            detalleMovimiento.setCantidad(detalleDto.getCantidad());
            detalleMovimiento.setLote(loteGuardado);
            detalleMovimiento.setEmpresa(usuario.getEmpresa());

            detalleMovimiento.setPrecioCompra(
                    detalleDto.getPrecioCompra() != null ? BigDecimal.valueOf(detalleDto.getPrecioCompra()) : BigDecimal.ZERO);
            detalleMovimiento.setPrecioVenta(
                    detalleDto.getPrecioVenta() != null ? BigDecimal.valueOf(detalleDto.getPrecioVenta()) : BigDecimal.ZERO);

            detallesAGuardar.add(detalleMovimiento);
        }

        movimientoGuardado.setDetalles(detallesAGuardar);

        // 3. Actualización de stock y PRECIO
        for (DetalleEntradaDto detalleDto : entradaDto.getDetalles()) {
            Producto productoAfectado = productoRepository.findById(detalleDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Error interno: Producto no encontrado con ID: " + detalleDto.getIdProducto()));

            int stockActual = productoAfectado.getStock() != null ? productoAfectado.getStock() : 0;
            productoAfectado.setStock(stockActual + detalleDto.getCantidad());

            if (detalleDto.getPrecioCompra() != null && detalleDto.getPrecioCompra() > 0) {
                productoAfectado.setPrecioCompra(BigDecimal.valueOf(detalleDto.getPrecioCompra()));
            }

            productoRepository.save(productoAfectado);
        }
    }

    @Override
    public List<MovimientoHistorialDto> obtenerHistorialDeEntradas() {
        return movimientoRepository.findByTipoMovimientoOrderByFechaMovimientoDesc("ENTRADA").stream()
                .map(this::convertirAMovimientoHistorialDto)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // --- LÓGICA DEL REPORTE DE STOCK ACTUAL ---
    // -------------------------------------------------------------------------

    @Override
    public List<ReporteDto> generarReporteStockActual(
            Integer categoriaId,
            String marca,
            Boolean stockBajoMinimo,
            String sortBy,
            String sortDir) {
        Specification<Producto> spec = Specification.where(null);

        if (categoriaId != null) {
            spec = spec.and(ProductoSpecification.hasCategoria(categoriaId));
        }
        // Filtro por marca eliminado — campo 'marca' ya no existe en Producto
        if (Boolean.TRUE.equals(stockBajoMinimo)) {
            spec = spec.and(ProductoSpecification.isStockBajoMinimo());
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortBy);

        List<Producto> productos = productoRepository.findAll(spec, sort);

        return productos.stream()
                .map(p -> {
                    String nombreCategoria = p.getCategoria() != null ? p.getCategoria().getNombreCategoria()
                            : "Sin Categoría";

                    String ubicacionStr = "N/A";
                    if (p.getUbicacion() != null) {
                        Ubicacion ubicacion = p.getUbicacion();
                        String codigoRepisa = "S/R";

                        if (ubicacion.getRepisa() != null && ubicacion.getRepisa().getCodigo() != null) {
                            codigoRepisa = ubicacion.getRepisa().getCodigo();
                        }

                        ubicacionStr = String.format("Repisa: %s, Fila: %d, Columna: %d",
                                codigoRepisa,
                                ubicacion.getFila(),
                                ubicacion.getColumna());
                    }

                    int stockReal = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto())
                            .stream()
                            .mapToInt(Lote::getCantidad)
                            .sum();

                    return new ReporteDto(
                            p.getIdProducto(),
                            p.getNombreProducto(),
                            nombreCategoria,
                            null, // marca eliminada del esquema
                            ubicacionStr,
                            stockReal,
                            p.getStockMinimo());
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // --- NUEVOS MÉTODOS PARA GESTIÓN COMPLETA DE MOVIMIENTOS ---
    // -------------------------------------------------------------------------

    @Override
    public List<MovimientoHistorialDto> listarMovimientos(LocalDate fechaInicio, LocalDate fechaFin, String tipo) {
        OffsetDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC) : null;

        String tipoFiltro = (tipo != null && !tipo.equalsIgnoreCase("todos")) ? tipo : null;

        List<MovimientoInventario> movimientos = movimientoRepository.buscarPorFiltros(inicio, fin, tipoFiltro);

        return movimientos.stream()
                .map(this::convertirAMovimientoHistorialDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Integer idMovimiento) {
        MovimientoInventario movimiento = movimientoRepository.findById(idMovimiento)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        revertirImpactoLotes(movimiento);
        movimientoRepository.delete(movimiento);
    }

    @Override
    @Transactional
    public void actualizarMovimiento(Integer idMovimiento, MovimientoUpdateDto dto) {
        MovimientoInventario movimiento = movimientoRepository.findById(idMovimiento)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        revertirImpactoLotes(movimiento);

        movimiento.getDetalles().clear();

        movimiento.setTipoMovimiento(dto.getTipoMovimiento());
        movimiento.setFechaMovimiento(
                OffsetDateTime.of(dto.getFecha(), dto.getHora(), ZoneOffset.UTC));
        Usuario nuevoResponsable = usuarioRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        movimiento.setUsuario(nuevoResponsable);

        List<DetalleMovimiento> nuevosDetalles = new ArrayList<>();

        for (DetalleMovimientoUpdateDto detDto : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if ("ENTRADA".equalsIgnoreCase(dto.getTipoMovimiento())) {
                aumentarStockPorLotes(producto, detDto.getCantidad());
            } else {
                disminuirStockPorLotes(producto, detDto.getCantidad());
            }

            DetalleMovimiento nuevoDetalle = new DetalleMovimiento();
            nuevoDetalle.setMovimientoInventario(movimiento);
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(detDto.getCantidad());
            nuevoDetalle.setPrecioVenta(producto.getPrecioVenta());
            nuevoDetalle.setPrecioCompra(producto.getPrecioCompra());
            nuevoDetalle.setEmpresa(movimiento.getEmpresa());

            nuevosDetalles.add(nuevoDetalle);
        }

        movimiento.getDetalles().addAll(nuevosDetalles);
        movimientoRepository.save(movimiento);
    }

    // --- LÓGICA DE AJUSTE DE INVENTARIO ---
    @Override
    @Transactional
    public void registrarAjusteInventario(AjusteInventarioDto ajusteDto, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(ajusteDto.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        List<Lote> lotesActuales = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());
        int stockSistema = lotesActuales.stream().mapToInt(Lote::getCantidad).sum();
        int stockReal = ajusteDto.getStockReal();
        int diferencia = stockReal - stockSistema;

        if (diferencia == 0)
            return;

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("AJUSTE");
        movimiento.setUsuario(usuario);
        movimiento.setFechaMovimiento(OffsetDateTime.now());
        movimiento.setMotivo("Revisión Periódica: " + (diferencia > 0 ? "Sobrante" : "Faltante"));
        movimiento.setEmpresa(usuario.getEmpresa());

        MovimientoInventario movGuardado = movimientoRepository.save(movimiento);

        if (diferencia > 0) {
            Lote loteAjuste = new Lote();
            loteAjuste.setProducto(producto);
            loteAjuste.setCantidad(diferencia);
            loteAjuste.setCodigoLote("AJUSTE-" + System.currentTimeMillis());
            loteAjuste.setEmpresa(usuario.getEmpresa());
            if (Boolean.TRUE.equals(producto.getEsPerecible())) {
                loteAjuste.setFechaVencimiento(OffsetDateTime.now().plusMonths(6));
            }
            loteRepository.save(loteAjuste);

        } else {
            int cantidadADescontar = Math.abs(diferencia);

            List<Lote> lotesParaDescuento;

            if (Boolean.TRUE.equals(producto.getEsPerecible())) {
                lotesParaDescuento = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());
            } else {
                lotesParaDescuento = loteRepository.findLotesPorCantidadDesc(producto.getIdProducto());
            }

            for (Lote lote : lotesParaDescuento) {
                if (cantidadADescontar <= 0)
                    break;
                int disponible = lote.getCantidad();
                int descontar = Math.min(disponible, cantidadADescontar);

                lote.setCantidad(disponible - descontar);
                loteRepository.save(lote);

                cantidadADescontar -= descontar;
            }
        }

        // Guardar Detalle para historial
        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setMovimientoInventario(movGuardado);
        detalle.setProducto(producto);
        detalle.setCantidad(Math.abs(diferencia));
        detalle.setPrecioVenta(producto.getPrecioVenta());
        detalle.setPrecioCompra(producto.getPrecioCompra());
        detalle.setEmpresa(usuario.getEmpresa());
        // stockAnterior y stockNuevo eliminados del esquema — info va en el motivo del movimiento

        detalleMovimientoRepository.save(detalle);

        producto.setStock(stockReal);
        productoRepository.save(producto);
    }

    @Override
    public List<MovimientoHistorialDto> obtenerHistorialDeAjustes() {
        return movimientoRepository.findByTipoMovimientoOrderByFechaMovimientoDesc("AJUSTE").stream()
                .map(this::convertirAMovimientoHistorialDto)
                .collect(Collectors.toList());
    }
    // --- MÉTODOS PRIVADOS DE AYUDA ---

    private void revertirImpactoLotes(MovimientoInventario movimiento) {
        for (DetalleMovimiento detalle : movimiento.getDetalles()) {
            if ("ENTRADA".equalsIgnoreCase(movimiento.getTipoMovimiento())) {
                disminuirStockPorLotes(detalle.getProducto(), detalle.getCantidad());
            } else if ("SALIDA".equalsIgnoreCase(movimiento.getTipoMovimiento())) {
                aumentarStockPorLotes(detalle.getProducto(), detalle.getCantidad());
            }
            // AJUSTE: lógica compleja omitida para MVP
        }
    }

    private void aumentarStockPorLotes(Producto producto, int cantidad) {
        List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());

        if (lotes.isEmpty()) {
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setCantidad(cantidad);
            lote.setCodigoLote("RECOVERY-" + System.currentTimeMillis());
            lote.setEmpresa(producto.getEmpresa());
            loteRepository.save(lote);
        } else {
            Lote lotePrioritario = lotes.get(0);
            lotePrioritario.setCantidad(lotePrioritario.getCantidad() + cantidad);
            loteRepository.save(lotePrioritario);
        }

        producto.setStock(producto.getStock() + cantidad);
        productoRepository.save(producto);
    }

    private void disminuirStockPorLotes(Producto producto, int cantidadRequerida) {
        List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());
        int pendiente = cantidadRequerida;

        for (Lote lote : lotes) {
            if (pendiente <= 0)
                break;

            int disponible = lote.getCantidad();
            if (disponible >= pendiente) {
                lote.setCantidad(disponible - pendiente);
                pendiente = 0;
            } else {
                lote.setCantidad(0);
                pendiente -= disponible;
            }
            loteRepository.save(lote);
        }

        if (pendiente > 0) {
            throw new IllegalStateException(
                    "Stock insuficiente en lotes para producto: " + producto.getNombreProducto());
        }

        producto.setStock(producto.getStock() - cantidadRequerida);
        productoRepository.save(producto);
    }

    private MovimientoHistorialDto convertirAMovimientoHistorialDto(MovimientoInventario mov) {
        MovimientoHistorialDto dto = new MovimientoHistorialDto();
        dto.setIdMovimiento(mov.getIdMovimiento());
        dto.setMotivo(mov.getMotivo() != null ? mov.getMotivo() : mov.getTipoMovimiento());
        dto.setFechaMovimiento(mov.getFechaMovimiento() != null ? mov.getFechaMovimiento().toLocalDateTime() : null);
        dto.setTipoMovimiento(mov.getTipoMovimiento());

        Usuario usuario = mov.getUsuario();
        dto.setNombreUsuario(usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "Desconocido");
        dto.setIdUsuario(usuario != null ? usuario.getIdUsuario() : null);

        List<DetalleHistorialDto> detalles = mov.getDetalles().stream()
                .map(d -> new DetalleHistorialDto(
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombreProducto(),
                        d.getCantidad(),
                        d.getPrecioVenta() != null ? d.getPrecioVenta().doubleValue() : 0.0,
                        d.getPrecioCompra() != null ? d.getPrecioCompra().doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
        dto.setDetalles(detalles);

        Double totalGeneral = detalles.stream()
                .mapToDouble(d -> {
                    double precio = "ENTRADA".equalsIgnoreCase(mov.getTipoMovimiento()) ? d.getPrecioCompra()
                            : d.getPrecioVenta();
                    return d.getCantidad() * precio;
                })
                .sum();
        dto.setTotalGeneral(totalGeneral);

        return dto;
    }

}