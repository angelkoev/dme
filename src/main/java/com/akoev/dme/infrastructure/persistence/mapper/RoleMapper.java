package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.Role;
import com.akoev.dme.infrastructure.persistence.entity.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public Role toDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public Set<Role> toDomainSet(Set<RoleEntity> entities) {
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }
}
