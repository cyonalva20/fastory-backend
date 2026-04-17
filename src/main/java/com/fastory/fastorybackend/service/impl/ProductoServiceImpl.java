package com.fastory.fastorybackend.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.*;
import com.fastory.fastorybackend.entity.*;
import com.fastory.fastorybackend.exception.*;
import com.fastory.fastorybackend.repository.CategoriaRepository;
import com.fastory.fastorybackend.repository.LoteRepository;
import com.fastory.fastorybackend.repository.ProductoRepository;
import com.fastory.fastorybackend.repository.RepisaRepository;
import com.fastory.fastorybackend.repository.UbicacionRepository;
import com.fastory.fastorybackend.repository.UsuarioRepository;
import com.fastory.fastorybackend.service.CategoriaService;
import com.fastory.fastorybackend.service.ProductoService;
import com.fastory.fastorybackend.service.UbicacionService;
import com.fastory.fastorybackend.specification.ProductoSpecification;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    private final UbicacionRepository ubicacionRepository;

    private final LoteRepository loteRepository;

    private final RepisaRepository repisaRepository;

    private final CategoriaService categoriaService;

    private final UbicacionService ubicacionService;

    private final UsuarioRepository usuarioRepository;

    private final CategoriaRepository categoriaRepository;

    // Formateador para las fechas de LoteDetalleDto y fechaProxima
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }

    @Override
    public Optional<Producto> obtenerPorNombre(String nombre) {
        return productoRepository.findByNombreProducto(nombre);
    }

    @Override
    @Transactional
    public Producto guardar(Producto producto) {
        // Validaciones de ubicación
        if (producto.getUbicacion() != null && producto.getUbicacion().getIdUbicacion() != null) {
            Ubicacion ubicacion = ubicacionRepository.findById(producto.getUbicacion().getIdUbicacion())
                    .orElseThrow(() -> new ResourceNotFoundException("La ubicación seleccionada no existe."));

            if (EstadoUbicacion.OCUPADO.equals(ubicacion.getEstado())) {
                throw new IllegalStateException("La ubicación seleccionada ya se encuentra ocupada.");
            }

            ubicacion.setEstado(EstadoUbicacion.OCUPADO);
            producto.setUbicacion(ubicacion);
        }

        Producto productoGuardado = productoRepository.save(producto);

        // Crear lote inicial si hay stock
        if (productoGuardado.getStock() != null && productoGuardado.getStock() > 0) {
            Lote lote = new Lote();
            lote.setProducto(productoGuardado);
            lote.setCodigoLote("L" + productoGuardado.getIdProducto() + "-" + System.currentTimeMillis());
            lote.setCantidad(productoGuardado.getStock());
            lote.setIdEmpresa(productoGuardado.getIdEmpresa());
            // fechaVencimiento ya no está en Producto — se maneja a nivel de lote
            loteRepository.save(lote);
        }

        return productoGuardado;
    }

    @Override
    public Producto actualizar(Producto producto) {
        if (!productoRepository.existsById(producto.getIdProducto())) {
            throw new IllegalArgumentException("El producto no existe");
        }
        return productoRepository.save(producto);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return productoRepository.existsByNombreProducto(nombre);
    }

    // --- PANEL PRINCIPAL (Index.tsx) ---

    @Override
    @Transactional(readOnly = true)
    public List<ProductoInventarioDto> getInventario(String nombre, Integer categoriaId, String repisa, Integer fila,
            Integer columna, String sortBy, String sortDir) {

        Specification<Producto> spec = Specification.where(null);

        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and(ProductoSpecification.hasNombre(nombre));
        }
        if (categoriaId != null) {
            spec = spec.and(ProductoSpecification.hasCategoria(categoriaId));
        }
        if (repisa != null && !repisa.trim().isEmpty()) {
            spec = spec.and(ProductoSpecification.hasRepisa(repisa));
        }
        if (fila != null) {
            spec = spec.and(ProductoSpecification.hasFila(fila));
        }
        if (columna != null) {
            spec = spec.and(ProductoSpecification.hasColumna(columna));
        }

        Sort sort;
        Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());

        if ("ubicacion".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(
                    new Sort.Order(direction, "ubicacion.repisa.codigo"),
                    new Sort.Order(direction, "ubicacion.fila"),
                    new Sort.Order(direction, "ubicacion.columna"));
        } else if ("categoria".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(new Sort.Order(direction, "categoria.nombreCategoria"));
        } else {
            // Ordenamiento por proveedor eliminado — ya no existe la relación
            String sortField = sortBy.equals("stock") ? "stock" : "nombreProducto";
            if (sortBy.equals("precio") || sortBy.equals("stockMinimo")) {
                sortField = sortBy;
            }
            sort = Sort.by(new Sort.Order(direction, sortField));
        }

        List<Producto> productos = productoRepository.findAll(spec, sort);

        return productos.stream().map(p -> {

            int stockDisp = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto())
                    .stream()
                    .mapToInt(Lote::getCantidad)
                    .sum();

            String ubicacionStr = "N/A";
            if (p.getUbicacion() != null && p.getUbicacion().getRepisa() != null) {
                ubicacionStr = String.format("%s-%d-%d",
                        p.getUbicacion().getRepisa().getCodigo(),
                        p.getUbicacion().getFila(),
                        p.getUbicacion().getColumna());
            }

            String categoriaNombre = (p.getCategoria() != null) ? p.getCategoria().getNombreCategoria() : "N/A";

            // Proveedor eliminado del esquema de Producto
            String proveedorNombre = "N/A";

            return new ProductoInventarioDto(
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    categoriaNombre,
                    p.getPrecioCompra() != null ? p.getPrecioCompra().doubleValue() : 0.0,
                    p.getPrecioVenta() != null ? p.getPrecioVenta().doubleValue() : 0.0,
                    stockDisp,
                    p.getStockMinimo(),
                    ubicacionStr,
                    proveedorNombre
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoInventarioDto> obtenerAlertasStock() {
        List<Producto> productosCriticos = productoRepository.findProductosConStockCriticoCalculado();

        return productosCriticos.stream()
                .map(this::mapToInventarioDto)
                .collect(Collectors.toList());
    }

    private ProductoInventarioDto mapToInventarioDto(Producto p) {
        int stockDisp = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto())
                .stream()
                .mapToInt(Lote::getCantidad)
                .sum();

        String ubicacionStr = "N/A";
        if (p.getUbicacion() != null && p.getUbicacion().getRepisa() != null) {
            ubicacionStr = String.format("%s-%d-%d",
                    p.getUbicacion().getRepisa().getCodigo(),
                    p.getUbicacion().getFila(),
                    p.getUbicacion().getColumna());
        }

        String categoriaNombre = (p.getCategoria() != null) ? p.getCategoria().getNombreCategoria() : "N/A";

        return new ProductoInventarioDto(
                p.getIdProducto(),
                p.getNombreProducto(),
                categoriaNombre,
                p.getPrecioCompra() != null ? p.getPrecioCompra().doubleValue() : 0.0,
                p.getPrecioVenta() != null ? p.getPrecioVenta().doubleValue() : 0.0,
                stockDisp,
                p.getStockMinimo(),
                ubicacionStr,
                "N/A"); // Proveedor eliminado del esquema
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDetalleDto getProductoDetalle(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        List<Lote> lotes = loteRepository.findLotesDisponiblesParaSalida(p.getIdProducto());

        int stockDisp = lotes.stream()
                .mapToInt(Lote::getCantidad)
                .sum();

        String fechaProxima = "N/A";
        if (Boolean.TRUE.equals(p.getEsPerecible())) {
            fechaProxima = lotes.stream()
                    .filter(l -> l.getFechaVencimiento() != null)
                    .map(Lote::getFechaVencimiento)
                    .findFirst()
                    .map(fecha -> fecha.format(DATE_FORMATTER))
                    .orElse("N/A");
        }

        List<LoteDetalleDto> lotesDto = lotes.stream()
                .map(lote -> new LoteDetalleDto(
                        lote.getIdLote(),
                        lote.getCodigoLote(),
                        lote.getCantidad(),
                        lote.getFechaVencimiento() != null ? lote.getFechaVencimiento().format(DATE_FORMATTER) : null))
                .collect(Collectors.toList());

        String ubicacionStr = "N/A";
        if (p.getUbicacion() != null && p.getUbicacion().getRepisa() != null) {
            ubicacionStr = String.format("%s-%d-%d",
                    p.getUbicacion().getRepisa().getCodigo(),
                    p.getUbicacion().getFila(),
                    p.getUbicacion().getColumna());
        }

        ProductoDetalleDto detalleDto = new ProductoDetalleDto();
        detalleDto.setIdProducto(p.getIdProducto());
        detalleDto.setNombre(p.getNombreProducto());
        detalleDto.setCategoria(p.getCategoria() != null ? p.getCategoria().getNombreCategoria() : "N/A");
        detalleDto.setIdCategoria(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null);
        detalleDto.setMarca(null); // marca eliminada del esquema
        detalleDto.setDescripcion(null); // descripcion eliminada del esquema
        detalleDto.setPrecioCompra(p.getPrecioCompra() != null ? p.getPrecioCompra().doubleValue() : null);
        detalleDto.setPrecioVenta(p.getPrecioVenta() != null ? p.getPrecioVenta().doubleValue() : null);
        detalleDto.setStockDisponible(stockDisp);
        detalleDto.setStockMinimo(p.getStockMinimo());
        detalleDto.setUbicacion(ubicacionStr);

        if (p.getUbicacion() != null) {
            detalleDto.setIdUbicacion(p.getUbicacion().getIdUbicacion());
            detalleDto.setIdRepisa(
                    p.getUbicacion().getRepisa() != null ? p.getUbicacion().getRepisa().getIdRepisa() : null);
            detalleDto.setFila(p.getUbicacion().getFila());
            detalleDto.setColumna(p.getUbicacion().getColumna());
        }

        detalleDto.setPerecible(Boolean.TRUE.equals(p.getEsPerecible()));
        detalleDto.setFechaVencimientoProxima(fechaProxima);
        detalleDto.setProveedor("N/A"); // proveedor eliminado del esquema
        detalleDto.setLotes(lotesDto);

        return detalleDto;
    }

    @Override
    @Transactional(readOnly = true)
    public FiltrosDto getFiltrosInventario() {
        List<CategoriaDto> categorias = categoriaService.obtenerTodasConConteo();
        List<RepisaDetalleDto> repisas = ubicacionService.obtenerTodasLasRepisasConDimensiones();

        return new FiltrosDto(categorias, repisas);
    }

    @Override
    @Transactional
    public ProductoDetalleDto actualizarProductoParcial(Integer idProducto, ProductoUpdateDto dto, String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String nombreRol = (usuario.getRol() != null) ? usuario.getRol().getNombreRol() : "";
        boolean esAdmin = "ADMINISTRADOR".equalsIgnoreCase(nombreRol) || "Administrador".equalsIgnoreCase(nombreRol);
        boolean esSuperAdmin = usuario.getIdUsuario().equals(1);

        if (!esAdmin && !esSuperAdmin) {
            throw new AccessDeniedException("Usuario no autorizado para esta operación.");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + idProducto));

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Categoría no encontrada con id: " + dto.getIdCategoria()));

        if (!producto.getNombreProducto().equalsIgnoreCase(dto.getNombreProducto())) {
            if (productoRepository.existsByNombreProducto(dto.getNombreProducto())) {
                throw new IllegalArgumentException("Ya existe otro producto con el nombre: " + dto.getNombreProducto());
            }
        }

        producto.setNombreProducto(dto.getNombreProducto());
        // descripcion y marca eliminadas del esquema
        producto.setPrecioCompra(dto.getPrecioCompra() != null ? BigDecimal.valueOf(dto.getPrecioCompra()) : producto.getPrecioCompra());
        producto.setPrecioVenta(dto.getPrecioVenta() != null ? BigDecimal.valueOf(dto.getPrecioVenta()) : producto.getPrecioVenta());
        producto.setStockMinimo(dto.getStockMinimo());
        producto.setCategoria(categoria);

        // 5. ACTUALIZAR UBICACIÓN CON MANEJO DE UBICACIÓN OCUPADA
        if (dto.getIdRepisa() != null && dto.getFila() != null && dto.getColumna() != null) {
            Repisa repisa = repisaRepository.findById(dto.getIdRepisa())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Repisa no encontrada con id: " + dto.getIdRepisa()));

            Ubicacion nuevaUbicacion = ubicacionRepository
                    .findByRepisaAndFilaAndColumna(repisa, dto.getFila(), dto.getColumna())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Ubicación no encontrada: Repisa %s, Fila %d, Columna %d",
                                    repisa.getCodigo(), dto.getFila(), dto.getColumna())));

            boolean esDiferenteUbicacion = producto.getUbicacion() == null ||
                    !producto.getUbicacion().getIdUbicacion().equals(nuevaUbicacion.getIdUbicacion());

            if (esDiferenteUbicacion) {
                if (EstadoUbicacion.OCUPADO.equals(nuevaUbicacion.getEstado())) {

                    Optional<Producto> productoOcupante = productoRepository.findByUbicacion(nuevaUbicacion);

                    if (dto.getForzarCambioUbicacion() == null || !dto.getForzarCambioUbicacion()) {
                        if (productoOcupante.isPresent()) {
                            throw new UbicacionOcupadaException(
                                    "La ubicación está ocupada por otro producto",
                                    productoOcupante.get().getIdProducto(),
                                    productoOcupante.get().getNombreProducto());
                        } else {
                            throw new IllegalStateException(
                                    "La ubicación seleccionada ya está ocupada por otro producto.");
                        }
                    }

                    if (productoOcupante.isPresent()) {
                        Producto prodAnterior = productoOcupante.get();
                        prodAnterior.setUbicacion(null);
                        productoRepository.save(prodAnterior);
                    }
                }

                if (producto.getUbicacion() != null) {
                    Ubicacion ubicacionAntigua = producto.getUbicacion();
                    ubicacionAntigua.setEstado(EstadoUbicacion.LIBRE);
                    ubicacionRepository.save(ubicacionAntigua);
                }

                nuevaUbicacion.setEstado(EstadoUbicacion.OCUPADO);
                ubicacionRepository.save(nuevaUbicacion);
                producto.setUbicacion(nuevaUbicacion);
            }
        }

        productoRepository.save(producto);

        return this.getProductoDetalle(idProducto);
    }
}
