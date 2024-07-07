package com.fp.TransService.service;
import com.fp.TransService.config.GlobalModelMapper;
import com.fp.TransService.dto.TransactionDetail;
import com.fp.TransService.repository.TransactionRepository;
import com.pja.common.dto.OrderSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Service
@Slf4j
public class TransactionDetailService {

    private final TransactionRepository transactionDetailRepository;
    private final CustomerBalanceService customerBalanceService;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();
    private final WebClient genericWebClient;

    @Autowired
    public TransactionDetailService(TransactionRepository transactionDetailRepository, CustomerBalanceService customerBalanceService, WebClient genericWebClient) {
        this.transactionDetailRepository = transactionDetailRepository;
        this.customerBalanceService = customerBalanceService;
        this.genericWebClient = genericWebClient;
    }
    public Mono<TransactionDetail> processTransaction(Integer transactionId, Integer amount) {
        return transactionDetailRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")))
                .flatMap(transactionDetail -> {
                    Integer customerId = transactionDetail.getCustomerId();
                    return customerBalanceService.deductAmount(customerId, Float.valueOf(amount))
                            .then(sendOrder(transactionDetail.getOrderId().intValue(), customerId, amount))
                            .then(Mono.just(transactionDetail))
                            .doOnSuccess(detail -> log.info("Transaction processed successfully: {}", detail))
                            .doOnError(error -> log.error("Failed to process transaction: {}", error.getMessage()));
                });
    }

    public Mono<OrderSendResponse> sendOrder(Integer orderId, Integer customerId, Integer amount) {
        log.info("Sending OrderSendResponse: {}", orderId);
        OrderSendResponse response = new OrderSendResponse();
        response.setOrderId(Long.valueOf(orderId));
        response.setCustomerId(customerId);
        response.setStatus("Success");
        response.setAmount(amount.floatValue());

        return this.genericWebClient.post()
                .uri("/processor")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(response), OrderSendResponse.class)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        log.info("Success :{}", orderId);
                        return Mono.just(response);
                    } else {
                        log.error("Error occurred while retrieving customer ID, status code: {}", clientResponse.statusCode());
                        return clientResponse.createException().flatMap(Mono::error);
                    }
                })
                .doOnError(e -> log.error("Error occurred while retrieving customer ID: {}", e.getMessage(), e));
    }


    public Mono<TransactionDetail> saveTransactionDetail(TransactionDetail transactionDetail) {
        return transactionDetailRepository.save(transactionDetail);
    }
    public Mono<TransactionDetail>update(TransactionDetail transactionDetail,Long orderid){
        return transactionDetailRepository.findByOrderId(orderid)
                .flatMap(transactionDetails->{
                    mapper.map(transactionDetail,transactionDetails);
                    return transactionDetailRepository.save(transactionDetails);
                });
    }

    public Mono<TransactionDetail> getTransactionDetailById(Long id) {
        return transactionDetailRepository.findByOrderId(id);
    }

    public Flux<TransactionDetail> getAllTransactionDetails() {
        return transactionDetailRepository.findAll();
    }

    public Mono<Void> deleteTransactionDetail(Integer id) {
        return transactionDetailRepository.deleteById(id);
    }
}

