package com.example.project.dto.request;

import com.example.project.Enum.WalletType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRequest {

    @NotBlank(message = "Wallet name is required")
    @Size(max = 100, message = "Wallet name cannot exceed 100 characters")
    private String name;

    private WalletType type;

    @PositiveOrZero(message = "Initial balance must be zero or positive")
    private Double initialBalance;

    private String currency;

    private String icon;

    private String color;

    private Boolean isDefault;
}
