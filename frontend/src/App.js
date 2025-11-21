import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from 'stomp-websocket';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import OrderForm from './components/OrderForm';
import Statistics from './components/Statistics';
import OrderList from './components/OrderList';
import './App.css';

function App() {
  const [statistics, setStatistics] = useState({
    totalOrders: 0,
    runningAverage: 0,
    totalRevenue: 0,
    successfulOrders: 0,
    failedOrders: 0
  });
  const [orders, setOrders] = useState([]);
  const [connected, setConnected] = useState(false);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    connectWebSocket();
    fetchStatistics();
    
    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws');
    const client = Stomp.over(socket);
    
    client.connect({}, () => {
      setConnected(true);
      toast.success('Connected to Kafka system!');
      
      client.subscribe('/topic/statistics', (message) => {
        const stats = JSON.parse(message.body);
        setStatistics(stats);
      });
      
      client.subscribe('/topic/orders', (message) => {
        const order = JSON.parse(message.body);
        setOrders(prev => [order, ...prev].slice(0, 20));
        
        if (order.status === 'SUCCESS') {
          toast.success(`Order ${order.orderId.substring(0, 8)}... processed!`);
        } else {
          toast.error(`Order ${order.orderId.substring(0, 8)}... failed!`);
        }
      });
      
      setStompClient(client);
    }, (error) => {
      console.error('WebSocket connection error:', error);
      setConnected(false);
      toast.error('Failed to connect to backend');
      setTimeout(connectWebSocket, 5000);
    });
  };

  const fetchStatistics = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/orders/statistics');
      const data = await response.json();
      setStatistics(data);
    } catch (error) {
      console.error('Failed to fetch statistics:', error);
    }
  };

  const handleOrderSubmit = async (orderData) => {
    try {
      const response = await fetch('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData)
      });
      
      const data = await response.json();
      
      if (response.ok) {
        toast.info(`Order ${data.orderId.substring(0, 8)}... created and sent to Kafka`);
        return true;
      } else {
        toast.error(data.message || 'Failed to create order');
        return false;
      }
    } catch (error) {
      console.error('Error creating order:', error);
      toast.error('Network error. Please try again.');
      return false;
    }
  };

  const handleResetStatistics = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/orders/statistics/reset', {
        method: 'POST'
      });
      
      if (response.ok) {
        setStatistics({
          totalOrders: 0,
          runningAverage: 0,
          totalRevenue: 0,
          successfulOrders: 0,
          failedOrders: 0
        });
        setOrders([]);
        toast.success('Statistics reset successfully');
      }
    } catch (error) {
      console.error('Error resetting statistics:', error);
      toast.error('Failed to reset statistics');
    }
  };

  return (
    <div className="App">
      <ToastContainer position="top-right" autoClose={3000} />
      
      <header className="App-header">
        <h1>🚀 Kafka Order Processing System</h1>
        <p className="student-info">Student: EG/2020/3903 | Big Data Analysis Assignment</p>
        <div className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
          <span className="status-dot"></span>
          {connected ? 'Connected to Kafka' : 'Disconnected'}
        </div>
      </header>

      <div className="container">
        <div className="left-panel">
          <OrderForm onSubmit={handleOrderSubmit} />
          <OrderList orders={orders} />
        </div>

        <div className="right-panel">
          <Statistics 
            statistics={statistics} 
            onReset={handleResetStatistics}
          />
        </div>
      </div>
    </div>
  );
}

export default App;
