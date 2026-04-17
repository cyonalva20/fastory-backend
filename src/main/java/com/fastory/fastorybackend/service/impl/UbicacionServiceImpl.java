package com.fastory.fastorybackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.AsignarUbicacionDto;
import com.fastory.fastorybackend.dto.RepisaCreateDto;
import com.fastory.fastorybackend.dto.RepisaDetalleDto;
import com.fastory.fastorybackend.dto.UbicacionDto;
import com.fastory.fastorybackend.entity.EstadoUbicacion;
import com.fastory.fastorybackend.entity.Producto;
import com.fastory.fastorybackend.entity.Repisa;
import com.fastory.fastorybackend.entity.Ubicacion;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.ProductoRepository;
import com.fastory.fastorybackend.repository.RepisaRepository;
import com.fastory.fastorybackend.repository.UbicacionRepository;
import com.fastory.fastorybackend.service.UbicacionService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private final RepisaRepository repisaRepository;

    private final UbicacionRepository ubicacionRepository;

    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public void crearRepisaYGenerarUbicaciones(RepisaCreateDto repisaCreateDto) {
        if (repisaRepository.existsByCodigo(repisaCreateDto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una repisa con el código: " + repisaCreateDto.getCodigo());
        }
        Repisa repisa = new Repisa();
        repisa.setCodigo(repisaCreateDto.getCodigo());
        // Campo 'descripcion' eliminado de la entidad Repisa en el nuevo esquema
        Repisa repisaGuardada = repisaRepository.save(repisa);

        List<Ubicacion> ubicaciones = new ArrayList<>();
        for (int i = 1; i <= repisaCreateDto.getNumeroFilas(); i++) {
            for (int j = 1; j <= repisaCreateDto.getNumeroColumnas(); j++) {
                Ubicacion ubicacion = new Ubicacion();
                ubicacion.setRepisa(repisaGuardada);
                ubicacion.setFila(i);
                ubicacion.setColumna(j);
                ubicacion.setEstado(EstadoUbicacion.LIBRE);
                ubicaciones.add(ubicacion);
            }
        }
        ubicacionRepository.saveAll(ubicaciones);
    }

    @Override
    @Transactional
    public UbicacionDto asignarProductoAUbicacion(AsignarUbicacionDto asignarDto) {
        Producto producto = productoRepository.findById(asignarDto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + asignarDto.getProductoId()));

        Repisa repisa = repisaRepository.findById(asignarDto.getRepisaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Repisa no encontrada con id: " + asignarDto.getRepisaId()));

        Ubicacion ubicacion = ubicacionRepository
                .findByRepisaAndFilaAndColumna(repisa, asignarDto.getFila(), asignarDto.getColumna())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Ubicación no encontrada en la repisa " + repisa.getCodigo()
                                + ", fila " + asignarDto.getFila() + ", columna " + asignarDto.getColumna()));

        if (EstadoUbicacion.OCUPADO.equals(ubicacion.getEstado())) {
            throw new IllegalStateException("La ubicación ya está ocupada.");
        }

        // Si el producto ya tenía una ubicación, la liberamos
        if (producto.getUbicacion() != null) {
            Ubicacion ubicacionAntigua = producto.getUbicacion();
            ubicacionAntigua.setEstado(EstadoUbicacion.LIBRE);
            ubicacionRepository.save(ubicacionAntigua);
        }

        ubicacion.setEstado(EstadoUbicacion.OCUPADO);
        producto.setUbicacion(ubicacion);

        Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
        productoRepository.save(producto);

        return mapEntityToDto(ubicacionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepisaDetalleDto> obtenerTodasLasRepisasConDimensiones() {
        List<Repisa> repisas = repisaRepository.findAll();
        return repisas.stream().map(repisa -> {
            RepisaDetalleDto dto = new RepisaDetalleDto();
            dto.setIdRepisa(repisa.getIdRepisa());
            dto.setCodigo(repisa.getCodigo());

            Integer filas = ubicacionRepository.findMaxFilaByRepisa(repisa);
            Integer columnas = ubicacionRepository.findMaxColumnaByRepisa(repisa);

            dto.setNumeroFilas(filas != null ? filas : 0);
            dto.setNumeroColumnas(columnas != null ? columnas : 0);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionDto> obtenerUbicacionesPorRepisa(Integer repisaId) {
        Repisa repisa = repisaRepository.findById(repisaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la repisa con id: " + repisaId));

        return ubicacionRepository.findByRepisa(repisa).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    private UbicacionDto mapEntityToDto(Ubicacion ubicacion) {
        UbicacionDto dto = new UbicacionDto();
        dto.setIdUbicacion(ubicacion.getIdUbicacion());
        dto.setIdRepisa(ubicacion.getRepisa().getIdRepisa());
        dto.setFila(ubicacion.getFila());
        dto.setColumna(ubicacion.getColumna());
        dto.setEstado(ubicacion.getEstado().name()); // Enum → String for DTO
        return dto;
    }
}
