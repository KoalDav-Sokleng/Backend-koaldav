package com.example.project.repository;

import com.example.project.Entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByIsDefaultTrue();

    List<Wallet> findAllByOrderByIdAsc();
}
