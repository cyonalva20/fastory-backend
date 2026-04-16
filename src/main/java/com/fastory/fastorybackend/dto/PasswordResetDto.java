package com.fastory.fastorybackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetDto {

    // DTO para solicitar código
    public static class ForgotRequest {
        @NotBlank
        @Email
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // DTO para verificar código
    public static class VerifyCodeRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 6, max = 6)
        private String codigo;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }
    }

    // DTO para cambiar contraseña
    public static class ChangePasswordRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 6, max = 6)
        private String codigo; // Se requiere el código nuevamente por seguridad
        @NotBlank
        @Size(min = 8)
        private String newPassword;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}