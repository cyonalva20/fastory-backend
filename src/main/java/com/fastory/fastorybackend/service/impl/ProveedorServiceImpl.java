package com.fastory.fastorybackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.ProveedorCreateDto;
import com.fastory.fastorybackend.dto.ProveedorDto;
import com.fastory.fastorybackend.entity.Proveedor;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.ProveedorRepository;
import com.fastory.fastorybackend.service.ProveedorService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerTodas() {
        return proveedorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDto> obtenerTodasConConteo() {
        return proveedorRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProveedorDto crearProveedor(ProveedorCreateDto createDto) {
        if (proveedorRepository.existsByNombreProveedor(createDto.getNombreProveedor())) {
            throw new IllegalArgumentException(
                    "Ya existe un proveedor con el nombre: " + createDto.getNombreProveedor());
        }
        if (proveedorRepository.existsByTelefono(createDto.getTelefono())) {
            throw new IllegalArgumentException("Ya existe un proveedor con el teléfono" + createDto.getTelefono());
        }

        Proveedor nuevoProveedor = new Proveedor();
        nuevoProveedor.setNombreProveedor(createDto.getNombreProveedor());
        nuevoProveedor.setTelefono(createDto.getTelefono());

        Proveedor proveedorGuardado = proveedorRepository.save(nuevoProveedor);

        return mapEntityToDto(proveedorGuardado);
    }

    @Override
    @Transactional
    public ProveedorDto actualizarProveedor(Integer id, ProveedorCreateDto updateDto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));

        // Verificar si el nuevo nombre ya está en uso por OTRO proveedor
        proveedorRepository.findByNombreProveedor(updateDto.getNombreProveedor())
                .ifPresent(proveedorExistente -> {
                    if (!proveedorExistente.getIdProveedor().equals(id)) {
                        throw new IllegalArgumentException(
                                "Ya existe otro proveedor con el nombre: " + updateDto.getNombreProveedor());
                    }
                });

        // Verificar si el nuevo telefono ya está en uso por OTRO proveedor
        proveedorRepository.findByTelefono(updateDto.getTelefono())
                .ifPresent(proveedorExistente -> {
                    if (!proveedorExistente.getIdProveedor().equals(id)) {
                        throw new IllegalArgumentException(
                                "Ya existe otro proveedor con el teléfono: " + updateDto.getTelefono());
                    }
                });

        proveedor.setNombreProveedor(updateDto.getNombreProveedor());
        proveedor.setTelefono(updateDto.getTelefono());

        Proveedor proveedorActualizado = proveedorRepository.save(proveedor);
        return mapEntityToDto(proveedorActualizado);
    }

    @Override
    @Transactional
    public void eliminarProveedor(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));

        // Validación clave: no eliminar si hay productos asociados
        if (proveedor.getProductos() != null && !proveedor.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el proveedor '" + proveedor.getNombreProveedor()
                    + "' porque tiene productos asociados.");
        }

        proveedorRepository.delete(proveedor);
    }

    private ProveedorDto mapEntityToDto(Proveedor proveedor) {
        ProveedorDto dto = new ProveedorDto();
        dto.setIdProveedor(proveedor.getIdProveedor());
        dto.setNombreProveedor(proveedor.getNombreProveedor());
        dto.setTelefono(proveedor.getTelefono());
        dto.setCantidadProductos(proveedor.getProductos() != null ? proveedor.getProductos().size() : 0);
        return dto;
    }
}
