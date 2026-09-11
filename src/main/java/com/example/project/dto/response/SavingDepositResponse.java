package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class SavingDepositResponse {
    private Long id;
    private String title;
    private LocalDate date;
    private String source;
    private BigDecimal amount;
    private String notes;
}