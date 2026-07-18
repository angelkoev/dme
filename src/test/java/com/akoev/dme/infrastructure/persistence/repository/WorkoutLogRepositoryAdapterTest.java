package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.GenerationSource;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.RoleName;
import com.akoev.dme.domain.model.SessionExercise;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.RoleRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutLogRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void savesAndReloadsWorkoutLogAndFindsRecentByUser() {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        User user = userRepository.save(User.builder()
                .username("log.owner")
                .email("log.owner@example.com")
                .passwordHash("hashed-password")
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(userRole))
                .build());

        Exercise pushUp = exerciseRepository.save(Exercise.builder()
                .name("Push-up")
                .primaryMuscleGroup(MuscleGroup.CHEST)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .exerciseType(ExerciseType.COMPOUND)
                .build());

        WorkoutPlan plan = workoutPlanRepository.save(WorkoutPlan.builder()
                .userId(user.getId())
                .goal(TrainingGoal.GENERAL_FITNESS)
                .generatedAt(Instant.now())
                .active(true)
                .generationSource(GenerationSource.RULE_BASED)
                .sessions(List.of(WorkoutSession.builder()
                        .sessionIndex(1)
                        .name("Full Body")
                        .exercises(List.of(SessionExercise.builder()
                                .exercise(pushUp)
                                .orderIndex(1)
                                .sets(3)
                                .repRangeMin(8)
                                .repRangeMax(12)
                                .restSeconds(60)
                                .build()))
                        .build()))
                .build());
        Long sessionId = plan.getSessions().get(0).getId();

        WorkoutLog saved = workoutLogRepository.save(WorkoutLog.builder()
                .workoutSessionId(sessionId)
                .userId(user.getId())
                .performedAt(Instant.now())
                .completionPercentage(90)
                .rating(4)
                .perceivedIntensity(7)
                .notes("Felt strong")
                .build());

        assertThat(saved.getId()).isNotNull();

        List<WorkoutLog> recent = workoutLogRepository.findRecentByUserId(user.getId(), Instant.now().minus(1, ChronoUnit.DAYS));
        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getCompletionPercentage()).isEqualTo(90);
        assertThat(recent.get(0).getRating()).isEqualTo(4);

        List<WorkoutLog> all = workoutLogRepository.findAllByUserId(user.getId());
        assertThat(all).hasSize(1);
    }
}
