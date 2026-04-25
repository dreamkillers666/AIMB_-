import axios from 'axios'
import ElementUI from 'element-ui'
import router from "@/router"; // 导入路由，用于401时跳转

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 30000 // 适当延长超时时间，以防大文件或慢查询
})

// request 拦截器
// 可以在请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

    const userJson = localStorage.getItem("user");
    if (userJson) {
        const user = JSON.parse(userJson);
        // 确保 user 对象和 token 都存在
        if (user && user.token) {
            // 【核心修改】
            // 1. 请求头名称从 'token' 改为 'Authorization'
            // 2. token 值前面加上 'Bearer ' 和一个空格
            config.headers['Authorization'] = 'Bearer ' + user.token;
        }
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件流
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        console.log('err' + error) // for debug
        // 【优化】在这里统一处理HTTP错误状态码，比在成功回调里判断业务码更标准
        if (error.response && error.response.status === 401) {
            ElementUI.Message({
                message: '登录状态已过期，请重新登录',
                type: 'error'
            });
            // 跳转到登录页面
            router.push("/login");
        }
        return Promise.reject(error)
    }
)

export default request