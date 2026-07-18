package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.RoleName;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.WorkoutStreak;
import com.akoev.dme.domain.repository.RoleRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.domain.repository.WorkoutStreakRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutStreakRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private WorkoutStreakRepository workoutStreakRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void savesAndReloadsAndUpdatesWorkoutStreak() {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        User user = userRepository.save(User.builder()
                .username("streak.owner")
                .email("streak.owner@example.com")
                .passwordHash("hashed-password")
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(userRole))
                .build());

        workoutStreakRepository.save(WorkoutStreak.builder()
                .userId(user.getId())
                .currentStreak(1)
                .longestStreak(1)
                .lastWorkoutDate(LocalDate.now().minusDays(1))
                .build());

        workoutStreakRepository.save(WorkoutStreak.builder()
                .userId(user.getId())
                .currentStreak(2)
                .longestStreak(2)
                .lastWorkoutDate(LocalDate.now())
                .build());

        WorkoutStreak reloaded = workoutStreakRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(reloaded.getCurrentStreak()).isEqualTo(2);
        assertThat(reloaded.getLongestStreak()).isEqualTo(2);
        assertThat(reloaded.getLastWorkoutDate()).isEqualTo(LocalDate.now());
    }
}
