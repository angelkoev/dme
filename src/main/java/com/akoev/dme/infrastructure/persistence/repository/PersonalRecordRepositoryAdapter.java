package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.MetricType;
import com.akoev.dme.domain.model.PersonalRecord;
import com.akoev.dme.domain.repository.PersonalRecordRepository;
import com.akoev.dme.infrastructure.persistence.entity.ExerciseEntity;
import com.akoev.dme.infrastructure.persistence.entity.PersonalRecordEntity;
import com.akoev.dme.infrastructure.persistence.entity.UserEntity;
import com.akoev.dme.infrastructure.persistence.mapper.PersonalRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalRecordRepositoryAdapter implements PersonalRecordRepository {

    private final PersonalRecordJpaRepository personalRecordJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ExerciseJpaRepository exerciseJpaRepository;
    private final PersonalRecordMapper personalRecordMapper;

    @Override
    @Transactional
    public PersonalRecord save(PersonalRecord record) {
        UserEntity user = userJpaRepository.findById(record.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + record.getUserId()));
        ExerciseEntity exercise = exerciseJpaRepository.findById(record.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown exercise id: " + record.getExerciseId()));

        PersonalRecordEntity entity = record.getId() != null
                ? personalRecordJpaRepository.findById(record.getId()).orElseGet(PersonalRecordEntity::new)
                : new PersonalRecordEntity();
        entity.setUser(user);
        entity.setExercise(exercise);
        entity.setMetricType(record.getMetricType());
        entity.setValue(record.getValue());
        entity.setAchievedAt(record.getAchievedAt() != null ? record.getAchievedAt() : Instant.now());

        return personalRecordMapper.toDomain(personalRecordJpaRepository.save(entity));
    }

    @Override
    public List<PersonalRecord> findAllByUserId(Long userId) {
        return personalRecordJpaRepository.findAllByUser_Id(userId).stream().map(personalRecordMapper::toDomain).toList();
    }

    @Override
    public Optional<PersonalRecord> findBestByUserIdAndExerciseIdAndMetricType(
            Long userId, Long exerciseId, MetricType metricType) {
        return personalRecordJpaRepository
                .findFirstByUser_IdAndExercise_IdAndMetricTypeOrderByValueDesc(userId, exerciseId, metricType)
                .map(personalRecordMapper::toDomain);
    }
}
