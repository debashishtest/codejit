package com.codejit.execution;

import com.codejit.common.dto.execution.CodeRequest;
import com.codejit.common.dto.execution.RunResponse;
import com.codejit.common.dto.execution.SubmissionResponse;
import com.codejit.common.dto.execution.SubmissionStatus;
import com.codejit.common.dto.execution.TestResult;
import com.codejit.execution.controller.ExecutionController;
import com.codejit.execution.service.SubmissionService;
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

@WebMvcTest(ExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubmissionService submissionService;

    @Test
    @DisplayName("POST /run should evaluate visible tests")
    void testRunEndpoint() throws Exception {
        CodeRequest request = CodeRequest.builder()
                .sourceCode("public class Main { public static void main(String[] args) {} }")
                .language("java")
                .build();

        RunResponse response = RunResponse.builder()
                .results(List.of(
                        TestResult.builder()
                                .sequence(0)
                                .passed(true)
                                .actualOutput("test")
                                .expectedOutput("test")
                                .runtimeMillis(50)
                                .build()
                ))
                .totalRuntimeMillis(50)
                .success(true)
                .build();

        when(submissionService.runCode(eq(1L), eq(2L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/assessments/1/questions/2/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results[0].passed").value(true));
    }

    @Test
    @DisplayName("POST /submit should process code submission")
    void testSubmitEndpoint() throws Exception {
        CodeRequest request = CodeRequest.builder()
                .sourceCode("public class Main { public static void main(String[] args) {} }")
                .language("java")
                .build();

        SubmissionResponse response = SubmissionResponse.builder()
                .id(100L)
                .assessmentId(1L)
                .questionId(2L)
                .status(SubmissionStatus.PASSED)
                .passedTests(1)
                .totalTests(1)
                .submittedAt("2026-08-31T12:00:00")
                .build();

        when(submissionService.submitCode(eq(1L), eq(2L), any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/assessments/1/questions/2/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PASSED"));
    }

    @Test
    @DisplayName("GET /submissions/{id} should retrieve submission")
    void testGetSubmission() throws Exception {
        SubmissionResponse response = SubmissionResponse.builder()
                .id(100L)
                .assessmentId(1L)
                .questionId(2L)
                .status(SubmissionStatus.PASSED)
                .passedTests(1)
                .totalTests(1)
                .build();

        when(submissionService.getSubmission(eq(100L))).thenReturn(response);

        mockMvc.perform(get("/api/v1/submissions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }
}

