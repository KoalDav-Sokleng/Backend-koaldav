package com.example.project.repository;

import com.example.project.Entity.Goal;
import com.example.project.Enum.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByStatusNotAndDeadlineBetween(GoalStatus status, LocalDate startDate, LocalDate endDate);
}