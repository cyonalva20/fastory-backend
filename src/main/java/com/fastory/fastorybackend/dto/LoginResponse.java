package com.fastory.fastorybackend.dto;

public class LoginResponse {
    private String message;
    private String rol;
    private String token; // null por el momento hasta implementar JWT
    private Integer idUsuario;

    public LoginResponse() {
    }

    public LoginResponse(String message, String rol, String token, Integer idUsuario) {
        this.message = message;
        this.rol = rol;
        this.token = token;
        this.idUsuario = idUsuario; // <-- AÑADIDO
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // --- AÑADIDO GETTER Y SETTER ---
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

}
