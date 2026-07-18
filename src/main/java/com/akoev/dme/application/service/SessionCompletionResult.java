package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.PersonalRecord;
import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.domain.model.WorkoutStreak;

import java.util.List;

public record SessionCompletionResult(WorkoutLog log, WorkoutStreak streak, List<PersonalRecord> newPersonalRecords) {
}
