package com.fastory.fastorybackend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // Importar

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling // Habilitar tareas programadas
public class FastorybackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastorybackendApplication.class, args);
    }

    /**
     * Este método se ejecuta una sola vez justo después de que la aplicación
     * inicia.
     * Establece la zona horaria por defecto de toda la JVM a America/Lima.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        System.out.println("Zona horaria configurada a: " + new java.util.Date());
    }
}
