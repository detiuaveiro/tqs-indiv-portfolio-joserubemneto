import React from 'react';
import './RequestCard.css';

const RequestCard = ({ request, onUpdateStatus }) => {
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
      month: 'short',
      day: 'numeric',
    });
  };

  const getTimeSlotLabel = (slot) => {
    const labels = {
      MORNING: '🌅 Morning',
      AFTERNOON: '☀️ Afternoon',
      EVENING: '🌆 Evening',
    };
    return labels[slot] || slot;
  };

  const canUpdate = request.status !== 'COMPLETED' && request.status !== 'CANCELLED';

  return (
    <div className="request-card">
      <div className="card-header">
        <div className="card-id">
          <span className="id-label">Request</span>
          <span className="id-number">#{request.id}</span>
        </div>
        <span className={getStatusBadgeClass(request.status)}>
          {getStatusLabel(request.status)}
        </span>
      </div>

      <div className="card-body">
        <div className="info-row">
          <span className="info-icon">📍</span>
          <div className="info-content">
            <strong>{request.municipalityName}</strong>
            <p className="info-detail">{request.pickupAddress}</p>
          </div>
        </div>

        <div className="info-row">
          <span className="info-icon">👤</span>
          <div className="info-content">
            <strong>{request.citizenName}</strong>
            {request.citizenPhone && (
              <p className="info-detail">📞 {request.citizenPhone}</p>
            )}
          </div>
        </div>

        <div className="info-row">
          <span className="info-icon">📦</span>
          <div className="info-content">
            <p className="item-description">{request.itemDescription}</p>
          </div>
        </div>

        <div className="info-row">
          <span className="info-icon">📅</span>
          <div className="info-content">
            <strong>{formatDate(request.preferredDate)}</strong>
            <p className="info-detail">{getTimeSlotLabel(request.preferredTimeSlot)}</p>
          </div>
        </div>

        <div className="info-row info-meta">
          <span className="info-icon">🕐</span>
          <div className="info-content">
            <p className="info-detail">Created: {formatDate(request.createdAt)}</p>
          </div>
        </div>
      </div>

      <div className="card-footer">
        {canUpdate ? (
          <button
            className="btn btn-update"
            onClick={() => onUpdateStatus(request)}
          >
            Update Status
          </button>
        ) : (
          <div className="status-final">
            {request.status === 'COMPLETED' ? '✅ Completed' : '❌ Cancelled'}
          </div>
        )}
      </div>
    </div>
  );
};

export default RequestCard;

