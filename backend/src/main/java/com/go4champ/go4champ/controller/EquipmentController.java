package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.Equipment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "EquipmentController", description = "API für verfügbare Equipment-Optionen")
@RestController
@RequestMapping("/api")
public class EquipmentController {

    @Operation(summary = "Holt alle verfügbaren Equipment-Optionen")
    @GetMapping("/equipment/available")
    public ResponseEntity<Map<String, Object>> getAvailableEquipment() {
        Map<String, Object> response = new HashMap<>();

        // Equipment-Namen (für Backend)
        List<String> equipmentNames = Arrays.asList(Equipment.getAllNames());

        // Display-Namen (für Frontend)
        List<String> displayNames = Arrays.asList(Equipment.getAllDisplayNames());

        // Vollständige Equipment-Info
        Map<String, String> equipmentMap = new HashMap<>();
        for (Equipment equipment : Equipment.values()) {
            equipmentMap.put(equipment.name(), equipment.getDisplayName());
        }

        response.put("equipmentNames", equipmentNames);
        response.put("displayNames", displayNames);
        response.put("equipmentMap", equipmentMap);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validiert Equipment-Namen")
    @PostMapping("/equipment/validate")
    public ResponseEntity<Map<String, Object>> validateEquipment(@RequestBody List<String> equipmentList) {
        Map<String, Object> response = new HashMap<>();
        List<String> validEquipment = equipmentList.stream()
                .filter(equipment -> {
                    try {
                        Equipment.fromString(equipment);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .toList();

        response.put("validEquipment", validEquipment);
        response.put("isValid", validEquipment.size() == equipmentList.size());

        return ResponseEntity.ok(response);
    }
}