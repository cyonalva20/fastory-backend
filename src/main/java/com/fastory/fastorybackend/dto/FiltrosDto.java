package com.fastory.fastorybackend.dto;

import java.util.List;

// DTO para cargar los filtros del Index.tsx de una sola vez
public class FiltrosDto {

    private List<CategoriaDto> categorias; // Reutilizamos tu DTO existente
    private List<RepisaDetalleDto> repisas; // Reutilizamos tu DTO existente

    public FiltrosDto(List<CategoriaDto> categorias, List<RepisaDetalleDto> repisas) {
        this.categorias = categorias;
        this.repisas = repisas;
    }

    // Getters y Setters
    public List<CategoriaDto> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaDto> categorias) {
        this.categorias = categorias;
    }

    public List<RepisaDetalleDto> getRepisas() {
        return repisas;
    }

    public void setRepisas(List<RepisaDetalleDto> repisas) {
        this.repisas = repisas;
    }
}