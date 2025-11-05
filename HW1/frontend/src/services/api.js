import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Municipality endpoints
export const getMunicipalities = async () => {
  const response = await api.get('/municipalities');
  return response.data;
};

// Citizen endpoints
export const createServiceRequest = async (requestData) => {
  const response = await api.post('/requests', requestData);
  return response.data;
};

export const getServiceRequestByToken = async (token) => {
  const response = await api.get(`/requests/${token}`);
  return response.data;
};

export const cancelServiceRequest = async (token) => {
  const response = await api.delete(`/requests/${token}`);
  return response.data;
};

export default api;

