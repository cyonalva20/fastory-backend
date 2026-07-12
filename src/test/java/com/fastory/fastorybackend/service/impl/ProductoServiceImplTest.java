package com.fastory.fastorybackend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fastory.fastorybackend.entity.Producto;
import com.fastory.fastorybackend.entity.Categoria;
import com.fastory.fastorybackend.entity.Ubicacion;
import com.fastory.fastorybackend.entity.Lote;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.CategoriaRepository;
import com.fastory.fastorybackend.repository.LoteRepository;
import com.fastory.fastorybackend.repository.ProductoRepository;
import com.fastory.fastorybackend.repository.RepisaRepository;
import com.fastory.fastorybackend.repository.UbicacionRepository;
import com.fastory.fastorybackend.repository.UsuarioRepository;
import com.fastory.fastorybackend.service.CategoriaService;
import com.fastory.fastorybackend.service.UbicacionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del servicio de Productos")
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UbicacionRepository ubicacionRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private RepisaRepository repisaRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private UbicacionService ubicacionService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoArroz;
    private Producto productoLeche;
    private Categoria categoriaAlimentos;

    @BeforeEach
    void setUp() {
        // Arrange global: preparar datos reutilizables
        categoriaAlimentos = new Categoria();
        categoriaAlimentos.setIdCategoria(1);
        categoriaAlimentos.setNombreCategoria("Alimentos");

        productoArroz = new Producto();
        productoArroz.setIdProducto(1);
        productoArroz.setNombreProducto("Arroz Extra");
        productoArroz.setUnidadMedida("kg");
        productoArroz.setStock(100);
        productoArroz.setStockMinimo(10);
        productoArroz.setPrecioCompra(3.50);
        productoArroz.setPrecioVenta(5.00);
        productoArroz.setPerecible(false);
        productoArroz.setCategoria(categoriaAlimentos);

        productoLeche = new Producto();
        productoLeche.setIdProducto(2);
        productoLeche.setNombreProducto("Leche Gloria");
        productoLeche.setUnidadMedida("litro");
        productoLeche.setStock(50);
        productoLeche.setStockMinimo(20);
        productoLeche.setPrecioCompra(4.00);
        productoLeche.setPrecioVenta(6.50);
        productoLeche.setPerecible(true);
        productoLeche.setCategoria(categoriaAlimentos);
    }

    // ══════════════════════════════════════════
    // listarTodos
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Debe retornar todos los productos del inventario")
    void listarTodos_retornaListaCompleta() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoArroz, productoLeche);
        when(productoRepository.findAll()).thenReturn(productos);

        // Act
        List<Producto> resultado = productoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Arroz Extra", resultado.get(0).getNombreProducto());
        assertEquals("Leche Gloria", resultado.get(1).getNombreProducto());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacia cuando no hay productos")
    void listarTodos_sinProductos_retornaListaVacia() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Producto> resultado = productoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(productoRepository, times(1)).findAll();
    }

    // ══════════════════════════════════════════
    // obtenerPorId
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Debe retornar producto cuando existe el ID")
    void obtenerPorId_existente_retornaProducto() {
        // Arrange
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoArroz));

        // Act
        Optional<Producto> resultado = productoService.obtenerPorId(1);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Arroz Extra", resultado.get().getNombreProducto());
        assertEquals(5.00, resultado.get().getPrecioVenta());
        verify(productoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debe retornar vacio cuando el ID no existe")
    void obtenerPorId_noExiste_retornaVacio() {
        // Arrange
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> resultado = productoService.obtenerPorId(999);

        // Assert
        assertFalse(resultado.isPresent());
        verify(productoRepository, times(1)).findById(999);
    }

    // ══════════════════════════════════════════
    // obtenerPorNombre
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Debe encontrar producto por nombre exacto")
    void obtenerPorNombre_existente_retornaProducto() {
        // Arrange
        when(productoRepository.findByNombreProducto("Arroz Extra"))
                .thenReturn(Optional.of(productoArroz));

        // Act
        Optional<Producto> resultado = productoService.obtenerPorNombre("Arroz Extra");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getIdProducto());
        verify(productoRepository).findByNombreProducto("Arroz Extra");
    }

    // ══════════════════════════════════════════
    // existePorNombre
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Debe confirmar que un producto existe por nombre")
    void existePorNombre_existente_retornaTrue() {
        // Arrange
        when(productoRepository.existsByNombreProducto("Arroz Extra")).thenReturn(true);

        // Act
        boolean resultado = productoService.existePorNombre("Arroz Extra");

        // Assert
        assertTrue(resultado);
        verify(productoRepository).existsByNombreProducto("Arroz Extra");
    }

    @Test
    @DisplayName("Debe confirmar que un producto no existe por nombre")
    void existePorNombre_noExiste_retornaFalse() {
        // Arrange
        when(productoRepository.existsByNombreProducto("Producto Fantasma")).thenReturn(false);

        // Act
        boolean resultado = productoService.existePorNombre("Producto Fantasma");

        // Assert
        assertFalse(resultado);
        verify(productoRepository).existsByNombreProducto("Producto Fantasma");
    }

    // ══════════════════════════════════════════
    // actualizar
    // ══════════════════════════════════════════

    @Test
    @DisplayName("Debe actualizar producto existente correctamente")
    void actualizar_productoExistente_actualizaCorrectamente() {
        // Arrange
        when(productoRepository.existsById(1)).thenReturn(true);
        productoArroz.setPrecioVenta(7.00);
        when(productoRepository.save(productoArroz)).thenReturn(productoArroz);

        // Act
        Producto resultado = productoService.actualizar(productoArroz);

        // Assert
        assertNotNull(resultado);
        assertEquals(7.00, resultado.getPrecioVenta());
        verify(productoRepository).existsById(1);
        verify(productoRepository).save(productoArroz);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al actualizar producto inexistente")
    void actualizar_productoNoExiste_lanzaExcepcion() {
        // Arrange
        when(productoRepository.existsById(999)).thenReturn(false);
        productoArroz.setIdProducto(999);

        // Act y Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> productoService.actualizar(productoArroz));

        assertEquals("El producto no existe", excepcion.getMessage());
        verify(productoRepository, never()).save(any());
    }
}
