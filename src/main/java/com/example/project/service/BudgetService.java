package com.example.project.service;

import com.example.project.Entity.Budget;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.BudgetRequest;
import com.example.project.dto.response.BudgetResponse;
import com.example.project.mapper.BudgetMapper;
import com.example.project.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    public BudgetService(BudgetRepository budgetRepository, BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.budgetMapper = budgetMapper;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        return budgetRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(budgetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        return budgetMapper.toResponse(budget);
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        Budget entity = budgetMapper.toEntity(request);
        Budget saved = budgetRepository.save(entity);
        return budgetMapper.toResponse(saved);
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        budget.setName(request.getName().trim());
        budget.setCategory(request.getCategory().trim());
        budget.setLimitAmount(request.getLimitAmount());
        if (request.getPeriod() != null)
            budget.setPeriod(request.getPeriod());
        if (request.getStartDate() != null)
            budget.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)
            budget.setEndDate(request.getEndDate());
        if (request.getIcon() != null)
            budget.setIcon(request.getIcon());
        if (request.getColor() != null)
            budget.setColor(request.getColor());

        Budget updated = budgetRepository.save(budget);
        return budgetMapper.toResponse(updated);
    }

    @Transactional
    public boolean deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        budgetRepository.delete(budget);
        return true;
    }
}
