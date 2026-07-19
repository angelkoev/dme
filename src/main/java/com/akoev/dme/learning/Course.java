package com.akoev.dme.learning;

public record Course(Long id, String name, SkillArea skillArea, SkillLevel level, int durationHours) {
}
