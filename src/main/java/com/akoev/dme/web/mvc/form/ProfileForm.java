package com.akoev.dme.web.mvc.form;

import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Sex;
import com.akoev.dme.domain.model.TrainingGoal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutable Thymeleaf form-backing bean for /profile. Kept separate from the
 * immutable ProfileUpdateCommand/UpdateProfileRequest records: th:field
 * checkbox-list binding and indexed nested rows (limitations) need plain
 * JavaBean getter/setter access, which records don't provide.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileForm {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;
    private Sex sex;
    private Integer heightCm;
    private BigDecimal weightKg;

    @NotNull
    private ExperienceLevel experienceLevel;

    @NotNull
    private TrainingGoal primaryGoal;

    @Min(1)
    @Max(7)
    private int daysPerWeek = 3;

    @Min(10)
    private int sessionDurationMinutes = 45;

    private String notes;

    @NotNull
    private Location location = Location.ANYWHERE;

    private List<Long> equipmentIds = new ArrayList<>();
    private List<Long> favoriteExerciseIds = new ArrayList<>();
    private List<Long> dislikedExerciseIds = new ArrayList<>();
    private List<MuscleGroup> preferredCategories = new ArrayList<>();
    private List<MuscleGroup> unwantedCategories = new ArrayList<>();
    private List<DayOfWeek> restDays = new ArrayList<>();
    private List<LimitationRow> limitations = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LimitationRow {
        private MuscleGroup muscleGroup;
        private String note;
    }
}
