package com.akoev.dme.fitness.engine.rulebased.scoring;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import com.akoev.dme.fitness.engine.RecentActivitySummary;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every bonus/penalty weight in {@link FitnessExerciseScorer} is at least
 * twice the maximum random variety jitter, so single-factor comparisons
 * below are deterministic despite the jitter — no need to control randomness.
 */
class FitnessExerciseScorerTest {

    private final FitnessExerciseScorer scorer = new FitnessExerciseScorer();

    @Test
    void compoundExercisesScoreHigherForStrengthGoal() {
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.STRENGTH).build();
        FitnessDecisionContext context = context(profile, RecentActivitySummary.empty());

        double compoundScore = scorer.score(context, exercise(1L, ExerciseType.COMPOUND, MuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        double isolationScore = scorer.score(context, exercise(2L, ExerciseType.ISOLATION, MuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));

        assertThat(compoundScore).isGreaterThan(isolationScore);
    }

    @Test
    void isolationExercisesScoreHigherForEnduranceGoal() {
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.ENDURANCE).build();
        FitnessDecisionContext context = context(profile, RecentActivitySummary.empty());

        double compoundScore = scorer.score(context, exercise(1L, ExerciseType.COMPOUND, MuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        double isolationScore = scorer.score(context, exercise(2L, ExerciseType.ISOLATION, MuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));

        assertThat(isolationScore).isGreaterThan(compoundScore);
    }

    @Test
    void favoriteExerciseScoresHigherThanNonFavorite() {
        Exercise favorite = exercise(1L, ExerciseType.COMPOUND, MuscleGroup.BACK, DifficultyLevel.INTERMEDIATE);
        Exercise other = exercise(2L, ExerciseType.COMPOUND, MuscleGroup.BACK, DifficultyLevel.INTERMEDIATE);
        UserProfile profile = UserProfile.builder()
                .primaryGoal(TrainingGoal.HYPERTROPHY)
                .favoriteExercises(Set.of(favorite))
                .build();
        FitnessDecisionContext context = context(profile, RecentActivitySummary.empty());

        assertThat(scorer.score(context, favorite)).isGreaterThan(scorer.score(context, other));
    }

    @Test
    void preferredCategoryScoresHigherThanNonPreferred() {
        UserProfile profile = UserProfile.builder()
                .primaryGoal(TrainingGoal.GENERAL_FITNESS)
                .preferredCategories(Set.of(MuscleGroup.BACK))
                .build();
        FitnessDecisionContext context = context(profile, RecentActivitySummary.empty());

        double preferred = scorer.score(context, exercise(1L, ExerciseType.COMPOUND, MuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        double notPreferred = scorer.score(context, exercise(2L, ExerciseType.COMPOUND, MuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));

        assertThat(preferred).isGreaterThan(notPreferred);
    }

    @Test
    void recentlyUsedExerciseIsPenalized() {
        Exercise recentlyUsed = exercise(1L, ExerciseType.COMPOUND, MuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE);
        Exercise fresh = exercise(2L, ExerciseType.COMPOUND, MuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE);
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.HYPERTROPHY).build();
        RecentActivitySummary recentActivity = RecentActivitySummary.builder()
                .recentlyUsedExerciseIds(Set.of(1L))
                .build();
        FitnessDecisionContext context = context(profile, recentActivity);

        assertThat(scorer.score(context, fresh)).isGreaterThan(scorer.score(context, recentlyUsed));
    }

    @Test
    void heavilyTrainedMuscleGroupIsPenalized() {
        Exercise overtrainedGroup = exercise(1L, ExerciseType.COMPOUND, MuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE);
        Exercise freshGroup = exercise(2L, ExerciseType.COMPOUND, MuscleGroup.BACK, DifficultyLevel.INTERMEDIATE);
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.HYPERTROPHY).build();
        RecentActivitySummary recentActivity = RecentActivitySummary.builder()
                .recentLoadByMuscleGroup(Map.of(MuscleGroup.CHEST, 4))
                .build();
        FitnessDecisionContext context = context(profile, recentActivity);

        assertThat(scorer.score(context, freshGroup)).isGreaterThan(scorer.score(context, overtrainedGroup));
    }

    @Test
    void untrainedMuscleGroupIsRewardedOnceThereIsHistoryShowingOthersWereTrained() {
        Exercise candidate = exercise(1L, ExerciseType.COMPOUND, MuscleGroup.BACK, DifficultyLevel.INTERMEDIATE);
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.HYPERTROPHY).build();

        FitnessDecisionContext noHistory = context(profile, RecentActivitySummary.empty());
        FitnessDecisionContext othersTrainedButNotThisOne = context(profile, RecentActivitySummary.builder()
                .recentLoadByMuscleGroup(Map.of(MuscleGroup.CHEST, 2))
                .build());

        // Same exercise (BACK) either way, never itself trained recently in
        // either scenario — isolates the "weak/neglected muscle group" bonus
        // from the overtraining penalty, which only ever fires for a group
        // that WAS trained.
        assertThat(scorer.score(othersTrainedButNotThisOne, candidate))
                .isGreaterThan(scorer.score(noHistory, candidate));
    }

    @Test
    void lowCompletionFavorsBeginnerDifficulty() {
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.GENERAL_FITNESS).build();
        RecentActivitySummary recentActivity = RecentActivitySummary.builder().lastCompletionPercentage(40).build();
        FitnessDecisionContext context = context(profile, recentActivity);

        double beginner = scorer.score(context, exercise(1L, ExerciseType.COMPOUND, MuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        double advanced = scorer.score(context, exercise(2L, ExerciseType.COMPOUND, MuscleGroup.QUADRICEPS, DifficultyLevel.ADVANCED));

        assertThat(beginner).isGreaterThan(advanced);
    }

    @Test
    void highCompletionFavorsAdvancedDifficulty() {
        UserProfile profile = UserProfile.builder().primaryGoal(TrainingGoal.GENERAL_FITNESS).build();
        RecentActivitySummary recentActivity = RecentActivitySummary.builder().lastCompletionPercentage(95).build();
        FitnessDecisionContext context = context(profile, recentActivity);

        double beginner = scorer.score(context, exercise(1L, ExerciseType.COMPOUND, MuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        double advanced = scorer.score(context, exercise(2L, ExerciseType.COMPOUND, MuscleGroup.QUADRICEPS, DifficultyLevel.ADVANCED));

        assertThat(advanced).isGreaterThan(beginner);
    }

    private FitnessDecisionContext context(UserProfile profile, RecentActivitySummary recentActivity) {
        return FitnessDecisionContext.builder()
                .profile(profile)
                .recentActivity(recentActivity)
                .targetMovementPattern(MovementPattern.PUSH)
                .build();
    }

    private Exercise exercise(Long id, ExerciseType type, MuscleGroup muscleGroup, DifficultyLevel difficultyLevel) {
        return Exercise.builder()
                .id(id)
                .name("Exercise " + id)
                .primaryMuscleGroup(muscleGroup)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(difficultyLevel)
                .exerciseType(type)
                .build();
    }
}
