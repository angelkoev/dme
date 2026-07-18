package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.UserLimitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLimitationJpaRepository extends JpaRepository<UserLimitationEntity, Long> {

    List<UserLimitationEntity> findAllByUser_Id(Long userId);
}
