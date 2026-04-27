package com.grishma.payguard.account.service;

import com.grishma.payguard.account.model.Account;
import com.grishma.payguard.account.repository.AccountRepository;
import com.grishma.payguard.common.dto.AccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldCreateAccount() {
        AccountRequest request = new AccountRequest("Grishma Kamath", "grishma@icici.com", "9876543210", "SAVINGS", 50000.0);
        Account saved = new Account("Grishma Kamath", "grishma@icici.com", "9876543210", "SAVINGS", 50000.0);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account result = accountService.createAccount(request);

        assertNotNull(result);
        assertEquals("Grishma Kamath", result.getCustomerName());
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals(50000.0, result.getBalance());
        assertEquals("ACTIVE", result.getStatus());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldGetAccountFromCache() {
        Account cached = new Account("Test User", "test@icici.com", "1234567890", "CURRENT", 10000.0);
        when(valueOperations.get("account:ACC1")).thenReturn(cached);

        Optional<Account> result = accountService.getById("ACC1");

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getCustomerName());
        verify(accountRepository, never()).findById(anyString());
    }

    @Test
    void shouldFallbackToDbOnCacheMiss() {
        Account dbAccount = new Account("DB User", "db@icici.com", "0000000000", "SAVINGS", 25000.0);
        when(valueOperations.get("account:ACC2")).thenReturn(null);
        when(accountRepository.findById("ACC2")).thenReturn(Optional.of(dbAccount));

        Optional<Account> result = accountService.getById("ACC2");

        assertTrue(result.isPresent());
        assertEquals("DB User", result.get().getCustomerName());
    }

    @Test
    void shouldGetAllAccounts() {
        List<Account> accounts = List.of(
                new Account("User1", "u1@icici.com", "111", "SAVINGS", 10000.0),
                new Account("User2", "u2@icici.com", "222", "CURRENT", 20000.0)
        );
        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> result = accountService.getAllAccounts();
        assertEquals(2, result.size());
    }

    @Test
    void shouldSearchByName() {
        Account match = new Account("Grishma Kamath", "grishma@icici.com", "123", "SAVINGS", 50000.0);
        when(accountRepository.findByCustomerNameContainingIgnoreCase("grishma")).thenReturn(List.of(match));

        List<Account> result = accountService.searchByName("grishma");
        assertEquals(1, result.size());
        assertEquals("Grishma Kamath", result.get(0).getCustomerName());
    }

    @Test
    void shouldCreditAccount() {
        Account account = new Account("User", "u@icici.com", "123", "SAVINGS", 10000.0);
        when(accountRepository.findById("ACC1")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.credit("ACC1", 5000.0);

        assertEquals(15000.0, result.getBalance());
    }

    @Test
    void shouldDebitAccount() {
        Account account = new Account("User", "u@icici.com", "123", "SAVINGS", 10000.0);
        when(accountRepository.findById("ACC1")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.debit("ACC1", 3000.0);

        assertEquals(7000.0, result.getBalance());
    }

    @Test
    void shouldThrowOnInsufficientBalance() {
        Account account = new Account("User", "u@icici.com", "123", "SAVINGS", 1000.0);
        when(accountRepository.findById("ACC1")).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class, () ->
                accountService.debit("ACC1", 5000.0));
    }

    @Test
    void shouldDeactivateAccount() {
        Account account = new Account("User", "u@icici.com", "123", "SAVINGS", 10000.0);
        when(accountRepository.findById("ACC1")).thenReturn(Optional.of(account));

        accountService.deactivateAccount("ACC1");

        assertEquals("INACTIVE", account.getStatus());
        verify(accountRepository).save(account);
        verify(redisTemplate).delete("account:ACC1");
    }

    @Test
    void shouldThrowForNonExistentAccountOnUpdate() {
        AccountRequest request = new AccountRequest("New Name", "new@icici.com", "999", "SAVINGS", 0);
        when(accountRepository.findById("ACC999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                accountService.updateAccount("ACC999", request));
    }
}
