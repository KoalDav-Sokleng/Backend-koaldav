package com.example.project.repository;

import com.example.project.Entity.Garden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GardenRepository extends JpaRepository<Garden, Long> {
    Optional<Garden> findTopByOrderByIdAsc();
}
