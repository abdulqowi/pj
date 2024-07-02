package com.fp.TransService.service;

import com.fp.TransService.dto.CustomerBalance;
import com.fp.TransService.repository.CustomerBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerBalanceService {

    private final CustomerBalanceRepository customerBalanceRepository;

    @Autowired
    public CustomerBalanceService(CustomerBalanceRepository customerBalanceRepository) {
        this.customerBalanceRepository = customerBalanceRepository;
    }

    public Mono<CustomerBalance> saveCustomerBalance(CustomerBalance customerBalance) {
        return customerBalanceRepository.save(customerBalance);
    }

    public Mono<CustomerBalance> getCustomerBalanceById(Integer id) {
        return customerBalanceRepository.findById(id);
    }

    public Flux<CustomerBalance> getAllCustomerBalances() {
        return customerBalanceRepository.findAll();
    }

    public Mono<Void> deleteCustomerBalance(Integer id) {
        return customerBalanceRepository.deleteById(id);
    }
}
