package com.bigdata.service;

import com.bigdata.avro.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducerService {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    @Value("${student.reg-no}")
    private String studentRegNo;

    public CompletableFuture<String> sendOrder(String productName, Double price, Integer quantity) {
        String orderId = UUID.randomUUID().toString();
        
        Order order = Order.newBuilder()
                .setOrderId(orderId)
                .setProductName(productName)
                .setPrice(price)
                .setQuantity(quantity)
                .setTimestamp(System.currentTimeMillis())
                .setStudentRegNo(studentRegNo)
                .setStatus("PENDING")
                .build();

        CompletableFuture<SendResult<String, Order>> future = kafkaTemplate.send(ordersTopic, orderId, order);
        
        return future.handle((result, ex) -> {
            if (ex == null) {
                log.info("Order produced successfully: {} - Topic: {}, Partition: {}, Offset: {}",
                        orderId, result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                return orderId;
            } else {
                log.error("Failed to produce order: {}", orderId, ex);
                throw new RuntimeException("Failed to send order", ex);
            }
        });
    }

}
