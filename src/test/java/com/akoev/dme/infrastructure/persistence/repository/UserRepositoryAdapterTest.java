package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.RoleName;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.RoleRepository;
import com.akoev.dme.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void savesAndReloadsUserWithProfileEquipmentAndLimitations() {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        List<Equipment> equipment = equipmentRepository.findAll();
        Equipment dumbbell = equipment.stream().filter(e -> e.getName().equals("Dumbbell")).findFirst().orElseThrow();
        Equipment bodyweight = equipment.stream().filter(e -> e.getName().equals("Bodyweight")).findFirst().orElseThrow();

        Exercise pullUp = exerciseRepository.save(Exercise.builder()
                .name("Pull-up")
                .primaryMuscleGroup(MuscleGroup.BACK)
                .movementPattern(MovementPattern.PULL)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .exerciseType(ExerciseType.COMPOUND)
                .build());
        Exercise burpee = exerciseRepository.save(Exercise.builder()
                .name("Burpee")
                .primaryMuscleGroup(MuscleGroup.FULL_BODY)
                .movementPattern(MovementPattern.FULL_BODY)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .exerciseType(ExerciseType.COMPOUND)
                .build());

        UserProfile profile = UserProfile.builder()
                .experienceLevel(ExperienceLevel.BEGINNER)
                .primaryGoal(TrainingGoal.HYPERTROPHY)
                .daysPerWeek(3)
                .sessionDurationMinutes(60)
                .location(Location.HOME)
                .availableEquipment(Set.of(dumbbell, bodyweight))
                .limitations(List.of(UserLimitation.builder()
                        .muscleGroup(MuscleGroup.SHOULDERS)
                        .note("Rotator cuff strain, avoid overhead pressing")
                        .build()))
                .restDays(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
                .preferredCategories(Set.of(MuscleGroup.BACK))
                .unwantedCategories(Set.of(MuscleGroup.CALVES))
                .favoriteExercises(Set.of(pullUp))
                .dislikedExercises(Set.of(burpee))
                .build();

        User newUser = User.builder()
                .username("iva.petrova")
                .email("iva@example.com")
                .passwordHash("hashed-password")
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(userRole))
                .profile(profile)
                .build();

        User saved = userRepository.save(newUser);
        assertThat(saved.getId()).isNotNull();

        User reloaded = userRepository.findByUsername("iva.petrova").orElseThrow();

        assertThat(reloaded.getEmail()).isEqualTo("iva@example.com");
        assertThat(reloaded.getRoles()).extracting(Role::getName).containsExactly(RoleName.ROLE_USER);

        UserProfile reloadedProfile = reloaded.getProfile();
        assertThat(reloadedProfile).isNotNull();
        assertThat(reloadedProfile.getPrimaryGoal()).isEqualTo(TrainingGoal.HYPERTROPHY);
        assertThat(reloadedProfile.getDaysPerWeek()).isEqualTo(3);
        assertThat(reloadedProfile.getLocation()).isEqualTo(Location.HOME);
        assertThat(reloadedProfile.getAvailableEquipment())
                .extracting(Equipment::getName)
                .containsExactlyInAnyOrder("Dumbbell", "Bodyweight");
        assertThat(reloadedProfile.getLimitations()).hasSize(1);
        assertThat(reloadedProfile.getLimitations().get(0).getMuscleGroup()).isEqualTo(MuscleGroup.SHOULDERS);
        assertThat(reloadedProfile.getRestDays()).containsExactlyInAnyOrder(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        assertThat(reloadedProfile.getPreferredCategories()).containsExactly(MuscleGroup.BACK);
        assertThat(reloadedProfile.getUnwantedCategories()).containsExactly(MuscleGroup.CALVES);
        assertThat(reloadedProfile.getFavoriteExercises()).extracting(Exercise::getName).containsExactly("Pull-up");
        assertThat(reloadedProfile.getDislikedExercises()).extracting(Exercise::getName).containsExactly("Burpee");
    }
}
