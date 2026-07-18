package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.UserRepository;
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
public class UserProfileService {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final ExerciseRepository exerciseRepository;

    public UserProfile getProfile(Long userId) {
        User user = findUser(userId);
        if (user.getProfile() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not created yet");
        }
        return user.getProfile();
    }

    @Transactional
    public UserProfile updateProfile(Long userId, ProfileUpdateCommand command) {
        User user = findUser(userId);

        Set<Equipment> equipment = Set.copyOf(equipmentRepository.findAllById(nullToEmpty(command.equipmentIds())));
        Set<Exercise> favorites = Set.copyOf(exerciseRepository.findAllById(nullToEmpty(command.favoriteExerciseIds())));
        Set<Exercise> dislikes = Set.copyOf(exerciseRepository.findAllById(nullToEmpty(command.dislikedExerciseIds())));
        List<UserLimitation> limitations = command.limitations() == null ? List.of() : command.limitations().stream()
                .map(l -> UserLimitation.builder().muscleGroup(l.muscleGroup()).note(l.note()).build())
                .toList();

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .birthDate(command.birthDate())
                .sex(command.sex())
                .heightCm(command.heightCm())
                .weightKg(command.weightKg())
                .experienceLevel(command.experienceLevel())
                .primaryGoal(command.primaryGoal())
                .daysPerWeek(command.daysPerWeek())
                .sessionDurationMinutes(command.sessionDurationMinutes())
                .notes(command.notes())
                .location(command.location())
                .availableEquipment(equipment)
                .favoriteExercises(favorites)
                .dislikedExercises(dislikes)
                .preferredCategories(command.preferredCategories() == null ? Set.of() : command.preferredCategories())
                .unwantedCategories(command.unwantedCategories() == null ? Set.of() : command.unwantedCategories())
                .limitations(limitations)
                .restDays(command.restDays() == null ? Set.of() : command.restDays())
                .build();

        user.setProfile(profile);
        User saved = userRepository.save(user);
        return saved.getProfile();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private <T> Set<T> nullToEmpty(Set<T> set) {
        return set == null ? Set.of() : set;
    }
}
