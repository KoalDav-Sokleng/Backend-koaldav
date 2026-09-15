package com.example.project.controller;

import com.example.project.dto.request.WalletDepositRequest;
import com.example.project.dto.request.WalletRequest;
import com.example.project.dto.response.WalletResponse;
import com.example.project.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public List<WalletResponse> getAllWallets() {
        return walletService.getAllWallets();
    }

    @GetMapping("/{id}")
    public WalletResponse getWalletById(@PathVariable Long id) {
        return walletService.getWalletById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@Valid @RequestBody WalletRequest request) {
        return walletService.createWallet(request);
    }

    @PostMapping("/{id}/deposit")
    public WalletResponse deposit(@PathVariable Long id, @Valid @RequestBody WalletDepositRequest request) {
        return walletService.deposit(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteWallet(@PathVariable Long id) {
        boolean deleted = walletService.deleteWallet(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("id", id);
        response.put("message", "Wallet deleted successfully");
        return ResponseEntity.ok(response);
    }
}
