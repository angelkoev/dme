package com.akoev.dme.web.mvc.form;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable Thymeleaf form-backing bean for the /admin/exercises create/edit
 * pages. Same rationale as ProfileForm: th:field checkbox-list binding needs
 * a plain JavaBean, not the immutable ExerciseUpsertCommand/ExerciseRequest
 * records.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExerciseForm {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private MuscleGroup primaryMuscleGroup;

    @NotNull
    private MovementPattern movementPattern;

    @NotNull
    private DifficultyLevel difficultyLevel;

    @NotNull
    private ExerciseType exerciseType;

    private String instructions;
    private String videoUrl;

    private List<Long> equipmentIds = new ArrayList<>();
}
