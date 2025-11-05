import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Home from './components/Home';
import CreateRequest from './components/CreateRequest';
import CheckRequest from './components/CheckRequest';
import StaffDashboard from './components/StaffDashboard';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <div className="nav-container">
            <Link to="/" className="nav-logo">
              🗑️ ZeroMonos
            </Link>
            <ul className="nav-menu">
              <li className="nav-item">
                <Link to="/" className="nav-link">
                  Home
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/create" className="nav-link">
                  New Request
                </Link>
              </li>
              <li className="nav-item">
                <Link to="/check" className="nav-link">
                  Check Status
                </Link>
              </li>
            </ul>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/create" element={<CreateRequest />} />
            <Route path="/check" element={<CheckRequest />} />
            <Route path="/staff" element={<StaffDashboard />} />
          </Routes>
        </main>

        <footer className="footer">
          <p>&copy; 2025 ZeroMonos Waste Collection Service</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;
