package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripGoalResponse {
    private Long id;
    private String name;
    private String description;
    private String type;
    private BigDecimal target;
    private BigDecimal saved;
    private LocalDate deadline;
    private String image;
    private LocalDateTime createdAt;
}
