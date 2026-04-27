package com.grishma.payguard.account.service;

import com.grishma.payguard.account.model.Account;
import com.grishma.payguard.account.model.Deposit;
import com.grishma.payguard.account.repository.DepositRepository;
import com.grishma.payguard.common.dto.DepositRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private DepositService depositService;

    @Test
    void shouldCreateDeposit() {
        Account account = new Account("User", "u@icici.com", "123", "SAVINGS", 100000.0);
        DepositRequest request = new DepositRequest("ACC1", 50000.0, "FIXED", 12, 7.5);

        when(accountService.getById("ACC1")).thenReturn(Optional.of(account));
        when(accountService.debit("ACC1", 50000.0)).thenReturn(account);
        when(depositRepository.save(any(Deposit.class))).thenAnswer(inv -> inv.getArgument(0));

        Deposit result = depositService.createDeposit(request);

        assertNotNull(result);
        assertEquals("ACC1", result.getAccountId());
        assertEquals(50000.0, result.getAmount());
        assertEquals("FIXED", result.getDepositType());
        assertEquals(12, result.getTenureMonths());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getMaturityAmount() > 50000.0);
    }

    @Test
    void shouldThrowForNonExistentAccount() {
        DepositRequest request = new DepositRequest("ACC999", 50000.0, "FIXED", 12, 7.5);
        when(accountService.getById("ACC999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                depositService.createDeposit(request));
    }

    @Test
    void shouldGetDepositsByAccount() {
        Deposit d1 = new Deposit("ACC1", 50000.0, "FIXED", 12, 7.5);
        Deposit d2 = new Deposit("ACC1", 25000.0, "RECURRING", 6, 6.0);
        when(depositRepository.findByAccountId("ACC1")).thenReturn(List.of(d1, d2));

        List<Deposit> result = depositService.getByAccountId("ACC1");

        assertEquals(2, result.size());
    }

    @Test
    void shouldGetActiveDeposits() {
        Deposit active = new Deposit("ACC1", 50000.0, "FIXED", 12, 7.5);
        when(depositRepository.findByStatus("ACTIVE")).thenReturn(List.of(active));

        List<Deposit> result = depositService.getActiveDeposits();

        assertEquals(1, result.size());
    }

    @Test
    void shouldGetTotalDeposits() {
        when(depositRepository.getTotalDepositsByAccountId("ACC1")).thenReturn(75000.0);

        Double total = depositService.getTotalDeposits("ACC1");

        assertEquals(75000.0, total);
    }

    @Test
    void shouldReturnZeroForNoDeposits() {
        when(depositRepository.getTotalDepositsByAccountId("ACC999")).thenReturn(null);

        Double total = depositService.getTotalDeposits("ACC999");

        assertEquals(0.0, total);
    }

    @Test
    void shouldCloseDeposit() {
        Deposit deposit = new Deposit("ACC1", 50000.0, "FIXED", 12, 7.5);
        Account account = new Account("User", "u@icici.com", "123", "SAVINGS", 50000.0);

        when(depositRepository.findById("DEP1")).thenReturn(Optional.of(deposit));
        when(accountService.credit(eq("ACC1"), anyDouble())).thenReturn(account);

        Deposit result = depositService.closeDeposit("DEP1");

        assertEquals("CLOSED", result.getStatus());
        verify(depositRepository).save(deposit);
        verify(accountService).credit(eq("ACC1"), eq(deposit.getMaturityAmount()));
    }

    @Test
    void shouldThrowWhenClosingNonExistentDeposit() {
        when(depositRepository.findById("DEP999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                depositService.closeDeposit("DEP999"));
    }

    @Test
    void shouldCalculateMaturityAmountCorrectly() {
        Deposit deposit = new Deposit("ACC1", 100000.0, "FIXED", 12, 10.0);
        assertTrue(deposit.getMaturityAmount() > 100000.0);
        assertEquals(110000.0, deposit.getMaturityAmount(), 100.0);
    }
}
