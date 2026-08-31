package com.codejit.assessment.controller;

import com.codejit.assessment.service.AssessmentService;
import com.codejit.common.dto.assessment.AssessmentRequest;
import com.codejit.common.dto.assessment.AssessmentResponse;
import com.codejit.common.dto.assessment.AssessmentSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public ResponseEntity<List<AssessmentSummaryDto>> getAssessments(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        List<AssessmentSummaryDto> summaries = assessmentService.getAssessments(userEmail);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping
    public ResponseEntity<AssessmentResponse> createAssessment(
            @RequestBody AssessmentRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        Long userId = (userIdStr != null && !userIdStr.isBlank()) ? Long.parseLong(userIdStr) : null;
        AssessmentResponse response = assessmentService.createAssessment(request, userEmail, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        AssessmentResponse response = assessmentService.getAssessmentById(id, false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startAssessment(@PathVariable Long id) {
        assessmentService.startAssessment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/join/{code}")
    public ResponseEntity<AssessmentResponse> getAssessmentByCode(@PathVariable String code) {
        AssessmentResponse response = assessmentService.getAssessmentByShareCode(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<Void> joinAssessment(
            @PathVariable String code,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        assessmentService.joinAssessment(code, userEmail != null ? userEmail : "anonymous");
        return ResponseEntity.noContent().build();
    }
}

