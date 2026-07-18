package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.infrastructure.persistence.entity.UserLimitationEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    private final EquipmentMapper equipmentMapper;
    private final UserLimitationMapper userLimitationMapper;
    private final ExerciseMapper exerciseMapper;

    public UserProfile toDomain(UserProfileEntity entity, List<UserLimitationEntity> limitationEntities) {
        if (entity == null) {
            return null;
        }
        List<UserLimitation> limitations = userLimitationMapper.toDomainList(limitationEntities);
        return UserProfile.builder()
                .userId(entity.getId())
                .birthDate(entity.getBirthDate())
                .sex(entity.getSex())
                .heightCm(entity.getHeightCm())
                .weightKg(entity.getWeightKg())
                .experienceLevel(entity.getExperienceLevel())
                .primaryGoal(entity.getPrimaryGoal())
                .daysPerWeek(entity.getDaysPerWeek())
                .sessionDurationMinutes(entity.getSessionDurationMinutes())
                .notes(entity.getNotes())
                .location(entity.getLocation())
                .availableEquipment(equipmentMapper.toDomainSet(entity.getAvailableEquipment()))
                .limitations(limitations)
                .restDays(new HashSet<>(entity.getRestDays()))
                .preferredCategories(new HashSet<>(entity.getPreferredCategories()))
                .unwantedCategories(new HashSet<>(entity.getUnwantedCategories()))
                .favoriteExercises(exerciseMapper.toDomainSet(entity.getFavoriteExercises()))
                .dislikedExercises(exerciseMapper.toDomainSet(entity.getDislikedExercises()))
                .build();
    }
}
