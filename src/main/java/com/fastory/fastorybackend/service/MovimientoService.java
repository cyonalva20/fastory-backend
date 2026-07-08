package com.fastory.fastorybackend.service;

import java.time.LocalDate;
import java.util.List;

import com.fastory.fastorybackend.dto.*;

public interface MovimientoService {

    // --- MÉTODOS DE BÚSQUEDA Y MOVIMIENTOS ---

    List<ProductoBusquedaDto> buscarProductosPorNombre(String nombre);

    void registrarSalida(RegistroSalidaDto salidaDto, String username);

    List<MovimientoHistorialDto> obtenerHistorialDeSalidas();

    void registrarEntrada(RegistroEntradaDto entradaDto, String username);

    List<MovimientoHistorialDto> obtenerHistorialDeEntradas();

    // --- METODO DE REPORTE DE STOCK ---

    List<ReporteDto> generarReporteStockActual(
            Integer categoriaId,
            Boolean stockBajoMinimo,
            String sortBy,
            String sortDir);

    // --- NUEVOS MÉTODOS ---
    List<MovimientoHistorialDto> listarMovimientos(LocalDate fechaInicio, LocalDate fechaFin, String tipo);

    void eliminarMovimiento(Integer idMovimiento);

    void actualizarMovimiento(Integer idMovimiento, MovimientoUpdateDto updateDto);

    // --- NUEVO: AJUSTE DE INVENTARIO ---
    void registrarAjusteInventario(AjusteInventarioDto ajusteDto, String username);

    List<MovimientoHistorialDto> obtenerHistorialDeAjustes();

}