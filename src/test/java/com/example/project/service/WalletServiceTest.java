package com.example.project.service;

import com.example.project.Entity.Wallet;
import com.example.project.Enum.WalletType;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.WalletDepositRequest;
import com.example.project.dto.request.WalletRequest;
import com.example.project.dto.response.WalletResponse;
import com.example.project.mapper.WalletMapper;
import com.example.project.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Spy
    private WalletMapper walletMapper = new WalletMapper();

    @InjectMocks
    private WalletService walletService;

    private Wallet sampleWallet;

    @BeforeEach
    void setUp() {
        sampleWallet = Wallet.builder()
                .id(1L)
                .name("ABA Bank")
                .type(WalletType.BANK)
                .balance(500.0)
                .currency("USD")
                .icon("🏦")
                .color("#004B87")
                .isDefault(true)
                .build();
    }

    @Test
    void createWallet_ShouldSaveAndReturnResponse() {
        WalletRequest request = WalletRequest.builder()
                .name("ABA Bank")
                .type(WalletType.BANK)
                .initialBalance(500.0)
                .currency("USD")
                .icon("🏦")
                .color("#004B87")
                .isDefault(true)
                .build();

        when(walletRepository.findByIsDefaultTrue()).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(sampleWallet);

        WalletResponse response = walletService.createWallet(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ABA Bank", response.getName());
        assertEquals(500.0, response.getBalance());
        assertEquals(WalletType.BANK, response.getType());
    }

    @Test
    void deposit_ShouldIncreaseBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sampleWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletDepositRequest depositRequest = WalletDepositRequest.builder()
                .amount(150.0)
                .note("Salary deposit")
                .build();

        WalletResponse response = walletService.deposit(1L, depositRequest);

        assertNotNull(response);
        assertEquals(650.0, response.getBalance());
    }

    @Test
    void getOrCreateDefaultWallet_WhenDefaultExists_ShouldReturnIt() {
        when(walletRepository.findByIsDefaultTrue()).thenReturn(Optional.of(sampleWallet));

        Wallet defaultWallet = walletService.getOrCreateDefaultWallet();

        assertNotNull(defaultWallet);
        assertEquals("ABA Bank", defaultWallet.getName());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void deleteWallet_WhenFound_ShouldDelete() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sampleWallet));

        boolean deleted = walletService.deleteWallet(1L);

        assertTrue(deleted);
        verify(walletRepository, times(1)).delete(sampleWallet);
    }

    @Test
    void deleteWallet_WhenNotFound_ShouldThrow() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> walletService.deleteWallet(99L));
    }
}
