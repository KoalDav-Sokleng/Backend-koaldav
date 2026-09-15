package com.example.project.service;

import com.example.project.Entity.Budget;
import com.example.project.Entity.Expense;
import com.example.project.Entity.Wallet;
import com.example.project.Enum.BudgetPeriod;
import com.example.project.Enum.WalletType;
import com.example.project.dto.exception.InsufficientBalanceException;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.ExpenseRequest;
import com.example.project.dto.response.ExpenseListResponse;
import com.example.project.dto.response.ExpenseResponse;
import com.example.project.dto.response.FinanceOverviewResponse;
import com.example.project.mapper.ExpenseMapper;
import com.example.project.repository.BudgetRepository;
import com.example.project.repository.ExpenseRepository;
import com.example.project.repository.WalletRepository;
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
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private WalletService walletService;

    @Spy
    private ExpenseMapper expenseMapper = new ExpenseMapper();

    @InjectMocks
    private ExpenseService expenseService;

    private Expense sampleExpense;
    private Wallet sampleWallet;
    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        sampleWallet = Wallet.builder()
                .id(1L)
                .name("Personal Wallet")
                .type(WalletType.PERSONAL)
                .balance(200.0)
                .currency("USD")
                .isDefault(true)
                .build();

        sampleBudget = Budget.builder()
                .id(1L)
                .name("Food Budget")
                .category("Food")
                .limitAmount(300.0)
                .spentAmount(50.0)
                .period(BudgetPeriod.MONTHLY)
                .build();

        sampleExpense = Expense.builder()
                .id(1L)
                .title("Grocery Shopping")
                .amount(45.50)
                .category("Food")
                .icon("🍔")
                .date(LocalDate.of(2026, 3, 15))
                .note("Weekly groceries")
                .wallet(sampleWallet)
                .budget(sampleBudget)
                .build();
    }

    @Test
    void createExpense_WithWalletAndBudget_ShouldDeductBalanceAndIncrementBudget() {
        ExpenseRequest request = ExpenseRequest.builder()
                .title("Grocery Shopping")
                .amount(45.50)
                .category("Food")
                .date(LocalDate.of(2026, 3, 15))
                .note("Weekly groceries")
                .walletId(1L)
                .budgetId(1L)
                .build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(sampleWallet));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(sampleBudget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("🍔", response.getIcon());
        assertEquals("Food", response.getCategory());
        assertEquals(45.50, response.getAmount());
        assertEquals(1L, response.getWalletId());
        assertEquals("Personal Wallet", response.getWalletName());
        assertEquals(1L, response.getBudgetId());
        assertEquals("Food Budget", response.getBudgetName());

        assertEquals(154.50, sampleWallet.getBalance());
        assertEquals(95.50, sampleBudget.getSpentAmount());
        verify(walletRepository, times(1)).save(sampleWallet);
        verify(budgetRepository, times(1)).save(sampleBudget);
    }

    @Test
    void createExpense_WhenInsufficientBalance_ShouldThrowException() {
        sampleWallet.setBalance(20.0);
        ExpenseRequest request = ExpenseRequest.builder()
                .title("Expensive Dinner")
                .amount(100.0)
                .category("Food")
                .date(LocalDate.of(2026, 3, 15))
                .walletId(1L)
                .build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(sampleWallet));

        assertThrows(InsufficientBalanceException.class, () -> expenseService.createExpense(request));
        verify(expenseRepository, never()).save(any());
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
    void deleteExpense_WhenExists_ShouldRefundWalletAndAdjustBudget() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));

        boolean result = expenseService.deleteExpense(1L);

        assertTrue(result);
        assertEquals(245.50, sampleWallet.getBalance());
        assertEquals(4.50, sampleBudget.getSpentAmount());
        verify(walletRepository, times(1)).save(sampleWallet);
        verify(budgetRepository, times(1)).save(sampleBudget);
        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    void deleteExpense_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(999L));
        verify(expenseRepository, never()).delete(any());
    }
}
