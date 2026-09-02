package com.example.project.Entity;

import com.example.project.Enum.SavingGoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saving_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String icon;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SavingGoalStatus status = SavingGoalStatus.ACTIVE;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "savingGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingDeposit> deposits = new ArrayList<>();
}