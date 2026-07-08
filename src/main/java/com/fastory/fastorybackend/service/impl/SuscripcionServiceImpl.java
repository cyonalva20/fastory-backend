package com.fastory.fastorybackend.service.impl;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastory.fastorybackend.dto.SuscripcionEstadoDto;
import com.fastory.fastorybackend.entity.Empresa;
import com.fastory.fastorybackend.exception.ResourceNotFoundException;
import com.fastory.fastorybackend.repository.EmpresaRepository;
import com.fastory.fastorybackend.service.SuscripcionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public SuscripcionEstadoDto obtenerEstado(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        return mapearADto(empresa);
    }

    @Override
    @Transactional
    public SuscripcionEstadoDto renovar(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime nuevaFechaVencimiento;

        if (empresa.getFechaVencimiento() != null && empresa.getFechaVencimiento().isAfter(now)) {
            // Si no ha vencido, suma 30 días a la fecha actual de vencimiento
            nuevaFechaVencimiento = empresa.getFechaVencimiento().plusDays(30);
        } else {
            // Si ya venció o no tiene, suma 30 días desde hoy
            nuevaFechaVencimiento = now.plusDays(30);
        }

        empresa.setFechaVencimiento(nuevaFechaVencimiento);
        empresa.setEstadoSuscripcion("ACTIVO");

        Empresa guardada = empresaRepository.save(empresa);

        return mapearADto(guardada);
    }

    @Override
    @Transactional
    public void verificarYActualizarVencimientos() {
        // En un entorno de producción con miles de empresas, esto se debería hacer con
        // un update masivo directo en BD por rendimiento (ej. @Modifying en Repository),
        // pero lo implementaremos usando JPA por simplicidad.
        List<Empresa> empresas = empresaRepository.findAll();
        OffsetDateTime now = OffsetDateTime.now();

        int actualizadas = 0;

        for (Empresa emp : empresas) {
            if (!"VENCIDO".equals(emp.getEstadoSuscripcion()) && emp.getFechaVencimiento() != null) {
                if (emp.getFechaVencimiento().isBefore(now)) {
                    emp.setEstadoSuscripcion("VENCIDO");
                    empresaRepository.save(emp);
                    actualizadas++;
                }
            }
        }
    }

    private SuscripcionEstadoDto mapearADto(Empresa empresa) {
        long diasRestantes = 0;
        String mensaje = "";
        OffsetDateTime now = OffsetDateTime.now();

        if (empresa.getFechaVencimiento() != null) {
            diasRestantes = ChronoUnit.DAYS.between(now, empresa.getFechaVencimiento());
            if (diasRestantes < 0) {
                diasRestantes = 0;
            }
        }

        switch (empresa.getEstadoSuscripcion()) {
            case "PRUEBA":
                mensaje = "Estás en el periodo de prueba gratuita. Te quedan " + diasRestantes + " días.";
                break;
            case "ACTIVO":
                mensaje = "Tu suscripción está activa. Se renovará en " + diasRestantes + " días.";
                break;
            case "VENCIDO":
                mensaje = "Tu suscripción ha vencido. Por favor, renueva para seguir utilizando el sistema.";
                break;
            default:
                mensaje = "Estado de suscripción desconocido.";
        }

        return SuscripcionEstadoDto.builder()
                .estadoSuscripcion(empresa.getEstadoSuscripcion())
                .fechaVencimiento(empresa.getFechaVencimiento())
                .diasRestantes(diasRestantes)
                .mensaje(mensaje)
                .build();
    }
}
