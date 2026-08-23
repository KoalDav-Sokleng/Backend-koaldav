//package com.example.project.controller;
//
//import com.example.project.Enum.GoalStatus;
//import com.example.project.dto.exception.GlobalExceptionHandler;
//import com.example.project.dto.exception.ResourceNotFoundException;
//import com.example.project.dto.request.GoalRequest;
//import com.example.project.dto.response.GoalResponse;
//import com.example.project.service.GoalService;
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
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(GoalController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(GlobalExceptionHandler.class)
//class GoalControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private GoalService goalService;
//
//
//    @Test
//    void createGoal_WithValidRequest_ShouldReturn200() throws Exception {
//        GoalRequest request = new GoalRequest("Master Spring Boot", LocalDate.now().plusDays(30));
//        GoalResponse response = new GoalResponse(1L, "Master Spring Boot", LocalDate.now().plusDays(30), GoalStatus.IN_PROGRESS);
//
//        when(goalService.createGoal(any(GoalRequest.class))).thenReturn(response);
//
//        mockMvc.perform(post("/api/goals")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.title").value("Master Spring Boot"))
//                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
//    }
//
//    @Test
//    void createGoal_WithBlankTitle_ShouldReturn400BadRequest() throws Exception {
//        GoalRequest request = new GoalRequest("", LocalDate.now().plusDays(30));
//
//        mockMvc.perform(post("/api/goals")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.errors.title").value("Title is required"));
//    }
//
//    @Test
//    void getGoal_WhenExists_ShouldReturn200() throws Exception {
//        GoalResponse response = new GoalResponse(1L, "Master Spring Boot", LocalDate.now().plusDays(30), GoalStatus.IN_PROGRESS);
//
//        when(goalService.getGoal(1L)).thenReturn(response);
//
//        mockMvc.perform(get("/api/goals/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.title").value("Master Spring Boot"));
//    }
//
//    @Test
//    void getGoal_WhenNotFound_ShouldReturn404NotFound() throws Exception {
//        when(goalService.getGoal(99L)).thenThrow(new ResourceNotFoundException("Goal", "id", 99L));
//
//        mockMvc.perform(get("/api/goals/99"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").value("Goal not found with id : '99'"));
//    }
//
//    @Test
//    void getAllGoals_ShouldReturn200() throws Exception {
//        GoalResponse response = new GoalResponse(1L, "Goal", LocalDate.now(), GoalStatus.IN_PROGRESS);
//        when(goalService.getAllGoals()).thenReturn(List.of(response));
//
//        mockMvc.perform(get("/api/goals"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].title").value("Goal"));
//    }
//
//    @Test
//    void updateGoal_WithValidRequest_ShouldReturn200() throws Exception {
//        GoalRequest request = new GoalRequest("Updated Goal", LocalDate.now().plusDays(45));
//        GoalResponse response = new GoalResponse(1L, "Updated Goal", LocalDate.now().plusDays(45), GoalStatus.IN_PROGRESS);
//
//        when(goalService.updateGoal(eq(1L), any(GoalRequest.class))).thenReturn(response);
//
//        mockMvc.perform(put("/api/goals/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.title").value("Updated Goal"));
//    }
//
//    @Test
//    void deleteGoal_WhenExists_ShouldReturn204NoContent() throws Exception {
//        mockMvc.perform(delete("/api/goals/1"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void getGoal_WithInvalidIdType_ShouldReturn400BadRequest() throws Exception {
//        mockMvc.perform(get("/api/goals/not-a-number"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.message").value("Parameter 'id' should be of type Long"));
//    }
//
//    @Test
//    void createGoal_WithMalformedJson_ShouldReturn400BadRequest() throws Exception {
//        mockMvc.perform(post("/api/goals")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"deadline\": \"invalid-date-format\"}"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid parameter format"));
//    }
//}
//
