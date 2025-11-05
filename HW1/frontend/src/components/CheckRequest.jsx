import React, { useState } from 'react';
import { getServiceRequestByToken, cancelServiceRequest } from '../services/api';
import './CheckRequest.css';

const CheckRequest = () => {
  const [token, setToken] = useState('');
  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setRequest(null);

    try {
      const data = await getServiceRequestByToken(token);
      setRequest(data);
    } catch (err) {
      console.error('Error fetching request:', err);
      const errorMessage = err.apiError?.message || 'Failed to fetch request. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    setLoading(true);
    setError(null);

    try {
      await cancelServiceRequest(token);
      // Refresh the request to show updated status
      const updatedData = await getServiceRequestByToken(token);
      setRequest(updatedData);
      setShowCancelConfirm(false);
    } catch (err) {
      console.error('Error cancelling request:', err);
      const errorMessage = err.apiError?.message || 'Failed to cancel request. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadgeClass = (status) => {
    const classes = {
      RECEIVED: 'status-received',
      ASSIGNED: 'status-assigned',
      IN_PROGRESS: 'status-in-progress',
      COMPLETED: 'status-completed',
      CANCELLED: 'status-cancelled',
    };
    return `status-badge ${classes[status] || ''}`;
  };

  const getStatusLabel = (status) => {
    const labels = {
      RECEIVED: 'Received',
      ASSIGNED: 'Assigned',
      IN_PROGRESS: 'In Progress',
      COMPLETED: 'Completed',
      CANCELLED: 'Cancelled',
    };
    return labels[status] || status;
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('pt-PT', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatDateTime = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleString('pt-PT', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTimeSlotLabel = (slot) => {
    const labels = {
      MORNING: 'Morning (08:00 - 12:00)',
      AFTERNOON: 'Afternoon (12:00 - 18:00)',
      EVENING: 'Evening (18:00 - 21:00)',
    };
    return labels[slot] || slot;
  };

  const canCancel = request && 
    request.status !== 'COMPLETED' && 
    request.status !== 'CANCELLED';

  return (
    <div className="check-request-container">
      <h1>Check Your Request</h1>
      <p className="subtitle">Enter your access token to view request details</p>

      <form onSubmit={handleSearch} className="search-form">
        <div className="search-group">
          <input
            type="text"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Enter your access token"
            required
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Searching...' : 'Search'}
          </button>
        </div>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      {request && (
        <div className="request-details">
          <div className="request-header">
            <h2>Request Details</h2>
            <span className={getStatusBadgeClass(request.status)}>
              {getStatusLabel(request.status)}
            </span>
          </div>

          <div className="details-grid">
            <div className="detail-section">
              <h3>📍 Location</h3>
              <p>
                <strong>Municipality:</strong> {request.municipalityName}
              </p>
              <p>
                <strong>Address:</strong> {request.pickupAddress}
              </p>
            </div>

            <div className="detail-section">
              <h3>👤 Contact</h3>
              <p>
                <strong>Name:</strong> {request.citizenName}
              </p>
              {request.citizenEmail && (
                <p>
                  <strong>Email:</strong> {request.citizenEmail}
                </p>
              )}
              {request.citizenPhone && (
                <p>
                  <strong>Phone:</strong> {request.citizenPhone}
                </p>
              )}
            </div>

            <div className="detail-section">
              <h3>📦 Items</h3>
              <p>{request.itemDescription}</p>
            </div>

            <div className="detail-section">
              <h3>📅 Collection Schedule</h3>
              <p>
                <strong>Date:</strong> {formatDate(request.preferredDate)}
              </p>
              <p>
                <strong>Time Slot:</strong>{' '}
                {getTimeSlotLabel(request.preferredTimeSlot)}
              </p>
            </div>

            <div className="detail-section">
              <h3>ℹ️ Request Info</h3>
              <p>
                <strong>Request ID:</strong> #{request.id}
              </p>
              <p>
                <strong>Created:</strong> {formatDateTime(request.createdAt)}
              </p>
              <p>
                <strong>Last Update:</strong> {formatDateTime(request.updatedAt)}
              </p>
            </div>
          </div>

          {request.statusHistory && request.statusHistory.length > 0 && (
            <div className="status-timeline">
              <h3>📊 Status History</h3>
              <div className="timeline">
                {request.statusHistory.map((history, index) => (
                  <div key={history.id} className="timeline-item">
                    <div className="timeline-marker"></div>
                    <div className="timeline-content">
                      <div className="timeline-header">
                        <span className={getStatusBadgeClass(history.newStatus)}>
                          {getStatusLabel(history.newStatus)}
                        </span>
                        <span className="timeline-date">
                          {formatDateTime(history.timestamp)}
                        </span>
                      </div>
                      {history.notes && (
                        <p className="timeline-notes">{history.notes}</p>
                      )}
                      {history.previousStatus && (
                        <p className="timeline-transition">
                          From {getStatusLabel(history.previousStatus)}
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {canCancel && (
            <div className="request-actions">
              {!showCancelConfirm ? (
                <button
                  className="btn btn-danger"
                  onClick={() => setShowCancelConfirm(true)}
                >
                  Cancel Request
                </button>
              ) : (
                <div className="cancel-confirm">
                  <p>Are you sure you want to cancel this request?</p>
                  <div className="confirm-buttons">
                    <button
                      className="btn btn-danger"
                      onClick={handleCancel}
                      disabled={loading}
                    >
                      Yes, Cancel
                    </button>
                    <button
                      className="btn btn-secondary"
                      onClick={() => setShowCancelConfirm(false)}
                    >
                      No, Keep It
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default CheckRequest;

