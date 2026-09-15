package com.example.project.service;

import com.example.project.Entity.Wallet;
import com.example.project.Enum.WalletType;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.WalletDepositRequest;
import com.example.project.dto.request.WalletRequest;
import com.example.project.dto.response.WalletResponse;
import com.example.project.mapper.WalletMapper;
import com.example.project.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    public WalletService(WalletRepository walletRepository, WalletMapper walletMapper) {
        this.walletRepository = walletRepository;
        this.walletMapper = walletMapper;
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getAllWallets() {
        return walletRepository.findAllByOrderByIdAsc().stream()
                .map(walletMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // Unset existing default wallet
            walletRepository.findByIsDefaultTrue().ifPresent(existing -> {
                existing.setIsDefault(false);
                walletRepository.save(existing);
            });
        }

        // If this is the first wallet created, make it default automatically
        if (walletRepository.count() == 0) {
            request.setIsDefault(true);
        }

        Wallet entity = walletMapper.toEntity(request);
        Wallet saved = walletRepository.save(entity);
        return walletMapper.toResponse(saved);
    }

    @Transactional
    public WalletResponse deposit(Long id, WalletDepositRequest request) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));

        double currentBalance = wallet.getBalance() != null ? wallet.getBalance() : 0.0;
        wallet.setBalance(currentBalance + request.getAmount());
        Wallet updated = walletRepository.save(wallet);
        return walletMapper.toResponse(updated);
    }

    @Transactional
    public Wallet getOrCreateDefaultWallet() {
        return walletRepository.findByIsDefaultTrue()
                .orElseGet(() -> {
                    List<Wallet> all = walletRepository.findAllByOrderByIdAsc();
                    if (!all.isEmpty()) {
                        Wallet first = all.get(0);
                        first.setIsDefault(true);
                        return walletRepository.save(first);
                    }
                    // Create an initial default wallet
                    Wallet defaultWallet = Wallet.builder()
                            .name("Personal Wallet")
                            .type(WalletType.PERSONAL)
                            .balance(0.0)
                            .currency("USD")
                            .icon("💳")
                            .color("#6C63FF")
                            .isDefault(true)
                            .build();
                    return walletRepository.save(defaultWallet);
                });
    }

    @Transactional
    public boolean deleteWallet(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        walletRepository.delete(wallet);
        return true;
    }
}
