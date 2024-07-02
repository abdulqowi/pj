package com.fp.TransService.service;
import com.fp.TransService.dto.TransactionDetail;
import com.fp.TransService.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Service
public class TransactionDetailService {

    private final TransactionRepository transactionDetailRepository;

    @Autowired
    public TransactionDetailService(TransactionRepository transactionDetailRepository) {
        this.transactionDetailRepository = transactionDetailRepository;
    }

    public Mono<TransactionDetail> saveTransactionDetail(TransactionDetail transactionDetail) {
        return transactionDetailRepository.save(transactionDetail);
    }

    public Mono<TransactionDetail> getTransactionDetailById(Integer id) {
        return transactionDetailRepository.findById(id);
    }

    public Flux<TransactionDetail> getAllTransactionDetails() {
        return transactionDetailRepository.findAll();
    }

    public Mono<Void> deleteTransactionDetail(Integer id) {
        return transactionDetailRepository.deleteById(id);
    }
}

