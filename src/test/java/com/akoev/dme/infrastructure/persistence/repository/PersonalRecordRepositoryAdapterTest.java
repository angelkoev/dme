package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MetricType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.PersonalRecord;
import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.RoleName;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.PersonalRecordRepository;
import com.akoev.dme.domain.repository.RoleRepository;
import com.akoev.dme.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalRecordRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private PersonalRecordRepository personalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void savesAndReloadsPersonalRecordAndFindsBestByExerciseAndMetric() {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        User user = userRepository.save(User.builder()
                .username("pr.owner")
                .email("pr.owner@example.com")
                .passwordHash("hashed-password")
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(userRole))
                .build());

        Exercise deadlift = exerciseRepository.save(Exercise.builder()
                .name("Deadlift")
                .primaryMuscleGroup(MuscleGroup.BACK)
                .movementPattern(MovementPattern.PULL)
                .difficultyLevel(DifficultyLevel.ADVANCED)
                .exerciseType(ExerciseType.COMPOUND)
                .build());

        personalRecordRepository.save(PersonalRecord.builder()
                .userId(user.getId())
                .exerciseId(deadlift.getId())
                .metricType(MetricType.MAX_WEIGHT)
                .value(new BigDecimal("100.00"))
                .achievedAt(Instant.now())
                .build());
        PersonalRecord best = personalRecordRepository.save(PersonalRecord.builder()
                .userId(user.getId())
                .exerciseId(deadlift.getId())
                .metricType(MetricType.MAX_WEIGHT)
                .value(new BigDecimal("120.00"))
                .achievedAt(Instant.now())
                .build());

        List<PersonalRecord> all = personalRecordRepository.findAllByUserId(user.getId());
        assertThat(all).hasSize(2);

        PersonalRecord found = personalRecordRepository
                .findBestByUserIdAndExerciseIdAndMetricType(user.getId(), deadlift.getId(), MetricType.MAX_WEIGHT)
                .orElseThrow();
        assertThat(found.getId()).isEqualTo(best.getId());
        assertThat(found.getValue()).isEqualByComparingTo("120.00");
    }
}
