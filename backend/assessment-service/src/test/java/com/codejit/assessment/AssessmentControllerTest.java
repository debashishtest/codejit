package com.codejit.assessment;

import com.codejit.assessment.controller.AssessmentController;
import com.codejit.assessment.service.AssessmentService;
import com.codejit.common.dto.assessment.AssessmentRequest;
import com.codejit.common.dto.assessment.AssessmentResponse;
import com.codejit.common.dto.assessment.AssessmentStatus;
import com.codejit.common.dto.assessment.AssessmentSummaryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssessmentService assessmentService;

    @Test
    @DisplayName("GET /api/v1/assessments should return assessment summaries")
    void testGetAssessments() throws Exception {
        AssessmentSummaryDto summary = AssessmentSummaryDto.builder()
                .id(1L)
                .title("Algorithms Round 1")
                .durationMinutes(60)
                .status(AssessmentStatus.STARTED)
                .questionCount(3)
                .build();

        when(assessmentService.getAssessments(any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/assessments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Algorithms Round 1"));
    }

    @Test
    @DisplayName("POST /api/v1/assessments should create assessment")
    void testCreateAssessment() throws Exception {
        AssessmentRequest request = AssessmentRequest.builder()
                .title("New Assessment")
                .durationMinutes(30)
                .build();

        AssessmentResponse response = AssessmentResponse.builder()
                .id(10L)
                .title("New Assessment")
                .shareCode("CODE1234")
                .durationMinutes(30)
                .status(AssessmentStatus.DRAFT)
                .build();

        when(assessmentService.createAssessment(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.shareCode").value("CODE1234"));
    }

    @Test
    @DisplayName("GET /api/v1/assessments/join/{code} should return assessment details")
    void testJoinByCode() throws Exception {
        AssessmentResponse response = AssessmentResponse.builder()
                .id(10L)
                .title("Live Assessment")
                .shareCode("JOIN1234")
                .durationMinutes(30)
                .status(AssessmentStatus.STARTED)
                .build();

        when(assessmentService.getAssessmentByShareCode(eq("JOIN1234"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/assessments/join/JOIN1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareCode").value("JOIN1234"));
    }
}

