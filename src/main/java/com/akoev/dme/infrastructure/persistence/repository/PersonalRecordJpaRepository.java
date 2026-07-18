package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.MetricType;
import com.akoev.dme.infrastructure.persistence.entity.PersonalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalRecordJpaRepository extends JpaRepository<PersonalRecordEntity, Long> {

    List<PersonalRecordEntity> findAllByUser_Id(Long userId);

    Optional<PersonalRecordEntity> findFirstByUser_IdAndExercise_IdAndMetricTypeOrderByValueDesc(
            Long userId, Long exerciseId, MetricType metricType);
}
