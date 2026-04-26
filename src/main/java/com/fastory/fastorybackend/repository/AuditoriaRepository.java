package com.fastory.fastorybackend.repository;

import com.fastory.fastorybackend.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    List<Auditoria> findByIdEmpresaOrderByFechaAuditoriaDesc(Integer idEmpresa);

    List<Auditoria> findByIdEmpresaAndIdUsuarioOrderByFechaAuditoriaDesc(Integer idEmpresa, Integer idUsuario);

    List<Auditoria> findByIdEmpresaAndTablaAfectadaOrderByFechaAuditoriaDesc(Integer idEmpresa, String tablaAfectada);
}
