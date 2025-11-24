# Send test orders to Kafka backend
Write-Host "`nSending test orders..." -ForegroundColor Cyan

$orders = @(
    @{ productName = "Laptop"; price = 1200.99; quantity = 2 }
    @{ productName = "Mouse"; price = 25.50; quantity = 5 }
    @{ productName = "Keyboard"; price = 75.00; quantity = 3 }
    @{ productName = "Monitor"; price = 350.00; quantity = 1 }
    @{ productName = "USB Cable"; price = 12.99; quantity = 10 }
)

foreach ($order in $orders) {
    Write-Host "Sending: $($order.productName) - `$$($order.price) x $($order.quantity)" -ForegroundColor White
    
    $body = $order | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body
        Write-Host "  Order ID: $($response.orderId)" -ForegroundColor Green
    } catch {
        Write-Host "  Failed" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 1
}

Write-Host "`nProducer/Consumer Logs:" -ForegroundColor Cyan
docker logs kafka-backend 2>&1 | Select-String -Pattern "Order produced successfully|Received order|Order processed successfully|Running Average" | Select-Object -Last 15
