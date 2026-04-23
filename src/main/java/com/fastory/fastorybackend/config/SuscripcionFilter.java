package com.fastory.fastorybackend.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fastory.fastorybackend.repository.EmpresaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuscripcionFilter extends OncePerRequestFilter {

    private final EmpresaRepository empresaRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Evitar el filtro para OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bypassear rutas de autenticación y suscripción
        if (path.startsWith("/auth/") || path.startsWith("/api/v1/suscripcion/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof TenantUserDetails) {
            TenantUserDetails userDetails = (TenantUserDetails) authentication.getPrincipal();

            // Buscar empresa en base de datos para obtener el estado real
            empresaRepository.findById(userDetails.getIdEmpresa()).ifPresentOrElse(empresa -> {
                if ("VENCIDO".equals(empresa.getEstadoSuscripcion())) {
                    log.warn("Acceso bloqueado para el tenant {} por suscripción vencida.", empresa.getIdEmpresa());
                    bloquearPorPagoRequerido(response);
                } else {
                    try {
                        filterChain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, () -> {
                try {
                    filterChain.doFilter(request, response);
                } catch (IOException | ServletException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            // Si no hay contexto de seguridad de tenant (por ejemplo, rutas públicas que el
            // JWT filter dejó pasar), simplemente continuamos.
            filterChain.doFilter(request, response);
        }
    }

    private void bloquearPorPagoRequerido(HttpServletResponse response) {
        response.setStatus(HttpStatus.PAYMENT_REQUIRED.value());
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse("Suscripción Vencida. Por favor renueve su suscripción para continuar.")));
        } catch (IOException e) {
            log.error("Error al escribir respuesta 402", e);
        }
    }

    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
