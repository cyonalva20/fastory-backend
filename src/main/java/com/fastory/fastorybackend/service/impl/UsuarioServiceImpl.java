package com.fastory.fastorybackend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.UsuarioDto;
import com.fastory.fastorybackend.dto.UsuarioRequestDto;
import com.fastory.fastorybackend.entity.Rol;
import com.fastory.fastorybackend.entity.Usuario;
import com.fastory.fastorybackend.entity.Empresa;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.RolRepository;
import com.fastory.fastorybackend.repository.UsuarioRepository;
import com.fastory.fastorybackend.repository.EmpresaRepository;
import com.fastory.fastorybackend.service.UsuarioService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import com.fastory.fastorybackend.config.TenantUserDetails;

@Service
@lombok.RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;

    private final RolRepository rolRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmpresaRepository empresaRepository;

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        // Validacion de duplicados
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        if (usuario.getFechaIngreso() == null) {
            usuario.setFechaIngreso(OffsetDateTime.now());
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }
        String hashed = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(hashed);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void registrarOnboarding(com.fastory.fastorybackend.dto.RegistroRequest request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        // Crear la empresa
        Empresa empresa = new Empresa();
        empresa.setNombreEmpresa(request.getNombreEmpresa());
        empresa.setEstado("PRUEBA");
        // No se mapea RUC/direccion porque el request basico podria no tenerlos
        Empresa empresaGuardada = empresaRepository.save(empresa);

        // Buscar rol administrador (asumiendo que existe en la BD o se crea un ID por defecto, p.ej. idRol = 1 o nombre = 'ADMINISTRADOR')
        Rol rolAdmin = rolRepository.findByNombreRol("ADMINISTRADOR")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ADMINISTRADOR no encontrado en el sistema"));

        Usuario admin = new Usuario();
        admin.setNombre(request.getNombre());
        admin.setApellido(request.getApellido());
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setEmail(request.getEmail());
        admin.setRol(rolAdmin);
        admin.setEmpresa(empresaGuardada);
        admin.setFechaIngreso(OffsetDateTime.now());
        admin.setEstado(true);

        usuarioRepository.save(admin);
    }

    @Override
    @Transactional
    public UsuarioDto crearUsuario(UsuarioRequestDto request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setFechaIngreso(OffsetDateTime.now());
        usuario.setEstado(true);
        usuario.setEmail(request.getEmail());

        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Empresa empresa = empresaRepository.findById(userDetails.getIdEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        usuario.setEmpresa(empresa);

        Usuario guardado = usuarioRepository.save(usuario);
        return mapToDto(guardado);
    }

    @Override
    @Transactional
    public UsuarioDto actualizarUsuario(Integer id, UsuarioRequestDto request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar si cambió el username y si ya existe
        if (!usuario.getUsername().equals(request.getUsername()) &&
                usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());

        // Actualizar contraseña solo si viene en el request
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Actualizar Rol
        if (!usuario.getRol().getIdRol().equals(request.getIdRol())) {
            Rol nuevoRol = rolRepository.findById(request.getIdRol())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
            usuario.setRol(nuevoRol);
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapToDto(actualizado);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuariosDto() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarUsuario(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(idUsuario);
    }

    private UsuarioDto mapToDto(Usuario u) {
        return new UsuarioDto(
                u.getIdUsuario(),
                u.getUsername(),
                u.getNombre(),
                u.getApellido(),
                u.getRol().getNombreRol(),
                u.getRol().getIdRol(),
                u.getFechaIngreso(),
                u.getEstado(),
                u.getEmail(),
                u.getIdEmpresa());
    }
}