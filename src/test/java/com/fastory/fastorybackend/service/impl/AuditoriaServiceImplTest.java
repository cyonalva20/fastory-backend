package com.fastory.fastorybackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fastory.fastorybackend.entity.Auditoria;
import com.fastory.fastorybackend.repository.AuditoriaRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del servicio de Auditoria")
class AuditoriaServiceImplTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditoriaServiceImpl auditoriaService;

    private Auditoria auditoria1;
    private Auditoria auditoria2;

    @BeforeEach
    void setUp() {
        // Arrange global: preparar datos reutilizables
        auditoria1 = new Auditoria();
        auditoria1.setIdAuditoria(1);
        auditoria1.setIdEmpresa(1);
        auditoria1.setIdUsuario(10);
        auditoria1.setTablaAfectada("producto");
        auditoria1.setIdRegistro(5);
        auditoria1.setAccion("CREAR");
        auditoria1.setDatosNuevos("{\"nombre\":\"Arroz\"}");

        auditoria2 = new Auditoria();
        auditoria2.setIdAuditoria(2);
        auditoria2.setIdEmpresa(1);
        auditoria2.setIdUsuario(10);
        auditoria2.setTablaAfectada("movimiento");
        auditoria2.setIdRegistro(8);
        auditoria2.setAccion("ACTUALIZAR");
        auditoria2.setDatosAnteriores("{\"cantidad\":10}");
        auditoria2.setDatosNuevos("{\"cantidad\":20}");
    }

    @Test
    @DisplayName("Debe retornar todas las auditorias de una empresa")
    void obtenerTodas_conRegistros_retornaLista() {
        // Arrange
        List<Auditoria> auditorias = Arrays.asList(auditoria1, auditoria2);
        when(auditoriaRepository.findByIdEmpresaOrderByFechaAuditoriaDesc(1)).thenReturn(auditorias);

        // Act
        List<Auditoria> resultado = auditoriaService.obtenerTodas(1);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("CREAR", resultado.get(0).getAccion());
        verify(auditoriaRepository, times(1)).findByIdEmpresaOrderByFechaAuditoriaDesc(1);
    }

    @Test
    @DisplayName("Debe retornar lista vacia cuando no hay auditorias")
    void obtenerTodas_sinRegistros_retornaListaVacia() {
        // Arrange
        when(auditoriaRepository.findByIdEmpresaOrderByFechaAuditoriaDesc(99)).thenReturn(Collections.emptyList());

        // Act
        List<Auditoria> resultado = auditoriaService.obtenerTodas(99);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar auditorias filtradas por usuario")
    void obtenerPorUsuario_conRegistros_retornaFiltrado() {
        // Arrange
        when(auditoriaRepository.findByIdEmpresaAndIdUsuarioOrderByFechaAuditoriaDesc(1, 10))
                .thenReturn(Arrays.asList(auditoria1, auditoria2));

        // Act
        List<Auditoria> resultado = auditoriaService.obtenerPorUsuario(1, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        resultado.forEach(a -> assertEquals(10, a.getIdUsuario()));
    }

    @Test
    @DisplayName("Debe retornar lista vacia para usuario sin actividad")
    void obtenerPorUsuario_sinActividad_retornaListaVacia() {
        // Arrange
        when(auditoriaRepository.findByIdEmpresaAndIdUsuarioOrderByFechaAuditoriaDesc(1, 999))
                .thenReturn(Collections.emptyList());

        // Act
        List<Auditoria> resultado = auditoriaService.obtenerPorUsuario(1, 999);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar auditorias filtradas por tabla afectada")
    void obtenerPorTabla_conRegistros_retornaFiltrado() {
        // Arrange
        when(auditoriaRepository.findByIdEmpresaAndTablaAfectadaOrderByFechaAuditoriaDesc(1, "producto"))
                .thenReturn(List.of(auditoria1));

        // Act
        List<Auditoria> resultado = auditoriaService.obtenerPorTabla(1, "producto");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("producto", resultado.get(0).getTablaAfectada());
    }

    @Test
    @DisplayName("Debe retornar lista vacia para tabla sin auditorias")
    void obtenerPorTabla_tablaSinRegistros_retornaListaVacia() {
        // Arrange
        when(auditoriaRepository.findByIdEmpresaAndTablaAfectadaOrderByFechaAuditoriaDesc(1, "inexistente"))
                .thenReturn(Collections.emptyList());

        // Act
        List<Auditoria> resultado = auditoriaService.obtenerPorTabla(1, "inexistente");

        // Assert
        assertTrue(resultado.isEmpty());
    }
}
