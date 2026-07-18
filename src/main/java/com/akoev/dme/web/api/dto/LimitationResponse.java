package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.UserLimitation;

public record LimitationResponse(Long id, MuscleGroup muscleGroup, String note) {

    public static LimitationResponse from(UserLimitation limitation) {
        return new LimitationResponse(limitation.getId(), limitation.getMuscleGroup(), limitation.getNote());
    }
}
