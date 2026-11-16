package com.codejit.interview.controller;

import com.codejit.common.dto.ai.*;
import com.codejit.interview.service.AiInterviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-interviews")
public class AiInterviewController {

    private final AiInterviewService aiInterviewService;

    public AiInterviewController(AiInterviewService aiInterviewService) {
        this.aiInterviewService = aiInterviewService;
    }

    @GetMapping
    public ResponseEntity<List<AiInterviewSummaryDto>> getSessions(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(aiInterviewService.getSessions(userEmail));
    }

    @PostMapping
    public ResponseEntity<AiInterviewSessionDto> createSession(
            @RequestBody CreateAiInterviewRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        Long userId = parseUserId(userIdStr);
        AiInterviewSessionDto created = aiInterviewService.createSession(request, userEmail, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiInterviewSessionDto> getSession(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(aiInterviewService.getSession(id, userEmail));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<AiInterviewSessionDto> submitAnswer(
            @PathVariable Long id,
            @RequestBody SubmitAiAnswerRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(aiInterviewService.submitAnswer(id, request, userEmail));
    }

    @PostMapping("/{id}/hint")
    public ResponseEntity<AiInterviewSessionDto> requestHint(
            @PathVariable Long id,
            @RequestBody(required = false) RequestHintRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        Long turnId = request != null ? request.getTurnId() : null;
        return ResponseEntity.ok(aiInterviewService.requestHint(id, turnId, userEmail));
    }

    @PostMapping("/{id}/monitor-events")
    public ResponseEntity<AiMonitoringSummaryDto> recordMonitorEvent(
            @PathVariable Long id,
            @RequestBody RecordMonitorEventRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(aiInterviewService.recordMonitorEvent(id, request, userEmail));
    }

    @PostMapping("/{id}/monitor-frame")
    public ResponseEntity<EyeMonitorResultDto> monitorFrame(
            @PathVariable Long id,
            @RequestBody MonitorFrameRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(aiInterviewService.monitorFrame(id, request, userEmail));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<AiInterviewSessionDto> completeSession(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(aiInterviewService.completeSession(id, userEmail));
    }

    private Long parseUserId(String userIdStr) {
        if (userIdStr == null || userIdStr.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
