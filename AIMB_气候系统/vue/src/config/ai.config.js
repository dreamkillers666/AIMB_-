/**
 * ENSO AI 助手配置
 *
 * 说明：
 * 1) 这里等价于 Codex CLI 的配置文件（model_provider / model / wire_api / OPENAI_API_KEY），
 *    统一收敛成前端可读取的 JS 配置，便于后续替换端点或模型。
 * 2) baseUrl 默认走「同源代理」/ai-gateway/v1：
 *    - 开发环境由 vue.config.js 的 devServer.proxy 转发到 https://api.gs88.shop；
 *    - 生产环境请在静态服务器 / 反向代理上做同样的一条转发规则
 *      （或直接改用 VUE_APP_AI_BASE_URL 指向你自己的后端代理）。
 *    之所以默认不走绝对地址：api.gs88.shop 没有返回 Access-Control-Allow-Origin，
 *    浏览器直连一定被 CORS 拦截（curl 能通不代表页面能通）。
 * 3) directBaseUrl 作为兜底候选：当前端被部署在「没有配置代理、但网关本身允许跨域」
 *    的环境时，客户端会自动切到直连地址。
 * 4) 如需临时覆盖，可在 vue 目录下新建 .env.local 并写入
 *    VUE_APP_AI_API_KEY / VUE_APP_AI_BASE_URL / VUE_APP_AI_MODEL。
 * 5) 安全提醒：前端打包后 apiKey 会出现在浏览器中，仅适用于内网/演示环境；
 *    生产环境请改为由后端代理转发，不要把密钥暴露在静态资源里。
 */

const DEFAULT_MODEL = 'gpt-5.5'
const DEFAULT_GATEWAY_ORIGIN = 'https://api.gs88.shop'
// 同源代理前缀（与 vue.config.js 的 devServer.proxy 保持一致）
const DEFAULT_PROXY_BASE = '/ai-gateway/v1'
const DEFAULT_DIRECT_BASE = `${DEFAULT_GATEWAY_ORIGIN}/v1`
const DEFAULT_API_KEY = 'sk-9214623399b1dc4ad9bbaaf870794bdeb4339a8d248ca144c3c976ec682dee2a'

// 读取构建期环境变量（未配置时回落到配置常量）
const env = (typeof process !== 'undefined' && process.env) ? process.env : {}

const trimSlash = value => String(value || '').replace(/\/+$/, '')

const baseUrl = trimSlash(env.VUE_APP_AI_BASE_URL || DEFAULT_PROXY_BASE)
const directBaseUrl = trimSlash(env.VUE_APP_AI_DIRECT_BASE_URL || DEFAULT_DIRECT_BASE)

export const aiConfig = {
  // ---- 基础接入信息 ----
  modelProvider: 'OpenAI',
  model: env.VUE_APP_AI_MODEL || DEFAULT_MODEL,
  reviewModel: DEFAULT_MODEL,
  baseUrl,
  modelsUrl: `${baseUrl}/models`,
  // 主地址不可用时的备用地址（同源代理 -> 直连）
  fallbackBaseUrls: directBaseUrl && directBaseUrl !== baseUrl ? [directBaseUrl] : [],
  apiKey: env.VUE_APP_AI_API_KEY || DEFAULT_API_KEY,
  wireApi: 'responses', // responses | chat.completions
  requiresOpenaiAuth: true,
  disableResponseStorage: true,
  apiKeyModelDiscovery: true,

  // ---- 模型提供方（保留结构化写法，便于对照配置文件原文）----
  modelProviders: {
    OpenAI: {
      name: 'OpenAI',
      base_url: DEFAULT_GATEWAY_ORIGIN,
      proxy_base_url: baseUrl,
      model_catalog_url: `${baseUrl}/models`,
      wire_api: 'responses',
      requires_openai_auth: true
    }
  },

  // ---- 运行参数 ----
  requestTimeoutMs: 180000, // 整体请求上限
  idleTimeoutMs: 90000, // 流式期间「多久没有新内容」判定超时（推理模型首字可能较慢）
  stream: true,
  temperature: 1,
  maxContextMessages: 20, // 送上下文时保留的最近消息条数
  welcomeMessage:
    '我是 ENSO AI 助手，可以解读 ENSO 历史事件、LSTA-Swin 历史预测与影响研判报告，也能回答 Niño3.4 / ONI、海气耦合、模型技巧相关的问题。',
  systemPrompt: [
    '你是「ENSO AI 助手」，服务对象是气候预测业务与科研人员。',
    '你的知识范围覆盖：ENSO（厄尔尼诺—南方涛动）监测与预测、Niño3.4 / ONI / SOI 指数、海表与次表层海温距平（SSTA / SSTA150）、海表风应力（τx / τy）、暖水体积与海洋热含量、ENSO 遥相关及其对中国降水、温度与农业的影响。',
    '平台模型为 LSTA-Swin（长提前期 20 个月 Niño3.4 / ONI 预测，长期窗口 12 个月、近期窗口 3 个月），验证指标包含 PCC、MAE、RMSE 与三档状态命中率。',
    '回答要求：使用简体中文；结论先行、分点表述；涉及数值时标明单位与时间范围；区分「观测事实」「模型预测」与「不确定性」，不确定时明确说明；不编造数据，缺少依据时直接说明缺少依据。'
  ].join('\n'),
  quickPrompts: [
    'Niño3.4 和 ONI 有什么区别？',
    '拉尼娜衰减期对中国春季降水有什么影响？',
    'LSTA-Swin 的 20 个月预测技巧如何随提前期衰减？',
    '2025 年 1 月 ENSO 影响研判报告的结论是什么？'
  ],
  // ---- 弹片（悬浮球旁的消息提醒式卡片）尺寸 ----
  popover: {
    gap: 14, // 悬浮球与弹片之间的间距
    chatWidth: 330,
    chatHeight: 380,
    briefingWidth: 392,
    briefingHeight: 480
  }
}

export default aiConfig
