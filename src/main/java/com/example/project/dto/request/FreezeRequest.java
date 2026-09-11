package com.example.project.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FreezeRequest {
    private LocalDate date; // accepted for symmetry with the frontend; server clock is authoritative
}