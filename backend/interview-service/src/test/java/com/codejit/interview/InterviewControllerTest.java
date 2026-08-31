package com.codejit.interview;

import com.codejit.common.dto.interview.InterviewRequest;
import com.codejit.common.dto.interview.InterviewResponse;
import com.codejit.common.dto.interview.InterviewStatus;
import com.codejit.interview.controller.InterviewController;
import com.codejit.interview.service.InterviewService;
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

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterviewService interviewService;

    @Test
    @DisplayName("GET /api/v1/interviews should return list of interviews")
    void testGetInterviews() throws Exception {
        InterviewResponse room = InterviewResponse.builder()
                .id(1L)
                .title("Front-end Architecture")
                .shareCode("FE1234")
                .status(InterviewStatus.SCHEDULED)
                .build();

        when(interviewService.getInterviews(any())).thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].shareCode").value("FE1234"));
    }

    @Test
    @DisplayName("POST /api/v1/interviews should create interview room")
    void testCreateInterview() throws Exception {
        InterviewRequest request = InterviewRequest.builder()
                .title("Distributed Systems")
                .build();

        InterviewResponse room = InterviewResponse.builder()
                .id(2L)
                .title("Distributed Systems")
                .shareCode("DS9999")
                .status(InterviewStatus.SCHEDULED)
                .build();

        when(interviewService.createInterview(any(), any(), any())).thenReturn(room);

        mockMvc.perform(post("/api/v1/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.shareCode").value("DS9999"));
    }

    @Test
    @DisplayName("POST /api/v1/interviews/join/{code} should join room")
    void testJoinInterview() throws Exception {
        InterviewResponse room = InterviewResponse.builder()
                .id(2L)
                .title("Distributed Systems")
                .shareCode("DS9999")
                .status(InterviewStatus.LIVE)
                .build();

        when(interviewService.joinInterview(eq("DS9999"), any(), any())).thenReturn(room);

        mockMvc.perform(post("/api/v1/interviews/join/DS9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVE"));
    }
}

