package com.fastory.fastorybackend.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fastory.fastorybackend.service.SuscripcionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuscripcionScheduler {

    private final SuscripcionService suscripcionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void verificarYActualizarVencimientos() {
        log.info("Iniciando tarea programada: Verificación de vencimientos de suscripción...");
        suscripcionService.verificarYActualizarVencimientos();
        log.info("Finalizó tarea programada: Verificación de vencimientos de suscripción.");
    }
}
