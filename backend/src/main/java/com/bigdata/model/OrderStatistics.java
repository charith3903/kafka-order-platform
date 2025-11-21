package com.bigdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatistics {
    private long totalOrders;
    private double runningAverage;
    private double totalRevenue;
    private long successfulOrders;
    private long failedOrders;
}
