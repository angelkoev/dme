package com.akoev.dme.fitness.engine.rulebased.scoring;

import com.akoev.dme.decisionengine.ScoringStrategy;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weighted scoring for candidate exercises. Plain additive constants rather
 * than a learned model — this is the rule-based "intelligence" for v1.
 * {@link ScoringStrategy} is the seam an AI-backed scorer would implement
 * later, so swapping it out never touches the rest of the engine.
 */
@Component
public class FitnessExerciseScorer implements ScoringStrategy<FitnessDecisionContext, Exercise> {

    private static final double BASE_SCORE = 50;
    private static final double GOAL_FIT_WEIGHT = 20;
    private static final double FAVORITE_BONUS = 15;
    private static final double PREFERRED_CATEGORY_BONUS = 10;
    private static final double RECENCY_PENALTY = 30;
    // Must stay at least twice VARIETY_JITTER_MAX even for a single recent
    // session, otherwise jitter can cancel or invert the "avoid overtraining"
    // ordering right when it matters most (the 1-session case).
    private static final double OVERTRAINING_PENALTY_PER_RECENT_SESSION = 12;
    private static final double DIFFICULTY_ADJUSTMENT = 10;
    // Same ≥ 2x VARIETY_JITTER_MAX convention as every other weight below.
    private static final double WEAK_MUSCLE_GROUP_BONUS = 10;
    private static final double VARIETY_JITTER_MAX = 5;

    private static final Set<TrainingGoal> COMPOUND_FAVORING_GOALS = Set.of(TrainingGoal.STRENGTH, TrainingGoal.HYPERTROPHY);
    private static final int LOW_COMPLETION_THRESHOLD = 60;
    private static final int HIGH_COMPLETION_THRESHOLD = 90;

    @Override
    public double score(FitnessDecisionContext context, Exercise candidate) {
        double score = BASE_SCORE;
        score += goalFitScore(context, candidate);
        score += favoriteScore(context, candidate);
        score += preferredCategoryScore(context, candidate);
        score -= recencyPenalty(context, candidate);
        score -= overtrainingPenalty(context, candidate);
        score += weakMuscleGroupBonus(context, candidate);
        score += difficultyAdjustment(context, candidate);
        score += ThreadLocalRandom.current().nextDouble(0, VARIETY_JITTER_MAX);
        return score;
    }

    private double goalFitScore(FitnessDecisionContext context, Exercise candidate) {
        boolean favorsCompound = COMPOUND_FAVORING_GOALS.contains(context.getProfile().getPrimaryGoal());
        boolean isCompound = candidate.getExerciseType() == ExerciseType.COMPOUND;
        return favorsCompound == isCompound ? GOAL_FIT_WEIGHT : 0;
    }

    private double favoriteScore(FitnessDecisionContext context, Exercise candidate) {
        boolean isFavorite = context.getProfile().getFavoriteExercises().stream()
                .anyMatch(exercise -> exercise.getId().equals(candidate.getId()));
        return isFavorite ? FAVORITE_BONUS : 0;
    }

    private double preferredCategoryScore(FitnessDecisionContext context, Exercise candidate) {
        return context.getProfile().getPreferredCategories().contains(candidate.getPrimaryMuscleGroup())
                ? PREFERRED_CATEGORY_BONUS : 0;
    }

    private double recencyPenalty(FitnessDecisionContext context, Exercise candidate) {
        return context.getRecentActivity().getRecentlyUsedExerciseIds().contains(candidate.getId())
                ? RECENCY_PENALTY : 0;
    }

    private double overtrainingPenalty(FitnessDecisionContext context, Exercise candidate) {
        int recentSessions = context.getRecentActivity().getRecentLoadByMuscleGroup()
                .getOrDefault(candidate.getPrimaryMuscleGroup(), 0);
        return recentSessions * OVERTRAINING_PENALTY_PER_RECENT_SESSION;
    }

    // Complement to overtrainingPenalty: rewards a muscle group that recent
    // history shows as neglected relative to others, rather than only
    // penalizing one that's been trained a lot. Requires recent history to
    // exist at all (an empty map means "no data", not "everything is weak") —
    // otherwise every candidate would get this bonus on a brand-new user's
    // very first plan, which changes nothing about relative ranking but is
    // meaningless noise.
    private double weakMuscleGroupBonus(FitnessDecisionContext context, Exercise candidate) {
        Map<MuscleGroup, Integer> recentLoad = context.getRecentActivity().getRecentLoadByMuscleGroup();
        if (recentLoad.isEmpty()) {
            return 0;
        }
        return recentLoad.getOrDefault(candidate.getPrimaryMuscleGroup(), 0) == 0 ? WEAK_MUSCLE_GROUP_BONUS : 0;
    }

    private double difficultyAdjustment(FitnessDecisionContext context, Exercise candidate) {
        Integer completion = context.getRecentActivity().getLastCompletionPercentage();
        if (completion == null) {
            return 0;
        }
        DifficultyLevel difficulty = candidate.getDifficultyLevel();
        if (completion < LOW_COMPLETION_THRESHOLD) {
            return difficulty == DifficultyLevel.BEGINNER ? DIFFICULTY_ADJUSTMENT : -DIFFICULTY_ADJUSTMENT;
        }
        if (completion > HIGH_COMPLETION_THRESHOLD) {
            return difficulty == DifficultyLevel.ADVANCED ? DIFFICULTY_ADJUSTMENT : 0;
        }
        return 0;
    }
}
