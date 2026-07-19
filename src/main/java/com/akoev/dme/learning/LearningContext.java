package com.akoev.dme.learning;

import jakarta.validation.constraints.NotNull;

/**
 * Both fields are {@code @NotNull} (enforced via {@code @Valid} on
 * {@code LearningController.recommend}): {@code currentLevel} is switched on
 * by {@code LevelRule}/{@code LearningScorer}, and a plain {@code switch}
 * throws on a null enum — without this validation a missing field surfaced
 * as an opaque 500 instead of a 400.
 */
public record LearningContext(@NotNull SkillArea targetSkillArea, @NotNull SkillLevel currentLevel) {
}
