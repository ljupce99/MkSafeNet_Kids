package com.mksafenet.controller;

import com.mksafenet.model.School;
import com.mksafenet.model.User;
import com.mksafenet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/schools")
    public ResponseEntity<List<School>> getSchools() {
        return ResponseEntity.ok(adminService.getAllSchools());
    }

    @PostMapping("/schools")
    public ResponseEntity<?> createSchool(@RequestBody Map<String, String> body) {
        try {
            School school = adminService.createSchool(
                body.get("name"), body.get("address"), body.get("city"));
            return ResponseEntity.ok(school);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/schools/{id}")
    public ResponseEntity<?> getSchoolDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.getSchoolDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/teachers")
    public ResponseEntity<?> createTeacher(@RequestBody Map<String, Object> body) {
        try {
            User teacher = adminService.createTeacher(
                (String) body.get("username"),
                (String) body.get("password"),
                (String) body.get("displayName"),
                Long.parseLong(body.get("schoolId").toString())
            );
            return ResponseEntity.ok(Map.of(
                "id", teacher.getId(),
                "username", teacher.getUsername(),
                "displayName", teacher.getDisplayName(),
                "schoolId", teacher.getSchool().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/teachers")
    public ResponseEntity<?> getTeachers() {
        return ResponseEntity.ok(adminService.getAllTeachers().stream()
            .map(t -> Map.of(
                "id", t.getId(),
                "username", t.getUsername(),
                "displayName", t.getDisplayName(),
                "schoolId", t.getSchool() != null ? t.getSchool().getId() : null,
                "schoolName", t.getSchool() != null ? t.getSchool().getName() : null
            )).toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getGlobalStats());
    }
}
