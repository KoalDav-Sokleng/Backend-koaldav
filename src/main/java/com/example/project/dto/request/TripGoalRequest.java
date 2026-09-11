package com.example.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripGoalRequest {
    private String name;
    private String description;
    private BigDecimal target;
    private LocalDate deadline;
    private String imageUrl;
    private MultipartFile file;
}