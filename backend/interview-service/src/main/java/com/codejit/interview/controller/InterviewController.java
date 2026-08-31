package com.codejit.interview.controller;

import com.codejit.common.dto.interview.InterviewRequest;
import com.codejit.common.dto.interview.InterviewResponse;
import com.codejit.interview.service.InterviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getInterviews(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        List<InterviewResponse> responses = interviewService.getInterviews(userEmail);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> createInterview(
            @RequestBody InterviewRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        Long userId = (userIdStr != null && !userIdStr.isBlank()) ? Long.parseLong(userIdStr) : null;
        InterviewResponse response = interviewService.createInterview(request, userEmail, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        InterviewResponse response = interviewService.getInterviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/join/{code}")
    public ResponseEntity<InterviewResponse> getInterviewByCode(@PathVariable String code) {
        InterviewResponse response = interviewService.getInterviewByShareCode(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<InterviewResponse> joinInterview(
            @PathVariable String code,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        Long userId = (userIdStr != null && !userIdStr.isBlank()) ? Long.parseLong(userIdStr) : null;
        InterviewResponse response = interviewService.joinInterview(code, userEmail, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<InterviewResponse> startInterview(@PathVariable Long id) {
        InterviewResponse response = interviewService.startInterview(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<InterviewResponse> endInterview(@PathVariable Long id) {
        InterviewResponse response = interviewService.endInterview(id);
        return ResponseEntity.ok(response);
    }
}

