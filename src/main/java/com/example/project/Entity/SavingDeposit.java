package com.example.project.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "saving_deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false)
    private LocalDate depositDate;

    private String source;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_goal_id", nullable = false)
    private SavingGoal savingGoal;
}