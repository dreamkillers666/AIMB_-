/**
 * ENSO AI 助手 —— 模型调用客户端
 *
 * 走 OpenAI Responses 协议（wire_api = "responses"），即 POST {baseUrl}/responses。
 *
 * 支持：
 *  - 端点自动解析：先试同源代理 /ai-gateway/v1，失败再回退直连地址并缓存结果
 *  - 流式（SSE）输出，逐字回调 onDelta
 *  - 非流式兜底（流式失败或被禁用时自动回退）
 *  - 空闲超时：流式期间只要还在持续吐字就不算超时
 */
import aiConfig from '@/config/ai.config'

export class AiRequestError extends Error {
  constructor(message, status, detail) {
    super(message)
    this.name = 'AiRequestError'
    this.status = status || 0
    this.detail = detail || ''
  }
}

const joinUrl = (base, path) => `${String(base).replace(/\/+$/, '')}/${String(path).replace(/^\/+/, '')}`

const buildHeaders = () => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${aiConfig.apiKey}`
})

const safeParse = text => {
  try {
    return JSON.parse(text)
  } catch (error) {
    return null
  }
}

/** 依次尝试的候选端点（同源代理优先，其后是直连） */
const candidateBases = () => {
  const list = [aiConfig.baseUrl]
  const fallbacks = Array.isArray(aiConfig.fallbackBaseUrls) ? aiConfig.fallbackBaseUrls : []
  fallbacks.forEach(base => {
    if (base && list.indexOf(base) === -1) list.push(base)
  })
  return list.filter(Boolean)
}

/** 把网络层异常翻译成用户能看懂、能行动的中文提示 */
const describeNetworkError = (error, base) => {
  const isProxy = String(base).charAt(0) === '/'
  if (error && error.name === 'AbortError') return '连接 AI 网关超时'
  if (isProxy) {
    return `无法连接同源代理 ${base}（请确认前端服务已按 vue.config.js 配置了 /ai-gateway 转发，或改用 VUE_APP_AI_BASE_URL 指向后端代理）`
  }
  return `无法连接 AI 网关 ${base}（浏览器跨域被拦截或网络不可达；网关未返回 Access-Control-Allow-Origin，请通过代理访问）`
}

/** 从服务端错误体里提取可读信息 */
const describeFailure = (status, body) => {
  if (body && typeof body === 'object') {
    const error = body.error
    if (error && typeof error.message === 'string' && error.message) return error.message
    if (typeof body.message === 'string' && body.message) return body.message
    if (typeof body.detail === 'string' && body.detail) return body.detail
  }
  if (typeof body === 'string' && body.trim()) return body.slice(0, 200)
  if (status === 401 || status === 403) return 'AI 网关鉴权失败（apiKey 无效或已过期）'
  if (status === 404) return 'AI 网关接口路径不存在'
  if (status === 429) return 'AI 网关限流，请稍后重试'
  return `请求失败（HTTP ${status}）`
}

/** 带整体超时的简单 fetch */
const fetchWithTimeout = async (url, init, timeoutMs) => {
  if (typeof AbortController !== 'function') return fetch(url, init)
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)
  try {
    return await fetch(url, { ...init, signal: controller.signal })
  } finally {
    clearTimeout(timer)
  }
}

/**
 * 创建可「续命」的中止控制器：
 * 流式期间每收到一段内容就调用 touch()，只有真正长时间无响应才会中止。
 */
const createAbortScope = (idleMs, hardMs, externalSignal) => {
  if (typeof AbortController !== 'function') {
    return { controller: null, touch: () => {}, cleanup: () => {}, idleTimedOut: () => false }
  }
  const controller = new AbortController()
  let idledOut = false
  const started = Date.now()
  let idleTimer = null
  const arm = () => {
    if (idleTimer) clearTimeout(idleTimer)
    const remaining = Math.max(hardMs - (Date.now() - started), 1000)
    idleTimer = setTimeout(() => {
      idledOut = true
      controller.abort()
    }, Math.min(idleMs, remaining))
  }
  arm()
  const forwardAbort = () => controller.abort()
  if (externalSignal) {
    if (externalSignal.aborted) controller.abort()
    else externalSignal.addEventListener('abort', forwardAbort)
  }
  return {
    controller,
    touch: arm,
    idleTimedOut: () => idledOut,
    cleanup: () => {
      if (idleTimer) clearTimeout(idleTimer)
      if (externalSignal) externalSignal.removeEventListener('abort', forwardAbort)
    }
  }
}

const normalizeAbort = (error, externalSignal, idleMs, idleTimedOut) => {
  if (error && error.name === 'AbortError') {
    if (externalSignal && externalSignal.aborted) return new AiRequestError('已停止生成', 0, 'aborted')
    const seconds = Math.round((idleTimedOut && idleTimedOut() ? idleMs : aiConfig.requestTimeoutMs) / 1000)
    return new AiRequestError(`请求超时（${seconds} 秒）`, 0, 'timeout')
  }
  return error
}

/** 从非流式响应体里提取纯文本 */
export const extractOutputText = payload => {
  if (!payload || typeof payload !== 'object') return ''
  if (typeof payload.output_text === 'string' && payload.output_text.trim()) return payload.output_text
  const output = Array.isArray(payload.output) ? payload.output : []
  return output
    .filter(item => item && item.type === 'message' && Array.isArray(item.content))
    .reduce((acc, item) => acc.concat(item.content), [])
    .filter(part => part && (part.type === 'output_text' || part.type === 'text'))
    .map(part => part.text || '')
    .join('')
}

/* ------------------------------------------------------------------ */
/* 端点解析                                                            */
/* ------------------------------------------------------------------ */

let resolvedBase = null
let resolving = null

/** 探测单个端点是否「真的可用」（能返回 JSON 模型目录） */
const probeBase = async base => {
  const response = await fetchWithTimeout(joinUrl(base, 'models'), { method: 'GET', headers: buildHeaders() }, 15000)
  const text = await response.text()
  const data = safeParse(text)
  if (!response.ok) {
    throw new AiRequestError(describeFailure(response.status, data), response.status, text)
  }
  if (!data) {
    // 典型场景：静态服务器把 /ai-gateway/... 回落到了 index.html（SPA history fallback）
    throw new AiRequestError('接口返回的不是 JSON（该地址没有配置 AI 转发，可能被前端页面回退接管）', response.status, text)
  }
  const ids = Array.isArray(data.data) ? data.data.map(item => item && item.id).filter(Boolean) : []
  return { base, models: ids }
}

/** 解析并缓存可用端点；失败时抛错，不缓存失败结果 */
export const resolveEndpoint = async () => {
  if (resolvedBase) return { base: resolvedBase, models: null, cached: true }
  if (resolving) return resolving
  resolving = (async () => {
    let lastError = null
    for (const base of candidateBases()) {
      try {
        const result = await probeBase(base)
        resolvedBase = result.base
        return { ...result, cached: false }
      } catch (error) {
        lastError = error instanceof AiRequestError
          ? error
          : new AiRequestError(describeNetworkError(error, base), 0, '')
      }
    }
    throw lastError || new AiRequestError('没有可用的 AI 接口地址', 0, '')
  })()
  try {
    return await resolving
  } finally {
    resolving = null
  }
}

/** 请求失败（或网络中断）后清掉缓存，下次自动重新选路 */
export const resetEndpoint = () => {
  resolvedBase = null
}

/** 连接测试：确认端点可达、密钥有效、目标模型存在 */
export const probeConnection = async () => {
  const result = await resolveEndpoint()
  const ids = result.models || []
  let models = ids
  if (!ids.length) {
    const again = await probeBase(result.base)
    models = again.models
  }
  return {
    ok: true,
    endpoint: result.base,
    models,
    modelAvailable: !models.length || models.indexOf(aiConfig.model) > -1
  }
}

/* ------------------------------------------------------------------ */
/* 调用                                                                */
/* ------------------------------------------------------------------ */

/** 把对话历史转换为 Responses 协议要求的 input 结构 */
const toResponsesInput = messages => (Array.isArray(messages) ? messages : [])
  .filter(item => item && item.role && typeof item.content === 'string' && item.content.trim())
  .map(item => ({
    role: item.role === 'assistant' ? 'assistant' : 'user',
    content: item.content
  }))

const buildPayload = messages => ({
  model: aiConfig.model,
  instructions: aiConfig.systemPrompt,
  input: toResponsesInput(messages),
  store: !aiConfig.disableResponseStorage
})

/** 非流式调用 */
const requestOnce = async (base, payload, externalSignal) => {
  const scope = createAbortScope(aiConfig.requestTimeoutMs, aiConfig.requestTimeoutMs, externalSignal)
  try {
    const response = await fetch(joinUrl(base, 'responses'), {
      method: 'POST',
      headers: buildHeaders(),
      body: JSON.stringify(payload),
      signal: scope.controller ? scope.controller.signal : undefined
    })
    const text = await response.text()
    if (!response.ok) {
      throw new AiRequestError(describeFailure(response.status, safeParse(text)), response.status, text)
    }
    const data = safeParse(text)
    if (!data) throw new AiRequestError('响应内容无法解析为 JSON', response.status, text)
    return extractOutputText(data)
  } catch (error) {
    throw normalizeAbort(error, externalSignal, aiConfig.requestTimeoutMs, scope.idleTimedOut)
  } finally {
    scope.cleanup()
  }
}

/** 流式调用，返回拼接后的完整文本 */
const requestStream = async (base, payload, onDelta, externalSignal) => {
  const scope = createAbortScope(aiConfig.idleTimeoutMs, aiConfig.requestTimeoutMs, externalSignal)
  let full = ''
  try {
    const response = await fetch(joinUrl(base, 'responses'), {
      method: 'POST',
      headers: buildHeaders(),
      body: JSON.stringify({ ...payload, stream: true }),
      signal: scope.controller ? scope.controller.signal : undefined
    })
    if (!response.ok) {
      const text = await response.text()
      throw new AiRequestError(describeFailure(response.status, safeParse(text)), response.status, text)
    }
    if (!response.body || typeof response.body.getReader !== 'function') {
      // 环境不支持流式读取，回退非流式
      scope.cleanup()
      return await requestOnce(base, { ...payload, stream: false }, externalSignal)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    // eslint-disable-next-line no-constant-condition
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      scope.touch()
      buffer += decoder.decode(value, { stream: true })
      const blocks = buffer.split('\n\n')
      buffer = blocks.pop() || ''
      for (const block of blocks) {
        for (const rawLine of block.split('\n')) {
          const line = rawLine.trim()
          if (!line || line.indexOf('data:') !== 0) continue
          const dataText = line.slice(5).trim()
          if (!dataText || dataText === '[DONE]') continue
          const event = safeParse(dataText)
          if (!event) continue
          if (event.type === 'response.output_text.delta' && typeof event.delta === 'string') {
            full += event.delta
            if (onDelta) onDelta(event.delta, full)
          } else if (event.type === 'response.completed' && !full) {
            const text = extractOutputText(event.response)
            if (text) {
              full = text
              if (onDelta) onDelta(text, full)
            }
          } else if (event.type === 'response.failed' || event.type === 'error') {
            const detail = event.response ? describeFailure(0, event.response) : (event.message || '模型返回失败')
            throw new AiRequestError(detail, 0, dataText)
          }
        }
      }
    }
    if (!full.trim()) {
      throw new AiRequestError('模型没有返回文本内容，请重试或更换模型', 0, '')
    }
    return full
  } catch (error) {
    throw normalizeAbort(error, externalSignal, aiConfig.idleTimeoutMs, scope.idleTimedOut)
  } finally {
    scope.cleanup()
  }
}

/**
 * 发起一次 AI 对话
 * @param {Array<{role:string, content:string}>} messages 对话历史（含最新一条用户消息）
 * @param {{ onDelta?: Function, signal?: Object, stream?: boolean }} options
 * @returns {Promise<string>} 助手回复全文
 */
export const chatCompletion = async (messages, options = {}) => {
  const { onDelta, signal } = options
  const useStream = options.stream !== undefined ? options.stream : aiConfig.stream
  const payload = buildPayload(messages)

  // 先解析可用端点（结果会缓存，不额外增加每次会话的开销）
  let base
  try {
    const resolved = await resolveEndpoint()
    base = resolved.base
  } catch (error) {
    throw error instanceof AiRequestError ? error : new AiRequestError(String(error && error.message) || 'AI 接口不可用', 0, '')
  }

  try {
    if (!useStream) return await requestOnce(base, { ...payload, stream: false }, signal)
    try {
      return await requestStream(base, payload, onDelta, signal)
    } catch (error) {
      if (!(error instanceof AiRequestError)) throw error
      // 用户主动停止 / 服务端明确报错 / 已经有流式内容产出时不重试
      if (error.detail === 'aborted' || error.status >= 400 || error.detail === 'timeout') throw error
      return await requestOnce(base, { ...payload, stream: false }, signal)
    }
  } catch (error) {
    // 端点层面的问题（网络中断、代理失效）→ 清缓存，下次自动重新选路
    if (error instanceof AiRequestError && !error.status) resetEndpoint()
    throw error
  }
}

export const aiRuntimeInfo = () => ({
  provider: aiConfig.modelProvider,
  model: aiConfig.model,
  baseUrl: aiConfig.baseUrl,
  wireApi: aiConfig.wireApi,
  hasKey: Boolean(aiConfig.apiKey)
})

export default { chatCompletion, probeConnection, resolveEndpoint, resetEndpoint, extractOutputText, aiRuntimeInfo, AiRequestError }
