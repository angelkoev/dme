package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long userId;
    private LocalDate birthDate;
    private Sex sex;
    private Integer heightCm;
    private BigDecimal weightKg;
    private ExperienceLevel experienceLevel;
    private TrainingGoal primaryGoal;
    private int daysPerWeek;
    private int sessionDurationMinutes;
    private String notes;
    @Builder.Default
    private Set<Equipment> availableEquipment = new HashSet<>();
    @Builder.Default
    private List<UserLimitation> limitations = new ArrayList<>();
}
