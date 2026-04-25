import axios from 'axios'
import ElementUI from 'element-ui'

// let is_baseURL = 'http://www.luwenhe.top:8090'
// let is_baseURL = 'http://10.0.3.67:8090'
// let is_baseURL = 'http://1.94.119.107:8090'
let is_baseURL = 'http://localhost:8090'

// 创建axios实例
export function request(config) {
  const instance = axios.create({
    baseURL: is_baseURL
  })

  // 请求拦截器: 请求接口前需要进行拦截
  instance.interceptors.request.use(config => {
    // 将token放入请求头中
    config.headers.token = localStorage.getItem('token')
    // 改变请求头
    config.headers['Content-Type'] = 'application/json'
    return config
  }, error => {
    // 在请求错误后要做些什么
    console.log(error)
  })

  // 响应拦截器: 请求接口并得到响应后进行拦截
  instance.interceptors.response.use(response => {
    let resData = response.data

    if (resData.status === 500) {
      ElementUI.Message.error('token过期, 请重新登陆!')
      localStorage.clear()
      window.location.href = "/login"
    }
    // 在响应成功后必须要将响应的得到的数据返回,否则在index.js中无法拿到响应成功的数据
    return response.data;
  }, error => {
    // 在响应错误后要做些什么
    console.log(error)
  })

  // 直接返回一个promise
  return instance(config)
}
