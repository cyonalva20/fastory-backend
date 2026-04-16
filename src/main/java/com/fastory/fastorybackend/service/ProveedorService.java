package com.fastory.fastorybackend.service;

import java.util.List;

import com.fastory.fastorybackend.dto.ProveedorCreateDto;
import com.fastory.fastorybackend.dto.ProveedorDto;
import com.fastory.fastorybackend.entity.Proveedor;

public interface ProveedorService {

    List<Proveedor> obtenerTodas();

    List<ProveedorDto> obtenerTodasConConteo();

    ProveedorDto crearProveedor(ProveedorCreateDto createDto);

    ProveedorDto actualizarProveedor(Integer id, ProveedorCreateDto updateDto);

    void eliminarProveedor(Integer id);
}
