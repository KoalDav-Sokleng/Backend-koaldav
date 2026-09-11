package com.example.project.service;

import com.example.project.Entity.Expense;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.ExpenseRequest;
import com.example.project.dto.response.ExpenseListResponse;
import com.example.project.dto.response.ExpenseResponse;
import com.example.project.dto.response.FinanceOverviewResponse;
import com.example.project.mapper.ExpenseMapper;
import com.example.project.repository.ExpenseRepository;
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
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Spy
    private ExpenseMapper expenseMapper = new ExpenseMapper();

    @InjectMocks
    private ExpenseService expenseService;

    private Expense sampleExpense;

    @BeforeEach
    void setUp() {
        sampleExpense = Expense.builder()
                .id(1L)
                .title("Grocery Shopping")
                .amount(45.50)
                .category("Food")
                .icon("🍔")
                .date(LocalDate.of(2026, 3, 15))
                .note("Weekly groceries")
                .build();
    }

    @Test
    void createExpense_ShouldMapIconAndSave() {
        ExpenseRequest request = ExpenseRequest.builder()
                .title("Grocery Shopping")
                .amount(45.50)
                .category("Food")
                .date(LocalDate.of(2026, 3, 15))
                .note("Weekly groceries")
                .build();

        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("🍔", response.getIcon());
        assertEquals("Food", response.getCategory());
        assertEquals(45.50, response.getAmount());
    }

    @Test
    void getOverview_ShouldReturn12MonthsAndCategoryData() {
        when(expenseRepository.findByYear(2026)).thenReturn(List.of(sampleExpense));

        FinanceOverviewResponse overview = expenseService.getOverview(2026);

        assertNotNull(overview);
        assertEquals(12, overview.getMonthlyData().size());
        assertEquals("Jan", overview.getMonthlyData().get(0).getMonth());
        assertEquals(0.0, overview.getMonthlyData().get(0).getAmount());
        assertEquals("Mar", overview.getMonthlyData().get(2).getMonth());
        assertEquals(45.50, overview.getMonthlyData().get(2).getAmount());

        assertEquals(1, overview.getCategoryData().size());
        assertEquals("Food", overview.getCategoryData().get(0).getName());
        assertEquals("#6C63FF", overview.getCategoryData().get(0).getColor());
        assertEquals("🍔", overview.getCategoryData().get(0).getIcon());
        assertEquals(45.50, overview.getTotalAmount());
    }

    @Test
    void getExpenses_ShouldReturnPaginatedList() {
        when(expenseRepository.findByYear(2026))
                .thenReturn(List.of(sampleExpense));

        ExpenseListResponse response = expenseService.getExpenses("Food", 2026, 3, 1, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getPage());
        assertEquals(20, response.getLimit());
        assertEquals(1, response.getExpenses().size());
        assertEquals("Grocery Shopping", response.getExpenses().get(0).getTitle());
    }

    @Test
    void deleteExpense_WhenExists_ShouldDelete() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));

        boolean result = expenseService.deleteExpense(1L);

        assertTrue(result);
        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    void deleteExpense_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(999L));
        verify(expenseRepository, never()).delete(any());
    }
}
