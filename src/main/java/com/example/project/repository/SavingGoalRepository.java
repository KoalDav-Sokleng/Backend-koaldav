package com.example.project.repository;

import com.example.project.Entity.SavingGoal;
import com.example.project.Enum.SavingGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {
    List<SavingGoal> findByStatus(SavingGoalStatus status);

    List<SavingGoal> findByStatusAndDeadlineBefore(SavingGoalStatus status, LocalDate date);
}