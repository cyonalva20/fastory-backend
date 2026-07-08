package com.fastory.fastorybackend.service.impl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fastory.fastorybackend.dto.CategoriaDto;
import com.fastory.fastorybackend.entity.Categoria;
import com.fastory.fastorybackend.entity.Producto;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.CategoriaRepository;
import com.fastory.fastorybackend.repository.EmpresaRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del servicio de Categorias")
class CategoriaServiceImplTest {
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @InjectMocks
    private CategoriaServiceImpl categoriaService;
    private Categoria categoriaElectronica;
    private Categoria categoriaAlimentos;
    @BeforeEach
    void setUp() {
        // Arrange global: preparar datos reutilizables
        categoriaElectronica = new Categoria();
        categoriaElectronica.setIdCategoria(1);
        categoriaElectronica.setNombreCategoria("Electronica");
        categoriaElectronica.setProductos(new ArrayList<>());
        categoriaAlimentos = new Categoria();
        categoriaAlimentos.setIdCategoria(2);
        categoriaAlimentos.setNombreCategoria("Alimentos");
        categoriaAlimentos.setProductos(new ArrayList<>());
    }
    @Test
    @DisplayName("Debe retornar todas las categorias existentes")
    void obtenerTodas_retornaListaCompleta() {
        // Arrange
        List<Categoria> categorias = Arrays.asList(categoriaElectronica, categoriaAlimentos);
        when(categoriaRepository.findAll()).thenReturn(categorias);
        // Act
        List<Categoria> resultado = categoriaService.obtenerTodas();
        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Electronica", resultado.get(0).getNombreCategoria());
        verify(categoriaRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Debe retornar lista vacia cuando no hay categorias")
    void obtenerTodas_sinCategorias_retornaListaVacia() {
        // Arrange
        when(categoriaRepository.findAll()).thenReturn(Collections.emptyList());
        // Act
        List<Categoria> resultado = categoriaService.obtenerTodas();
        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(categoriaRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Debe retornar DTOs con conteo de productos correcto")
    void obtenerTodasConConteo_retornaDtosConConteo() {
        // Arrange
        Producto producto1 = new Producto();
        producto1.setIdProducto(1);
        Producto producto2 = new Producto();
        producto2.setIdProducto(2);
        categoriaElectronica.setProductos(Arrays.asList(producto1, producto2));
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoriaElectronica, categoriaAlimentos));
        // Act
        List<CategoriaDto> resultado = categoriaService.obtenerTodasConConteo();
        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(2, resultado.get(0).getCantidadProductos());
        assertEquals(0, resultado.get(1).getCantidadProductos());
    }
    @Test
    @DisplayName("Debe eliminar categoria sin productos asociados")
    void eliminarCategoria_sinProductos_eliminaCorrectamente() {
        // Arrange
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaElectronica));
        // Act
        categoriaService.eliminarCategoria(1);
        // Assert
        verify(categoriaRepository, times(1)).delete(categoriaElectronica);
    }
    @Test
    @DisplayName("Debe lanzar excepcion al eliminar categoria con productos")
    void eliminarCategoria_conProductos_lanzaExcepcion() {
        // Arrange
        Producto producto = new Producto();
        producto.setIdProducto(1);
        categoriaElectronica.setProductos(List.of(producto));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaElectronica));
        // Act y Assert
        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                () -> categoriaService.eliminarCategoria(1));
        assertTrue(excepcion.getMessage().contains("tiene productos asociados"));
        verify(categoriaRepository, never()).delete(any());
    }
    @Test
    @DisplayName("Debe lanzar excepcion al eliminar categoria inexistente")
    void eliminarCategoria_noExiste_lanzaExcepcion() {
        // Arrange
        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());
        // Act y Assert
        assertThrows(ResourceNotFoundException.class,
                () -> categoriaService.eliminarCategoria(999));
        verify(categoriaRepository, never()).delete(any());
    }
}
