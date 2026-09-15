package com.example.project.service;

import com.example.project.Entity.Budget;
import com.example.project.Enum.BudgetPeriod;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.BudgetRequest;
import com.example.project.dto.response.BudgetResponse;
import com.example.project.mapper.BudgetMapper;
import com.example.project.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Spy
    private BudgetMapper budgetMapper = new BudgetMapper();

    @InjectMocks
    private BudgetService budgetService;

    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        sampleBudget = Budget.builder()
                .id(1L)
                .name("Food & Dining")
                .category("Food")
                .limitAmount(200.0)
                .spentAmount(50.0)
                .period(BudgetPeriod.MONTHLY)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .icon("🍔")
                .color("#6C63FF")
                .build();
    }

    @Test
    void createBudget_ShouldSaveAndReturnResponse() {
        BudgetRequest request = BudgetRequest.builder()
                .name("Food & Dining")
                .category("Food")
                .limitAmount(200.0)
                .period(BudgetPeriod.MONTHLY)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .icon("🍔")
                .color("#6C63FF")
                .build();

        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);

        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Food & Dining", response.getName());
        assertEquals(200.0, response.getLimitAmount());
        assertEquals(50.0, response.getSpentAmount());
        assertEquals(150.0, response.getRemainingAmount());
        assertEquals(25.0, response.getPercentageUsed());
        assertFalse(response.getIsOverbudget());
    }

    @Test
    void updateBudget_ShouldUpdateFieldsAndReturnResponse() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(sampleBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetRequest updateReq = BudgetRequest.builder()
                .name("Food & Drinks")
                .category("Food")
                .limitAmount(300.0)
                .period(BudgetPeriod.MONTHLY)
                .build();

        BudgetResponse response = budgetService.updateBudget(1L, updateReq);

        assertNotNull(response);
        assertEquals("Food & Drinks", response.getName());
        assertEquals(300.0, response.getLimitAmount());
    }

    @Test
    void deleteBudget_WhenFound_ShouldDelete() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(sampleBudget));

        boolean deleted = budgetService.deleteBudget(1L);

        assertTrue(deleted);
        verify(budgetRepository, times(1)).delete(sampleBudget);
    }
}
