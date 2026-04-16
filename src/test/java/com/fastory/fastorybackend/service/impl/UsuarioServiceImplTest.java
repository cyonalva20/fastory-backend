package com.fastory.fastorybackend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fastory.fastorybackend.entity.Rol;
import com.fastory.fastorybackend.entity.Usuario;
import com.fastory.fastorybackend.repository.UsuarioRepository;
import com.fastory.fastorybackend.service.impl.UsuarioServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioServiceImpl;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setUsername("username");
        usuario.setPassword("password");
        usuario.setApellido("Escobedo");
        usuario.setNombre("Sopp");
        usuario.setRol(new Rol());
        // FechaIngreso y Estado pueden quedar nulos según el test
    }

    // ---------- registrarUsuario (caso exitoso) ----------
    @Test
    void registrarUsuario_exitoso() {
        // Dado
        when(usuarioRepository.findByUsername(usuario.getUsername()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(usuario.getPassword()))
                .thenReturn("hashedPassword");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Cuando
        Usuario resultado = usuarioServiceImpl.registrarUsuario(usuario);

        // Entonces
        assertNotNull(resultado);
        assertEquals("hashedPassword", resultado.getPassword());
        assertNotNull(resultado.getFechaIngreso(), "La fecha de ingreso debe asignarse por defecto");
        assertTrue(resultado.getEstado(), "El estado debe ser verdadero por defecto");

        // Verificar llamadas
        verify(usuarioRepository).findByUsername(usuario.getUsername());
        verify(passwordEncoder).encode("password");
        verify(usuarioRepository).save(any(Usuario.class));

        // Capturar el usuario guardado para comprobar sus valores
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("hashedPassword", guardado.getPassword());
        assertTrue(guardado.getEstado());
        assertNotNull(guardado.getFechaIngreso());
    }

    // ---------- registrarUsuario (caso duplicado) ----------
    @Test
    void registrarUsuario_usuarioDuplicado_lanzaExcepcion() {
        // Dado
        when(usuarioRepository.findByUsername(usuario.getUsername()))
                .thenReturn(Optional.of(usuario));

        // Cuando y Entonces
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioServiceImpl.registrarUsuario(usuario));

        assertEquals("El usuario ya existe", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // ---------- buscarPorUsername ----------
    @Test
    void buscarPorUsername_encontrado() {
        when(usuarioRepository.findByUsername("username"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioServiceImpl.buscarPorUsername("username");

        assertTrue(resultado.isPresent());
        assertEquals("username", resultado.get().getUsername());
        verify(usuarioRepository).findByUsername("username");
    }

    @Test
    void buscarPorUsername_noEncontrado() {
        when(usuarioRepository.findByUsername("noExiste"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioServiceImpl.buscarPorUsername("noExiste");

        assertFalse(resultado.isPresent());
        verify(usuarioRepository).findByUsername("noExiste");
    }

    // ---------- listarUsuarios ----------

    // ---------- eliminarUsuario ----------
    @Test
    void eliminarUsuario_invocaRepositorio() {
        // Dado
        Integer id = 1;
        // Simulamos que el repositorio encuentra al usuario antes de borrarlo
        // Esto evita el ResourceNotFoundException que rompió tu compilación
        when(usuarioRepository.existsById(id)).thenReturn(true);

        // Cuando
        usuarioServiceImpl.eliminarUsuario(id);

        // Entonces
        verify(usuarioRepository, times(1)).deleteById(id);
    }
}
