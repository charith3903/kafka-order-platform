package com.bigdata.service;

import com.bigdata.avro.Order;
import com.bigdata.model.OrderStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumerService {

    private final OrderAggregationService aggregationService;
    private final KafkaTemplate<String, String> dlqKafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${kafka.topics.orders-dlq}")
    private String dlqTopic;

    @Value("${kafka.retry.max-attempts}")
    private int maxRetries;

    @Value("${kafka.retry.delay-seconds}")
    private int retryDelaySeconds;

    private final Map<String, Integer> retryCount = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.topics.orders}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrder(@Payload Order order, 
                            @Header(KafkaHeaders.RECEIVED_KEY) String key,
                            Acknowledgment acknowledgment) {
        
        String orderId = order.getOrderId().toString();
        log.info("Received order: {} - Product: {}, Price: ${}, Quantity: {}", 
                orderId, order.getProductName(), order.getPrice(), order.getQuantity());

        try {
            processOrder(order);
            
            double runningAverage = aggregationService.updateRunningAverage(
                    order.getPrice(), order.getQuantity());
            
            OrderStatistics stats = aggregationService.getStatistics();
            messagingTemplate.convertAndSend("/topic/statistics", stats);
            
            Map<String, Object> orderNotification = new HashMap<>();
            orderNotification.put("orderId", orderId);
            orderNotification.put("productName", order.getProductName().toString());
            orderNotification.put("price", order.getPrice());
            orderNotification.put("quantity", order.getQuantity());
            orderNotification.put("status", "SUCCESS");
            orderNotification.put("runningAverage", runningAverage);
            
            messagingTemplate.convertAndSend("/topic/orders", orderNotification);
            
            acknowledgment.acknowledge();
            retryCount.remove(orderId);
            
            log.info("Order processed successfully: {} | Running Average: ${}", 
                    orderId, String.format("%.2f", runningAverage));
            
        } catch (Exception e) {
            handleFailure(order, acknowledgment, e);
        }
    }

    private void processOrder(Order order) throws Exception {
        if (order.getPrice() < 0) {
            throw new IllegalArgumentException("Invalid price value");
        }
        
        int retries = retryCount.getOrDefault(order.getOrderId().toString(), 0);
        
        if (retries == 1) {
            throw new RuntimeException("Temporary failure - simulating retry");
        }
    }

    private void handleFailure(Order order, Acknowledgment acknowledgment, Exception e) {
        String orderId = order.getOrderId().toString();
        int currentRetry = retryCount.getOrDefault(orderId, 0);
        
        if (currentRetry < maxRetries) {
            retryCount.put(orderId, currentRetry + 1);
            log.warn("Order processing failed: {} | Retry {}/{} | Error: {}", 
                    orderId, currentRetry + 1, maxRetries, e.getMessage());
            
            try {
                Thread.sleep(retryDelaySeconds * 1000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            try {
                processOrder(order);
                
                double runningAverage = aggregationService.updateRunningAverage(
                        order.getPrice(), order.getQuantity());
                
                OrderStatistics stats = aggregationService.getStatistics();
                messagingTemplate.convertAndSend("/topic/statistics", stats);
                
                acknowledgment.acknowledge();
                retryCount.remove(orderId);
                
                log.info("Order processed successfully after retry: {}", orderId);
                
            } catch (Exception retryEx) {
                handleFailure(order, acknowledgment, retryEx);
            }
            
        } else {
            log.error("Max retries reached for order: {} | Sending to DLQ", orderId);
            
            aggregationService.recordFailure();
            
            sendToDLQ(order, e.getMessage());
            
            Map<String, Object> failureNotification = new HashMap<>();
            failureNotification.put("orderId", orderId);
            failureNotification.put("productName", order.getProductName().toString());
            failureNotification.put("status", "FAILED");
            failureNotification.put("reason", e.getMessage());
            
            messagingTemplate.convertAndSend("/topic/orders", failureNotification);
            
            acknowledgment.acknowledge();
            retryCount.remove(orderId);
        }
    }

    private void sendToDLQ(Order order, String errorReason) {
        try {
            String dlqMessage = String.format(
                    "{\"orderId\":\"%s\",\"productName\":\"%s\",\"price\":%.2f," +
                    "\"quantity\":%d,\"timestamp\":%d,\"errorReason\":\"%s\"," +
                    "\"failedTimestamp\":%d,\"studentRegNo\":\"%s\"}",
                    order.getOrderId(), order.getProductName(), order.getPrice(),
                    order.getQuantity(), order.getTimestamp(), errorReason,
                    System.currentTimeMillis(), order.getStudentRegNo()
            );
            
            dlqKafkaTemplate.send(dlqTopic, order.getOrderId().toString(), dlqMessage);
            log.info("Order sent to DLQ: {}", order.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to send order to DLQ: {}", order.getOrderId(), e);
        }
    }

}
