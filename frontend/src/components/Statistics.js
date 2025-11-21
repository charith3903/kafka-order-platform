import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import './Statistics.css';

function Statistics({ statistics, onReset }) {
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  };

  const successRate = statistics.totalOrders > 0 
    ? ((statistics.successfulOrders / statistics.totalOrders) * 100).toFixed(1)
    : 0;

  return (
    <div className="statistics-container">
      <div className="statistics-header">
        <h2>📊 Real-Time Statistics</h2>
        <button onClick={onReset} className="btn-reset">🔄 Reset</button>
      </div>

      <div className="stats-grid">
        <div className="stat-card primary">
          <div className="stat-icon">📦</div>
          <div className="stat-content">
            <div className="stat-value">{statistics.totalOrders}</div>
            <div className="stat-label">Total Orders</div>
          </div>
        </div>

        <div className="stat-card success">
          <div className="stat-icon">💰</div>
          <div className="stat-content">
            <div className="stat-value">{formatCurrency(statistics.runningAverage)}</div>
            <div className="stat-label">Running Average Price</div>
          </div>
        </div>

        <div className="stat-card info">
          <div className="stat-icon">💵</div>
          <div className="stat-content">
            <div className="stat-value">{formatCurrency(statistics.totalRevenue)}</div>
            <div className="stat-label">Total Revenue</div>
          </div>
        </div>

        <div className="stat-card success-rate">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <div className="stat-value">{statistics.successfulOrders}</div>
            <div className="stat-label">Successful Orders</div>
          </div>
        </div>

        <div className="stat-card failure">
          <div className="stat-icon">❌</div>
          <div className="stat-content">
            <div className="stat-value">{statistics.failedOrders}</div>
            <div className="stat-label">Failed Orders</div>
          </div>
        </div>

        <div className="stat-card percentage">
          <div className="stat-icon">📈</div>
          <div className="stat-content">
            <div className="stat-value">{successRate}%</div>
            <div className="stat-label">Success Rate</div>
          </div>
        </div>
      </div>

      <div className="features-info">
        <h3>✨ System Features</h3>
        <ul>
          <li>✅ Avro Serialization with Schema Registry</li>
          <li>✅ Real-time Running Average Calculation</li>
          <li>✅ Retry Logic (Max 3 attempts, 2s delay)</li>
          <li>✅ Dead Letter Queue (DLQ) for failed messages</li>
          <li>✅ WebSocket for real-time updates</li>
          <li>✅ Spring Boot + Kafka + React integration</li>
        </ul>
      </div>
    </div>
  );
}

export default Statistics;
