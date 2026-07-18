package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.MetricType;
import com.akoev.dme.domain.model.PersonalRecord;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.model.WorkoutStreak;
import com.akoev.dme.domain.repository.PersonalRecordRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutPlanRepository;
import com.akoev.dme.domain.repository.WorkoutStreakRepository;
import com.akoev.dme.fitness.engine.GenerationRequest;
import com.akoev.dme.fitness.engine.RecentActivitySummary;
import com.akoev.dme.fitness.engine.RecentActivitySummaryBuilder;
import com.akoev.dme.fitness.engine.WorkoutPlanGenerator;
import com.akoev.dme.fitness.engine.assist.MotivationalMessageService;
import com.akoev.dme.fitness.engine.assist.WorkoutExplanationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanGenerator workoutPlanGenerator;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutStreakRepository workoutStreakRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final RecentActivitySummaryBuilder recentActivitySummaryBuilder;
    private final WorkoutExplanationService explanationService;
    private final MotivationalMessageService motivationalMessageService;

    @Transactional
    public GenerationResult generate(Long userId, TrainingGoal goalOverride) {
        WorkoutPlan generated = workoutPlanGenerator.generate(new GenerationRequest(userId, goalOverride));
        WorkoutPlan saved = workoutPlanRepository.save(generated);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        String explanation = explanationService.explain(saved, user.getProfile());
        RecentActivitySummary recentActivity = recentActivitySummaryBuilder.build(userId);
        String motivation = motivationalMessageService.motivate(recentActivity);

        return new GenerationResult(saved, explanation, motivation);
    }

    public WorkoutPlan getActivePlan(Long userId) {
        return workoutPlanRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active workout plan"));
    }

    public List<WorkoutPlan> listAll(Long userId) {
        return workoutPlanRepository.findAllByUserId(userId);
    }

    public WorkoutPlan getById(Long userId, Long planId) {
        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found: " + planId));
        if (!plan.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This workout plan does not belong to you");
        }
        return plan;
    }

    @Transactional
    public SessionCompletionResult completeSession(Long userId, Long planId, Long sessionId, CompleteSessionCommand command) {
        WorkoutPlan plan = getById(userId, planId);
        WorkoutSession session = plan.getSessions().stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found in this plan: " + sessionId));

        WorkoutLog log = workoutLogRepository.save(WorkoutLog.builder()
                .workoutSessionId(session.getId())
                .userId(userId)
                .performedAt(Instant.now())
                .completionPercentage(command.completionPercentage())
                .rating(command.rating())
                .perceivedIntensity(command.perceivedIntensity())
                .notes(command.notes())
                .build());

        WorkoutStreak streak = updateStreak(userId);
        Set<Long> exerciseIdsInSession = session.getExercises().stream()
                .map(sessionExercise -> sessionExercise.getExercise().getId())
                .collect(Collectors.toSet());
        List<PersonalRecord> newRecords = detectPersonalRecords(userId, exerciseIdsInSession, command.exercisePerformances());

        return new SessionCompletionResult(log, streak, newRecords);
    }

    private WorkoutStreak updateStreak(Long userId) {
        LocalDate today = LocalDate.now();
        WorkoutStreak existing = workoutStreakRepository.findByUserId(userId)
                .orElse(WorkoutStreak.builder().userId(userId).currentStreak(0).longestStreak(0).build());

        int newCurrentStreak;
        if (existing.getLastWorkoutDate() == null || existing.getLastWorkoutDate().equals(today.minusDays(1))) {
            newCurrentStreak = existing.getCurrentStreak() + 1;
        } else if (existing.getLastWorkoutDate().equals(today)) {
            newCurrentStreak = existing.getCurrentStreak();
        } else {
            newCurrentStreak = 1;
        }

        return workoutStreakRepository.save(WorkoutStreak.builder()
                .userId(userId)
                .currentStreak(newCurrentStreak)
                .longestStreak(Math.max(existing.getLongestStreak(), newCurrentStreak))
                .lastWorkoutDate(today)
                .build());
    }

    private List<PersonalRecord> detectPersonalRecords(Long userId, Set<Long> exerciseIdsInSession,
                                                         List<CompleteSessionCommand.ExercisePerformanceCommand> performances) {
        if (performances == null || performances.isEmpty()) {
            return List.of();
        }
        List<PersonalRecord> newRecords = new ArrayList<>();
        for (CompleteSessionCommand.ExercisePerformanceCommand performance : performances) {
            if (!exerciseIdsInSession.contains(performance.exerciseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Exercise " + performance.exerciseId() + " is not part of this session");
            }
            if (performance.weightKg() != null) {
                maybeSavePersonalRecord(userId, performance.exerciseId(), MetricType.MAX_WEIGHT, performance.weightKg())
                        .ifPresent(newRecords::add);
            }
            if (performance.reps() != null) {
                maybeSavePersonalRecord(userId, performance.exerciseId(), MetricType.MAX_REPS, BigDecimal.valueOf(performance.reps()))
                        .ifPresent(newRecords::add);
            }
        }
        return newRecords;
    }

    // Synchronized to close the check-then-act race between reading the
    // current best and inserting a new one: two near-simultaneous completions
    // for the same (user, exercise, metric) could otherwise both read the
    // same "current best" and both insert. This only guards a single JVM
    // instance, which matches this project's actual deployment model.
    private synchronized Optional<PersonalRecord> maybeSavePersonalRecord(
            Long userId, Long exerciseId, MetricType metricType, BigDecimal value) {
        Optional<PersonalRecord> best = personalRecordRepository
                .findBestByUserIdAndExerciseIdAndMetricType(userId, exerciseId, metricType);
        if (best.isPresent() && best.get().getValue().compareTo(value) >= 0) {
            return Optional.empty();
        }
        return Optional.of(personalRecordRepository.save(PersonalRecord.builder()
                .userId(userId)
                .exerciseId(exerciseId)
                .metricType(metricType)
                .value(value)
                .achievedAt(Instant.now())
                .build()));
    }
}
