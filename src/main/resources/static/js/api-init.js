/**
 * 文件名: api-init.js
 * 功能: 全局Axios初始化配置
 * 说明: 
 * 1. 为所有HTTP请求自动添加JWT令牌认证头
 * 2. 统一处理401未授权错误，自动跳转到登录页
 * 3. 使用IIFE（立即执行函数）避免污染全局命名空间
 */

// 使用IIFE（Immediately Invoked Function Expression）立即执行函数
(function () {
  // 检查axios是否已加载，未加载则直接返回
  if (!window.axios) return;

  /**
   * 请求拦截器：在每个请求发送前执行
   * 功能：自动从localStorage获取accessToken并添加到请求头
   * 数据源：统一使用localStorage.accessToken
   */
  axios.interceptors.request.use(
    function (config) {
      try {
        // 从本地存储获取访问令牌
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
          // 确保headers对象存在
          config.headers = config.headers || {};
          // 添加Authorization头，使用Bearer令牌格式
          config.headers.Authorization = `Bearer ${accessToken}`;
        }
      } catch (e) {
        // 忽略错误，避免阻塞请求
      }
      return config;
    },
    function (error) { 
      // 请求错误时的处理
      return Promise.reject(error); 
    }
  );

  /**
   * 响应拦截器：在收到响应后执行
   * 功能：统一处理401未授权错误
   * 处理逻辑：
   * 1. 清除本地存储的令牌
   * 2. 重定向到登录页面
   */
  axios.interceptors.response.use(
    function (response) { 
      // 响应成功，直接返回响应数据
      return response; 
    },
    function (error) {
      try {
        // 检查是否为401未授权错误
        if (error && error.response && error.response.status === 401) {
          // 清除本地存储的令牌
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          
          // 重定向到登录页面
          if (window && window.location) {
            window.location.href = '/auction/login';
          }
        }
      } catch (e) {
        // 忽略错误，避免阻塞错误处理
      }
      // 返回rejected promise，让调用者可以继续处理错误
      return Promise.reject(error);
    }
  );
})();


