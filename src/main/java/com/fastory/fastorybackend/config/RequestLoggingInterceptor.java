package com.fastory.fastorybackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que registra cada petición HTTP en el MDC (Mapped Diagnostic Context)
 * de SLF4J. Esto permite que Loki reciba labels estructurados como:
 *   job="api", endpoint="/api/v1/productos", method="GET", status="200"
 *
 * Grafana puede luego hacer queries como:
 *   sum by(endpoint) (count_over_time({job="api"}[30m]))
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Guardar timestamp de inicio para calcular duración
        request.setAttribute("startTime", System.currentTimeMillis());

        // Inyectar labels en el MDC para que Loki los capture
        MDC.put("job", "api");
        MDC.put("method", request.getMethod());
        MDC.put("endpoint", request.getRequestURI());
        MDC.put("ip", getClientIp(request));

        return true; // Continuar con el handler
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        // Actualizar MDC con datos de la respuesta
        MDC.put("status", String.valueOf(response.getStatus()));
        MDC.put("duration", String.valueOf(duration));

        // Determinar nivel de log según status code
        int status = response.getStatus();
        String logLine = String.format("%s %s - status: %d - time: %dms",
                request.getMethod(), request.getRequestURI(), status, duration);

        if (status >= 500) {
            MDC.put("level_label", "ERROR");
            log.error(logLine);
        } else if (status >= 400) {
            MDC.put("level_label", "WARN");
            log.warn(logLine);
        } else {
            MDC.put("level_label", "INFO");
            log.info(logLine);
        }

        // Limpiar el MDC para evitar contaminación entre requests
        MDC.clear();
    }

    private String getClientIp(HttpServletRequest request) {
        // Buscar IP real detrás del ALB/proxy
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
