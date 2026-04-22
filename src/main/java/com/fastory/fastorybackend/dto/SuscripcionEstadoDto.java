package com.fastory.fastorybackend.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionEstadoDto {
    private String estadoSuscripcion;
    private OffsetDateTime fechaVencimiento;
    private long diasRestantes;
    private String mensaje;
}
