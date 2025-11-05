import React, { useState, useEffect } from 'react';
import { getAllServiceRequests } from '../services/staffApi';
import { getMunicipalities } from '../services/api';
import RequestCard from './RequestCard';
import UpdateStatusModal from './UpdateStatusModal';
import './StaffDashboard.css';

const StaffDashboard = () => {
  const [requests, setRequests] = useState([]);
  const [municipalities, setMunicipalities] = useState([]);
  const [selectedMunicipality, setSelectedMunicipality] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [stats, setStats] = useState({
    total: 0,
    received: 0,
    assigned: 0,
    inProgress: 0,
    completed: 0,
    cancelled: 0,
  });

  useEffect(() => {
    loadMunicipalities();
    loadRequests();
  }, []);

  useEffect(() => {
    calculateStats();
  }, [requests]);

  const loadMunicipalities = async () => {
    try {
      const data = await getMunicipalities();
      setMunicipalities(data);
    } catch (err) {
      console.error('Error loading municipalities:', err);
      // Don't set error for this, it's not critical
    }
  };

  const loadRequests = async (municipality = null) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAllServiceRequests(municipality);
      setRequests(data);
    } catch (err) {
      console.error('Error loading requests:', err);
      const errorMessage = err.apiError?.message || 'Failed to load requests. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = () => {
    const newStats = {
      total: requests.length,
      received: requests.filter((r) => r.status === 'RECEIVED').length,
      assigned: requests.filter((r) => r.status === 'ASSIGNED').length,
      inProgress: requests.filter((r) => r.status === 'IN_PROGRESS').length,
      completed: requests.filter((r) => r.status === 'COMPLETED').length,
      cancelled: requests.filter((r) => r.status === 'CANCELLED').length,
    };
    setStats(newStats);
  };

  const handleMunicipalityFilter = (e) => {
    const municipality = e.target.value;
    setSelectedMunicipality(municipality);
    loadRequests(municipality || null);
  };

  const handleUpdateStatus = (request) => {
    setSelectedRequest(request);
    setShowModal(true);
  };

  const handleStatusUpdated = () => {
    setShowModal(false);
    setSelectedRequest(null);
    loadRequests(selectedMunicipality || null);
  };

  const filteredRequests = statusFilter
    ? requests.filter((r) => r.status === statusFilter)
    : requests;

  return (
    <div className="staff-dashboard">
      <div className="dashboard-header">
        <h1>📊 Staff Dashboard</h1>
        <p className="subtitle">Manage all waste collection requests</p>
      </div>

      {/* Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📦</div>
          <div className="stat-content">
            <h3>{stats.total}</h3>
            <p>Total Requests</p>
          </div>
        </div>
        <div className="stat-card stat-received">
          <div className="stat-icon">📥</div>
          <div className="stat-content">
            <h3>{stats.received}</h3>
            <p>Received</p>
          </div>
        </div>
        <div className="stat-card stat-assigned">
          <div className="stat-icon">👥</div>
          <div className="stat-content">
            <h3>{stats.assigned}</h3>
            <p>Assigned</p>
          </div>
        </div>
        <div className="stat-card stat-in-progress">
          <div className="stat-icon">🚛</div>
          <div className="stat-content">
            <h3>{stats.inProgress}</h3>
            <p>In Progress</p>
          </div>
        </div>
        <div className="stat-card stat-completed">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h3>{stats.completed}</h3>
            <p>Completed</p>
          </div>
        </div>
        <div className="stat-card stat-cancelled">
          <div className="stat-icon">❌</div>
          <div className="stat-content">
            <h3>{stats.cancelled}</h3>
            <p>Cancelled</p>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-group">
          <label htmlFor="municipalityFilter">Filter by Municipality:</label>
          <select
            id="municipalityFilter"
            value={selectedMunicipality}
            onChange={handleMunicipalityFilter}
          >
            <option value="">All Municipalities</option>
            {municipalities.map((municipality) => (
              <option key={municipality.code} value={municipality.name}>
                {municipality.name}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="statusFilter">Filter by Status:</label>
          <select
            id="statusFilter"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="RECEIVED">Received</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <button className="btn btn-refresh" onClick={() => loadRequests(selectedMunicipality || null)}>
          🔄 Refresh
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading requests...</p>
        </div>
      ) : (
        <>
          <div className="requests-summary">
            <p>
              Showing <strong>{filteredRequests.length}</strong> request(s)
              {selectedMunicipality && ` for ${selectedMunicipality}`}
            </p>
          </div>

          {filteredRequests.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📭</div>
              <h3>No requests found</h3>
              <p>There are no requests matching your filters.</p>
            </div>
          ) : (
            <div className="requests-grid">
              {filteredRequests.map((request) => (
                <RequestCard
                  key={request.id}
                  request={request}
                  onUpdateStatus={handleUpdateStatus}
                />
              ))}
            </div>
          )}
        </>
      )}

      {showModal && selectedRequest && (
        <UpdateStatusModal
          request={selectedRequest}
          onClose={() => setShowModal(false)}
          onStatusUpdated={handleStatusUpdated}
        />
      )}
    </div>
  );
};

export default StaffDashboard;

