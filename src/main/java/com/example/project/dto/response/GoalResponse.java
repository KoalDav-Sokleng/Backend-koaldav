package com.example.project.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GoalResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate deadline;
    private String status;
    private List<MilestoneResponse> milestones;
}
