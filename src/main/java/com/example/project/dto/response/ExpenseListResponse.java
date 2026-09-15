package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseListResponse {
    private List<ExpenseResponse> expenses;
    private Integer total;
    private Integer page;
    private Integer limit;
}
