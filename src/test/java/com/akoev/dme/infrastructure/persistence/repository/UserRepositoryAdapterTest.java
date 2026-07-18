package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.RoleName;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.RoleRepository;
import com.akoev.dme.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Test
    void savesAndReloadsUserWithProfileEquipmentAndLimitations() {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        List<Equipment> equipment = equipmentRepository.findAll();
        Equipment dumbbell = equipment.stream().filter(e -> e.getName().equals("Dumbbell")).findFirst().orElseThrow();
        Equipment bodyweight = equipment.stream().filter(e -> e.getName().equals("Bodyweight")).findFirst().orElseThrow();

        UserProfile profile = UserProfile.builder()
                .experienceLevel(ExperienceLevel.BEGINNER)
                .primaryGoal(TrainingGoal.HYPERTROPHY)
                .daysPerWeek(3)
                .sessionDurationMinutes(60)
                .availableEquipment(Set.of(dumbbell, bodyweight))
                .limitations(List.of(UserLimitation.builder()
                        .muscleGroup(MuscleGroup.SHOULDERS)
                        .note("Rotator cuff strain, avoid overhead pressing")
                        .build()))
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
        assertThat(reloadedProfile.getAvailableEquipment())
                .extracting(Equipment::getName)
                .containsExactlyInAnyOrder("Dumbbell", "Bodyweight");
        assertThat(reloadedProfile.getLimitations()).hasSize(1);
        assertThat(reloadedProfile.getLimitations().get(0).getMuscleGroup()).isEqualTo(MuscleGroup.SHOULDERS);
    }
}
