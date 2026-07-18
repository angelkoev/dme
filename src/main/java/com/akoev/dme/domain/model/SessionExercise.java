package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionExercise {
    private Long id;
    private Exercise exercise;
    private int orderIndex;
    private int sets;
    private int repRangeMin;
    private int repRangeMax;
    private int restSeconds;
    private String notes;
}
