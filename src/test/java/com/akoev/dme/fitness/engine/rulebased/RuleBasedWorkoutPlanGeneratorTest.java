package com.akoev.dme.fitness.engine.rulebased;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.SessionExercise;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutSessionRepository;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import com.akoev.dme.fitness.engine.GenerationRequest;
import com.akoev.dme.fitness.engine.assist.NoOpAmbiguityResolver;
import com.akoev.dme.fitness.engine.rulebased.rules.DislikedExerciseRule;
import com.akoev.dme.fitness.engine.rulebased.rules.EquipmentAvailabilityRule;
import com.akoev.dme.fitness.engine.rulebased.rules.ExperienceLevelRule;
import com.akoev.dme.fitness.engine.rulebased.rules.InjuryContraindicationRule;
import com.akoev.dme.fitness.engine.rulebased.rules.MovementPatternMatchRule;
import com.akoev.dme.fitness.engine.rulebased.rules.UnwantedCategoryRule;
import com.akoev.dme.fitness.engine.rulebased.scoring.FitnessExerciseScorer;
import com.akoev.dme.fitness.engine.rulebased.strategy.EnduranceGoalStrategy;
import com.akoev.dme.fitness.engine.rulebased.strategy.FatLossGoalStrategy;
import com.akoev.dme.fitness.engine.rulebased.strategy.GeneralFitnessGoalStrategy;
import com.akoev.dme.fitness.engine.rulebased.strategy.GoalStrategyResolver;
import com.akoev.dme.fitness.engine.rulebased.strategy.HypertrophyGoalStrategy;
import com.akoev.dme.fitness.engine.rulebased.strategy.StrengthGoalStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleBasedWorkoutPlanGeneratorTest {

    private static final Equipment BODYWEIGHT = Equipment.builder().id(1L).name("Bodyweight").build();

    @Test
    void generatesOneSessionPerDayUsingOnlyAvailableEquipmentWithNoDuplicateExercises() {
        UserRepository userRepository = mock(UserRepository.class);
        ExerciseRepository exerciseRepository = mock(ExerciseRepository.class);
        WorkoutLogRepository workoutLogRepository = mock(WorkoutLogRepository.class);
        WorkoutSessionRepository workoutSessionRepository = mock(WorkoutSessionRepository.class);

        UserProfile profile = UserProfile.builder()
                .userId(1L)
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .primaryGoal(TrainingGoal.HYPERTROPHY)
                .daysPerWeek(3)
                .sessionDurationMinutes(60)
                .availableEquipment(Set.of(BODYWEIGHT))
                .build();
        User user = User.builder().id(1L).username("test.user").profile(profile).build();

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(exerciseRepository.findAll()).thenReturn(catalog());
        when(workoutLogRepository.findRecentByUserId(anyLong(), any())).thenReturn(List.of());

        RuleBasedWorkoutPlanGenerator generator = new RuleBasedWorkoutPlanGenerator(
                userRepository, exerciseRepository, workoutLogRepository, workoutSessionRepository,
                goalStrategyResolver(), rules(), new FitnessExerciseScorer(), new NoOpAmbiguityResolver());

        WorkoutPlan plan = generator.generate(new GenerationRequest(1L));

        assertThat(plan.getGoal()).isEqualTo(TrainingGoal.HYPERTROPHY);
        assertThat(plan.getSessions()).hasSize(3);
        assertThat(plan.getSessions().get(0).getName()).containsIgnoringCase("push");

        List<Long> usedExerciseIds = plan.getSessions().stream()
                .flatMap(session -> session.getExercises().stream())
                .map(SessionExercise::getExercise)
                .map(Exercise::getId)
                .toList();
        assertThat(usedExerciseIds).doesNotHaveDuplicates();

        Set<Long> bodyweightEquipmentIds = Set.of(BODYWEIGHT.getId());
        plan.getSessions().stream()
                .flatMap(session -> session.getExercises().stream())
                .map(SessionExercise::getExercise)
                .forEach(exercise -> assertThat(exercise.getRequiredEquipment())
                        .extracting(Equipment::getId)
                        .allMatch(bodyweightEquipmentIds::contains));
    }

    @Test
    void throwsWhenUserHasNoProfile() {
        UserRepository userRepository = mock(UserRepository.class);
        User userWithoutProfile = User.builder().id(2L).username("no.profile").build();
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(userWithoutProfile));

        RuleBasedWorkoutPlanGenerator generator = new RuleBasedWorkoutPlanGenerator(
                userRepository, mock(ExerciseRepository.class), mock(WorkoutLogRepository.class),
                mock(WorkoutSessionRepository.class), goalStrategyResolver(), rules(),
                new FitnessExerciseScorer(), new NoOpAmbiguityResolver());

        assertThatThrownBy(() -> generator.generate(new GenerationRequest(2L)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throwsForUnknownUser() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        RuleBasedWorkoutPlanGenerator generator = new RuleBasedWorkoutPlanGenerator(
                userRepository, mock(ExerciseRepository.class), mock(WorkoutLogRepository.class),
                mock(WorkoutSessionRepository.class), goalStrategyResolver(), rules(),
                new FitnessExerciseScorer(), new NoOpAmbiguityResolver());

        assertThatThrownBy(() -> generator.generate(new GenerationRequest(99L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private GoalStrategyResolver goalStrategyResolver() {
        return new GoalStrategyResolver(List.of(
                new StrengthGoalStrategy(), new HypertrophyGoalStrategy(), new FatLossGoalStrategy(),
                new EnduranceGoalStrategy(), new GeneralFitnessGoalStrategy()));
    }

    private List<Rule<FitnessDecisionContext, Exercise>> rules() {
        return List.of(
                new EquipmentAvailabilityRule(), new ExperienceLevelRule(), new InjuryContraindicationRule(),
                new DislikedExerciseRule(), new UnwantedCategoryRule(), new MovementPatternMatchRule());
    }

    private List<Exercise> catalog() {
        List<Exercise> exercises = new ArrayList<>();
        long id = 1;
        for (int i = 0; i < 5; i++) {
            exercises.add(exercise(id++, "Push " + i, MovementPattern.PUSH, MuscleGroup.CHEST));
        }
        for (int i = 0; i < 5; i++) {
            exercises.add(exercise(id++, "Pull " + i, MovementPattern.PULL, MuscleGroup.BACK));
        }
        for (int i = 0; i < 5; i++) {
            exercises.add(exercise(id++, "Legs " + i, MovementPattern.LEGS, MuscleGroup.QUADRICEPS));
        }
        for (int i = 0; i < 3; i++) {
            exercises.add(exercise(id++, "Core " + i, MovementPattern.CORE, MuscleGroup.CORE));
        }
        return exercises;
    }

    private Exercise exercise(Long id, String name, MovementPattern pattern, MuscleGroup muscleGroup) {
        return Exercise.builder()
                .id(id)
                .name(name)
                .primaryMuscleGroup(muscleGroup)
                .movementPattern(pattern)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .exerciseType(ExerciseType.COMPOUND)
                .requiredEquipment(new HashSet<>(Set.of(BODYWEIGHT)))
                .build();
    }
}
