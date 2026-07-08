package com.fastory.fastorybackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastory.fastorybackend.config.TenantUserDetails;
import com.fastory.fastorybackend.entity.Auditoria;
import com.fastory.fastorybackend.repository.AuditoriaRepository;
import com.fastory.fastorybackend.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@lombok.RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaServiceImpl.class);

    private final AuditoriaRepository auditoriaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void registrar(String tablaAfectada, Integer idRegistro, String accion,
                          Object datosAnteriores, Object datosNuevos) {
        TenantUserDetails userDetails = (TenantUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Auditoria auditoria = new Auditoria();
        auditoria.setIdEmpresa(userDetails.getIdEmpresa());
        auditoria.setIdUsuario(userDetails.getIdUsuario());
        auditoria.setTablaAfectada(tablaAfectada);
        auditoria.setIdRegistro(idRegistro);
        auditoria.setAccion(accion);
        auditoria.setDatosAnteriores(serializarAJson(datosAnteriores));
        auditoria.setDatosNuevos(serializarAJson(datosNuevos));

        auditoriaRepository.save(auditoria);
    }

    @Override
    public List<Auditoria> obtenerTodas(Integer idEmpresa) {
        return auditoriaRepository.findByIdEmpresaOrderByFechaAuditoriaDesc(idEmpresa);
    }

    @Override
    public List<Auditoria> obtenerPorUsuario(Integer idEmpresa, Integer idUsuario) {
        return auditoriaRepository.findByIdEmpresaAndIdUsuarioOrderByFechaAuditoriaDesc(idEmpresa, idUsuario);
    }

    @Override
    public List<Auditoria> obtenerPorTabla(Integer idEmpresa, String tabla) {
        return auditoriaRepository.findByIdEmpresaAndTablaAfectadaOrderByFechaAuditoriaDesc(idEmpresa, tabla);
    }

    /**
     * Serializa un objeto a JSON String usando ObjectMapper.
     * Retorna null si el objeto es null.
     */
    private String serializarAJson(Object objeto) {
        if (objeto == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (Exception e) {
            logger.error("Error al serializar objeto a JSON para auditoría: " + e.getMessage());
            return "{}";
        }
    }
}
