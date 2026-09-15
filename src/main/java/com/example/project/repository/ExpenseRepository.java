package com.example.project.repository;

import com.example.project.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByDateDescIdDesc();

    @Query("SELECT e FROM Expense e WHERE EXTRACT(YEAR FROM e.date) = :year ORDER BY e.date DESC, e.id DESC")
    List<Expense> findByYear(@Param("year") int year);
}
