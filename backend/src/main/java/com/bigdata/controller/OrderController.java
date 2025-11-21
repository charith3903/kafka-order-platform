package com.bigdata.controller;

import com.bigdata.model.OrderRequest;
import com.bigdata.model.OrderStatistics;
import com.bigdata.service.OrderAggregationService;
import com.bigdata.service.OrderProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderProducerService producerService;
    private final OrderAggregationService aggregationService;

    @PostMapping
    public CompletableFuture<ResponseEntity<Map<String, String>>> createOrder(
            @RequestBody OrderRequest request) {
        
        log.info("Creating order: Product={}, Price=${}, Quantity={}", 
                request.getProductName(), request.getPrice(), request.getQuantity());
        
        return producerService.sendOrder(
                request.getProductName(), 
                request.getPrice(), 
                request.getQuantity()
        ).thenApply(orderId -> {
            Map<String, String> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("message", "Order created successfully");
            response.put("status", "PENDING");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }).exceptionally(ex -> {
            log.error("Failed to create order", ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order");
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        });
    }

    @GetMapping("/statistics")
    public ResponseEntity<OrderStatistics> getStatistics() {
        OrderStatistics stats = aggregationService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/statistics/reset")
    public ResponseEntity<Map<String, String>> resetStatistics() {
        aggregationService.reset();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Statistics reset successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("studentRegNo", "EG/2020/3903");
        return ResponseEntity.ok(response);
    }

}
