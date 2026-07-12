package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProveedorCreateDto {
    @NotBlank(message = "El nombre del proveedor no puede estar vacío.")
    @Size(max = 150, message = "El nombre del proveedor no puede exceder los 150 caracteres.")
    private String nombreProveedor;

    @Size(max = 9, message = "El teléfono de la categoría no puede exceder los 9 caracteres.")
    private String telefono;

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
