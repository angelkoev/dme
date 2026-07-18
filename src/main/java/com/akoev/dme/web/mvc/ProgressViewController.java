package com.akoev.dme.web.mvc;

import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.PersonalRecord;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.PersonalRecordRepository;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutStreakRepository;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProgressViewController {

    private final WorkoutStreakRepository workoutStreakRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final ExerciseRepository exerciseRepository;

    @GetMapping("/progress")
    public String progress(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        workoutStreakRepository.findByUserId(principal.getId())
                .ifPresent(streak -> model.addAttribute("streak", streak));

        List<PersonalRecord> personalRecords = personalRecordRepository.findAllByUserId(principal.getId());
        Map<Long, String> exerciseNames = exerciseRepository
                .findAllById(personalRecords.stream().map(PersonalRecord::getExerciseId).toList())
                .stream()
                .collect(Collectors.toMap(Exercise::getId, Exercise::getName));
        model.addAttribute("personalRecords", personalRecords);
        model.addAttribute("exerciseNames", exerciseNames);

        model.addAttribute("workoutLogs", workoutLogRepository.findAllByUserId(principal.getId()));
        return "progress";
    }
}
