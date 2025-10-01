// Global Axios initialization: unified token handling and 401 redirect
(function () {
  if (!window.axios) return;

  // Request interceptor - add Authorization header (single source: localStorage.accessToken)
  axios.interceptors.request.use(
    function (config) {
      try {
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
          config.headers = config.headers || {};
          config.headers.Authorization = `Bearer ${accessToken}`;
        }
      } catch (e) {}
      return config;
    },
    function (error) { return Promise.reject(error); }
  );

  // Response interceptor - handle 401
  axios.interceptors.response.use(
    function (response) { return response; },
    function (error) {
      try {
        if (error && error.response && error.response.status === 401) {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          // Redirect to login
          if (window && window.location) {
            window.location.href = '/auction/login';
          }
        }
      } catch (e) {}
      return Promise.reject(error);
    }
  );
})();


