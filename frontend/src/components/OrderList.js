import React from 'react';
import './OrderList.css';

function OrderList({ orders }) {
  const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  return (
    <div className="order-list-container">
      <h2>📋 Recent Orders (Last 20)</h2>
      <div className="order-list">
        {orders.length === 0 ? (
          <div className="empty-state">
            <p>No orders yet. Create your first order!</p>
          </div>
        ) : (
          orders.map((order, index) => (
            <div key={index} className={`order-item ${order.status.toLowerCase()}`}>
              <div className="order-header">
                <span className="order-id">
                  {order.orderId ? order.orderId.substring(0, 8) + '...' : 'N/A'}
                </span>
                <span className={`order-status ${order.status.toLowerCase()}`}>
                  {order.status === 'SUCCESS' ? '✅' : '❌'} {order.status}
                </span>
              </div>
              <div className="order-details">
                <div><strong>Product:</strong> {order.productName}</div>
                {order.price && <div><strong>Price:</strong> ${order.price.toFixed(2)}</div>}
                {order.quantity && <div><strong>Quantity:</strong> {order.quantity}</div>}
                {order.runningAverage && (
                  <div><strong>Avg:</strong> ${order.runningAverage.toFixed(2)}</div>
                )}
                {order.reason && (
                  <div className="error-reason"><strong>Reason:</strong> {order.reason}</div>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default OrderList;
