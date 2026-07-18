package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public List<Exercise> listAll() {
        return exerciseRepository.findAll();
    }

    public Exercise getById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found: " + id));
    }

    @Transactional
    public Exercise create(ExerciseUpsertCommand command) {
        return exerciseRepository.save(toDomain(null, command));
    }

    @Transactional
    public Exercise update(Long id, ExerciseUpsertCommand command) {
        getById(id);
        return exerciseRepository.save(toDomain(id, command));
    }

    private Exercise toDomain(Long id, ExerciseUpsertCommand command) {
        Set<Equipment> equipment = command.equipmentIds() == null ? Set.of() : command.equipmentIds().stream()
                .map(equipmentId -> Equipment.builder().id(equipmentId).build())
                .collect(Collectors.toSet());

        return Exercise.builder()
                .id(id)
                .name(command.name())
                .description(command.description())
                .primaryMuscleGroup(command.primaryMuscleGroup())
                .movementPattern(command.movementPattern())
                .difficultyLevel(command.difficultyLevel())
                .exerciseType(command.exerciseType())
                .instructions(command.instructions())
                .videoUrl(command.videoUrl())
                .requiredEquipment(equipment)
                .build();
    }
}
