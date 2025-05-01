import axios from "axios";

const BASE_URL = 'http://localhost:6789/api/v1/'

const api = axios.create({
    baseURL: BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        "Referrer-Policy": "strict-origin-when-cross-origin",
    },
    withCredentials: true,
})

api.interceptors.request.use((config) => { return config }, (error) => { return Promise.reject })

api.interceptors.response.use((response) => { return response.data }, (error) => {
    if (error.response) {
        switch (error.response.status) {
            case 401:
                throw new Error("Unauthorized: Invalid credentials");
            case 403:
                throw new Error("Forbidden: Access denied");
            case 404:
                throw new Error("Not found");
            default:
                throw new Error(error.response.data.content);
        }
    }
    return Promise.reject(error)
})

export const apiService = {
    get: (url, config = {}) => api.get(url, config),
    post: (url, data, config = {}) => api.post(url, data, config),
    put: (url, data, config = {}) => api.put(url, data, config),
    patch: (url, data, config = {}) => api.patch(url, data, config),
    delete: (url, config = {}) => api.delete(url, config),
}
export default apiService;