import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const triggerScan = async () => {
    const response = await api.post('/scan');
    return response.data;
};

export const getEC2Instances = async () => {
    const response = await api.get('/instances');
    return response.data;
};

export const getS3Buckets = async () => {
    const response = await api.get('/buckets');
    return response.data;
};

export const getCISResults = async () => {
    const response = await api.get('/cis-results');
    return response.data;
};

export const getDashboardSummary = async () => {
    const response = await api.get('/dashboard/summary');
    return response.data;
};

export default api;
