package com.bigdata.service;

import com.bigdata.model.OrderStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class OrderAggregationService {

    private final AtomicLong totalOrders = new AtomicLong(0);
    private final AtomicReference<Double> totalPrice = new AtomicReference<>(0.0);
    private final AtomicReference<Double> totalRevenue = new AtomicReference<>(0.0);
    private final AtomicLong successfulOrders = new AtomicLong(0);
    private final AtomicLong failedOrders = new AtomicLong(0);

    public synchronized double updateRunningAverage(double price, int quantity) {
        totalOrders.incrementAndGet();
        successfulOrders.incrementAndGet();
        totalPrice.updateAndGet(current -> current + price);
        totalRevenue.updateAndGet(current -> current + (price * quantity));
        
        double average = totalPrice.get() / totalOrders.get();
        log.info("Running Average Updated: ${} | Total Orders: {}", 
                String.format("%.2f", average), totalOrders.get());
        return average;
    }

    public synchronized void recordFailure() {
        failedOrders.incrementAndGet();
    }

    public OrderStatistics getStatistics() {
        long total = totalOrders.get();
        double average = total > 0 ? totalPrice.get() / total : 0.0;
        
        return new OrderStatistics(
                total,
                average,
                totalRevenue.get(),
                successfulOrders.get(),
                failedOrders.get()
        );
    }

    public void reset() {
        totalOrders.set(0);
        totalPrice.set(0.0);
        totalRevenue.set(0.0);
        successfulOrders.set(0);
        failedOrders.set(0);
        log.info("Statistics reset");
    }

}
