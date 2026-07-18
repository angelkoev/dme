package com.akoev.dme.web.api;

import com.akoev.dme.domain.repository.PersonalRecordRepository;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutStreakRepository;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import com.akoev.dme.web.api.dto.PersonalRecordResponse;
import com.akoev.dme.web.api.dto.WorkoutLogResponse;
import com.akoev.dme.web.api.dto.WorkoutStreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class ProgressController {

    private final WorkoutStreakRepository workoutStreakRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final WorkoutLogRepository workoutLogRepository;

    @GetMapping("/streak")
    public WorkoutStreakResponse streak(@AuthenticationPrincipal CustomUserDetails principal) {
        return workoutStreakRepository.findByUserId(principal.getId())
                .map(WorkoutStreakResponse::from)
                .orElseGet(WorkoutStreakResponse::empty);
    }

    @GetMapping("/personal-records")
    public List<PersonalRecordResponse> personalRecords(@AuthenticationPrincipal CustomUserDetails principal) {
        return personalRecordRepository.findAllByUserId(principal.getId()).stream()
                .map(PersonalRecordResponse::from)
                .toList();
    }

    @GetMapping("/workout-history")
    public List<WorkoutLogResponse> workoutHistory(@AuthenticationPrincipal CustomUserDetails principal) {
        return workoutLogRepository.findAllByUserId(principal.getId()).stream()
                .map(WorkoutLogResponse::from)
                .toList();
    }
}
