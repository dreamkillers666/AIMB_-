const { defineConfig } = require('@vue/cli-service')

// 上游 AI 网关（OpenAI Responses 协议）
// 可用 VUE_APP_AI_ORIGIN 覆盖，例如切到自建网关。
const AI_GATEWAY_ORIGIN = process.env.VUE_APP_AI_ORIGIN || 'https://api.gs88.shop'

module.exports = defineConfig({
  transpileDependencies: true,
  // 生产构建不输出 sourcemap：dist 体积减半，且不暴露源码
  productionSourceMap: false,
  devServer: {
    historyApiFallback: true,
    // 关键：必须关闭 dev-server 的响应压缩。
    // 开启压缩时 Express compression 会把 AI 的 text/event-stream 一起 gzip，
    // 流被压缩缓冲后浏览器端要等整段生成完才收到，表现为「不流式 / 60 秒超时」。
    compress: false,
    // api.gs88.shop 未返回 Access-Control-Allow-Origin，浏览器直连会被 CORS 拦截。
    // 这里把 /ai-gateway/* 反向代理到网关，前端改用同源相对路径请求，
    // 跨域问题从根上消失（同时也不会再把 apiKey 暴露在跨域预检里）。
    proxy: {
      '/ai-gateway': {
        target: AI_GATEWAY_ORIGIN,
        changeOrigin: true,
        secure: true,
        ws: false,
        pathRewrite: { '^/ai-gateway': '' },
        // SSE 长连接：放宽代理超时，避免流式回答被提前掐断
        timeout: 300000,
        proxyTimeout: 300000,
        // 告诉上游不要压缩，保证 SSE 逐段下发
        headers: { 'Accept-Encoding': 'identity' }
      },
      // python 引擎（ENSO 观测指数 / 风场）：开发环境代理到本地 8000，
      // 生产环境由 nginx 的 /pyeng/ location 转发，前后端路径一致
      '/pyeng': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true,
        pathRewrite: { '^/pyeng': '' },
        timeout: 60000,
        proxyTimeout: 60000
      }
    }
  }
})
