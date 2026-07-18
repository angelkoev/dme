package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.Equipment;

public record EquipmentResponse(Long id, String name) {

    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(equipment.getId(), equipment.getName());
    }
}
