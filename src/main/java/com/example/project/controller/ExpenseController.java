package com.example.project.controller;

import com.example.project.dto.request.ExpenseRequest;
import com.example.project.dto.response.ExpenseListResponse;
import com.example.project.dto.response.ExpenseResponse;
import com.example.project.dto.response.FinanceOverviewResponse;
import com.example.project.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/overview")
    public FinanceOverviewResponse getOverview(@RequestParam(required = false) Integer year) {
        return expenseService.getOverview(year);
    }

    @GetMapping("/expenses")
    public ExpenseListResponse getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        return expenseService.getExpenses(category, year, month, page, limit);
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.createExpense(request);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Map<String, Object>> deleteExpense(@PathVariable Long id) {
        boolean deleted = expenseService.deleteExpense(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("id", id);
        response.put("message", "Expense deleted successfully");
        return ResponseEntity.ok(response);
    }
}
