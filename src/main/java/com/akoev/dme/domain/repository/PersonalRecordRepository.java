package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.MetricType;
import com.akoev.dme.domain.model.PersonalRecord;

import java.util.List;
import java.util.Optional;

public interface PersonalRecordRepository {

    PersonalRecord save(PersonalRecord record);

    List<PersonalRecord> findAllByUserId(Long userId);

    Optional<PersonalRecord> findBestByUserIdAndExerciseIdAndMetricType(Long userId, Long exerciseId, MetricType metricType);
}
