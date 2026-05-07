package com.mksafenet.controller;

import com.mksafenet.dto.ChatRespondRequest;
import com.mksafenet.dto.ChatResponseDto;
import com.mksafenet.dto.ChatStartRequest;
import com.mksafenet.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/session/{token}")
    public ResponseEntity<?> validateSession(@PathVariable String token) {
        ChatService.SessionInfo info = chatService.validateSession(token);
        if (!info.valid()) {
            return ResponseEntity.status(404).body(Map.of("error", "Session not found or inactive"));
        }
        return ResponseEntity.ok(Map.of(
            "valid", true,
            "sessionName", info.sessionName(),
            "schoolName", info.schoolName()
        ));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startChat(@RequestBody ChatStartRequest request) {
        try {
            ChatResponseDto response = chatService.startChat(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/respond")
    public ResponseEntity<?> respond(@RequestBody ChatRespondRequest request) {
        try {
            ChatResponseDto response = chatService.respond(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
