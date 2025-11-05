import api from './api';

// Staff endpoints
export const getAllServiceRequests = async (municipality = null) => {
  const params = municipality ? { municipality } : {};
  const response = await api.get('/staff/requests', { params });
  return response.data;
};

export const updateServiceRequestStatus = async (id, statusData) => {
  const response = await api.put(`/staff/requests/${id}/status`, statusData);
  return response.data;
};

export default {
  getAllServiceRequests,
  updateServiceRequestStatus,
};

