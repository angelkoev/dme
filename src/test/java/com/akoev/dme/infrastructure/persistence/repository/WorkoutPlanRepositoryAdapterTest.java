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
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.RoleRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.domain.repository.WorkoutPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutPlanRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void savesAndReloadsWorkoutPlanWithSessionsAndExercises() {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        User user = userRepository.save(User.builder()
                .username("plan.owner")
                .email("plan.owner@example.com")
                .passwordHash("hashed-password")
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(userRole))
                .build());

        Exercise squat = exerciseRepository.save(Exercise.builder()
                .name("Back Squat")
                .primaryMuscleGroup(MuscleGroup.QUADRICEPS)
                .movementPattern(MovementPattern.LEGS)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .exerciseType(ExerciseType.COMPOUND)
                .build());

        SessionExercise sessionExercise = SessionExercise.builder()
                .exercise(squat)
                .orderIndex(1)
                .sets(4)
                .repRangeMin(6)
                .repRangeMax(10)
                .restSeconds(120)
                .build();

        WorkoutSession session = WorkoutSession.builder()
                .sessionIndex(1)
                .name("Leg Day")
                .exercises(List.of(sessionExercise))
                .build();

        WorkoutPlan plan = WorkoutPlan.builder()
                .userId(user.getId())
                .goal(TrainingGoal.STRENGTH)
                .generatedAt(Instant.now())
                .active(true)
                .generationSource(GenerationSource.RULE_BASED)
                .sessions(List.of(session))
                .build();

        WorkoutPlan saved = workoutPlanRepository.save(plan);
        assertThat(saved.getId()).isNotNull();

        WorkoutPlan reloaded = workoutPlanRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getGoal()).isEqualTo(TrainingGoal.STRENGTH);
        assertThat(reloaded.getSessions()).hasSize(1);

        WorkoutSession reloadedSession = reloaded.getSessions().get(0);
        assertThat(reloadedSession.getName()).isEqualTo("Leg Day");
        assertThat(reloadedSession.getExercises()).hasSize(1);
        assertThat(reloadedSession.getExercises().get(0).getExercise().getName()).isEqualTo("Back Squat");
        assertThat(reloadedSession.getExercises().get(0).getSets()).isEqualTo(4);

        WorkoutPlan active = workoutPlanRepository.findActiveByUserId(user.getId()).orElseThrow();
        assertThat(active.getId()).isEqualTo(saved.getId());
    }
}
