package com.codejit.assessment.service;

import com.codejit.assessment.entity.Assessment;
import com.codejit.assessment.entity.CodingQuestion;
import com.codejit.assessment.entity.TestCase;
import com.codejit.assessment.repository.AssessmentRepository;
import com.codejit.assessment.repository.CodingQuestionRepository;
import com.codejit.assessment.repository.TestCaseRepository;
import com.codejit.common.dto.assessment.*;
import com.codejit.common.exception.BadRequestException;
import com.codejit.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentRepository assessmentRepository;
    private final CodingQuestionRepository codingQuestionRepository;
    private final TestCaseRepository testCaseRepository;
    private final ShareCodeGenerator shareCodeGenerator;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_SHARE_PREFIX = "assessment:share:";
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            CodingQuestionRepository codingQuestionRepository,
            TestCaseRepository testCaseRepository,
            ShareCodeGenerator shareCodeGenerator,
            RedisTemplate<String, Object> redisTemplate) {
        this.assessmentRepository = assessmentRepository;
        this.codingQuestionRepository = codingQuestionRepository;
        this.testCaseRepository = testCaseRepository;
        this.shareCodeGenerator = shareCodeGenerator;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request, String creatorEmail, Long creatorId) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Assessment title is required");
        }

        String shareCode = generateUniqueShareCode();

        Assessment assessment = Assessment.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .shareCode(shareCode)
                .durationMinutes(request.getDurationMinutes() > 0 ? request.getDurationMinutes() : 60)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AssessmentStatus.DRAFT)
                .creatorEmail(creatorEmail)
                .creatorId(creatorId)
                .build();

        if (request.getQuestions() != null) {
            int qIndex = 1;
            for (CodingQuestionRequest qReq : request.getQuestions()) {
                CodingQuestion question = CodingQuestion.builder()
                        .question(qReq.getQuestion())
                        .questionNumber(qReq.getQuestionNumber() > 0 ? qReq.getQuestionNumber() : qIndex++)
                        .language(qReq.getLanguage() != null ? qReq.getLanguage() : "java")
                        .starterCode(qReq.getStarterCode())
                        .build();

                if (qReq.getTestCases() != null) {
                    int tcIndex = 0;
                    for (TestCaseRequest tcReq : qReq.getTestCases()) {
                        TestCase tc = TestCase.builder()
                                .sequence(tcReq.getSequence() >= 0 ? tcReq.getSequence() : tcIndex++)
                                .input(tcReq.getInput())
                                .expectedOutput(tcReq.getExpectedOutput())
                                .visible(tcReq.isVisible())
                                .build();
                        question.addTestCase(tc);
                    }
                }
                assessment.addQuestion(question);
            }
        }

        Assessment saved = assessmentRepository.save(assessment);
        cacheAssessmentShare(saved);

        return toAssessmentResponse(saved, false);
    }

    @Transactional(readOnly = true)
    public List<AssessmentSummaryDto> getAssessments(String creatorEmail) {
        List<Assessment> assessments = (creatorEmail != null && !creatorEmail.isBlank())
                ? assessmentRepository.findByCreatorEmailOrderByCreatedAtDesc(creatorEmail)
                : assessmentRepository.findAll();

        return assessments.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(Long id, boolean includeHidden) {
        Assessment assessment = assessmentRepository.findByIdWithQuestions(id)
                .or(() -> assessmentRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));

        return toAssessmentResponse(assessment, includeHidden);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentByShareCode(String shareCode) {
        Assessment assessment = assessmentRepository.findByShareCodeWithQuestions(shareCode.trim().toUpperCase())
                .or(() -> assessmentRepository.findByShareCodeIgnoreCase(shareCode.trim().toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with share code: " + shareCode));

        return toAssessmentResponse(assessment, false);
    }

    @Transactional
    public void startAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));

        assessment.setStatus(AssessmentStatus.STARTED);
        assessmentRepository.save(assessment);
        cacheAssessmentShare(assessment);
    }

    @Transactional(readOnly = true)
    public void joinAssessment(String shareCode, String candidateEmail) {
        Assessment assessment = assessmentRepository.findByShareCodeIgnoreCase(shareCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with code: " + shareCode));

        if (assessment.getStatus() == AssessmentStatus.ENDED) {
            throw new BadRequestException("This assessment has already ended.");
        }
        log.info("Candidate {} joined assessment {}", candidateEmail, assessment.getTitle());
    }

    private String generateUniqueShareCode() {
        for (int i = 0; i < 10; i++) {
            String code = shareCodeGenerator.generate();
            if (assessmentRepository.findByShareCodeIgnoreCase(code).isEmpty()) {
                return code;
            }
        }
        return shareCodeGenerator.generate();
    }

    private void cacheAssessmentShare(Assessment assessment) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(REDIS_SHARE_PREFIX + assessment.getShareCode(), assessment.getId().toString(), CACHE_TTL);
            }
        } catch (Exception e) {
            log.warn("Failed to cache assessment share code in Redis: {}", e.getMessage());
        }
    }

    private AssessmentSummaryDto toSummaryDto(Assessment assessment) {
        int qCount = assessment.getQuestions() != null ? assessment.getQuestions().size() : 0;
        return AssessmentSummaryDto.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .durationMinutes(assessment.getDurationMinutes())
                .status(assessment.getStatus())
                .questionCount(qCount)
                .build();
    }

    private AssessmentResponse toAssessmentResponse(Assessment assessment, boolean includeHidden) {
        List<CodingQuestion> questions = (assessment.getQuestions() != null && !assessment.getQuestions().isEmpty())
                ? assessment.getQuestions()
                : codingQuestionRepository.findByAssessmentIdOrderByQuestionNumberAsc(assessment.getId());

        List<CodingQuestionDto> qDtos = new ArrayList<>();
        for (CodingQuestion q : questions) {
            List<TestCase> testCases = (q.getTestCases() != null && !q.getTestCases().isEmpty())
                    ? q.getTestCases()
                    : testCaseRepository.findByCodingQuestionIdOrderBySequenceAsc(q.getId());

            List<TestCaseDto> visibleCases = testCases.stream()
                    .filter(TestCase::isVisible)
                    .map(tc -> TestCaseDto.builder()
                            .id(tc.getId())
                            .sequence(tc.getSequence())
                            .input(tc.getInput())
                            .expectedOutput(tc.getExpectedOutput())
                            .visible(true)
                            .build())
                    .collect(Collectors.toList());

            List<TestCaseDto> allCases = includeHidden
                    ? testCases.stream().map(tc -> TestCaseDto.builder()
                            .id(tc.getId())
                            .sequence(tc.getSequence())
                            .input(tc.getInput())
                            .expectedOutput(tc.getExpectedOutput())
                            .visible(tc.isVisible())
                            .build()).collect(Collectors.toList())
                    : visibleCases;

            qDtos.add(CodingQuestionDto.builder()
                    .id(q.getId())
                    .question(q.getQuestion())
                    .questionNumber(q.getQuestionNumber())
                    .language(q.getLanguage())
                    .starterCode(q.getStarterCode())
                    .visibleTestCases(visibleCases)
                    .testCases(allCases)
                    .build());
        }

        return AssessmentResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .shareCode(assessment.getShareCode())
                .durationMinutes(assessment.getDurationMinutes())
                .startTime(assessment.getStartTime())
                .endTime(assessment.getEndTime())
                .status(assessment.getStatus())
                .questionCount(qDtos.size())
                .questions(qDtos)
                .build();
    }
}

