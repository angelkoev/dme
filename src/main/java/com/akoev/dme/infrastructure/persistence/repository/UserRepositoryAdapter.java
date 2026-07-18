package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.infrastructure.persistence.entity.EquipmentEntity;
import com.akoev.dme.infrastructure.persistence.entity.RoleEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserLimitationEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserProfileEntity;
import com.akoev.dme.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final EquipmentJpaRepository equipmentJpaRepository;
    private final UserLimitationJpaRepository userLimitationJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(this::toDomainWithLimitations);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(this::toDomainWithLimitations);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = user.getId() != null
                ? userJpaRepository.findById(user.getId()).orElseGet(UserEntity::new)
                : new UserEntity();

        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setEnabled(user.isEnabled());
        entity.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now());
        entity.setRoles(resolveRoles(user));

        if (user.getProfile() != null) {
            UserProfileEntity profileEntity = entity.getProfile() != null ? entity.getProfile() : new UserProfileEntity();
            applyProfile(profileEntity, user.getProfile());
            profileEntity.setUser(entity);
            entity.setProfile(profileEntity);
        }

        UserEntity saved = userJpaRepository.save(entity);

        if (user.getProfile() != null) {
            replaceLimitations(saved, user.getProfile().getLimitations());
        }

        return toDomainWithLimitations(saved);
    }

    private Set<RoleEntity> resolveRoles(User user) {
        return user.getRoles().stream()
                .map(role -> roleJpaRepository.findByName(role.getName())
                        .orElseThrow(() -> new IllegalStateException("Unknown role: " + role.getName())))
                .collect(Collectors.toSet());
    }

    private void applyProfile(UserProfileEntity profileEntity, UserProfile profile) {
        profileEntity.setBirthDate(profile.getBirthDate());
        profileEntity.setSex(profile.getSex());
        profileEntity.setHeightCm(profile.getHeightCm());
        profileEntity.setWeightKg(profile.getWeightKg());
        profileEntity.setExperienceLevel(profile.getExperienceLevel());
        profileEntity.setPrimaryGoal(profile.getPrimaryGoal());
        profileEntity.setDaysPerWeek(profile.getDaysPerWeek());
        profileEntity.setSessionDurationMinutes(profile.getSessionDurationMinutes());
        profileEntity.setNotes(profile.getNotes());

        List<Long> equipmentIds = profile.getAvailableEquipment().stream().map(Equipment::getId).toList();
        Set<EquipmentEntity> equipmentEntities = new HashSet<>(equipmentJpaRepository.findAllById(equipmentIds));
        profileEntity.setAvailableEquipment(equipmentEntities);
    }

    private void replaceLimitations(UserEntity user, List<UserLimitation> limitations) {
        userLimitationJpaRepository.deleteAll(userLimitationJpaRepository.findAllByUser_Id(user.getId()));
        List<UserLimitationEntity> newEntities = limitations.stream()
                .map(limitation -> UserLimitationEntity.builder()
                        .user(user)
                        .muscleGroup(limitation.getMuscleGroup())
                        .note(limitation.getNote())
                        .build())
                .toList();
        userLimitationJpaRepository.saveAll(newEntities);
    }

    private User toDomainWithLimitations(UserEntity entity) {
        List<UserLimitationEntity> limitations = entity.getProfile() != null
                ? userLimitationJpaRepository.findAllByUser_Id(entity.getId())
                : List.of();
        return userMapper.toDomain(entity, limitations);
    }
}
