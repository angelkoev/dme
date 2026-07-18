package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.Exercise;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository {

    List<Exercise> findAll();

    Optional<Exercise> findById(Long id);

    Exercise save(Exercise exercise);
}
