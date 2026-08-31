package com.codejit.assessment;

import com.codejit.assessment.entity.Assessment;
import com.codejit.assessment.entity.CodingQuestion;
import com.codejit.assessment.entity.TestCase;
import com.codejit.assessment.repository.AssessmentRepository;
import com.codejit.assessment.repository.CodingQuestionRepository;
import com.codejit.assessment.repository.TestCaseRepository;
import com.codejit.assessment.service.AssessmentService;
import com.codejit.assessment.service.ShareCodeGenerator;
import com.codejit.common.dto.assessment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private CodingQuestionRepository codingQuestionRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private ShareCodeGenerator shareCodeGenerator;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private AssessmentService assessmentService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        assessmentService = new AssessmentService(
                assessmentRepository,
                codingQuestionRepository,
                testCaseRepository,
                shareCodeGenerator,
                redisTemplate
        );
    }

    @Test
    @DisplayName("Should create assessment with questions and test cases successfully")
    void testCreateAssessment() {
        AssessmentRequest request = AssessmentRequest.builder()
                .title("Java Core Assessment")
                .description("Test on data structures")
                .durationMinutes(45)
                .questions(List.of(
                        CodingQuestionRequest.builder()
                                .question("Reverse a linked list")
                                .questionNumber(1)
                                .language("java")
                                .starterCode("class Main {}")
                                .testCases(List.of(
                                        TestCaseRequest.builder()
                                                .input("[1,2,3]")
                                                .expectedOutput("[3,2,1]")
                                                .visible(true)
                                                .sequence(0)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        when(shareCodeGenerator.generate()).thenReturn("ABC12345");
        when(assessmentRepository.findByShareCodeIgnoreCase("ABC12345")).thenReturn(Optional.empty());

        Assessment saved = Assessment.builder()
                .id(1L)
                .title("Java Core Assessment")
                .description("Test on data structures")
                .shareCode("ABC12345")
                .durationMinutes(45)
                .status(AssessmentStatus.DRAFT)
                .creatorEmail("dev@codejit.io")
                .questions(List.of(
                        CodingQuestion.builder()
                                .id(10L)
                                .question("Reverse a linked list")
                                .questionNumber(1)
                                .language("java")
                                .starterCode("class Main {}")
                                .testCases(List.of(
                                        TestCase.builder()
                                                .id(100L)
                                                .input("[1,2,3]")
                                                .expectedOutput("[3,2,1]")
                                                .visible(true)
                                                .sequence(0)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        when(assessmentRepository.save(any(Assessment.class))).thenReturn(saved);

        AssessmentResponse response = assessmentService.createAssessment(request, "dev@codejit.io", 1L);

        assertNotNull(response);
        assertEquals("ABC12345", response.getShareCode());
        assertEquals("Java Core Assessment", response.getTitle());
        assertEquals(1, response.getQuestionCount());
    }

    @Test
    @DisplayName("Should lookup assessment by share code")
    void testGetAssessmentByShareCode() {
        Assessment assessment = Assessment.builder()
                .id(5L)
                .title("System Design Sprint")
                .shareCode("DESIGN99")
                .durationMinutes(60)
                .status(AssessmentStatus.STARTED)
                .build();

        when(assessmentRepository.findByShareCodeWithQuestions("DESIGN99")).thenReturn(Optional.of(assessment));

        AssessmentResponse response = assessmentService.getAssessmentByShareCode("DESIGN99");

        assertNotNull(response);
        assertEquals("DESIGN99", response.getShareCode());
        assertEquals("System Design Sprint", response.getTitle());
    }
}

