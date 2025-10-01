/**
 * 拍卖系统后台管理 JavaScript
 * 基于 Element UI 和 Vue 3
 */

// 全局配置
const AdminConfig = {
    // API 基础路径
    apiBaseUrl: '/api',
    // 分页默认配置
    pagination: {
        pageSize: 10,
        pageSizes: [10, 20, 50, 100]
    },
    // 日期格式
    dateFormat: 'YYYY-MM-DD HH:mm:ss',
    // 文件上传配置
    upload: {
        maxSize: 10 * 1024 * 1024, // 10MB
        allowedTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    }
};

// 工具函数
const AdminUtils = {
    /**
     * 格式化价格
     * @param {number} price 价格（分为单位）
     * @returns {string} 格式化后的价格
     */
    formatPrice(price) {
        if (!price) return '¥0.00';
        return '¥' + (price / 100).toFixed(2);
    },

    /**
     * 格式化时间
     * @param {string|Date} time 时间
     * @returns {string} 格式化后的时间
     */
    formatTime(time) {
        if (!time) return '-';
        const date = new Date(time);
        return date.toLocaleString('zh-CN');
    },

    /**
     * 获取状态标签类型
     * @param {string|number} status 状态
     * @returns {string} 标签类型
     */
    getStatusTagType(status) {
        const statusMap = {
            'pending': 'warning',
            'active': 'success',
            'inactive': 'info',
            'cancelled': 'danger',
            'completed': 'success',
            '1': 'warning',
            '2': 'success',
            '3': 'info',
            '4': 'danger'
        };
        return statusMap[status] || 'info';
    },

    /**
     * 获取状态文本
     * @param {string|number} status 状态
     * @returns {string} 状态文本
     */
    getStatusText(status) {
        const statusMap = {
            'pending': '待处理',
            'active': '进行中',
            'inactive': '已停用',
            'cancelled': '已取消',
            'completed': '已完成',
            '1': '待审核',
            '2': '已通过',
            '3': '已拒绝',
            '4': '已删除'
        };
        return statusMap[status] || '未知';
    },

    /**
     * 防抖函数
     * @param {Function} func 要防抖的函数
     * @param {number} delay 延迟时间
     * @returns {Function} 防抖后的函数
     */
    debounce(func, delay) {
        let timeoutId;
        return function (...args) {
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func.apply(this, args), delay);
        };
    },

    /**
     * 深拷贝对象
     * @param {any} obj 要拷贝的对象
     * @returns {any} 拷贝后的对象
     */
    deepClone(obj) {
        if (obj === null || typeof obj !== 'object') return obj;
        if (obj instanceof Date) return new Date(obj.getTime());
        if (obj instanceof Array) return obj.map(item => this.deepClone(item));
        if (typeof obj === 'object') {
            const clonedObj = {};
            for (const key in obj) {
                if (obj.hasOwnProperty(key)) {
                    clonedObj[key] = this.deepClone(obj[key]);
                }
            }
            return clonedObj;
        }
    }
};

