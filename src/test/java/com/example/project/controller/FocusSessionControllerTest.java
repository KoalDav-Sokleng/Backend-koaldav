package com.example.project.controller;

import com.example.project.dto.exception.GlobalExceptionHandler;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.FocusSessionRequest;
import com.example.project.dto.response.FocusSessionResponse;
import com.example.project.service.FocusSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FocusSessioncontroller.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FocusSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FocusSessionService focusSessionService;


    @Test
    void create_WithValidRequest_ShouldReturn200() throws Exception {
        FocusSessionRequest request = new FocusSessionRequest(45);
        FocusSessionResponse response = new FocusSessionResponse(1L, 45, LocalDate.now());

        when(focusSessionService.create(eq(5L), any(FocusSessionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/milestones/5/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()));
    }

    @Test
    void create_WithInvalidDuration_ShouldReturn400BadRequest() throws Exception {
        FocusSessionRequest request = new FocusSessionRequest(0);

        mockMvc.perform(post("/api/milestones/5/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.durationMinutes").value("Duration must be at least 1 minute"));
    }

    @Test
    void create_WhenMilestoneNotFound_ShouldReturn404NotFound() throws Exception {
        FocusSessionRequest request = new FocusSessionRequest(30);

        when(focusSessionService.create(eq(99L), any(FocusSessionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Milestone", "id", 99L));

        mockMvc.perform(post("/api/milestones/99/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Milestone not found with id : '99'"));
    }

    @Test
    void getSessions_ShouldReturn200() throws Exception {
        FocusSessionResponse response = new FocusSessionResponse(1L, 45, LocalDate.now());
        when(focusSessionService.getSessionsByMilestoneId(5L)).thenReturn(java.util.List.of(response));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/milestones/5/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].durationMinutes").value(45));
    }
}

