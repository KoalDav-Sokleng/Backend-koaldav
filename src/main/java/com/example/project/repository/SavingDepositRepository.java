package com.example.project.repository;

import com.example.project.Entity.SavingDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingDepositRepository extends JpaRepository<SavingDeposit, Long> {
}