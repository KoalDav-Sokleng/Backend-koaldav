//package com.example.project.controller;
//
//import com.example.project.Enum.MilestoneStatus;
//import com.example.project.dto.exception.GlobalExceptionHandler;
//import com.example.project.dto.exception.ResourceNotFoundException;
//import com.example.project.dto.request.MilestoneRequest;
//import com.example.project.dto.response.MilestoneResponse;
//import com.example.project.service.MilestoneService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(MilestoneController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(GlobalExceptionHandler.class)
//class MilestoneControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private MilestoneService milestoneService;
//
//
//    @Test
//    void create_WithValidRequest_ShouldReturn200() throws Exception {
//        MilestoneRequest request = new MilestoneRequest("Finish Chapter 1");
//        MilestoneResponse response = new MilestoneResponse(1L, "Finish Chapter 1", MilestoneStatus.IN_PROGRESS);
//
//        when(milestoneService.create(eq(1L), any(MilestoneRequest.class))).thenReturn(response);
//
//        mockMvc.perform(post("/api/goals/1/milestones")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.title").value("Finish Chapter 1"));
//    }
//
//    @Test
//    void complete_WhenFocusTimeLessThan60_ShouldReturn400BadRequest() throws Exception {
//        when(milestoneService.completeMilestone(1L, 10L))
//                .thenThrow(new IllegalStateException("Need at least 1 hour focus time"));
//
//        mockMvc.perform(patch("/api/goals/1/milestones/10/complete"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.message").value("Need at least 1 hour focus time"));
//    }
//
//    @Test
//    void complete_WhenMilestoneNotFound_ShouldReturn404NotFound() throws Exception {
//        when(milestoneService.completeMilestone(1L, 99L))
//                .thenThrow(new ResourceNotFoundException("Milestone", "id", 99L));
//
//        mockMvc.perform(patch("/api/goals/1/milestones/99/complete"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").value("Milestone not found with id : '99'"));
//    }
//
//    @Test
//    void complete_WhenValid_ShouldReturn200() throws Exception {
//        MilestoneResponse response = new MilestoneResponse(10L, "Finish Chapter 1", MilestoneStatus.COMPLETED);
//        when(milestoneService.completeMilestone(1L, 10L)).thenReturn(response);
//
//        mockMvc.perform(patch("/api/goals/1/milestones/10/complete"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("COMPLETED"));
//    }
//
//    @Test
//    void getMilestones_ShouldReturn200() throws Exception {
//        MilestoneResponse response = new MilestoneResponse(10L, "Finish Chapter 1", MilestoneStatus.IN_PROGRESS);
//        when(milestoneService.getMilestonesByGoalId(1L)).thenReturn(java.util.List.of(response));
//
//        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/goals/1/milestones"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(10L));
//    }
//
//    @Test
//    void getMilestone_ShouldReturn200() throws Exception {
//        MilestoneResponse response = new MilestoneResponse(10L, "Finish Chapter 1", MilestoneStatus.IN_PROGRESS);
//        when(milestoneService.getMilestone(1L, 10L)).thenReturn(response);
//
//        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/goals/1/milestones/10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(10L));
//    }
//}
//
