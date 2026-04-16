package com.fastory.fastorybackend.service;

import java.util.List;

import com.fastory.fastorybackend.dto.AsignarUbicacionDto;
import com.fastory.fastorybackend.dto.RepisaCreateDto;
import com.fastory.fastorybackend.dto.RepisaDetalleDto;
import com.fastory.fastorybackend.dto.UbicacionDto;

public interface UbicacionService {

    void crearRepisaYGenerarUbicaciones(RepisaCreateDto repisaCreateDto);

    UbicacionDto asignarProductoAUbicacion(AsignarUbicacionDto asignarDto);

    // --- Métodos nuevos para el frontend ---

    /**
     * Obtiene una lista de todas las repisas con sus detalles básicos y
     * dimensiones.
     * 
     * @return Lista de DTOs de repisas.
     */
    List<RepisaDetalleDto> obtenerTodasLasRepisasConDimensiones();

    /**
     * Obtiene todas las ubicaciones (con su estado) de una repisa específica.
     * 
     * @param repisaId El ID de la repisa a consultar.
     * @return Lista de DTOs de ubicaciones.
     */
    List<UbicacionDto> obtenerUbicacionesPorRepisa(Integer repisaId);
}
