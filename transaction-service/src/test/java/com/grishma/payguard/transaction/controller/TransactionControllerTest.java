package com.grishma.payguard.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grishma.payguard.common.dto.TransactionRequest;
import com.grishma.payguard.transaction.model.Transaction;
import com.grishma.payguard.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSubmitTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest("TXN001", 5000.0, "DEBIT", "ACC123");
        Transaction txn = new Transaction("TXN001", 5000.0, "DEBIT", "ACC123");

        when(transactionService.submitTransaction(any(TransactionRequest.class))).thenReturn(txn);

        mockMvc.perform(post("/api/transactions/assess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.transactionId").value("TXN001"));
    }

    @Test
    void shouldGetAllTransactions() throws Exception {
        List<Transaction> txns = List.of(
                new Transaction("TXN001", 5000.0, "DEBIT", "ACC123"),
                new Transaction("TXN002", 15000.0, "CREDIT", "ACC456")
        );
        when(transactionService.getAllTransactions()).thenReturn(txns);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetTransactionById() throws Exception {
        Transaction txn = new Transaction("TXN001", 5000.0, "DEBIT", "ACC123");
        when(transactionService.getByTransactionId("TXN001")).thenReturn(Optional.of(txn));

        mockMvc.perform(get("/api/transactions/TXN001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN001"))
                .andExpect(jsonPath("$.amount").value(5000.0));
    }

    @Test
    void shouldReturn404ForMissingTransaction() throws Exception {
        when(transactionService.getByTransactionId("TXN999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/TXN999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetHighRiskTransactions() throws Exception {
        Transaction highRisk = new Transaction("TXN003", 60000.0, "DEBIT", "ACC789");
        when(transactionService.getHighRiskTransactions()).thenReturn(List.of(highRisk));

        mockMvc.perform(get("/api/transactions/high-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetBlockedTransactions() throws Exception {
        when(transactionService.getBlockedTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions/blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/transactions/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Transaction Service"));
    }

    @Test
    void shouldGetByAccountId() throws Exception {
        List<Transaction> txns = List.of(new Transaction("TXN001", 5000.0, "DEBIT", "ACC123"));
        when(transactionService.getByAccountId("ACC123")).thenReturn(txns);

        mockMvc.perform(get("/api/transactions/account/ACC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
