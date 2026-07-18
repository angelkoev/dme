package com.akoev.dme.web.api;

import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.web.api.dto.EquipmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;

    @GetMapping
    public List<EquipmentResponse> list() {
        return equipmentRepository.findAll().stream().map(EquipmentResponse::from).toList();
    }
}
