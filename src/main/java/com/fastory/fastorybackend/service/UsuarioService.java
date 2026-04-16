package com.fastory.fastorybackend.service;

import java.util.List;
import java.util.Optional;

import com.fastory.fastorybackend.dto.UsuarioDto;
import com.fastory.fastorybackend.dto.UsuarioRequestDto;
import com.fastory.fastorybackend.entity.Usuario;

public interface UsuarioService {
    Optional<Usuario> buscarPorUsername(String username);

    void eliminarUsuario(Integer idUsuario);

    Usuario registrarUsuario(Usuario usuario); // Mantenemos el original para AuthController

    UsuarioDto crearUsuario(UsuarioRequestDto request); // Nuevo para admin

    UsuarioDto actualizarUsuario(Integer id, UsuarioRequestDto request);

    List<UsuarioDto> listarUsuariosDto(); // Retorna para la tabla
}
