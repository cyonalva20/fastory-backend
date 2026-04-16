package com.fastory.fastorybackend.dto;

public class RepisaDetalleDto {
    private Integer idRepisa;
    private String codigo;
    private Integer numeroFilas;
    private Integer numeroColumnas;

    // Getters y Setters

    public Integer getIdRepisa() {
        return idRepisa;
    }

    public void setIdRepisa(Integer idRepisa) {
        this.idRepisa = idRepisa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getNumeroFilas() {
        return numeroFilas;
    }

    public void setNumeroFilas(Integer numeroFilas) {
        this.numeroFilas = numeroFilas;
    }

    public Integer getNumeroColumnas() {
        return numeroColumnas;
    }

    public void setNumeroColumnas(Integer numeroColumnas) {
        this.numeroColumnas = numeroColumnas;
    }
}