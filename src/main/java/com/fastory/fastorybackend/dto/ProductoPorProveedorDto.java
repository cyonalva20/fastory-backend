package com.fastory.fastorybackend.dto;

public class ProductoPorProveedorDto {
    private Integer idProducto;
    private String nombreProducto;
    private Boolean esPerecible;

    public ProductoPorProveedorDto(Integer idProducto, String nombreProducto, Boolean esPerecible) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.esPerecible = esPerecible != null && esPerecible;
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

    public Boolean getEsPerecible() {
        return esPerecible;
    }

    public void setEsPerecible(Boolean esPerecible) {
        this.esPerecible = esPerecible;
    }

}
