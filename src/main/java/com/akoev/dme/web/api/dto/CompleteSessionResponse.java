package com.akoev.dme.web.api.dto;

import com.akoev.dme.application.service.SessionCompletionResult;

import java.time.Instant;
import java.util.List;

public record CompleteSessionResponse(
        Long logId,
        Instant performedAt,
        int completionPercentage,
        Integer rating,
        int currentStreak,
        int longestStreak,
        List<PersonalRecordResponse> newPersonalRecords
) {

    public static CompleteSessionResponse from(SessionCompletionResult result) {
        return new CompleteSessionResponse(
                result.log().getId(),
                result.log().getPerformedAt(),
                result.log().getCompletionPercentage(),
                result.log().getRating(),
                result.streak().getCurrentStreak(),
                result.streak().getLongestStreak(),
                result.newPersonalRecords().stream().map(PersonalRecordResponse::from).toList()
        );
    }
}
