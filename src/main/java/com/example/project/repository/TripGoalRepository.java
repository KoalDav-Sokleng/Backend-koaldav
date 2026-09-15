package com.example.project.repository;

import com.example.project.Entity.TripGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripGoalRepository extends JpaRepository<TripGoal, Long> {
}