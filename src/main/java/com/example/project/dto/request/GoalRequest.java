package com.example.project.dto.request;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class GoalRequest {
    private String title;
    private String description;
    private LocalDate deadline;
}
