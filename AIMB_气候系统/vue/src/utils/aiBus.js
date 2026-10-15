/**
 * AI 助手事件总线
 *
 * 页面（如 ENSO 预测分析与监测平台）通过该总线唤起全局 AI 助手，
 * 避免在每个页面重复挂载助手组件。
 */
import Vue from 'vue'

const bus = new Vue()

export const AI_EVENTS = {
  /** 打开 AI 对话（payload: { prompt?: string, autoSend?: boolean }） */
  OPEN_CHAT: 'enso-ai:open-chat',
  /** 打开报告解读面板（payload: { kind, title, subtitle, sections, context, source }） */
  OPEN_BRIEFING: 'enso-ai:open-briefing'
}

export const openAiChat = (payload = {}) => bus.$emit(AI_EVENTS.OPEN_CHAT, payload)

export const openAiBriefing = payload => bus.$emit(AI_EVENTS.OPEN_BRIEFING, payload)

export default bus
