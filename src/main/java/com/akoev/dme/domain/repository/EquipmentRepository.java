package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.Equipment;

import java.util.Collection;
import java.util.List;

public interface EquipmentRepository {

    List<Equipment> findAll();

    List<Equipment> findAllById(Collection<Long> ids);
}
