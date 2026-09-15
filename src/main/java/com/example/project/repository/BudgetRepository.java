package com.example.project.repository;

import com.example.project.Entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByCategoryIgnoreCase(String category);

    List<Budget> findAllByOrderByCreatedAtDesc();
}
