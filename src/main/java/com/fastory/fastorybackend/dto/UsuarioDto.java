package com.fastory.fastorybackend.dto;

import java.time.OffsetDateTime;

public class UsuarioDto {
    private Integer idUsuario;
    private String username;
    private String nombre;
    private String apellido;
    private String nombreRol;
    private String email;
    private Integer idRol;
    private OffsetDateTime fechaIngreso;
    private Boolean estado;

    public UsuarioDto(Integer idUsuario, String username, String nombre, String apellido, String nombreRol,
            Integer idRol, OffsetDateTime fechaIngreso, Boolean estado, String email) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreRol = nombreRol;
        this.idRol = idRol;
        this.fechaIngreso = fechaIngreso;
        this.estado = estado;
        this.email = email;
    }

    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public OffsetDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(OffsetDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}