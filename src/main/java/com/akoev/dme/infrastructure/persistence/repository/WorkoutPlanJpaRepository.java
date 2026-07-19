package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.WorkoutPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkoutPlanJpaRepository extends JpaRepository<WorkoutPlanEntity, Long> {

    List<WorkoutPlanEntity> findAllByUser_Id(Long userId);

    Optional<WorkoutPlanEntity> findFirstByUser_IdAndActiveTrueOrderByGeneratedAtDesc(Long userId);

    // Bulk flag flip rather than loading + re-saving entities: WorkoutPlanEntity.sessions
    // is CascadeType.ALL + orphanRemoval, so re-saving a fetched plan just to flip
    // `active` would risk Hibernate treating it as a session rewrite and cascading
    // into child workout_sessions/session_exercises (and, transitively, workout_logs
    // via ON DELETE CASCADE) for no reason.
    @Modifying
    @Query("update WorkoutPlanEntity w set w.active = false where w.user.id = :userId and w.active = true")
    void deactivateAllForUser(@Param("userId") Long userId);
}
