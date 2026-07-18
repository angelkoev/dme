package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.infrastructure.persistence.entity.EquipmentEntity;
import com.akoev.dme.infrastructure.persistence.entity.ExerciseEntity;
import com.akoev.dme.infrastructure.persistence.mapper.ExerciseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseRepositoryAdapter implements ExerciseRepository {

    private final ExerciseJpaRepository exerciseJpaRepository;
    private final EquipmentJpaRepository equipmentJpaRepository;
    private final ExerciseMapper exerciseMapper;

    @Override
    public List<Exercise> findAll() {
        return exerciseJpaRepository.findAll().stream().map(exerciseMapper::toDomain).toList();
    }

    @Override
    public Optional<Exercise> findById(Long id) {
        return exerciseJpaRepository.findById(id).map(exerciseMapper::toDomain);
    }

    @Override
    public List<Exercise> findAllById(Collection<Long> ids) {
        return exerciseJpaRepository.findAllById(ids).stream().map(exerciseMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Exercise save(Exercise exercise) {
        ExerciseEntity entity = exercise.getId() != null
                ? exerciseJpaRepository.findById(exercise.getId()).orElseGet(ExerciseEntity::new)
                : new ExerciseEntity();

        entity.setName(exercise.getName());
        entity.setDescription(exercise.getDescription());
        entity.setPrimaryMuscleGroup(exercise.getPrimaryMuscleGroup());
        entity.setMovementPattern(exercise.getMovementPattern());
        entity.setDifficultyLevel(exercise.getDifficultyLevel());
        entity.setExerciseType(exercise.getExerciseType());
        entity.setInstructions(exercise.getInstructions());
        entity.setVideoUrl(exercise.getVideoUrl());

        List<Long> equipmentIds = exercise.getRequiredEquipment().stream().map(Equipment::getId).toList();
        Set<EquipmentEntity> equipmentEntities = new HashSet<>(equipmentJpaRepository.findAllById(equipmentIds));
        entity.setRequiredEquipment(equipmentEntities);

        return exerciseMapper.toDomain(exerciseJpaRepository.save(entity));
    }
}
