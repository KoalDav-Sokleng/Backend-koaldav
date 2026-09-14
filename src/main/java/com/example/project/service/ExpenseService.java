package com.example.project.service;

import com.example.project.Entity.Budget;
import com.example.project.Entity.Expense;
import com.example.project.Entity.Wallet;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WalletRepository walletRepository;
    private final BudgetRepository budgetRepository;
    private final WalletService walletService;
    private final ExpenseMapper expenseMapper;

    private static final Map<String, String> CATEGORY_ICONS = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_COLORS = new LinkedHashMap<>();

    static {
        CATEGORY_ICONS.put("Food", "🍔");
        CATEGORY_ICONS.put("Shopping", "🛍️");
        CATEGORY_ICONS.put("Transportation", "🚗");
        CATEGORY_ICONS.put("Entertainment", "🎬");
        CATEGORY_ICONS.put("Bills", "💡");
        CATEGORY_ICONS.put("Education", "📚");
        CATEGORY_ICONS.put("Health", "💊");
        CATEGORY_ICONS.put("Other", "📦");

        CATEGORY_COLORS.put("Food", "#6C63FF");
        CATEGORY_COLORS.put("Shopping", "#9C8FFF");
        CATEGORY_COLORS.put("Transportation", "#C4BEFF");
        CATEGORY_COLORS.put("Entertainment", "#DDD9FF");
        CATEGORY_COLORS.put("Bills", "#7C6FFF");
        CATEGORY_COLORS.put("Education", "#A89FFF");
        CATEGORY_COLORS.put("Health", "#B5AEFF");
        CATEGORY_COLORS.put("Other", "#EDE9FE");
    }

    public ExpenseService(
            ExpenseRepository expenseRepository,
            WalletRepository walletRepository,
            BudgetRepository budgetRepository,
            WalletService walletService,
            ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.walletRepository = walletRepository;
        this.budgetRepository = budgetRepository;
        this.walletService = walletService;
        this.expenseMapper = expenseMapper;
    }

    public static String getIconForCategory(String category) {
        if (category == null)
            return "💸";
        return CATEGORY_ICONS.getOrDefault(category, "💸");
    }

    public static String getColorForCategory(String category) {
        if (category == null)
            return "#6C63FF";
        return CATEGORY_COLORS.getOrDefault(category, "#6C63FF");
    }

    @Transactional(readOnly = true)
    public FinanceOverviewResponse getOverview(Integer year) {
        int targetYear = (year != null && year > 1900 && year < 2100) ? year : LocalDate.now().getYear();
        List<Expense> yearExpenses = expenseRepository.findByYear(targetYear);

        // Group expenses by month (1 to 12)
        Map<Integer, Double> monthlyTotals = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            monthlyTotals.put(m, 0.0);
        }

        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        double grandTotal = 0.0;

        for (Expense e : yearExpenses) {
            int monthValue = e.getDate().getMonthValue();
            double amt = e.getAmount() != null ? e.getAmount() : 0.0;
            monthlyTotals.put(monthValue, monthlyTotals.get(monthValue) + amt);

            String cat = e.getCategory() != null ? e.getCategory() : "Other";
            categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + amt);

            grandTotal += amt;
        }

        // Build 12-month array
        List<FinanceOverviewResponse.MonthlyExpenseDTO> monthlyList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthlyList.add(new FinanceOverviewResponse.MonthlyExpenseDTO(
                    monthName,
                    Math.round(monthlyTotals.get(m) * 100.0) / 100.0));
        }

        // Build category array (only categories with spending)
        List<FinanceOverviewResponse.CategoryExpenseDTO> categoryList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String catName = entry.getKey();
            double val = Math.round(entry.getValue() * 100.0) / 100.0;
            if (val > 0) {
                categoryList.add(new FinanceOverviewResponse.CategoryExpenseDTO(
                        catName,
                        val,
                        getColorForCategory(catName),
                        getIconForCategory(catName)));
            }
        }

        return FinanceOverviewResponse.builder()
                .monthlyData(monthlyList)
                .categoryData(categoryList)
                .totalAmount(Math.round(grandTotal * 100.0) / 100.0)
                .build();
    }

    @Transactional(readOnly = true)
    public ExpenseListResponse getExpenses(String category, Integer year, Integer month, Integer page, Integer limit) {
        String filterCategory = (category != null && !category.isBlank() && !category.equalsIgnoreCase("All"))
                ? category.trim().toLowerCase()
                : null;

        List<Expense> all = (year != null)
                ? expenseRepository.findByYear(year)
                : expenseRepository.findAllByOrderByDateDescIdDesc();

        List<Expense> filtered = all.stream()
                .filter(e -> filterCategory == null
                        || (e.getCategory() != null && e.getCategory().equalsIgnoreCase(filterCategory)))
                .filter(e -> month == null || (e.getDate() != null && e.getDate().getMonthValue() == month))
                .sorted((a, b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp != 0)
                        return cmp;
                    return Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0);
                })
                .collect(Collectors.toList());

        int total = filtered.size();
        int p = (page != null && page > 0) ? page : 1;
        int l = (limit != null && limit > 0) ? limit : 20;

        int fromIndex = Math.min((p - 1) * l, total);
        int toIndex = Math.min(fromIndex + l, total);

        List<ExpenseResponse> paged = filtered.subList(fromIndex, toIndex).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());

        return ExpenseListResponse.builder()
                .expenses(paged)
                .total(total)
                .page(p)
                .limit(l)
                .build();
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        // 1. Resolve Wallet
        Wallet wallet;
        if (request.getWalletId() != null) {
            wallet = walletRepository.findById(request.getWalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + request.getWalletId()));
        } else {
            wallet = walletService.getOrCreateDefaultWallet();
        }

        // 2. Validate wallet balance
        double currentWalletBalance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
        if (currentWalletBalance < request.getAmount()) {
            throw new InsufficientBalanceException(String.format(
                    "Insufficient balance in wallet '%s'. Current balance: $%.2f, expense amount: $%.2f",
                    wallet.getName(), currentWalletBalance, request.getAmount()));
        }

        // 3. Deduct from wallet balance
        wallet.setBalance(currentWalletBalance - request.getAmount());
        walletRepository.save(wallet);

        // 4. Resolve Budget (optional)
        Budget budget = null;
        if (request.getBudgetId() != null) {
            budget = budgetRepository.findById(request.getBudgetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + request.getBudgetId()));
            double currentSpent = budget.getSpentAmount() != null ? budget.getSpentAmount() : 0.0;
            budget.setSpentAmount(currentSpent + request.getAmount());
            budgetRepository.save(budget);
        }

        // 5. Save Expense
        String icon = getIconForCategory(request.getCategory());
        Expense entity = expenseMapper.toEntity(request, icon, wallet, budget);
        Expense saved = expenseRepository.save(entity);
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    public boolean deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));

        // 1. Refund to Wallet
        if (expense.getWallet() != null) {
            Wallet wallet = expense.getWallet();
            double balance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
            wallet.setBalance(balance + expense.getAmount());
            walletRepository.save(wallet);
        }

        // 2. Adjust Budget spent amount
        if (expense.getBudget() != null) {
            Budget budget = expense.getBudget();
            double spent = budget.getSpentAmount() != null ? budget.getSpentAmount() : 0.0;
            budget.setSpentAmount(Math.max(0.0, spent - expense.getAmount()));
            budgetRepository.save(budget);
        }

        expenseRepository.delete(expense);
        return true;
    }
}
