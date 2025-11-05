import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;
      
      console.error(`API Error [${status}]:`, data);
      
      // Attach structured error information
      error.apiError = {
        status,
        message: data.message || 'An error occurred',
        errors: data.errors || null, // Validation errors
        timestamp: data.timestamp
      };
    } else if (error.request) {
      // Request made but no response
      console.error('Network Error: No response from server');
      error.apiError = {
        status: 0,
        message: 'Cannot connect to server. Please check your connection.',
        errors: null
      };
    } else {
      // Error setting up request
      console.error('Request Error:', error.message);
      error.apiError = {
        status: -1,
        message: error.message || 'An unexpected error occurred',
        errors: null
      };
    }
    
    return Promise.reject(error);
  }
);

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

