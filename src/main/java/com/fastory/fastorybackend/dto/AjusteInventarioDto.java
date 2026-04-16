package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.NotNull;

public class AjusteInventarioDto {

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer idProducto;

    @NotNull(message = "El stock real físico es obligatorio")
    private Integer stockReal;

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getStockReal() {
        return stockReal;
    }

    public void setStockReal(Integer stockReal) {
        this.stockReal = stockReal;
    }
}