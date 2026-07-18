package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.repository.WorkoutSessionRepository;
import com.akoev.dme.infrastructure.persistence.mapper.WorkoutSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionRepositoryAdapter implements WorkoutSessionRepository {

    private final WorkoutSessionJpaRepository workoutSessionJpaRepository;
    private final WorkoutSessionMapper workoutSessionMapper;

    @Override
    public Optional<WorkoutSession> findById(Long id) {
        return workoutSessionJpaRepository.findById(id).map(workoutSessionMapper::toDomain);
    }
}
