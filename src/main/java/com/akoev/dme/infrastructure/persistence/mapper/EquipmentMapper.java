package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.infrastructure.persistence.entity.EquipmentEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EquipmentMapper {

    public Equipment toDomain(EquipmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Equipment.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public Set<Equipment> toDomainSet(Set<EquipmentEntity> entities) {
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }
}
