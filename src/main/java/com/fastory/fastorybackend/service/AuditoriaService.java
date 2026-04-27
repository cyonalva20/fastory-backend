package com.fastory.fastorybackend.service;

import com.fastory.fastorybackend.entity.Auditoria;

import java.util.List;

public interface AuditoriaService {

    /**
     * Registra un evento de auditoría.
     * Obtiene idEmpresa e idUsuario automáticamente del SecurityContext.
     *
     * @param tablaAfectada  nombre de la tabla afectada (ej: "movimiento_inventario")
     * @param idRegistro     ID del registro afectado
     * @param accion         tipo de acción: CREAR, MODIFICAR, ELIMINAR
     * @param datosAnteriores estado anterior del registro (se serializa a JSON)
     * @param datosNuevos    estado nuevo del registro (se serializa a JSON)
     */
    void registrar(String tablaAfectada, Integer idRegistro, String accion,
                   Object datosAnteriores, Object datosNuevos);

    List<Auditoria> obtenerTodas(Integer idEmpresa);

    List<Auditoria> obtenerPorUsuario(Integer idEmpresa, Integer idUsuario);

    List<Auditoria> obtenerPorTabla(Integer idEmpresa, String tabla);
}
