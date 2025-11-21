import React, { useState } from 'react';
import './OrderForm.css';

const PRODUCTS = [
  'Laptop', 'Smartphone', 'Tablet', 'Headphones', 'Monitor',
  'Keyboard', 'Mouse', 'Webcam', 'Speaker', 'Charger'
];

function OrderForm({ onSubmit }) {
  const [formData, setFormData] = useState({
    productName: PRODUCTS[0],
    price: '',
    quantity: '1'
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    const success = await onSubmit({
      productName: formData.productName,
      price: parseFloat(formData.price),
      quantity: parseInt(formData.quantity)
    });
    
    if (success) {
      setFormData({
        productName: PRODUCTS[0],
        price: '',
        quantity: '1'
      });
    }
    
    setLoading(false);
  };

  const handleRandomize = () => {
    setFormData({
      productName: PRODUCTS[Math.floor(Math.random() * PRODUCTS.length)],
      price: (Math.random() * 1990 + 10).toFixed(2),
      quantity: Math.floor(Math.random() * 10) + 1
    });
  };

  return (
    <div className="order-form-container">
      <h2>📦 Create New Order</h2>
      <form onSubmit={handleSubmit} className="order-form">
        <div className="form-group">
          <label>Product Name</label>
          <select
            value={formData.productName}
            onChange={(e) => setFormData({ ...formData, productName: e.target.value })}
            required
          >
            {PRODUCTS.map(product => (
              <option key={product} value={product}>{product}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Price ($)</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            value={formData.price}
            onChange={(e) => setFormData({ ...formData, price: e.target.value })}
            placeholder="Enter price"
            required
          />
        </div>

        <div className="form-group">
          <label>Quantity</label>
          <input
            type="number"
            min="1"
            max="100"
            value={formData.quantity}
            onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
            required
          />
        </div>

        <div className="form-actions">
          <button type="button" onClick={handleRandomize} className="btn-secondary">
            🎲 Random
          </button>
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Sending...' : '✅ Create Order'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default OrderForm;
