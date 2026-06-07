package com.mksafenet.controller;

import com.mksafenet.model.Scenario;
import com.mksafenet.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scenarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<List<Scenario>> getAllScenarios() {
        return ResponseEntity.ok(scenarioService.getAllScenarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScenarioById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(scenarioService.getScenarioById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createScenario(@RequestBody Scenario scenario) {
        try {
            return ResponseEntity.ok(scenarioService.createScenario(scenario));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateScenario(@PathVariable Long id, @RequestBody Scenario scenario) {
        try {
            return ResponseEntity.ok(scenarioService.updateScenario(id, scenario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScenario(@PathVariable Long id) {
        try {
            scenarioService.deleteScenario(id);
            return ResponseEntity.ok(Map.of("message", "Scenario deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

