import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  return (
    <div className="home-container">
      <div className="hero-section">
        <h1>🗑️ ZeroMonos</h1>
        <h2>Waste Collection Service</h2>
        <p className="hero-subtitle">
          Schedule pickups for large items and bulky waste
        </p>
      </div>

      <div className="features-grid">
        <div className="feature-card">
          <div className="feature-icon">📦</div>
          <h3>Easy Scheduling</h3>
          <p>Request collection with just a few clicks</p>
          <Link to="/create" className="btn btn-primary">
            Create Request
          </Link>
        </div>

        <div className="feature-card">
          <div className="feature-icon">🔍</div>
          <h3>Track Your Request</h3>
          <p>Check status and details anytime</p>
          <Link to="/check" className="btn btn-secondary">
            Check Status
          </Link>
        </div>
      </div>

      <div className="info-section">
        <h3>How it works</h3>
        <div className="steps">
          <div className="step">
            <span className="step-number">1</span>
            <div>
              <h4>Submit Request</h4>
              <p>Fill out the form with your details and preferred collection date</p>
            </div>
          </div>
          <div className="step">
            <span className="step-number">2</span>
            <div>
              <h4>Receive Token</h4>
              <p>Get a unique access token to track your request</p>
            </div>
          </div>
          <div className="step">
            <span className="step-number">3</span>
            <div>
              <h4>Collection Day</h4>
              <p>Our team will pick up your items on the scheduled date</p>
            </div>
          </div>
        </div>
      </div>

      <div className="info-box">
        <h4>⏰ Time Slots Available:</h4>
        <ul>
          <li><strong>Morning:</strong> 08:00 - 12:00</li>
          <li><strong>Afternoon:</strong> 12:00 - 18:00</li>
          <li><strong>Evening:</strong> 18:00 - 21:00</li>
        </ul>
      </div>
    </div>
  );
};

export default Home;

