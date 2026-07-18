package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutLogEntity;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.akoev.dme.infrastructure.persistence.mapper.WorkoutLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutLogRepositoryAdapter implements WorkoutLogRepository {

    private final WorkoutLogJpaRepository workoutLogJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final WorkoutSessionJpaRepository workoutSessionJpaRepository;
    private final WorkoutLogMapper workoutLogMapper;

    @Override
    @Transactional
    public WorkoutLog save(WorkoutLog log) {
        WorkoutSessionEntity session = workoutSessionJpaRepository.findById(log.getWorkoutSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown workout session id: " + log.getWorkoutSessionId()));
        UserEntity user = userJpaRepository.findById(log.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + log.getUserId()));

        WorkoutLogEntity entity = log.getId() != null
                ? workoutLogJpaRepository.findById(log.getId()).orElseGet(WorkoutLogEntity::new)
                : new WorkoutLogEntity();
        entity.setWorkoutSession(session);
        entity.setUser(user);
        entity.setPerformedAt(log.getPerformedAt() != null ? log.getPerformedAt() : Instant.now());
        entity.setCompletionPercentage(log.getCompletionPercentage());
        entity.setRating(log.getRating());
        entity.setPerceivedIntensity(log.getPerceivedIntensity());
        entity.setNotes(log.getNotes());

        return workoutLogMapper.toDomain(workoutLogJpaRepository.save(entity));
    }

    @Override
    public List<WorkoutLog> findRecentByUserId(Long userId, Instant since) {
        return workoutLogJpaRepository.findAllByUser_IdAndPerformedAtAfterOrderByPerformedAtDesc(userId, since)
                .stream().map(workoutLogMapper::toDomain).toList();
    }

    @Override
    public List<WorkoutLog> findAllByUserId(Long userId) {
        return workoutLogJpaRepository.findAllByUser_IdOrderByPerformedAtDesc(userId)
                .stream().map(workoutLogMapper::toDomain).toList();
    }
}
