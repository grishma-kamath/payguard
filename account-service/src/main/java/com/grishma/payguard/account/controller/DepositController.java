package com.grishma.payguard.account.controller;

import com.grishma.payguard.account.model.Deposit;
import com.grishma.payguard.account.service.DepositService;
import com.grishma.payguard.common.dto.DepositRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping
    public ResponseEntity<Deposit> create(@RequestBody DepositRequest request) {
        return ResponseEntity.ok(depositService.createDeposit(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deposit> getById(@PathVariable String id) {
        return depositService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Deposit>> getByAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(depositService.getByAccountId(accountId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Deposit>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(depositService.getByType(type));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Deposit>> getActive() {
        return ResponseEntity.ok(depositService.getActiveDeposits());
    }

    @GetMapping("/maturing")
    public ResponseEntity<List<Deposit>> getMaturing(@RequestParam(defaultValue = "30") int daysAhead) {
        return ResponseEntity.ok(depositService.getMaturingDeposits(daysAhead));
    }

    @GetMapping("/total/{accountId}")
    public ResponseEntity<Map<String, Object>> getTotalDeposits(@PathVariable String accountId) {
        return ResponseEntity.ok(Map.of(
                "accountId", accountId,
                "totalDeposits", depositService.getTotalDeposits(accountId)
        ));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Deposit> close(@PathVariable String id) {
        return ResponseEntity.ok(depositService.closeDeposit(id));
    }
}
