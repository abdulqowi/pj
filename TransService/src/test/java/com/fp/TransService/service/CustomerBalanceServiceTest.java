package com.fp.TransService.service;

import com.fp.TransService.dto.CustomerBalance;
import com.fp.TransService.exception.AlreadyExistsException;
import com.fp.TransService.repository.CustomerBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerBalanceServiceTest {

    @Mock
    private CustomerBalanceRepository customerBalanceRepository;

    @InjectMocks
    private CustomerBalanceService customerBalanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeductAmount_SufficientBalance() {
        CustomerBalance customerBalance = new CustomerBalance();
        customerBalance.setCustomerId(1);
        customerBalance.setAmount(100f);

        when(customerBalanceRepository.findByCustomerId(1)).thenReturn(Mono.just(customerBalance));
        when(customerBalanceRepository.save(any(CustomerBalance.class))).thenReturn(Mono.just(customerBalance));

        StepVerifier.create(customerBalanceService.deductAmount(1, 50f))
                .expectNextMatches(balance -> balance.getAmount() == 50f)
                .verifyComplete();
    }

    @Test
    void testDeductAmount_InsufficientBalance() {
        CustomerBalance customerBalance = new CustomerBalance();
        customerBalance.setCustomerId(1);
        customerBalance.setAmount(30f);

        when(customerBalanceRepository.findByCustomerId(1)).thenReturn(Mono.just(customerBalance));

        StepVerifier.create(customerBalanceService.deductAmount(1, 50f))
                .expectError(AlreadyExistsException.class)
                .verify();
    }

    @Test
    void testSaveCustomerBalance() {
        CustomerBalance customerBalance = new CustomerBalance();
        when(customerBalanceRepository.save(customerBalance)).thenReturn(Mono.just(customerBalance));

        StepVerifier.create(customerBalanceService.saveCustomerBalance(customerBalance))
                .expectNext(customerBalance)
                .verifyComplete();
    }

    @Test
    void testGetCustomerBalanceById() {
        CustomerBalance customerBalance = new CustomerBalance();
        when(customerBalanceRepository.findByCustomerId(1)).thenReturn(Mono.just(customerBalance));

        StepVerifier.create(customerBalanceService.getCustomerBalanceById(1))
                .expectNext(customerBalance)
                .verifyComplete();
    }

    @Test
    void testUpdateCustomerBalance_ExistingCustomer() {
        CustomerBalance existingBalance = new CustomerBalance();
        existingBalance.setCustomerId(1);
        existingBalance.setAmount(100f);

        when(customerBalanceRepository.findByCustomerId(1)).thenReturn(Mono.just(existingBalance));
        when(customerBalanceRepository.save(any(CustomerBalance.class))).thenReturn(Mono.just(existingBalance));

        StepVerifier.create(customerBalanceService.updateCustomerBalance(1, 50f))
                .expectNextMatches(balance -> balance.getAmount() == 150f)
                .verifyComplete();
    }

    @Test
    void testUpdateCustomerBalance_NonExistingCustomer() {
        when(customerBalanceRepository.findByCustomerId(1)).thenReturn(Mono.empty());

        StepVerifier.create(customerBalanceService.updateCustomerBalance(1, 50f))
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    void testGetAllCustomerBalances() {
        CustomerBalance balance1 = new CustomerBalance();
        CustomerBalance balance2 = new CustomerBalance();
        when(customerBalanceRepository.findAll()).thenReturn(Flux.just(balance1, balance2));

        StepVerifier.create(customerBalanceService.getAllCustomerBalances())
                .expectNext(balance1, balance2)
                .verifyComplete();
    }

    @Test
    void testDeleteCustomerBalance() {
        when(customerBalanceRepository.deleteById(1)).thenReturn(Mono.empty());

        StepVerifier.create(customerBalanceService.deleteCustomerBalance(1))
                .verifyComplete();

        verify(customerBalanceRepository, times(1)).deleteById(1);
    }
}