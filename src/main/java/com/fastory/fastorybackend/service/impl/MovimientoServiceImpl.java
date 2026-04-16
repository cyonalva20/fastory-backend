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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@lombok.RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProveedorRepository proveedorRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository; // Inyectar repo de detalles
    // -------------------------------------------------------------------------
    // --- MÉTODOS DE BÚSQUEDA Y MOVIMIENTOS (Entradas y Salidas) ---
    // -------------------------------------------------------------------------

    @Override
    public List<ProductoBusquedaDto> buscarProductosPorNombre(String nombre) {
        List<Producto> productos;

        // 1. Si el nombre está vacío, buscar todos. Si no, filtrar.
        if (nombre == null || nombre.trim().isEmpty()) {
            productos = productoRepository.findAll();
        } else {
            productos = productoRepository.findByNombreProductoContainingIgnoreCase(nombre);
        }

        // 2. Mapear y calcular el stock REAL desde los lotes
        return productos.stream()
                .map(p -> {
                    // Calcular el stock real sumando los lotes disponibles
                    List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto());
                    int stockReal = lotes.stream().mapToInt(Lote::getCantidad).sum();

                    // 3. Devolver el DTO con el stock real
                    return new ProductoBusquedaDto(
                            p.getPerecible(),
                            p.getIdProducto(),
                            p.getNombreProducto(),
                            p.getDescripcionProducto(),
                            p.getPrecioVenta(),
                            stockReal // <-- Se usa el stock real
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registrarSalida(RegistroSalidaDto salidaDto, String username) { // 🔹 CAMBIO
        // Buscamos el usuario real por su username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("SALIDA");
        movimiento.setUsuario(usuario); // Asignamos el usuario autenticado

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
            detalleMovimiento.setPrecioVenta(producto.getPrecioVenta() != null ? producto.getPrecioVenta() : 0.0);
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
        return productoRepository.findByProveedorIdProveedor(idProveedor).stream()
                .map(p -> new ProductoPorProveedorDto(p.getIdProducto(), p.getNombreProducto(), p.getPerecible()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registrarEntrada(RegistroEntradaDto entradaDto, String username) { // 🔹 CAMBIO
        // Buscamos el usuario real por su username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Proveedor proveedor = proveedorRepository.findById(entradaDto.getIdProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor no encontrado con ID: " + entradaDto.getIdProveedor()));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("ENTRADA");
        movimiento.setUsuario(usuario); // Asignamos el usuario autenticado
        movimiento.setProveedor(proveedor);
        movimiento.setFechaMovimiento(
                entradaDto.getFechaEntrada() != null ? entradaDto.getFechaEntrada() : LocalDateTime.now());

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

        List<DetalleMovimiento> detallesAGuardar = new ArrayList<>();
        List<Lote> lotesAGuardar = new ArrayList<>();

        for (DetalleEntradaDto detalleDto : entradaDto.getDetalles()) {
            if (detalleDto.getCantidad() == null || detalleDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser un entero positivo.");
            }

            Producto producto = productoRepository.findById(detalleDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + detalleDto.getIdProducto()));

            if (Boolean.TRUE.equals(producto.getPerecible()) && detalleDto.getFechaVencimiento() == null) {
                throw new IllegalArgumentException("El producto '" + producto.getNombreProducto()
                        + "' es perecible y requiere fecha de vencimiento.");
            }

            // 1. CREACIÓN Y GUARDADO DEL LOTE
            Lote nuevoLote = new Lote();
            nuevoLote.setProducto(producto);
            nuevoLote.setCantidad(detalleDto.getCantidad());
            nuevoLote.setFechaVencimiento(detalleDto.getFechaVencimiento());
            nuevoLote.setMovimientoInventario(movimientoGuardado);
            nuevoLote.setCodigoLote("LOTE-" + producto.getIdProducto() + "-" + System.currentTimeMillis());

            Lote loteGuardado = loteRepository.save(nuevoLote);
            lotesAGuardar.add(loteGuardado);

            // 2. CREACIÓN DEL DETALLE DEL MOVIMIENTO
            DetalleMovimiento detalleMovimiento = new DetalleMovimiento();
            detalleMovimiento.setMovimientoInventario(movimientoGuardado);
            detalleMovimiento.setProducto(producto);
            detalleMovimiento.setCantidad(detalleDto.getCantidad());

            detalleMovimiento
                    .setPrecioCompra(detalleDto.getPrecioCompra() != null ? detalleDto.getPrecioCompra() : 0.0);
            detalleMovimiento.setPrecioVenta(detalleDto.getPrecioVenta() != null ? detalleDto.getPrecioVenta() : 0.0);
            detalleMovimiento.setObservacionDetalle(detalleDto.getObservacionDetalle());

            detalleMovimiento.setIdLote(loteGuardado.getIdLote());
            detalleMovimiento.setFechaVencimiento(loteGuardado.getFechaVencimiento());

            detallesAGuardar.add(detalleMovimiento);
        }

        movimientoGuardado.setDetalles(detallesAGuardar);
        movimientoGuardado.setLotes(lotesAGuardar);

        // 3. Actualización de stock y PRECIO
        for (DetalleEntradaDto detalleDto : entradaDto.getDetalles()) {
            Producto productoAfectado = productoRepository.findById(detalleDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Error interno: Producto no encontrado con ID: " + detalleDto.getIdProducto()));

            // Actualizar Stock
            int stockActual = productoAfectado.getStock() != null ? productoAfectado.getStock() : 0;
            productoAfectado.setStock(stockActual + detalleDto.getCantidad());

            // Actualizar Precio si aplica
            if (detalleDto.getPrecioCompra() != null && detalleDto.getPrecioCompra() > 0) {
                productoAfectado.setPrecioCompra(detalleDto.getPrecioCompra());
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
        // 1. Inicializar la especificación base (no nula)
        Specification<Producto> spec = Specification.where(null);

        // 2. Aplicar filtros condicionales
        if (categoriaId != null) {
            spec = spec.and(ProductoSpecification.hasCategoria(categoriaId));
        }
        if (marca != null && !marca.trim().isEmpty()) {
            spec = spec.and(ProductoSpecification.hasMarca(marca));
        }
        if (Boolean.TRUE.equals(stockBajoMinimo)) {
            spec = spec.and(ProductoSpecification.isStockBajoMinimo());
        }

        // 3. Configurar la ordenación dinámica
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortBy);

        // 4. Ejecutar la consulta con filtro y ordenación
        List<Producto> productos = productoRepository.findAll(spec, sort);

        // 5. Mapear los resultados al DTO (CON VERIFICACIÓN DE NULLS)
        return productos.stream()
                .map(p -> {
                    // VERIFICACIÓN SEGURA DE CATEGORIA
                    String nombreCategoria = p.getCategoria() != null ? p.getCategoria().getNombreCategoria()
                            : "Sin Categoría";

                    // VERIFICACIÓN SEGURA DE UBICACION y REPISA
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

                    // 🔹 CÁLCULO REAL DE STOCK: Suma de lotes disponibles
                    int stockReal = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto())
                            .stream()
                            .mapToInt(Lote::getCantidad)
                            .sum();

                    return new ReporteDto(
                            p.getIdProducto(),
                            p.getNombreProducto(),
                            nombreCategoria,
                            p.getMarca(),
                            ubicacionStr,
                            stockReal, // <-- USAMOS LA SUMA DE LOTES
                            p.getStockMinimo());
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // --- NUEVOS MÉTODOS PARA GESTIÓN COMPLETA DE MOVIMIENTOS ---
    // -------------------------------------------------------------------------

    @Override
    public List<MovimientoHistorialDto> listarMovimientos(LocalDate fechaInicio, LocalDate fechaFin, String tipo) {
        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        // Mapear "Todos" del frontend a null/vacio para la query
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

        // Revertir impacto en lotes
        revertirImpactoLotes(movimiento);
        // 2. Eliminar movimiento (Cascade borrará detalles)
        movimientoRepository.delete(movimiento);
    }

    @Override
    @Transactional
    public void actualizarMovimiento(Integer idMovimiento, MovimientoUpdateDto dto) {
        MovimientoInventario movimiento = movimientoRepository.findById(idMovimiento)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        // 1. Revertir impacto anterior (Devolver stock a lotes o restar de lotes)
        revertirImpactoLotes(movimiento);

        // 2. Limpiar detalles anteriores
        movimiento.getDetalles().clear();

        // 3. Actualizar cabecera
        movimiento.setTipoMovimiento(dto.getTipoMovimiento());
        movimiento.setFechaMovimiento(LocalDateTime.of(dto.getFecha(), dto.getHora()));
        Usuario nuevoResponsable = usuarioRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        movimiento.setUsuario(nuevoResponsable);

        // 4. Aplicar nuevos cambios con lógica de lotes
        List<DetalleMovimiento> nuevosDetalles = new ArrayList<>();

        for (DetalleMovimientoUpdateDto detDto : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if ("ENTRADA".equalsIgnoreCase(dto.getTipoMovimiento())) {
                // Si es entrada (o corrección de entrada), sumamos al lote más próximo
                aumentarStockPorLotes(producto, detDto.getCantidad());
            } else {
                // Si es salida, restamos del lote más próximo (FEFO)
                disminuirStockPorLotes(producto, detDto.getCantidad());
            }

            DetalleMovimiento nuevoDetalle = new DetalleMovimiento();
            nuevoDetalle.setMovimientoInventario(movimiento);
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(detDto.getCantidad());
            nuevoDetalle.setPrecioVenta(producto.getPrecioVenta());
            nuevoDetalle.setPrecioCompra(producto.getPrecioCompra());

            nuevosDetalles.add(nuevoDetalle);
        }

        movimiento.getDetalles().addAll(nuevosDetalles);
        movimientoRepository.save(movimiento);
    }

    /**
     * Método auxiliar para deshacer el efecto de un movimiento en el stock.
     */
    private void revertirStock(MovimientoInventario movimiento) {
        for (DetalleMovimiento detalle : movimiento.getDetalles()) {
            Producto producto = detalle.getProducto();
            int cantidad = detalle.getCantidad();

            if ("ENTRADA".equalsIgnoreCase(movimiento.getTipoMovimiento())) {
                // Si era entrada, al revertir restamos el stock
                if (producto.getStock() < cantidad) {
                    throw new IllegalStateException("No se puede revertir la entrada de " +
                            producto.getNombreProducto() + ". El stock actual es menor a la cantidad original.");
                }
                producto.setStock(producto.getStock() - cantidad);
            } else {
                // Si era salida, al revertir devolvemos el stock
                producto.setStock(producto.getStock() + cantidad);
            }
            productoRepository.save(producto);
        }
    }

    // --- NUEVO: LÓGICA DE AJUSTE DE INVENTARIO ---
    @Override
    @Transactional
    public void registrarAjusteInventario(AjusteInventarioDto ajusteDto, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(ajusteDto.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Calcular Stock del Sistema Actual
        List<Lote> lotesActuales = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());
        int stockSistema = lotesActuales.stream().mapToInt(Lote::getCantidad).sum();
        int stockReal = ajusteDto.getStockReal();
        int diferencia = stockReal - stockSistema;

        if (diferencia == 0)
            return; // No hay ajuste que hacer

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("AJUSTE");
        movimiento.setUsuario(usuario);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setMotivo("Revisión Periódica: " + (diferencia > 0 ? "Sobrante" : "Faltante"));

        MovimientoInventario movGuardado = movimientoRepository.save(movimiento);
        List<DetalleMovimiento> detalles = new ArrayList<>();

        if (diferencia > 0) {
            // --- SOBRANTE (Stock Real > Sistema) ---
            // Creamos un lote de ajuste para reflejar el ingreso
            Lote loteAjuste = new Lote();
            loteAjuste.setProducto(producto);
            loteAjuste.setCantidad(diferencia);
            loteAjuste.setCodigoLote("AJUSTE-" + System.currentTimeMillis());
            loteAjuste.setMovimientoInventario(movGuardado);
            // Si el producto es perecible, podríamos requerir fecha, pero en revisión
            // rápida asumimos una fecha segura o null
            if (Boolean.TRUE.equals(producto.getPerecible())) {
                // Idealmente el usuario debería ingresar la fecha del sobrante, pero por
                // simplicidad tomamos ahora + 1 año o null
                // O buscamos el lote con fecha más lejana y usamos esa.
                loteAjuste.setFechaVencimiento(LocalDate.now().plusMonths(6).atStartOfDay());
            }
            loteRepository.save(loteAjuste);

        } else {
            // --- FALTANTE (Stock Real < Sistema) ---
            // Hay que descontar la diferencia (valor absoluto) de los lotes según reglas
            int cantidadADescontar = Math.abs(diferencia);

            List<Lote> lotesParaDescuento;

            if (Boolean.TRUE.equals(producto.getPerecible())) {
                // REGLA 1: Perecibles -> Fecha vencimiento más próxima (FEFO)
                lotesParaDescuento = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());
            } else {
                // REGLA 2: No Perecibles -> Lote con mayor cantidad
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
        // Nota: Guardamos la diferencia. Positiva = Entrada, Negativa = Salida
        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setMovimientoInventario(movGuardado);
        detalle.setProducto(producto);
        detalle.setCantidad(Math.abs(diferencia)); // Guardamos magnitud
        // Usamos 'observacionDetalle' para guardar info extra del ajuste
        detalle.setObservacionDetalle(
                "Stock Anterior: " + stockSistema + " -> Nuevo: " + stockReal + " (Dif: " + diferencia + ")");
        detalle.setPrecioVenta(producto.getPrecioVenta());
        detalle.setPrecioCompra(producto.getPrecioCompra());
        detalle.setStockAnterior(stockSistema);
        detalle.setStockNuevo(stockReal);

        detalleMovimientoRepository.save(detalle);

        // Actualizar cabecera de producto
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
            } else if ("AJUSTE".equalsIgnoreCase(movimiento.getTipoMovimiento())) {
                // Revertir ajuste es complejo porque depende del signo.
                // Por simplicidad en MVP, asumimos reversión manual o lógica similar a salida.
                // Aquí implementamos lógica segura básica: restaurar stock global
                Producto p = detalle.getProducto();
                // Necesitaríamos saber si el ajuste fue positivo o negativo.
                // Se podría parsear la observación o guardar un campo 'signo'.
            }
        }
    }

    private void aumentarStockPorLotes(Producto producto, int cantidad) {
        // Buscar lote más próximo a vencer (o cualquiera si no es perecible)
        // Usamos findLotesDisponiblesParaSalida porque ordena por fechaVencimiento ASC
        List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(producto.getIdProducto());

        if (lotes.isEmpty()) {
            // Caso raro en edición: No hay lotes. Creamos uno genérico de recuperación.
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setCantidad(cantidad);
            // Fecha vencimiento null si no es perecible, o lógica por defecto
            loteRepository.save(lote);
        } else {
            // Sumar al primero (el más próximo a vencer)
            Lote lotePrioritario = lotes.get(0);
            lotePrioritario.setCantidad(lotePrioritario.getCantidad() + cantidad);
            loteRepository.save(lotePrioritario);
        }

        // Actualizar referencia global
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

        // Actualizar referencia global
        producto.setStock(producto.getStock() - cantidadRequerida);
        productoRepository.save(producto);
    }

    private MovimientoHistorialDto convertirAMovimientoHistorialDto(MovimientoInventario mov) {
        MovimientoHistorialDto dto = new MovimientoHistorialDto();
        dto.setIdMovimiento(mov.getIdMovimiento());
        dto.setMotivo(mov.getMotivo() != null ? mov.getMotivo() : mov.getTipoMovimiento());
        dto.setFechaMovimiento(mov.getFechaMovimiento());
        dto.setTipoMovimiento(mov.getTipoMovimiento()); // Asegura que se setea el tipo

        Usuario usuario = mov.getUsuario();
        dto.setNombreUsuario(usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "Desconocido");
        dto.setIdUsuario(usuario != null ? usuario.getIdUsuario() : null);

        List<DetalleHistorialDto> detalles = mov.getDetalles().stream()
                .map(d -> new DetalleHistorialDto(
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombreProducto(),
                        d.getCantidad(),
                        d.getPrecioVenta(),
                        d.getPrecioCompra(),
                        d.getStockAnterior(), // <--- ¡AQUÍ ESTÁ LA CLAVE! Pasamos los datos
                        d.getStockNuevo() // <--- ¡AQUÍ ESTÁ LA CLAVE! Pasamos los datos
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