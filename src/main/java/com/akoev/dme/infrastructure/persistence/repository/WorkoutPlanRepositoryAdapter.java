package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.SessionExercise;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.repository.WorkoutPlanRepository;
import com.akoev.dme.infrastructure.persistence.entity.ExerciseEntity;
import com.akoev.dme.infrastructure.persistence.entity.SessionExerciseEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutPlanEntity;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.akoev.dme.infrastructure.persistence.mapper.WorkoutPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutPlanRepositoryAdapter implements WorkoutPlanRepository {

    private final WorkoutPlanJpaRepository workoutPlanJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ExerciseJpaRepository exerciseJpaRepository;
    private final WorkoutPlanMapper workoutPlanMapper;

    @Override
    @Transactional
    public WorkoutPlan save(WorkoutPlan plan) {
        UserEntity user = userJpaRepository.findById(plan.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + plan.getUserId()));

        WorkoutPlanEntity entity = plan.getId() != null
                ? workoutPlanJpaRepository.findById(plan.getId()).orElseGet(WorkoutPlanEntity::new)
                : new WorkoutPlanEntity();

        entity.setUser(user);
        entity.setGoal(plan.getGoal());
        entity.setGeneratedAt(plan.getGeneratedAt() != null ? plan.getGeneratedAt() : Instant.now());
        entity.setActive(plan.isActive());
        entity.setGenerationSource(plan.getGenerationSource());

        entity.getSessions().clear();
        for (WorkoutSession session : plan.getSessions()) {
            entity.getSessions().add(toSessionEntity(session, entity));
        }

        return workoutPlanMapper.toDomain(workoutPlanJpaRepository.save(entity));
    }

    private WorkoutSessionEntity toSessionEntity(WorkoutSession session, WorkoutPlanEntity plan) {
        WorkoutSessionEntity sessionEntity = new WorkoutSessionEntity();
        sessionEntity.setWorkoutPlan(plan);
        sessionEntity.setSessionIndex(session.getSessionIndex());
        sessionEntity.setName(session.getName());

        for (SessionExercise sessionExercise : session.getExercises()) {
            sessionEntity.getSessionExercises().add(toSessionExerciseEntity(sessionExercise, sessionEntity));
        }
        return sessionEntity;
    }

    private SessionExerciseEntity toSessionExerciseEntity(SessionExercise sessionExercise, WorkoutSessionEntity session) {
        ExerciseEntity exerciseEntity = exerciseJpaRepository.findById(sessionExercise.getExercise().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown exercise id: " + sessionExercise.getExercise().getId()));

        SessionExerciseEntity entity = new SessionExerciseEntity();
        entity.setWorkoutSession(session);
        entity.setExercise(exerciseEntity);
        entity.setOrderIndex(sessionExercise.getOrderIndex());
        entity.setSets(sessionExercise.getSets());
        entity.setRepRangeMin(sessionExercise.getRepRangeMin());
        entity.setRepRangeMax(sessionExercise.getRepRangeMax());
        entity.setRestSeconds(sessionExercise.getRestSeconds());
        entity.setNotes(sessionExercise.getNotes());
        return entity;
    }

    @Override
    public Optional<WorkoutPlan> findById(Long id) {
        return workoutPlanJpaRepository.findById(id).map(workoutPlanMapper::toDomain);
    }

    @Override
    public List<WorkoutPlan> findAllByUserId(Long userId) {
        return workoutPlanJpaRepository.findAllByUser_Id(userId).stream().map(workoutPlanMapper::toDomain).toList();
    }

    @Override
    public Optional<WorkoutPlan> findActiveByUserId(Long userId) {
        return workoutPlanJpaRepository.findFirstByUser_IdAndActiveTrue(userId).map(workoutPlanMapper::toDomain);
    }
}
