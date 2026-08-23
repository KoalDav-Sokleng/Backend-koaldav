//package com.example.project.service;
//
//import com.example.project.Entity.FocusSession;
//import com.example.project.Entity.Goal;
//import com.example.project.Entity.Milestone;
//import com.example.project.Enum.GoalStatus;
//import com.example.project.Enum.MilestoneStatus;
//import com.example.project.dto.exception.ResourceNotFoundException;
//import com.example.project.dto.request.MilestoneRequest;
//import com.example.project.dto.response.MilestoneResponse;
//import com.example.project.mapper.MilestoneMapper;
//import com.example.project.repository.GoalRepository;
//import com.example.project.repository.MilestoneRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MilestoneServiceTest {
//
//    @Mock
//    private MilestoneRepository milestoneRepository;
//
//    @Mock
//    private GoalRepository goalRepository;
//
//    @Spy
//    private MilestoneMapper mapper = new MilestoneMapper();
//
//    @InjectMocks
//    private MilestoneService milestoneService;
//
//    private Goal sampleGoal;
//    private Milestone sampleMilestone;
//
//    @BeforeEach
//    void setUp() {
//        sampleGoal = new Goal();
//        sampleGoal.setId(1L);
//        sampleGoal.setTitle("Goal 1");
//        sampleGoal.setStatus(GoalStatus.IN_PROGRESS);
//
//        sampleMilestone = new Milestone();
//        sampleMilestone.setId(10L);
//        sampleMilestone.setTitle("Milestone 1");
//        sampleMilestone.setStatus(MilestoneStatus.IN_PROGRESS);
//        sampleMilestone.setGoal(sampleGoal);
//        sampleMilestone.setFocusSessions(new ArrayList<>());
//
//        sampleGoal.setMilestone(new ArrayList<>(List.of(sampleMilestone)));
//    }
//
//    @Test
//    void create_WhenGoalExists_ShouldCreateMilestone() {
//        MilestoneRequest request = new MilestoneRequest("Milestone 1");
//        when(goalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));
//        when(milestoneRepository.save(any(Milestone.class))).thenReturn(sampleMilestone);
//
//        MilestoneResponse response = milestoneService.create(1L, request);
//
//        assertNotNull(response);
//        assertEquals("Milestone 1", response.getTitle());
//        assertEquals(MilestoneStatus.IN_PROGRESS, response.getStatus());
//        verify(milestoneRepository, times(1)).save(any(Milestone.class));
//    }
//
//    @Test
//    void create_WhenGoalNotFound_ShouldThrowResourceNotFoundException() {
//        MilestoneRequest request = new MilestoneRequest("Milestone 1");
//        when(goalRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> milestoneService.create(99L, request));
//    }
//
//    @Test
//    void completeMilestone_WhenMilestoneNotFound_ShouldThrowResourceNotFoundException() {
//        when(milestoneRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> milestoneService.completeMilestone(99L));
//    }
//
//    @Test
//    void completeMilestone_WhenFocusTimeLessThan60Minutes_ShouldThrowIllegalStateException() {
//        FocusSession session = new FocusSession(1L, 45, LocalDate.now(), sampleMilestone);
//        sampleMilestone.setFocusSessions(List.of(session));
//
//        when(milestoneRepository.findById(10L)).thenReturn(Optional.of(sampleMilestone));
//
//        IllegalStateException ex = assertThrows(
//                IllegalStateException.class,
//                () -> milestoneService.completeMilestone(10L)
//        );
//        assertEquals("Need at least 1 hour focus time", ex.getMessage());
//    }
//
//    @Test
//    void completeMilestone_WhenNoSessions_ShouldThrowIllegalStateException() {
//        sampleMilestone.setFocusSessions(new ArrayList<>());
//        when(milestoneRepository.findById(10L)).thenReturn(Optional.of(sampleMilestone));
//
//        IllegalStateException ex = assertThrows(
//                IllegalStateException.class,
//                () -> milestoneService.completeMilestone(10L)
//        );
//        assertEquals("Need at least 1 hour focus time", ex.getMessage());
//    }
//
//    @Test
//    void completeMilestone_WhenFocusTimeAtLeast60Minutes_ShouldCompleteAndCheckGoal() {
//        FocusSession session1 = new FocusSession(1L, 30, LocalDate.now(), sampleMilestone);
//        FocusSession session2 = new FocusSession(2L, 35, LocalDate.now(), sampleMilestone);
//        sampleMilestone.setFocusSessions(List.of(session1, session2));
//
//        when(milestoneRepository.findById(10L)).thenReturn(Optional.of(sampleMilestone));
//        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(goalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));
//
//        MilestoneResponse response = milestoneService.completeMilestone(10L);
//
//        assertNotNull(response);
//        assertEquals(MilestoneStatus.COMPLETED, response.getStatus());
//        assertEquals(GoalStatus.COMPLETED, sampleGoal.getStatus());
//        verify(goalRepository, times(1)).save(sampleGoal);
//    }
//
//    @Test
//    void getMilestonesByGoalId_WhenGoalExists_ShouldReturnMilestones() {
//        when(goalRepository.existsById(1L)).thenReturn(true);
//        when(milestoneRepository.findByGoalId(1L)).thenReturn(List.of(sampleMilestone));
//
//        List<MilestoneResponse> responses = milestoneService.getMilestonesByGoalId(1L);
//
//        assertEquals(1, responses.size());
//        assertEquals("Milestone 1", responses.get(0).getTitle());
//    }
//
//    @Test
//    void getMilestonesByGoalId_WhenGoalNotFound_ShouldThrowException() {
//        when(goalRepository.existsById(99L)).thenReturn(false);
//
//        assertThrows(ResourceNotFoundException.class, () -> milestoneService.getMilestonesByGoalId(99L));
//    }
//
//    @Test
//    void getMilestone_WhenFound_ShouldReturnMilestone() {
//        when(goalRepository.existsById(1L)).thenReturn(true);
//        when(milestoneRepository.findById(10L)).thenReturn(Optional.of(sampleMilestone));
//
//        MilestoneResponse response = milestoneService.getMilestone(1L, 10L);
//
//        assertNotNull(response);
//        assertEquals("Milestone 1", response.getTitle());
//    }
//
//    @Test
//    void getMilestone_WhenMilestoneDoesNotBelongToGoal_ShouldThrowException() {
//        Goal otherGoal = new Goal();
//        otherGoal.setId(2L);
//        sampleMilestone.setGoal(otherGoal);
//
//        when(goalRepository.existsById(1L)).thenReturn(true);
//        when(milestoneRepository.findById(10L)).thenReturn(Optional.of(sampleMilestone));
//
//        assertThrows(ResourceNotFoundException.class, () -> milestoneService.getMilestone(1L, 10L));
//    }
//}
//
