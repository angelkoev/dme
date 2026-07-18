package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.WorkoutStreak;
import com.akoev.dme.domain.repository.WorkoutStreakRepository;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutStreakEntity;
import com.akoev.dme.infrastructure.persistence.mapper.WorkoutStreakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutStreakRepositoryAdapter implements WorkoutStreakRepository {

    private final WorkoutStreakJpaRepository workoutStreakJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final WorkoutStreakMapper workoutStreakMapper;

    @Override
    public Optional<WorkoutStreak> findByUserId(Long userId) {
        return workoutStreakJpaRepository.findById(userId).map(workoutStreakMapper::toDomain);
    }

    @Override
    @Transactional
    public WorkoutStreak save(WorkoutStreak streak) {
        UserEntity user = userJpaRepository.findById(streak.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + streak.getUserId()));

        WorkoutStreakEntity entity = workoutStreakJpaRepository.findById(streak.getUserId())
                .orElseGet(WorkoutStreakEntity::new);
        entity.setUser(user);
        entity.setCurrentStreak(streak.getCurrentStreak());
        entity.setLongestStreak(streak.getLongestStreak());
        entity.setLastWorkoutDate(streak.getLastWorkoutDate());

        return workoutStreakMapper.toDomain(workoutStreakJpaRepository.save(entity));
    }
}
