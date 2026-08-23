//package com.example.project.service;
//
//import com.example.project.Entity.FocusSession;
//import com.example.project.Entity.Milestone;
//import com.example.project.dto.exception.ResourceNotFoundException;
//import com.example.project.dto.request.FocusSessionRequest;
//import com.example.project.dto.response.FocusSessionResponse;
//import com.example.project.mapper.FocusSessionMapper;
//import com.example.project.repository.FocusSessionRepository;
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
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class FocusSessionServiceTest {
//
//    @Mock
//    private FocusSessionRepository repository;
//
//    @Mock
//    private MilestoneRepository milestoneRepository;
//
//    @Spy
//    private FocusSessionMapper mapper = new FocusSessionMapper();
//
//    @InjectMocks
//    private FocusSessionService focusSessionService;
//
//    private Milestone sampleMilestone;
//
//    @BeforeEach
//    void setUp() {
//        sampleMilestone = new Milestone();
//        sampleMilestone.setId(1L);
//        sampleMilestone.setTitle("Test Milestone");
//    }
//
//    @Test
//    void create_WhenMilestoneExists_ShouldSaveSessionWithCurrentDate() {
//        FocusSessionRequest request = new FocusSessionRequest(45);
//        FocusSession savedSession = new FocusSession(100L, 45, LocalDate.now(), sampleMilestone);
//
//        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(sampleMilestone));
//        when(repository.save(any(FocusSession.class))).thenReturn(savedSession);
//
//        FocusSessionResponse response = focusSessionService.create(1L, request);
//
//        assertNotNull(response);
//        assertEquals(100L, response.getId());
//        assertEquals(45, response.getDurationMinutes());
//        assertEquals(LocalDate.now(), response.getDate());
//        verify(repository, times(1)).save(any(FocusSession.class));
//    }
//
//    @Test
//    void create_WhenMilestoneNotFound_ShouldThrowResourceNotFoundException() {
//        FocusSessionRequest request = new FocusSessionRequest(45);
//        when(milestoneRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(
//                ResourceNotFoundException.class,
//                () -> focusSessionService.create(99L, request)
//        );
//        verify(repository, never()).save(any(FocusSession.class));
//    }
//
//    @Test
//    void getSessionsByMilestoneId_WhenMilestoneExists_ShouldReturnSessions() {
//        FocusSession session = new FocusSession(100L, 45, LocalDate.now(), sampleMilestone);
//        when(milestoneRepository.existsById(1L)).thenReturn(true);
//        when(repository.findByMilestoneId(1L)).thenReturn(java.util.List.of(session));
//
//        java.util.List<FocusSessionResponse> responses = focusSessionService.getSessionsByMilestoneId(1L);
//
//        assertEquals(1, responses.size());
//        assertEquals(45, responses.get(0).getDurationMinutes());
//    }
//
//    @Test
//    void getSessionsByMilestoneId_WhenMilestoneNotFound_ShouldThrowException() {
//        when(milestoneRepository.existsById(99L)).thenReturn(false);
//
//        assertThrows(ResourceNotFoundException.class, () -> focusSessionService.getSessionsByMilestoneId(99L));
//    }
//}
//
