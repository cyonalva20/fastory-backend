package com.fastory.fastorybackend.dto;

import java.util.List;

// Este DTO es para el modal "Ver Detalles"
public class ProductoDetalleDto {

    private Integer idProducto; // <-- AÑADIDO
    private String nombre;
    private String categoria;
    private Integer idCategoria;
    private String marca;
    private String descripcion;
    private Double precioCompra;
    private Double precioVenta;
    private Integer stockDisponible;
    private Integer stockMinimo;
    private String ubicacion;
    private boolean perecible;
    private String fechaVencimientoProxima; // La fecha más cercana
    private String proveedor; // <-- AÑADIDO
    private List<LoteDetalleDto> lotes;
    // --- NUEVOS CAMPOS PARA UBICACIÓN DETALLADA ---
    private Integer idUbicacion;
    private Integer idRepisa;
    private Integer fila;
    private Integer columna;

    // Getters y Setters

    // --- AÑADIDO GETTER Y SETTER ---
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    // -------------------------------
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isPerecible() {
        return perecible;
    }

    public void setPerecible(boolean perecible) {
        this.perecible = perecible;
    }

    public String getFechaVencimientoProxima() {
        return fechaVencimientoProxima;
    }

    public void setFechaVencimientoProxima(String fechaVencimientoProxima) {
        this.fechaVencimientoProxima = fechaVencimientoProxima;
    }

    // --- AÑADIDO GETTER Y SETTER ---
    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    // -------------------------------

    public List<LoteDetalleDto> getLotes() {
        return lotes;
    }

    public void setLotes(List<LoteDetalleDto> lotes) {
        this.lotes = lotes;
    }

    // Añadir getter y setter
    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    // --- NUEVOS GETTERS Y SETTERS ---
    public Integer getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Integer idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public Integer getIdRepisa() {
        return idRepisa;
    }

    public void setIdRepisa(Integer idRepisa) {
        this.idRepisa = idRepisa;
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

}