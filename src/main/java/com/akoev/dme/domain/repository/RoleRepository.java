package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.RoleName;

import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findByName(RoleName name);
}
