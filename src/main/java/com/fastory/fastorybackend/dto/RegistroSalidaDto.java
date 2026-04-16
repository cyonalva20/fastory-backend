package com.fastory.fastorybackend.dto;

import java.util.List;

public class RegistroSalidaDto {
    private String motivo;
    private String observacion;
    private List<DetalleSalidaDto> detalles;

    // Getters y Setters
    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<DetalleSalidaDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleSalidaDto> detalles) {
        this.detalles = detalles;
    }
}
