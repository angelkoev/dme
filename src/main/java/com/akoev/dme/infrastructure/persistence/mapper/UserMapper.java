package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.User;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserLimitationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleMapper roleMapper;
    private final UserProfileMapper userProfileMapper;

    public User toDomain(UserEntity entity, List<UserLimitationEntity> limitationEntities) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .roles(roleMapper.toDomainSet(entity.getRoles()))
                .profile(entity.getProfile() != null
                        ? userProfileMapper.toDomain(entity.getProfile(), limitationEntities)
                        : null)
                .build();
    }
}
