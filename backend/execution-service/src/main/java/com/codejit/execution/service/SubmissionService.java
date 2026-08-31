package com.codejit.execution.service;

import com.codejit.common.dto.assessment.TestCaseDto;
import com.codejit.common.dto.execution.*;
import com.codejit.common.event.SubmissionResultEvent;
import com.codejit.common.exception.BadRequestException;
import com.codejit.common.exception.ResourceNotFoundException;
import com.codejit.execution.engine.CodeExecutionEngine;
import com.codejit.execution.entity.Submission;
import com.codejit.execution.entity.SubmissionTestCaseResult;
import com.codejit.execution.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final CodeExecutionEngine executionEngine;
    private final SubmissionRepository submissionRepository;
    private final KafkaSubmissionResultProducer kafkaResultProducer;

    private static final long DEFAULT_TIMEOUT_MS = 4000L;

    public SubmissionService(
            CodeExecutionEngine executionEngine,
            SubmissionRepository submissionRepository,
            KafkaSubmissionResultProducer kafkaResultProducer) {
        this.executionEngine = executionEngine;
        this.submissionRepository = submissionRepository;
        this.kafkaResultProducer = kafkaResultProducer;
    }

    public RunResponse executeDirect(CodeRequest request) {
        if (request.getSourceCode() == null || request.getSourceCode().isBlank()) {
            throw new BadRequestException("Source code cannot be empty");
        }

        String language = request.getLanguage() != null ? request.getLanguage() : "java";
        List<TestResult> results = new ArrayList<>();
        long totalRuntime = 0;
        boolean allPassed = true;

        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            int seq = 0;
            for (TestCaseDto tc : request.getTestCases()) {
                TestResult res = executionEngine.execute(
                        language,
                        request.getSourceCode(),
                        tc.getInput() != null ? tc.getInput() : "",
                        tc.getExpectedOutput() != null ? tc.getExpectedOutput() : "",
                        seq++,
                        DEFAULT_TIMEOUT_MS
                );
                results.add(res);
                totalRuntime += res.getRuntimeMillis();
                if (!res.isPassed()) {
                    allPassed = false;
                }
            }
        } else {
            TestResult res = executionEngine.execute(
                    language,
                    request.getSourceCode(),
                    request.getInput() != null ? request.getInput() : "",
                    request.getExpectedOutput() != null ? request.getExpectedOutput() : "",
                    0,
                    DEFAULT_TIMEOUT_MS
            );
            results.add(res);
            totalRuntime += res.getRuntimeMillis();
            allPassed = res.isPassed();
        }

        return RunResponse.builder()
                .results(results)
                .totalRuntimeMillis(totalRuntime)
                .success(allPassed)
                .build();
    }

    @Transactional
    public RunResponse runCode(Long assessmentId, Long questionId, CodeRequest request) {
        return executeDirect(request);
    }

    @Transactional
    public SubmissionResponse submitCode(Long assessmentId, Long questionId, CodeRequest request, String candidateEmail, Long candidateId) {
        if (request.getSourceCode() == null || request.getSourceCode().isBlank()) {
            throw new BadRequestException("Source code cannot be empty");
        }

        String language = request.getLanguage() != null ? request.getLanguage() : "java";

        Submission submission = Submission.builder()
                .assessmentId(assessmentId)
                .questionId(questionId)
                .candidateEmail(candidateEmail)
                .candidateId(candidateId)
                .sourceCode(request.getSourceCode())
                .language(language)
                .status(SubmissionStatus.RUNNING)
                .passedTests(0)
                .totalTests(1)
                .build();

        Submission saved = submissionRepository.save(submission);

        TestResult testResult = executionEngine.execute(
                language,
                request.getSourceCode(),
                request.getInput() != null ? request.getInput() : "",
                request.getExpectedOutput() != null ? request.getExpectedOutput() : "",
                0,
                DEFAULT_TIMEOUT_MS
        );

        SubmissionTestCaseResult caseResult = SubmissionTestCaseResult.builder()
                .sequence(0)
                .passed(testResult.isPassed())
                .actualOutput(testResult.getActualOutput())
                .expectedOutput(testResult.getExpectedOutput())
                .errorMessage(testResult.getErrorMessage())
                .runtimeMillis(testResult.getRuntimeMillis())
                .build();

        saved.addResult(caseResult);

        SubmissionStatus finalStatus = testResult.isPassed() ? SubmissionStatus.PASSED
                : (testResult.getErrorMessage() != null && testResult.getErrorMessage().contains("Time Limit Exceeded"))
                ? SubmissionStatus.TIMEOUT
                : (testResult.getErrorMessage() != null && testResult.getErrorMessage().contains("Compilation"))
                ? SubmissionStatus.ERROR
                : SubmissionStatus.FAILED;

        saved.setStatus(finalStatus);
        saved.setPassedTests(testResult.isPassed() ? 1 : 0);
        saved.setTotalTests(1);
        saved.setCompletedAt(LocalDateTime.now());

        Submission completed = submissionRepository.save(saved);

        kafkaResultProducer.publishResult(SubmissionResultEvent.builder()
                .submissionId(completed.getId())
                .assessmentId(assessmentId)
                .questionId(questionId)
                .candidateId(candidateId)
                .status(finalStatus)
                .passedTests(completed.getPassedTests())
                .totalTests(completed.getTotalTests())
                .results(List.of(testResult))
                .completedAt(completed.getCompletedAt())
                .build());

        return toSubmissionResponse(completed);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(Long id) {
        Submission submission = submissionRepository.findByIdWithResults(id)
                .or(() -> submissionRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        return toSubmissionResponse(submission);
    }

    private SubmissionResponse toSubmissionResponse(Submission submission) {
        List<TestResult> results = submission.getResults() != null
                ? submission.getResults().stream().map(r -> TestResult.builder()
                        .sequence(r.getSequence())
                        .passed(r.isPassed())
                        .actualOutput(r.getActualOutput())
                        .expectedOutput(r.getExpectedOutput())
                        .errorMessage(r.getErrorMessage())
                        .runtimeMillis(r.getRuntimeMillis())
                        .build()).collect(Collectors.toList())
                : List.of();

        return SubmissionResponse.builder()
                .id(submission.getId())
                .assessmentId(submission.getAssessmentId())
                .questionId(submission.getQuestionId())
                .status(submission.getStatus())
                .passedTests(submission.getPassedTests())
                .totalTests(submission.getTotalTests())
                .submittedAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : LocalDateTime.now().toString())
                .results(results)
                .build();
    }
}
