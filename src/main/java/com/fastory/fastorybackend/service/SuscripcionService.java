package com.fastory.fastorybackend.service;

import com.fastory.fastorybackend.dto.SuscripcionEstadoDto;

public interface SuscripcionService {
    SuscripcionEstadoDto obtenerEstado(Integer idEmpresa);
    SuscripcionEstadoDto renovar(Integer idEmpresa);
    void verificarYActualizarVencimientos();
}
