package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProveedorCreateDto {
    @NotBlank(message = "El nombre del proveedor no puede estar vacío.")
    @Size(max = 150, message = "El nombre del proveedor no puede exceder los 150 caracteres.")
    private String nombreProveedor;

    @Size(max = 11, message = "El RUC no puede exceder los 11 caracteres.")
    @Pattern(regexp = "^[0-9]*$", message = "El RUC solo puede contener números.")
    private String rucProveedor;

    @Size(max = 15, message = "El teléfono no puede exceder los 15 caracteres.")
    private String telefono;

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getRucProveedor() {
        return rucProveedor;
    }

    public void setRucProveedor(String rucProveedor) {
        this.rucProveedor = rucProveedor;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
