package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserLimitationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserLimitationMapper {

    public UserLimitation toDomain(UserLimitationEntity entity) {
        return UserLimitation.builder()
                .id(entity.getId())
                .muscleGroup(entity.getMuscleGroup())
                .note(entity.getNote())
                .build();
    }

    public List<UserLimitation> toDomainList(List<UserLimitationEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    public UserLimitationEntity toEntity(UserLimitation domain, UserEntity owner) {
        UserLimitationEntity entity = new UserLimitationEntity();
        entity.setUser(owner);
        entity.setMuscleGroup(domain.getMuscleGroup());
        entity.setNote(domain.getNote());
        return entity;
    }
}
