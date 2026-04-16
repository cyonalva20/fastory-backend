package com.fastory.fastorybackend.service.impl;

import org.springframework.stereotype.Service;

import com.fastory.fastorybackend.entity.Rol;
import com.fastory.fastorybackend.repository.RolRepository;
import com.fastory.fastorybackend.service.RolService;

import java.util.List;

@Service
@lombok.RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }
}