// API 请求封装
const AdminAPI = {
    /**
     * 发送请求
     * @param {string} url 请求URL
     * @param {string} method 请求方法
     * @param {any} data 请求数据
     * @param {Object} options 请求选项
     * @returns {Promise} 请求Promise
     */
    request(url, method = 'GET', data = null, options = {}) {
        const config = {
            method,
            url: AdminConfig.apiBaseUrl + url,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        // 添加认证token
        const token = localStorage.getItem('adminToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // 添加请求数据
        if (data) {
            if (method === 'GET') {
                config.params = data;
            } else {
                config.data = data;
            }
        }

        return axios(config);
    },

    /**
     * GET 请求
     * @param {string} url 请求URL
     * @param {Object} params 请求参数
     * @param {Object} options 请求选项
     * @returns {Promise} 请求Promise
     */
    get(url, params = {}, options = {}) {
        return this.request(url, 'GET', params, options);
    },

    /**
     * POST 请求
     * @param {string} url 请求URL
     * @param {any} data 请求数据
     * @param {Object} options 请求选项
     * @returns {Promise} 请求Promise
     */
    post(url, data = {}, options = {}) {
        return this.request(url, 'POST', data, options);
    },

    /**
     * PUT 请求
     * @param {string} url 请求URL
     * @param {any} data 请求数据
     * @param {Object} options 请求选项
     * @returns {Promise} 请求Promise
     */
    put(url, data = {}, options = {}) {
        return this.request(url, 'PUT', data, options);
    },

    /**
     * DELETE 请求
     * @param {string} url 请求URL
     * @param {Object} options 请求选项
     * @returns {Promise} 请求Promise
     */
    delete(url, options = {}) {
        return this.request(url, 'DELETE', null, options);
    }
};

// 表格组件混入
const TableMixin = {
    data() {
        return {
            tableData: [],
            loading: false,
            pagination: {
                currentPage: 1,
                pageSize: AdminConfig.pagination.pageSize,
                total: 0
            },
            searchForm: {},
            selectedRows: []
        };
    },
    methods: {
        /**
         * 加载表格数据
         */
        async loadTableData() {
            this.loading = true;
            try {
                const params = {
                    page: this.pagination.currentPage,
                    size: this.pagination.pageSize,
                    ...this.searchForm
                };
                const response = await AdminAPI.get(this.tableApiUrl, params);
                if (response.data.code === 200) {
                    this.tableData = response.data.data.records || [];
                    this.pagination.total = response.data.data.total || 0;
                } else {
                    this.$message.error(response.data.message || '加载数据失败');
                }
            } catch (error) {
                console.error('Load table data error:', error);
                this.$message.error('加载数据失败');
            } finally {
                this.loading = false;
            }
        },

        /**
         * 搜索
         */
        handleSearch() {
            this.pagination.currentPage = 1;
            this.loadTableData();
        },

        /**
         * 重置搜索
         */
        handleReset() {
            this.searchForm = {};
            this.handleSearch();
        },

        /**
         * 分页大小改变
         * @param {number} size 新的分页大小
         */
        handleSizeChange(size) {
            this.pagination.pageSize = size;
            this.pagination.currentPage = 1;
            this.loadTableData();
        },

        /**
         * 当前页改变
         * @param {number} page 新的当前页
         */
        handleCurrentChange(page) {
            this.pagination.currentPage = page;
            this.loadTableData();
        },

        /**
         * 选择行改变
         * @param {Array} selection 选中的行
         */
        handleSelectionChange(selection) {
            this.selectedRows = selection;
        },

        /**
         * 刷新表格
         */
        refreshTable() {
            this.loadTableData();
        }
    }
};

// 表单组件混入
const FormMixin = {
    data() {
        return {
            formVisible: false,
            formLoading: false,
            formData: {},
            formRules: {},
            formTitle: ''
        };
    },
    methods: {
        /**
         * 打开表单
         * @param {Object} data 表单数据
         * @param {string} title 表单标题
         */
        openForm(data = {}, title = '') {
            this.formData = AdminUtils.deepClone(data);
            this.formTitle = title;
            this.formVisible = true;
        },

        /**
         * 关闭表单
         */
        closeForm() {
            this.formVisible = false;
            this.formData = {};
            this.$refs.formRef?.resetFields();
        },

        /**
         * 提交表单
         */
        async submitForm() {
            try {
                await this.$refs.formRef.validate();
                this.formLoading = true;
                
                const response = await this.saveFormData();
                if (response.data.code === 200) {
                    this.$message.success('保存成功');
                    this.closeForm();
                    this.refreshTable?.();
                } else {
                    this.$message.error(response.data.message || '保存失败');
                }
            } catch (error) {
                console.error('Submit form error:', error);
                this.$message.error('保存失败');
            } finally {
                this.formLoading = false;
            }
        },

        /**
         * 保存表单数据（子组件需要实现）
         */
        async saveFormData() {
            throw new Error('saveFormData method must be implemented');
        }
    }
};

// 全局错误处理
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response) {
            const { status, data } = error.response;
            
            switch (status) {
                case 401:
                    // 未授权，跳转到登录页
                    localStorage.removeItem('adminToken');
                    window.location.href = '/admin/login';
                    break;
                case 403:
                    // 禁止访问
                    ElMessage.error('没有权限访问该资源');
                    break;
                case 404:
                    // 资源不存在
                    ElMessage.error('请求的资源不存在');
                    break;
                case 500:
                    // 服务器错误
                    ElMessage.error('服务器内部错误');
                    break;
                default:
                    ElMessage.error(data.message || '请求失败');
            }
        } else {
            ElMessage.error('网络错误，请检查网络连接');
        }
        
        return Promise.reject(error);
    }
);

// 导出全局对象
window.AdminConfig = AdminConfig;
window.AdminUtils = AdminUtils;
window.AdminAPI = AdminAPI;
window.TableMixin = TableMixin;
window.FormMixin = FormMixin;
