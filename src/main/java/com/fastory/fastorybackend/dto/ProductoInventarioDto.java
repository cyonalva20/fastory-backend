package com.fastory.fastorybackend.dto;

// Este DTO es para la tabla principal del Index.tsx
public class ProductoInventarioDto {

    private Integer idProducto;
    private String nombre;
    private String categoria;
    private Double precioCompra;
    private Double precioVenta;
    private Integer stockDisponible;
    private Integer stockMinimo;
    private String ubicacion; // Formato "A-1-2"
    private String proveedor; // <-- AÑADIDO
    // Constructor, Getters y Setters

    public ProductoInventarioDto() {
    }

    public ProductoInventarioDto(Integer idProducto, String nombre, String categoria, Double precioCompra,
            Double precioVenta, Integer stockDisponible, Integer stockMinimo, String ubicacion, String proveedor) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockDisponible = stockDisponible;
        this.stockMinimo = stockMinimo;
        this.ubicacion = ubicacion;
        this.proveedor = proveedor; // <-- AÑADIDO
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

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

    // --- AÑADIDO GETTER Y SETTER ---
    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
}