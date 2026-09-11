package com.example.project.service;

import com.example.project.Entity.Habit;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.HabitRequest;
import com.example.project.dto.response.HabitResponse;
import com.example.project.mapper.HabitMapper;
import com.example.project.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Spy
    private HabitMapper habitMapper = new HabitMapper();

    @Mock
    private GardenService gardenService;

    @InjectMocks
    private HabitService habitService;

    private Habit sampleHabit;

    @BeforeEach
    void setUp() {
        sampleHabit = new Habit();
        sampleHabit.setId(1L);
        sampleHabit.setTitle("Drink Water");
        sampleHabit.setTarget("2L daily");
        sampleHabit.setType("water");
        sampleHabit.setStreak(0);
        sampleHabit.setStartDate(LocalDate.now());
    }

    @Test
    void getHabits_ShouldReturnList() {
        when(habitRepository.findAll()).thenReturn(List.of(sampleHabit));

        List<HabitResponse> responses = habitService.getHabits();

        assertEquals(1, responses.size());
        assertEquals("Drink Water", responses.get(0).getTitle());
    }

    @Test
    void createHabit_ShouldSaveAndReturnResponse() {
        HabitRequest request = new HabitRequest("Drink Water", "2L daily", "water");
        when(habitRepository.existsByTypeIgnoreCase("water")).thenReturn(false);
        when(habitRepository.save(any(Habit.class))).thenReturn(sampleHabit);

        HabitResponse response = habitService.createHabit(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Drink Water", response.getTitle());
    }

    @Test
    void createHabit_WhenTypeAlreadyExists_ShouldRejectDuplicate() {
        HabitRequest request = new HabitRequest("Drink More Water", "3L daily", "water");
        when(habitRepository.existsByTypeIgnoreCase("water")).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> habitService.createHabit(request));

        assertEquals("A habit with type 'water' already exists", exception.getMessage());
        verify(habitRepository, never()).save(any(Habit.class));
    }

    @Test
    void deleteHabit_WhenExists_ShouldDeleteAndSyncGarden() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));
        when(habitRepository.findAll()).thenReturn(Collections.emptyList());

        boolean result = habitService.deleteHabit(1L);

        assertTrue(result);
        verify(habitRepository, times(1)).delete(sampleHabit);
        verify(habitRepository, times(1)).flush();
    }

    @Test
    void deleteHabit_WhenNotFound_ShouldReturnFalse() {
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = habitService.deleteHabit(999L);

        assertFalse(result);
        verify(habitRepository, never()).delete(any());
    }
}
