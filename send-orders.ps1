# Kafka Order Platform - Automated Order Sender
# This script sends sample orders and displays producer/consumer logs

Write-Host "`n=== Kafka Order Platform - Running ===" -ForegroundColor Cyan
Write-Host "Backend URL: http://localhost:8080" -ForegroundColor Yellow
Write-Host "Sending sample orders...`n" -ForegroundColor Green

# Sample orders data
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
        Write-Host "  ✓ Order ID: $($response.orderId)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Failed to send order" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 1
}

Write-Host "`n=== Producer/Consumer Logs ===" -ForegroundColor Cyan
Write-Host "Fetching latest activity from backend...`n" -ForegroundColor Yellow

# Show logs with producer and consumer activity
docker logs kafka-backend 2>&1 | Select-String -Pattern "Order produced successfully|Received order|Order processed successfully|Running Average" | Select-Object -Last 20

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Orders sent successfully!" -ForegroundColor Green
Write-Host "Use docker logs -f kafka-backend to watch live logs" -ForegroundColor Yellow
