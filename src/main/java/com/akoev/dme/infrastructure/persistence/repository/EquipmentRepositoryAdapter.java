package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.infrastructure.persistence.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentRepositoryAdapter implements EquipmentRepository {

    private final EquipmentJpaRepository equipmentJpaRepository;
    private final EquipmentMapper equipmentMapper;

    @Override
    public List<Equipment> findAll() {
        return equipmentJpaRepository.findAll().stream().map(equipmentMapper::toDomain).toList();
    }

    @Override
    public List<Equipment> findAllById(Collection<Long> ids) {
        return equipmentJpaRepository.findAllById(ids).stream().map(equipmentMapper::toDomain).toList();
    }
}
