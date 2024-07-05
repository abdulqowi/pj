package com.fp.orchestrator.service.Listener;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic ProductDeductTopic() {
        return TopicBuilder.name("Product-deduct-event")
//                .partitions(10)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic ProductListenTopic() {
        return TopicBuilder.name("Product-listen-event")
//                .partitions(10)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic OrderListenTopic() {
        return TopicBuilder.name("Order-listen-event")
//                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic OrderSendTopic() {
        return TopicBuilder.name("Order-Send-event")
//                .partitions(10)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic SendProductTopic() {
        return TopicBuilder.name("Send-Product-Event")
//                .partitions(10)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic TransTopic() {
        return TopicBuilder.name("Transaction-event")
//                .partitions(10)
                .replicas(1)
                .build();
    }
}
