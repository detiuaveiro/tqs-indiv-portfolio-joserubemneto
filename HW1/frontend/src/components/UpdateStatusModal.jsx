import React, { useState } from 'react';
import { updateServiceRequestStatus } from '../services/staffApi';
import './UpdateStatusModal.css';

const UpdateStatusModal = ({ request, onClose, onStatusUpdated }) => {
  const [newStatus, setNewStatus] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const getAvailableStatuses = () => {
    const currentStatus = request.status;
    const statusOptions = [];

    switch (currentStatus) {
      case 'RECEIVED':
        statusOptions.push(
          { value: 'ASSIGNED', label: 'Assign to Team', icon: '👥' },
          { value: 'CANCELLED', label: 'Cancel Request', icon: '❌' }
        );
        break;
      case 'ASSIGNED':
        statusOptions.push(
          { value: 'IN_PROGRESS', label: 'Start Collection', icon: '🚛' },
          { value: 'CANCELLED', label: 'Cancel Request', icon: '❌' }
        );
        break;
      case 'IN_PROGRESS':
        statusOptions.push(
          { value: 'COMPLETED', label: 'Mark as Completed', icon: '✅' },
          { value: 'CANCELLED', label: 'Cancel Request', icon: '❌' }
        );
        break;
      case 'CANCELLED':
        statusOptions.push(
          { value: 'RECEIVED', label: 'Reopen Request', icon: '🔄' }
        );
        break;
      default:
        break;
    }

    return statusOptions;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newStatus) {
      setError('Please select a new status');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await updateServiceRequestStatus(request.id, {
        newStatus,
        notes: notes || undefined,
      });
      onStatusUpdated();
    } catch (err) {
      console.error('Error updating status:', err);
      const errorMessage = err.apiError?.message || 'Failed to update status. Please try again.';
      setError(errorMessage);
      setLoading(false);
    }
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

  const availableStatuses = getAvailableStatuses();

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Update Request Status</h2>
          <button className="modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          <div className="request-info-box">
            <div className="info-item">
              <strong>Request ID:</strong> #{request.id}
            </div>
            <div className="info-item">
              <strong>Citizen:</strong> {request.citizenName}
            </div>
            <div className="info-item">
              <strong>Municipality:</strong> {request.municipalityName}
            </div>
            <div className="info-item">
              <strong>Current Status:</strong>{' '}
              <span className="current-status">{getStatusLabel(request.status)}</span>
            </div>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="newStatus">
                New Status <span className="required">*</span>
              </label>
              {availableStatuses.length === 0 ? (
                <p className="no-transitions">
                  No status transitions available for this request.
                </p>
              ) : (
                <div className="status-options">
                  {availableStatuses.map((status) => (
                    <label key={status.value} className="status-option">
                      <input
                        type="radio"
                        name="newStatus"
                        value={status.value}
                        checked={newStatus === status.value}
                        onChange={(e) => setNewStatus(e.target.value)}
                        required
                      />
                      <div className="status-option-content">
                        <span className="status-icon">{status.icon}</span>
                        <span className="status-label">{status.label}</span>
                      </div>
                    </label>
                  ))}
                </div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="notes">Notes (optional)</label>
              <textarea
                id="notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Add any notes about this status change..."
                rows={4}
                maxLength={500}
              />
              <small className="char-counter">{notes.length}/500 characters</small>
            </div>

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={onClose}
                disabled={loading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading || !newStatus || availableStatuses.length === 0}
              >
                {loading ? 'Updating...' : 'Update Status'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default UpdateStatusModal;

