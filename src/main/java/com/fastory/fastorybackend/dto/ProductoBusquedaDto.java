package com.fastory.fastorybackend.dto;

public class ProductoBusquedaDto {
    private Integer idProducto;
    private String nombreProducto;
    private Double precioVenta;
    private Integer stock;
    private Boolean esPerecible;

    public ProductoBusquedaDto(Boolean esPerecible, Integer idProducto, String nombreProducto,
            Double precioVenta, Integer stock) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.esPerecible = esPerecible;
    }

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getEsPerecible() {
        return esPerecible;
    }

    public void setEsPerecible(Boolean esPerecible) {
        this.esPerecible = esPerecible;
    }
}
