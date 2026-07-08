package com.fastory.fastorybackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;
import com.fastory.fastorybackend.config.TenantUserDetails;
import com.fastory.fastorybackend.dto.CategoriaCreateDto;
import com.fastory.fastorybackend.dto.CategoriaDto;
import com.fastory.fastorybackend.entity.Categoria;
import com.fastory.fastorybackend.entity.Empresa;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.CategoriaRepository;
import com.fastory.fastorybackend.repository.EmpresaRepository;
import com.fastory.fastorybackend.service.CategoriaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> obtenerTodasConConteo() {
        return categoriaRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoriaDto crearCategoria(CategoriaCreateDto createDto) {
        if (categoriaRepository.existsByNombreCategoria(createDto.getNombreCategoria())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con el nombre: " + createDto.getNombreCategoria());
        }

        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombreCategoria(createDto.getNombreCategoria());
        
        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Empresa empresa = empresaRepository.findById(userDetails.getIdEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        nuevaCategoria.setEmpresa(empresa);
        // Campo 'descripcion' eliminado de la entidad Categoria en el nuevo esquema

        Categoria categoriaGuardada = categoriaRepository.save(nuevaCategoria);

        return mapEntityToDto(categoriaGuardada);
    }

    @Override
    @Transactional
    public CategoriaDto actualizarCategoria(Integer id, CategoriaCreateDto updateDto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Verificar si el nuevo nombre ya está en uso por OTRA categoría
        categoriaRepository.findByNombreCategoria(updateDto.getNombreCategoria())
                .ifPresent(categoriaExistente -> {
                    if (!categoriaExistente.getIdCategoria().equals(id)) {
                        throw new IllegalArgumentException(
                                "Ya existe otra categoría con el nombre: " + updateDto.getNombreCategoria());
                    }
                });

        categoria.setNombreCategoria(updateDto.getNombreCategoria());
        // Campo 'descripcion' eliminado de la entidad Categoria en el nuevo esquema

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return mapEntityToDto(categoriaActualizada);
    }

    @Override
    @Transactional
    public void eliminarCategoria(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Validación clave: no eliminar si hay productos asociados
        if (categoria.getProductos() != null && !categoria.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría '" + categoria.getNombreCategoria()
                    + "' porque tiene productos asociados.");
        }

        categoriaRepository.delete(categoria);
    }

    private CategoriaDto mapEntityToDto(Categoria categoria) {
        CategoriaDto dto = new CategoriaDto();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombreCategoria(categoria.getNombreCategoria());
        // Campo 'descripcion' eliminado de la entidad Categoria en el nuevo esquema
        dto.setCantidadProductos(categoria.getProductos() != null ? categoria.getProductos().size() : 0);
        return dto;
    }
}
