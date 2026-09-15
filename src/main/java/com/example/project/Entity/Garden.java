package com.example.project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Global streak / flower-growth / freeze state. Just like Habit,
 * this has no userId for the same reason (no auth is enforced
 * anywhere in the app yet) — GardenService treats this as a single
 * row and creates it on first use if it doesn't exist.
 */
@Entity
@Table(name = "garden")
@Getter
@Setter
@NoArgsConstructor
public class Garden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer streak = 0;

    private Integer bestStreak = 0;

    private Integer growthStage = 0; // 0-7, drives the sunflower visual

    private Integer freezes = 2; // starter freezes

    private LocalDate lastPerfectDate; // last date ALL habits were completed

    // Last date the decay/settle check was run — prevents applying
    // the "yesterday was missed" wilt more than once per calendar day.
    private LocalDate lastCheckedDate;

    @ElementCollection
    @CollectionTable(name = "garden_freeze_dates", joinColumns = @JoinColumn(name = "garden_id"))
    @Column(name = "freeze_date")
    private List<LocalDate> freezeUsedDates = new ArrayList<>();
}
