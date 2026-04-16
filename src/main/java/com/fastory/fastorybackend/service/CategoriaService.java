package com.fastory.fastorybackend.service;

import java.util.List;

import com.fastory.fastorybackend.dto.CategoriaCreateDto;
import com.fastory.fastorybackend.dto.CategoriaDto;
import com.fastory.fastorybackend.entity.Categoria;

/**
 * Interfaz del servicio para la gestión de Categorías.
 * Define las operaciones de negocio utilizando DTO para la comunicación.
 */
public interface CategoriaService {

    /**
     * Obtiene una lista simple de todas las categorías. Ideal para listas
     * seleccionables.
     * 
     * @return Lista de entidades Categoria.
     */
    List<Categoria> obtenerTodas();

    /**
     * Obtiene una lista de todas las categorías con su información y el conteo de
     * productos.
     * 
     * @return Lista de CategoriaDto.
     */
    List<CategoriaDto> obtenerTodasConConteo();

    /**
     * Crea una nueva categoría.
     * 
     * @param createDto DTO con la información para la creación.
     * @return El DTO de la categoría recién creada.
     */
    CategoriaDto crearCategoria(CategoriaCreateDto createDto);

    /**
     * Actualiza una categoría existente.
     * 
     * @param id        El ID de la categoría a actualizar.
     * @param updateDto DTO con la nueva información.
     * @return El DTO de la categoría actualizada.
     */
    CategoriaDto actualizarCategoria(Integer id, CategoriaCreateDto updateDto);

    /**
     * Elimina una categoría por su ID, solo si no tiene productos asociados.
     * 
     * @param id El ID de la categoría a eliminar.
     */
    void eliminarCategoria(Integer id);
}
