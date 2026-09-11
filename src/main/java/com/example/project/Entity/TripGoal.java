package com.example.project.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trip_goals")
public class TripGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private String type = "trip";

    @Column(nullable = false)
    private BigDecimal target;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal saved = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(name = "image_url")
    private String image;

    @Column(name = "image_public_id")
    private String imagePublicId;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
