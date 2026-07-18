package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.domain.model.RoleName;
import com.akoev.dme.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(RoleName name);
}
