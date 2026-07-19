package com.akoev.dme.learning;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseCatalog {

    private static final List<Course> COURSES = List.of(
            new Course(1L, "Programming Fundamentals", SkillArea.PROGRAMMING, SkillLevel.BEGINNER, 20),
            new Course(2L, "Object-Oriented Design", SkillArea.PROGRAMMING, SkillLevel.INTERMEDIATE, 25),
            new Course(3L, "Distributed Systems", SkillArea.PROGRAMMING, SkillLevel.ADVANCED, 35),
            new Course(4L, "Design Basics", SkillArea.DESIGN, SkillLevel.BEGINNER, 15),
            new Course(5L, "UX Research Methods", SkillArea.DESIGN, SkillLevel.INTERMEDIATE, 20),
            new Course(6L, "Design Systems at Scale", SkillArea.DESIGN, SkillLevel.ADVANCED, 30),
            new Course(7L, "Intro to Data Analysis", SkillArea.DATA, SkillLevel.BEGINNER, 18),
            new Course(8L, "Statistical Modeling", SkillArea.DATA, SkillLevel.INTERMEDIATE, 28),
            new Course(9L, "Machine Learning in Practice", SkillArea.DATA, SkillLevel.ADVANCED, 40),
            new Course(10L, "Managing Your First Team", SkillArea.MANAGEMENT, SkillLevel.BEGINNER, 12),
            new Course(11L, "Strategic Planning", SkillArea.MANAGEMENT, SkillLevel.INTERMEDIATE, 22),
            new Course(12L, "Organizational Leadership", SkillArea.MANAGEMENT, SkillLevel.ADVANCED, 30),
            new Course(13L, "Marketing Foundations", SkillArea.MARKETING, SkillLevel.BEGINNER, 14),
            new Course(14L, "Growth Marketing", SkillArea.MARKETING, SkillLevel.INTERMEDIATE, 20),
            new Course(15L, "Brand Strategy at Scale", SkillArea.MARKETING, SkillLevel.ADVANCED, 26)
    );

    public List<Course> findAll() {
        return COURSES;
    }
}
