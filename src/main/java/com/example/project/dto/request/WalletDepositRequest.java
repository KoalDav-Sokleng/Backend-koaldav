package com.example.project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletDepositRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Deposit amount must be greater than 0")
    private Double amount;

    private String note;
}
