package com.codejit.execution.controller;

import com.codejit.common.dto.execution.CodeRequest;
import com.codejit.common.dto.execution.RunResponse;
import com.codejit.common.dto.execution.SubmissionResponse;
import com.codejit.execution.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ExecutionController {

    private final SubmissionService submissionService;

    public ExecutionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/api/v1/execution/run")
    public ResponseEntity<RunResponse> executeDirect(@RequestBody CodeRequest request) {
        RunResponse response = submissionService.executeDirect(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/assessments/{assessmentId}/questions/{questionId}/run")
    public ResponseEntity<RunResponse> runCode(
            @PathVariable Long assessmentId,
            @PathVariable Long questionId,
            @RequestBody CodeRequest request) {
        RunResponse response = submissionService.runCode(assessmentId, questionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/assessments/{assessmentId}/questions/{questionId}/submit")
    public ResponseEntity<SubmissionResponse> submitCode(
            @PathVariable Long assessmentId,
            @PathVariable Long questionId,
            @RequestBody CodeRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        Long userId = (userIdStr != null && !userIdStr.isBlank()) ? Long.parseLong(userIdStr) : null;
        SubmissionResponse response = submissionService.submitCode(assessmentId, questionId, request, userEmail, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/submissions/{id}")
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable Long id) {
        SubmissionResponse response = submissionService.getSubmission(id);
        return ResponseEntity.ok(response);
    }
}
