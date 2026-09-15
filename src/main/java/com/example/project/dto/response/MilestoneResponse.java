package com.example.project.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MilestoneResponse {
    private Long id;
    private String title;
    private String status;
    private Integer totalFocusMinutes;
    private List<FocusSessionResponse> focusSessions;
}
