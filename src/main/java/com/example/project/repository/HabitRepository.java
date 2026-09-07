package com.example.project.repository;

import com.example.project.Entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Was a skeleton plain class per the project structure doc — now a
 * proper Spring Data repository, matching GoalRepository/MilestoneRepository.
 */
@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
}