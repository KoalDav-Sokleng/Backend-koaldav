package com.example.project.mapper;

import com.example.project.Entity.Wallet;
import com.example.project.Enum.WalletType;
import com.example.project.dto.request.WalletRequest;
import com.example.project.dto.response.WalletResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public Wallet toEntity(WalletRequest request) {
        if (request == null)
            return null;

        WalletType type = request.getType() != null ? request.getType() : WalletType.PERSONAL;
        Double balance = request.getInitialBalance() != null ? request.getInitialBalance() : 0.0;
        String currency = request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency()
                : "USD";
        String icon = request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "💳";
        String color = request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#6C63FF";
        Boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());

        return Wallet.builder()
                .name(request.getName().trim())
                .type(type)
                .balance(balance)
                .currency(currency)
                .icon(icon)
                .color(color)
                .isDefault(isDefault)
                .build();
    }

    public WalletResponse toResponse(Wallet wallet) {
        if (wallet == null)
            return null;

        return WalletResponse.builder()
                .id(wallet.getId())
                .name(wallet.getName())
                .type(wallet.getType())
                .balance(wallet.getBalance() != null ? Math.round(wallet.getBalance() * 100.0) / 100.0 : 0.0)
                .currency(wallet.getCurrency())
                .icon(wallet.getIcon())
                .color(wallet.getColor())
                .isDefault(wallet.getIsDefault())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
