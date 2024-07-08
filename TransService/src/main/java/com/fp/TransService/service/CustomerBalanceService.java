package com.fp.TransService.service;

import com.fp.TransService.dto.CustomerBalance;
import com.fp.TransService.exception.AlreadyExistsException;
import com.fp.TransService.repository.CustomerBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
public class CustomerBalanceService {

    private final CustomerBalanceRepository customerBalanceRepository;

    @Autowired
    public CustomerBalanceService(CustomerBalanceRepository customerBalanceRepository) {
        this.customerBalanceRepository = customerBalanceRepository;
    }
    public Mono<CustomerBalance> deductAmount(Integer customerId, Float amount) {
        return getCustomerBalanceById(customerId)
                .flatMap(customerBalance -> {
                    if (customerBalance.getAmount() < amount) {
                        return Mono.error(new AlreadyExistsException("Insufficient balance"));
                    }
                    customerBalance.setAmount(customerBalance.getAmount() - amount);
                    return saveCustomerBalance(customerBalance);
                });
    }
    public Mono<CustomerBalance> saveCustomerBalance(CustomerBalance customerBalance) {
        return customerBalanceRepository.save(customerBalance);
    }

    public Mono<CustomerBalance> getCustomerBalanceById(Integer id) {
        return customerBalanceRepository.findByCustomerId(id);
    }

    public Mono<CustomerBalance> updateCustomerBalance(Integer customerId, Float newBalance) {
        return customerBalanceRepository.findByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("CustomerId not found")))
                .flatMap(existingBalance -> {
                    existingBalance.setAmount(existingBalance.getAmount()+newBalance);
                    return customerBalanceRepository.save(existingBalance);
                });
    }
    public Flux<CustomerBalance> getAllCustomerBalances() {
        return customerBalanceRepository.findAll();
    }

    public Mono<Void> deleteCustomerBalance(Integer id) {
        return customerBalanceRepository.deleteById(id);
    }
}
