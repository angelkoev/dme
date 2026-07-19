package com.akoev.dme.learning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LearningPathTest {

    private final LearningScorer scorer = new LearningScorer();

    @Test
    void exactLevelMatchScoresHigherThanMismatch() {
        LearningContext context = new LearningContext(SkillArea.PROGRAMMING, SkillLevel.INTERMEDIATE);
        Course matching = new Course(1L, "A", SkillArea.PROGRAMMING, SkillLevel.INTERMEDIATE, 20);
        Course stretch = new Course(2L, "B", SkillArea.PROGRAMMING, SkillLevel.ADVANCED, 20);

        assertThat(scorer.score(context, matching)).isGreaterThan(scorer.score(context, stretch));
    }

    @Test
    void levelRuleExcludesCourseMoreThanOneStepAboveCurrentLevel() {
        LevelRule rule = new LevelRule();
        LearningContext beginner = new LearningContext(SkillArea.PROGRAMMING, SkillLevel.BEGINNER);
        Course advancedCourse = new Course(1L, "A", SkillArea.PROGRAMMING, SkillLevel.ADVANCED, 30);
        Course intermediateCourse = new Course(2L, "B", SkillArea.PROGRAMMING, SkillLevel.INTERMEDIATE, 25);

        assertThat(rule.isSatisfiedBy(beginner, advancedCourse)).isFalse();
        assertThat(rule.isSatisfiedBy(beginner, intermediateCourse)).isTrue();
    }

    @Test
    void recommendOnlyReturnsCoursesInTargetSkillArea() {
        CourseCatalog catalog = new CourseCatalog();
        LearningPathService service = new LearningPathService(
                catalog, List.of(new SkillAreaMatchRule(), new LevelRule()), new LearningScorer());

        LearningContext context = new LearningContext(SkillArea.DESIGN, SkillLevel.INTERMEDIATE);
        List<Course> recommendations = service.recommend(context);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).allMatch(course -> course.skillArea() == SkillArea.DESIGN);
    }
}
