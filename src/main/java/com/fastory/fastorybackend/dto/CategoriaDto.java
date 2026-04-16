package com.fastory.fastorybackend.dto;

/**
 * DTO para representar la información de una categoría, incluyendo la cantidad
 * de productos asociados.
 * Este es el objeto que se enviará al frontend para listar las categorías.
 */
public class CategoriaDto {

    private Integer idCategoria;
    private String nombreCategoria;
    private String descripcion;
    private int cantidadProductos;

    // Getters y Setters

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }
}