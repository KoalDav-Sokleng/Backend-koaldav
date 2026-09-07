package com.example.project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * NOTE ON MULTI-TENANCY: like Goal/Milestone/SavingGoal, this entity
 * has no userId — the app currently has no enforced login (/api/**
 * is permitAll()), so all data is effectively single-tenant. If real
 * auth gets wired in later, add a userId column here the same way
 * it would need to be added across every other module, not just this one.
 */
@Entity
@Table(name = "habits")
@Getter
@Setter
@NoArgsConstructor
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String target;

    private String type; // "water" | "workout" | "read" | "study" | "other" — kept as String to match the frontend's lowercase ids directly

    private Integer streak = 0;

    // Derived "completed" state comes from comparing this to today —
    // there is no separate stored boolean, so it can never drift out of sync.
    private LocalDate lastCompletedDate;

    private LocalDate startDate;

    public boolean isCompletedOn(LocalDate date) {
        return lastCompletedDate != null && lastCompletedDate.equals(date);
    }
}