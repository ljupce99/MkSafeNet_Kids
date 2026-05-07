package com.mksafenet.controller;

import com.mksafenet.model.User;
import com.mksafenet.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(@AuthenticationPrincipal User teacher,
                                            @RequestBody Map<String, String> body) {
        try {
            String name = body.getOrDefault("name", "Class Session");
            return ResponseEntity.ok(teacherService.createSession(teacher, name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(@AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(teacherService.getTeacherSessions(teacher));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<?> getSessionDetail(@AuthenticationPrincipal User teacher,
                                               @PathVariable Long id) {
        try {
            return ResponseEntity.ok(teacherService.getSessionDetail(teacher, id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
    }

    @GetMapping("/sessions/{id}/qr")
    public ResponseEntity<?> getSessionQr(@AuthenticationPrincipal User teacher,
                                           @PathVariable Long id) {
        try {
            return ResponseEntity.ok(teacherService.getSessionQr(teacher, id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/sessions/{id}/toggle")
    public ResponseEntity<?> toggleSession(@AuthenticationPrincipal User teacher,
                                            @PathVariable Long id,
                                            @RequestBody Map<String, Boolean> body) {
        try {
            teacherService.toggleSession(teacher, id, body.get("active"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
