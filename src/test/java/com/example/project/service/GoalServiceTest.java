//package com.example.project.service;
//
//import com.example.project.Entity.Goal;
//import com.example.project.Enum.GoalStatus;
//import com.example.project.dto.exception.ResourceNotFoundException;
//import com.example.project.dto.request.GoalRequest;
//import com.example.project.dto.response.GoalResponse;
//import com.example.project.mapper.GoalMapper;
//import com.example.project.repository.GoalRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class GoalServiceTest {
//
//    @Mock
//    private GoalRepository repository;
//
//    @Spy
//    private GoalMapper mapper = new GoalMapper();
//
//    @InjectMocks
//    private GoalService goalService;
//
//    private Goal sampleGoal;
//
//    @BeforeEach
//    void setUp() {
//        sampleGoal = new Goal();
//        sampleGoal.setId(1L);
//        sampleGoal.setTitle("Learn Spring Boot");
//        sampleGoal.setDeadline(LocalDate.now().plusMonths(1));
//        sampleGoal.setStatus(GoalStatus.IN_PROGRESS);
//    }
//
//    @Test
//    void createGoal_ShouldReturnSavedGoalResponse() {
//        GoalRequest request = new GoalRequest("Learn Spring Boot", LocalDate.now().plusMonths(1));
//        when(repository.save(any(Goal.class))).thenReturn(sampleGoal);
//
//        GoalResponse response = goalService.createGoal(request);
//
//        assertNotNull(response);
//        assertEquals(1L, response.getId());
//        assertEquals("Learn Spring Boot", response.getTitle());
//        assertEquals(GoalStatus.IN_PROGRESS, response.getStatus());
//        verify(repository, times(1)).save(any(Goal.class));
//    }
//
//    @Test
//    void getGoal_WhenExists_ShouldReturnGoalResponse() {
//        when(repository.findById(1L)).thenReturn(Optional.of(sampleGoal));
//
//        GoalResponse response = goalService.getGoal(1L);
//
//        assertNotNull(response);
//        assertEquals(1L, response.getId());
//        assertEquals("Learn Spring Boot", response.getTitle());
//    }
//
//    @Test
//    void getGoal_WhenNotFound_ShouldThrowResourceNotFoundException() {
//        when(repository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> goalService.getGoal(99L));
//    }
//
//    @Test
//    void updateGoal_WhenExists_ShouldUpdateAndReturnGoalResponse() {
//        GoalRequest updateRequest = new GoalRequest("Updated Title", LocalDate.now().plusDays(60));
//        when(repository.findById(1L)).thenReturn(Optional.of(sampleGoal));
//        when(repository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        GoalResponse response = goalService.updateGoal(1L, updateRequest);
//
//        assertNotNull(response);
//        assertEquals("Updated Title", response.getTitle());
//        verify(repository, times(1)).save(sampleGoal);
//    }
//
//    @Test
//    void updateGoal_WhenNotFound_ShouldThrowResourceNotFoundException() {
//        GoalRequest updateRequest = new GoalRequest("Updated Title", LocalDate.now().plusDays(60));
//        when(repository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> goalService.updateGoal(99L, updateRequest));
//    }
//
//    @Test
//    void deleteGoal_WhenExists_ShouldDelete() {
//        when(repository.existsById(1L)).thenReturn(true);
//
//        goalService.deleteGoal(1L);
//
//        verify(repository, times(1)).deleteById(1L);
//    }
//
//    @Test
//    void deleteGoal_WhenNotFound_ShouldThrowResourceNotFoundException() {
//        when(repository.existsById(99L)).thenReturn(false);
//
//        assertThrows(ResourceNotFoundException.class, () -> goalService.deleteGoal(99L));
//    }
//}
//
