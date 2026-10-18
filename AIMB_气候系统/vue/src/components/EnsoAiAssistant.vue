<template>
  <div class="enso-ai">
    <!-- ===== 悬浮 AI 助手图标（可拖动，全局可见） ===== -->
    <div
      class="ai-orb"
      :class="{ 'is-dragging': dragging, 'is-open': chatVisible || briefingVisible }"
      :style="{ left: orb.x + 'px', top: orb.y + 'px' }"
      role="button"
      tabindex="0"
      aria-label="ENSO AI 助手（可拖动，点击开启对话）"
      @mousedown="onDragStart"
      @touchstart.passive="onDragStart"
      @keydown.enter.prevent="toggleChat"
      @keydown.space.prevent="toggleChat"
    >
      <span class="orb-halo"></span>
      <span class="orb-ring"></span>
      <svg class="orb-svg" viewBox="0 0 64 64" aria-hidden="true">
        <defs>
          <radialGradient id="ensoAiOrbCore" cx="36%" cy="26%" r="80%">
            <stop offset="0%" stop-color="#8ff6e6" />
            <stop offset="45%" stop-color="#2fb8a5" />
            <stop offset="100%" stop-color="#0d5a63" />
          </radialGradient>
          <linearGradient id="ensoAiWave" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#ffe9a8" />
            <stop offset="55%" stop-color="#ffb86b" />
            <stop offset="100%" stop-color="#ff7a59" />
          </linearGradient>
        </defs>
        <circle cx="32" cy="32" r="30" fill="url(#ensoAiOrbCore)" />
        <circle cx="32" cy="32" r="29.2" fill="none" stroke="rgba(219,255,247,.5)" stroke-width="1.3" />
        <path d="M6.5 25.5C15 19.6 24 30.6 33 25.1 42 19.6 50 29.6 57.5 24.2" fill="none" stroke="rgba(224,255,251,.45)" stroke-width="1.5" stroke-linecap="round" />
        <path d="M5.5 39.2C14.5 33.4 23.5 44.6 32.5 38.4 41.5 32.2 50 42.4 58.5 37" fill="none" stroke="url(#ensoAiWave)" stroke-width="2.6" stroke-linecap="round" />
        <ellipse cx="45" cy="33" rx="7.4" ry="11" fill="rgba(255,186,110,.28)" />
        <ellipse cx="19" cy="31" rx="6.6" ry="10" fill="rgba(120,214,255,.22)" />
        <path d="M47.5 10.5l1.9 4.7 4.7 1.9-4.7 1.9-1.9 4.7-1.9-4.7-4.7-1.9 4.7-1.9z" fill="#fff8e2" />
      </svg>
      <span class="orb-tip">ENSO AI 助手</span>
    </div>

    <!-- ===== AI 对话弹片：锚定悬浮球旁边，无遮罩、不模糊页面 ===== -->
    <transition name="ai-pop">
      <section
        v-if="chatVisible"
        class="ai-pop ai-pop-chat"
        :class="popLayout.side"
        :style="popLayout.style"
        role="dialog"
        aria-label="ENSO AI 对话"
      >
        <span class="pop-caret" :style="popLayout.caretStyle"></span>
        <header class="ai-panel-head">
          <span class="head-mark"><i class="el-icon-cpu"></i></span>
          <div class="head-text">
            <strong>ENSO AI 助手</strong>
            <small>{{ runtime.provider }} · {{ runtime.model }} · {{ runtime.wireApi }} 协议</small>
          </div>
          <span class="conn-chip" :class="connState.tone" :title="connDetail || '点击气泡可重新检测'"><i></i>{{ connState.label }}</span>
          <button type="button" class="icon-btn" aria-label="关闭对话" @click="chatVisible = false"><i class="el-icon-close"></i></button>
        </header>

        <div ref="messageList" class="ai-messages">
          <article v-for="message in messages" :key="message.id" class="msg" :class="[message.role, { 'is-error': message.error }]">
            <span v-if="message.role === 'assistant'" class="msg-avatar"><i class="el-icon-cpu"></i></span>
            <div class="msg-body">
              <template v-if="message.content || !message.pending">
                <div
                  v-if="message.role === 'assistant'"
                  class="md-body"
                  :class="{ 'is-streaming': message.pending }"
                  v-html="renderMd(message.content)"
                ></div>
                <div v-else class="md-body is-plain">{{ message.content }}</div>
              </template>
              <p v-else class="msg-thinking">
                <span class="think-dots"><i></i><i></i><i></i></span>正在思考… {{ thinkSeconds }}s
              </p>
              <small v-if="message.error" class="msg-error"><i class="el-icon-warning-outline"></i>{{ message.errorText || '生成失败，请重试' }}</small>
            </div>
          </article>
          <div v-if="messages.length <= 1" class="quick-prompts">
            <span class="quick-title">可以这样问</span>
            <button v-for="prompt in quickPrompts" :key="prompt" type="button" @click="send(prompt)">{{ prompt }}</button>
          </div>
        </div>

        <footer class="ai-composer">
          <textarea
            v-model="draft"
            class="composer-input"
            rows="2"
            placeholder="询问 ENSO 位相、Niño3.4 / ONI、LSTA-Swin 预测技巧或区域影响…（Enter 发送 / Shift+Enter 换行）"
            @keydown.enter.exact.prevent="send()"
          ></textarea>
          <div class="composer-actions">
            <button type="button" class="ghost-btn" :disabled="sending" @click="resetChat">清空对话</button>
            <button v-if="sending" type="button" class="ghost-btn danger" @click="stopGenerating">停止生成</button>
            <button type="button" class="send-btn" :disabled="sending || !draft.trim()" @click="send()">
              <i :class="sending ? 'el-icon-loading' : 'el-icon-position'"></i>{{ sending ? '生成中' : '发送' }}
            </button>
          </div>
        </footer>
      </section>
    </transition>

    <!-- ===== 事件 / 报告解读弹片：消息提醒式，锚定悬浮球，内容流式展示 ===== -->
    <transition name="ai-pop">
      <section
        v-if="briefingVisible"
        class="ai-pop ai-pop-briefing"
        :class="popLayout.side"
        :style="popLayout.style"
        role="dialog"
        :aria-label="briefingHead.title"
      >
        <span class="pop-caret" :style="popLayout.caretStyle"></span>
        <header class="ai-panel-head">
          <span class="head-mark gold"><i :class="briefing.kind === 'model' ? 'el-icon-document' : 'el-icon-time'"></i></span>
          <div class="head-text">
            <strong>{{ briefingHead.title }}</strong>
            <small>{{ briefingHead.subtitle }}</small>
          </div>
          <span class="conn-chip" :class="streaming ? 'checking' : 'ok'"><i></i>{{ streaming ? '解读生成中' : '解读完成' }}</span>
          <button type="button" class="icon-btn" aria-label="关闭报告解读" @click="closeBriefing"><i class="el-icon-close"></i></button>
        </header>

        <div class="briefing-body">
          <!-- 消息提醒式流式气泡：先逐字吐出要点，吐完再展开明细 -->
          <div class="stream-bubble">
            <span class="stream-mark"><i class="el-icon-cpu"></i></span>
            <p class="stream-text">{{ streamedText }}<span v-if="streaming" class="typing-caret">▍</span></p>
            <span class="stream-progress"><span :style="{ width: streamPercent + '%' }"></span></span>
          </div>

          <div v-if="streamDone" class="briefing-detail">
          <!-- ---------- ENSO 历史事件 ---------- -->
          <template v-if="briefing.kind === 'event'">
            <div class="event-hero">
              <span class="event-type" :class="isCoolEvent ? 'cool' : 'warm'">{{ briefing.event.type }}</span>
              <h2>{{ briefing.event.label }}</h2>
              <p class="event-range">
                <i class="el-icon-date"></i> {{ briefing.event.start }} 至 {{ briefing.event.end || '—' }}
                <span class="dot">·</span>峰值 {{ briefing.event.peak || '—' }}
              </p>
            </div>

            <div class="briefing-facts">
              <div><span>事件类型</span><strong>{{ briefing.event.type }}</strong></div>
              <div><span>发生时段</span><strong>{{ briefing.event.start }} – {{ briefing.event.end || '—' }}</strong></div>
              <div><span>事件峰值</span><strong>{{ briefing.event.peak || '—' }}</strong></div>
              <div><span>时间轴位置</span><strong>{{ briefing.timeline.month }}</strong></div>
            </div>

            <section class="briefing-block">
              <h3><i class="el-icon-info"></i> 事件演变过程</h3>
              <p>{{ briefing.event.phase && briefing.event.phase !== '—' ? briefing.event.phase : '该事件暂无绑定的演变过程描述。' }}</p>
            </section>

            <section class="briefing-block">
              <h3><i class="el-icon-document"></i> 事件详细内容</h3>
              <p>{{ briefing.event.description || '该事件暂无详细描述。' }}</p>
            </section>

            <section class="briefing-block">
              <h3><i class="el-icon-data-line"></i> 当前时间轴状态</h3>
              <p v-if="briefing.timeline.hasData">
                {{ briefing.timeline.month }} 的 Niño3.4 指数为 <b>{{ briefing.timeline.value }}</b> ℃，
                按 ±0.5 ℃ 阈值判定处于「{{ briefing.timeline.phase }}」；时间游标位于事件序列第
                {{ briefing.timeline.cursor }} 帧（共 {{ briefing.timeline.total }} 帧），空间场显示范围为
                {{ briefing.timeline.range }}。
              </p>
              <p v-else>该事件仅列入 NOAA CPC ONI v6 历史事件目录，未绑定本地月度序列与空间场，因此时间轴无法展示逐月演变。</p>
            </section>

            <section class="briefing-block subtle">
              <h3><i class="el-icon-collection-tag"></i> 数据来源</h3>
              <p>{{ briefing.event.source || 'NOAA CPC ONI v6 历史事件目录' }}</p>
            </section>
          </template>

          <!-- ---------- LSTA-Swin 历史预测 ---------- -->
          <template v-else>
            <div class="event-hero model">
              <span class="event-type model-type">LSTA-Swin 历史预测</span>
              <h2>{{ briefingMonthLabel }} 起报 · 预置报告</h2>
              <p class="event-range">
                <i class="el-icon-folder-opened"></i> 已预生成 {{ reportMonthsLabel }}
                <span class="dot">·</span>当前时间轴 {{ briefing.timeline.month || '—' }}（{{ briefing.timeline.value || '—' }} ℃ · {{ briefing.timeline.phase || '—' }}）
              </p>
            </div>

            <template v-if="briefing.available">
              <div class="briefing-tabs">
                <button type="button" :class="{ active: briefingTab === 'impact' }" @click="briefingTab = 'impact'">
                  <i class="el-icon-data-analysis"></i>ENSO 影响研判技术报告
                </button>
                <button type="button" :class="{ active: briefingTab === 'validation' }" @click="briefingTab = 'validation'">
                  <i class="el-icon-aim"></i>提前期验证数据分析报告
                </button>
              </div>

              <!-- ① 影响研判技术报告 -->
              <template v-if="briefingTab === 'impact' && briefing.impact">
                <div class="report-title-row">
                  <div>
                    <p class="report-kicker">{{ briefing.impact.id }}</p>
                    <h3>{{ briefing.impact.title }}</h3>
                    <p class="report-meta">预测展望 {{ briefing.impact.horizon }} <span>·</span> 生成时间 {{ briefing.impact.generatedAt }}</p>
                  </div>
                  <span class="report-phase">{{ briefing.impact.phase }} · {{ briefing.impact.phaseValue }}</span>
                </div>

                <div class="thesis"><span>研判摘要</span><p>{{ briefing.impact.summary }}</p></div>

                <div class="indicator-grid">
                  <div v-for="item in briefing.impact.indicators" :key="item.label" class="indicator" :class="item.tone">
                    <span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.detail }}</small>
                  </div>
                </div>

                <div class="chip-row">
                  <span v-for="item in briefing.impact.highlights" :key="item.label" class="chip" :class="item.tone"><b>{{ item.label }}</b>{{ item.value }}</span>
                </div>

                <section class="briefing-block">
                  <h3><i class="el-icon-map-location"></i> 中国区域气候风险研判</h3>
                  <div class="risk-table-wrap">
                    <table class="risk-table">
                      <thead><tr><th>区域</th><th>降水倾向</th><th>温度倾向</th><th>主要风险</th><th>等级</th></tr></thead>
                      <tbody>
                        <tr v-for="row in briefing.impact.regionalRisks" :key="row.region">
                          <td class="region">{{ row.region }}</td>
                          <td>{{ row.precipitation }}</td>
                          <td>{{ row.temperature }}</td>
                          <td>{{ row.risk }}</td>
                          <td><span class="level" :class="row.levelTone">{{ row.level }}</span></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </section>

                <section class="briefing-block">
                  <h3><i class="el-icon-sugar"></i> 农业与产业影响</h3>
                  <ul class="bullet-list"><li v-for="(item, index) in briefing.impact.agriculture" :key="index">{{ item }}</li></ul>
                </section>

                <section class="briefing-block">
                  <h3><i class="el-icon-s-opportunity"></i> 建议关注与行动</h3>
                  <div v-for="action in briefing.impact.actions" :key="action.tag" class="action-row">
                    <b>{{ action.tag }}</b>
                    <div><strong>{{ action.title }}</strong><p>{{ action.text }}</p></div>
                  </div>
                </section>

                <div class="uncertainty">
                  <i class="el-icon-warning-outline"></i>
                  <div><b>不确定性与更新条件</b><p>{{ briefing.impact.uncertainty }}</p></div>
                </div>

                <p class="report-footnote">研判置信度：{{ briefing.impact.confidence }} · 分析框架：ENSO 位相识别 · 遥相关诊断 · 区域风险映射 · 农业暴露评估</p>
              </template>

              <!-- ② 提前期验证数据分析报告 -->
              <template v-else-if="briefing.validation">
                <div class="report-title-row">
                  <div>
                    <p class="report-kicker">{{ briefing.validation.id }} · HINDCAST DIAGNOSTICS</p>
                    <h3>{{ briefing.validation.title }}</h3>
                    <p class="report-meta">{{ briefing.month }} 起报 · Niño3.4 月指数逐提前期回报诊断 · {{ briefing.validation.metrics.count }} 个有效配对目标月</p>
                  </div>
                </div>

                <div class="metric-grid">
                  <div class="metric"><span>案例 MAE</span><strong>{{ briefing.validation.metrics.mae }}<em>℃</em></strong><small>单次历史轨迹</small></div>
                  <div class="metric"><span>案例 RMSE</span><strong>{{ briefing.validation.metrics.rmse }}<em>℃</em></strong><small>强调大误差惩罚</small></div>
                  <div class="metric"><span>平均偏差</span><strong :class="briefing.validation.metrics.biasTone">{{ briefing.validation.metrics.bias }}<em>℃</em></strong><small>{{ briefing.validation.metrics.biasNote }}</small></div>
                  <div class="metric"><span>三档状态命中率</span><strong>{{ briefing.validation.metrics.stateAccuracy }}<em>%</em></strong><small>{{ briefing.validation.metrics.stateHits }} / {{ briefing.validation.metrics.count }} 目标月</small></div>
                  <div class="metric"><span>总体验证 MAE</span><strong>{{ briefing.validation.metrics.aggregateMae }}<em>℃</em></strong><small>相同提前期汇总基准</small></div>
                </div>

                <div class="thesis"><span>研判摘要</span><p>{{ briefing.validation.summary }}</p></div>

                <section class="briefing-block">
                  <h3><i class="el-icon-data-line"></i> 关键发现</h3>
                  <div v-for="(item, index) in briefing.validation.findings" :key="'f' + index" class="numbered-row"><span>0{{ index + 1 }}</span><p>{{ item }}</p></div>
                </section>

                <section class="briefing-block">
                  <h3><i class="el-icon-warning-outline"></i> 误差成因诊断</h3>
                  <div v-for="(item, index) in briefing.validation.causes" :key="'c' + index" class="numbered-row plain"><span><i class="el-icon-arrow-right"></i></span><p>{{ item }}</p></div>
                </section>

                <section class="briefing-block">
                  <h3><i class="el-icon-s-opportunity"></i> 模型与数据改进建议</h3>
                  <div v-for="(item, index) in briefing.validation.recommendations" :key="'r' + index" class="reco-row"><b>{{ item.title }}</b><p>{{ item.detail }}</p></div>
                </section>

                <div class="verdict"><span>评估结论</span><strong>{{ briefing.validation.verdict }}</strong><p>{{ briefing.validation.nextStep }}</p></div>
                <p class="report-footnote">命中率按单月 Niño3.4 距平分为冷侧（≤−0.5℃）、中性（>−0.5℃且<+0.5℃）和暖侧（≥+0.5℃）三档，仅表示本历史轨迹的状态分档一致率，不等同于官方 ONI 事件准确率。</p>
              </template>
            </template>

            <!-- 无预置报告 -->
            <div v-else class="empty-report">
              <i class="el-icon-folder-delete"></i>
              <h3>{{ briefingMonthLabel }} 起报暂无预置报告</h3>
              <p>当前平台仅预生成了 <b>{{ reportMonthsLabel }}</b> 两个起报月份的《ENSO 影响研判技术报告》与《提前期验证数据分析报告》。切换到这两个月份即可查看完整报告内容，也可以让 AI 助手基于当前起报轨迹给出临时解读。</p>
            </div>
          </template>
          </div>
        </div>

        <footer class="briefing-foot">
          <button type="button" class="ghost-btn" @click="closeBriefing">关闭</button>
          <button type="button" class="send-btn" @click="askAiAboutBriefing">
            <i class="el-icon-magic-stick"></i>让 AI 深入解读
          </button>
        </footer>
      </section>
    </transition>
  </div>
</template>

<script>
import bus, { AI_EVENTS } from '@/utils/aiBus'
import { chatCompletion, probeConnection, aiRuntimeInfo, AiRequestError } from '@/utils/aiClient'
import { renderAiMarkdown } from '@/utils/aiMarkdown'
import aiConfig from '@/config/ai.config'
import { REPORT_MONTHS } from '@/data/ensoReports'

const STORAGE_KEY = 'enso-ai-orb-position'
const ORB_SIZE = 62
const EDGE = 6
const DRAG_THRESHOLD = 4

export default {
  name: 'EnsoAiAssistant',
  data() {
    return {
      orb: { x: 0, y: 0 },
      dragging: false,
      dragMoved: false,
      dragStart: { x: 0, y: 0 },
      dragOrigin: { x: 0, y: 0 },
      chatVisible: false,
      briefingVisible: false,
      briefing: { kind: 'event', event: {}, timeline: {}, available: false },
      briefingTab: 'impact',
      // 消息提醒式流式展示
      briefingText: '',
      streamedText: '',
      streaming: false,
      streamDone: false,
      streamTimer: null,
      viewport: { w: window.innerWidth || 1280, h: window.innerHeight || 800 },
      draft: '',
      sending: false,
      thinkSeconds: 0,
      thinkTimer: null,
      abortController: null,
      runtime: aiRuntimeInfo(),
      connState: { label: '未检测', tone: 'idle' },
      connDetail: '',
      messages: []
    }
  },
  computed: {
    quickPrompts() {
      return aiConfig.quickPrompts
    },
    /** 弹片尺寸与锚定位置：始终贴在悬浮球旁边，并保证不越出视口 */
    popLayout() {
      const cfg = aiConfig.popover || {}
      const gap = cfg.gap || 14
      const vw = Math.max(this.viewport.w || 1280, 320)
      const vh = Math.max(this.viewport.h || 800, 360)
      const wantWidth = this.briefingVisible ? (cfg.briefingWidth || 392) : (cfg.chatWidth || 330)
      const wantHeight = this.briefingVisible ? (cfg.briefingHeight || 480) : (cfg.chatHeight || 380)
      const width = Math.min(wantWidth, vw - 24)
      const height = Math.min(wantHeight, vh - 24)

      // 优先出现在悬浮球右侧；右侧放不下就翻到左侧
      let side = 'right'
      let left = this.orb.x + ORB_SIZE + gap
      if (left + width > vw - 12) {
        side = 'left'
        left = this.orb.x - width - gap
      }
      left = Math.max(12, Math.min(left, Math.max(12, vw - width - 12)))

      let top = this.orb.y + ORB_SIZE / 2 - height / 2
      top = Math.max(12, Math.min(top, Math.max(12, vh - height - 12)))

      const orbCenter = this.orb.y + ORB_SIZE / 2
      const caretTop = Math.max(20, Math.min(orbCenter - top, height - 20))

      return {
        side,
        style: {
          left: `${Math.round(left)}px`,
          top: `${Math.round(top)}px`,
          width: `${Math.round(width)}px`,
          height: `${Math.round(height)}px`
        },
        caretStyle: { top: `${Math.round(caretTop)}px` }
      }
    },
    streamPercent() {
      if (!this.briefingText) return 0
      return Math.min(100, Math.round(this.streamedText.length / this.briefingText.length * 100))
    },
    briefingHead() {
      if (this.briefing.kind === 'model') {
        return {
          title: 'LSTA-Swin 历史预测 · 报告解读',
          subtitle: `${this.briefingMonthLabel} 起报 · ${REPORT_MONTHS.length} 个预置月份`
        }
      }
      const event = this.briefing.event || {}
      return {
        title: 'ENSO 历史事件 · AI 解读',
        subtitle: `${event.label || '历史事件'} · 演变时间轴联动`
      }
    },
    briefingMonthLabel() {
      const month = this.briefing.month
      if (!month) return '—'
      const [year, value] = String(month).split('-')
      return `${year} 年 ${Number(value)} 月`
    },
    reportMonthsLabel() {
      return REPORT_MONTHS
        .map(month => `${month.slice(0, 4)} 年 ${Number(month.slice(5, 7))} 月`)
        .join('、')
    },
    isCoolEvent() {
      const type = (this.briefing.event && this.briefing.event.type) || ''
      return type.indexOf('拉尼娜') > -1
    }
  },
  created() {
    bus.$on(AI_EVENTS.OPEN_CHAT, this.handleOpenChat)
    bus.$on(AI_EVENTS.OPEN_BRIEFING, this.handleOpenBriefing)
  },
  mounted() {
    this.restoreOrbPosition()
    window.addEventListener('resize', this.clampOrb)
    document.addEventListener('mousedown', this.onDocumentMouseDown, true)
    document.addEventListener('keydown', this.onDocumentKeyDown)
    this.resetChat()
  },
  beforeDestroy() {
    bus.$off(AI_EVENTS.OPEN_CHAT, this.handleOpenChat)
    bus.$off(AI_EVENTS.OPEN_BRIEFING, this.handleOpenBriefing)
    window.removeEventListener('resize', this.clampOrb)
    document.removeEventListener('mousedown', this.onDocumentMouseDown, true)
    document.removeEventListener('keydown', this.onDocumentKeyDown)
    this.clearStreamTimer()
    this.clearThinkTimer()
    this.detachDragListeners()
    this.stopGenerating()
  },
  methods: {
    /** 助手回复按 Markdown + LaTeX 渲染 */
    renderMd(text) {
      return renderAiMarkdown(text)
    },
    /* ---------------- 悬浮球：位置与拖动 ---------------- */
    defaultPosition() {
      const width = window.innerWidth || 1280
      const height = window.innerHeight || 800
      return { x: width - ORB_SIZE - 28, y: height - ORB_SIZE - 96 }
    },
    restoreOrbPosition() {
      let saved = null
      try {
        saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
      } catch (error) {
        saved = null
      }
      const valid = saved && Number.isFinite(saved.x) && Number.isFinite(saved.y)
      this.orb = valid ? { x: saved.x, y: saved.y } : this.defaultPosition()
      this.clampOrb()
    },
    clampOrb() {
      const width = window.innerWidth || 1280
      const height = window.innerHeight || 800
      this.viewport = { w: width, h: height }
      this.orb = {
        x: Math.min(Math.max(this.orb.x, EDGE), Math.max(EDGE, width - ORB_SIZE - EDGE)),
        y: Math.min(Math.max(this.orb.y, EDGE), Math.max(EDGE, height - ORB_SIZE - EDGE))
      }
    },
    persistOrb() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.orb))
      } catch (error) {
        /* 忽略隐私模式下的写入失败 */
      }
    },
    onDragStart(event) {
      const point = this.readPoint(event)
      if (!point) return
      this.dragging = true
      this.dragMoved = false
      this.dragStart = point
      this.dragOrigin = { x: this.orb.x, y: this.orb.y }
      this.detachDragListeners()
      window.addEventListener('mousemove', this.onDragMove)
      window.addEventListener('mouseup', this.onDragEnd)
      window.addEventListener('touchmove', this.onDragMove, { passive: false })
      window.addEventListener('touchend', this.onDragEnd)
    },
    onDragMove(event) {
      if (!this.dragging) return
      const point = this.readPoint(event)
      if (!point) return
      const dx = point.x - this.dragStart.x
      const dy = point.y - this.dragStart.y
      if (!this.dragMoved && Math.abs(dx) + Math.abs(dy) > DRAG_THRESHOLD) this.dragMoved = true
      if (!this.dragMoved) return
      if (event.cancelable) event.preventDefault()
      this.orb = { x: this.dragOrigin.x + dx, y: this.dragOrigin.y + dy }
      this.clampOrb()
    },
    onDragEnd() {
      if (!this.dragging) return
      this.dragging = false
      this.detachDragListeners()
      if (this.dragMoved) {
        this.persistOrb()
      } else {
        this.toggleChat()
      }
      this.dragMoved = false
    },
    detachDragListeners() {
      window.removeEventListener('mousemove', this.onDragMove)
      window.removeEventListener('mouseup', this.onDragEnd)
      window.removeEventListener('touchmove', this.onDragMove)
      window.removeEventListener('touchend', this.onDragEnd)
    },
    readPoint(event) {
      if (!event) return null
      const source = event.touches && event.touches.length
        ? event.touches[0]
        : (event.changedTouches && event.changedTouches.length ? event.changedTouches[0] : event)
      if (!source || !Number.isFinite(source.clientX)) return null
      return { x: source.clientX, y: source.clientY }
    },

    /* ---------------- 对话 ---------------- */
    resetChat() {
      this.clearThinkTimer()
      this.messages = [{
        id: `welcome-${Date.now()}`,
        role: 'assistant',
        content: aiConfig.welcomeMessage,
        pending: false,
        error: false
      }]
      this.$nextTick(this.scrollToBottom)
    },
    toggleChat() {
      this.chatVisible = !this.chatVisible
      if (this.chatVisible) {
        this.closeBriefing()
        this.$nextTick(this.scrollToBottom)
        this.ensureConnection()
      }
    },
    handleOpenChat(payload = {}) {
      this.chatVisible = true
      this.closeBriefing()
      this.$nextTick(() => {
        this.scrollToBottom()
        if (payload.prompt) this.send(payload.prompt)
      })
      this.ensureConnection()
    },
    /** 点击弹片与悬浮球之外的区域即收起（消息提醒的交互习惯） */
    onDocumentMouseDown(event) {
      const target = event.target
      if (!target || typeof target.closest !== 'function') return
      if (target.closest('.ai-pop') || target.closest('.ai-orb')) return
      if (this.chatVisible) this.chatVisible = false
      if (this.briefingVisible) this.closeBriefing()
    },
    onDocumentKeyDown(event) {
      if (event.key !== 'Escape') return
      if (this.chatVisible) this.chatVisible = false
      if (this.briefingVisible) this.closeBriefing()
    },
    async ensureConnection(force) {
      if (!force && (this.connState.tone === 'checking' || this.connState.tone === 'ok')) return
      this.connState = { label: '检测中', tone: 'checking' }
      this.connDetail = ''
      try {
        const result = await probeConnection()
        this.connDetail = `端点：${result.endpoint || aiConfig.baseUrl}\n模型：${aiConfig.model}`
        this.connState = result.modelAvailable
          ? { label: '接口可用', tone: 'ok' }
          : { label: `未发现模型 ${aiConfig.model}`, tone: 'warn' }
      } catch (error) {
        this.connDetail = this.describeError(error)
        this.connState = { label: '接口不可用', tone: 'error' }
      }
    },
    scrollToBottom() {
      const list = this.$refs.messageList
      if (list) list.scrollTop = list.scrollHeight
    },
    async send(preset) {
      const content = String(preset === undefined || preset === null ? this.draft : preset).trim()
      if (!content || this.sending) return
      this.draft = ''
      this.messages.push({ id: `u-${Date.now()}`, role: 'user', content, pending: false, error: false })

      const history = this.messages
        .filter(message => !message.error)
        .slice(-aiConfig.maxContextMessages)
        .map(message => ({ role: message.role, content: message.content }))

      const reply = { id: `a-${Date.now()}`, role: 'assistant', content: '', pending: true, error: false }
      this.messages.push(reply)
      this.sending = true
      this.startThinkTimer()
      this.connState = { label: '生成中', tone: 'checking' }
      this.$nextTick(this.scrollToBottom)

      this.abortController = typeof AbortController === 'function' ? new AbortController() : null

      try {
        const text = await chatCompletion(history, {
          signal: this.abortController ? this.abortController.signal : undefined,
          onDelta: (delta, full) => {
            reply.content = full
            this.clearThinkTimer()
            this.$nextTick(this.scrollToBottom)
          }
        })
        reply.content = text
        this.connState = { label: '接口可用', tone: 'ok' }
      } catch (error) {
        if (error instanceof AiRequestError && error.detail === 'aborted') {
          reply.content = reply.content || '（已停止生成）'
        } else {
          reply.error = true
          reply.content = reply.content || '抱歉，本次请求未能完成。'
          reply.errorText = this.describeError(error)
          // 区分「超时」与「接口不可用」：超时通常是网关长时间无响应，不代表接口坏了
          const timedOut = error instanceof AiRequestError && error.detail === 'timeout'
          this.connState = timedOut
            ? { label: '请求超时', tone: 'error' }
            : { label: '接口不可用', tone: 'error' }
        }
      } finally {
        this.clearThinkTimer()
        reply.pending = false
        this.sending = false
        this.abortController = null
        this.$nextTick(this.scrollToBottom)
      }
    },
    startThinkTimer() {
      this.clearThinkTimer()
      this.thinkSeconds = 0
      this.thinkTimer = setInterval(() => { this.thinkSeconds += 1 }, 1000)
    },
    clearThinkTimer() {
      if (this.thinkTimer) {
        clearInterval(this.thinkTimer)
        this.thinkTimer = null
      }
    },
    describeError(error) {
      if (error instanceof AiRequestError) {
        return error.status ? `${error.message}（HTTP ${error.status}）` : error.message
      }
      if (error && error.message) return error.message
      return '未知错误'
    },
    stopGenerating() {
      if (this.abortController) {
        this.abortController.abort()
        this.abortController = null
      }
      this.clearThinkTimer()
      this.sending = false
    },

    /* ---------------- 报告解读（消息提醒式 · 流式） ---------------- */
    handleOpenBriefing(payload) {
      if (!payload) return
      this.briefing = {
        kind: payload.kind || 'event',
        event: payload.event || {},
        month: payload.month || '',
        available: Boolean(payload.available),
        impact: payload.impact || null,
        validation: payload.validation || null,
        timeline: payload.timeline || {}
      }
      this.briefingTab = 'impact'
      this.chatVisible = false
      this.briefingVisible = true
      // 先把整理好的要点逐字吐出来，吐完再展开明细
      this.$nextTick(() => this.startStreaming(this.buildBriefingText(this.briefing)))
    },
    closeBriefing() {
      this.briefingVisible = false
      this.clearStreamTimer()
      this.streaming = false
    },
    clearStreamTimer() {
      if (this.streamTimer) {
        clearInterval(this.streamTimer)
        this.streamTimer = null
      }
    },
    startStreaming(text) {
      this.clearStreamTimer()
      this.briefingText = String(text || '')
      this.streamedText = ''
      this.streamDone = false
      const total = this.briefingText.length
      if (!total) {
        this.streaming = false
        this.streamDone = true
        return
      }
      this.streaming = true
      const ticks = 120 // 约 120 * 16ms ≈ 1.9s 吐完整段
      const step = Math.max(1, Math.ceil(total / ticks))
      let index = 0
      this.streamTimer = setInterval(() => {
        index = Math.min(index + step, total)
        this.streamedText = this.briefingText.slice(0, index)
        if (index >= total) {
          this.clearStreamTimer()
          this.streaming = false
          this.streamDone = true
        }
      }, 16)
    },
    /** 把事件 / 报告数据整理成一段可直接阅读的要点文本 */
    buildBriefingText(payload) {
      const timeline = payload.timeline || {}
      const timelineLine = `当前时间轴：${timeline.month || '—'}（Niño3.4 ${timeline.value || '—'} ℃ · ${timeline.phase || '—'}）`

      if (payload.kind === 'event') {
        const event = payload.event || {}
        const lines = [
          `【ENSO 历史事件 · AI 解读】${event.label || '未知事件'}`,
          `事件类型：${event.type || '—'}｜发生时段：${event.start || '—'} ~ ${event.end || '—'}｜事件峰值：${event.peak || '—'}`,
          timelineLine,
          `演变过程：${event.phase && event.phase !== '—' ? event.phase : '该事件暂无绑定的逐月演变记录。'}`,
          `事件详情：${event.description || '该事件暂无详细描述。'}`
        ]
        if (!timeline.hasData) {
          lines.push('提示：该事件仅列入 NOAA CPC ONI v6 历史事件目录，未绑定本地月度序列与空间场，因此时间轴暂无法逐月展示。')
        }
        lines.push(`数据来源：${event.source || 'NOAA CPC ONI v6 历史事件目录'}`)
        return lines.join('\n')
      }

      const monthLabel = this.briefingMonthLabel
      if (!payload.available) {
        return [
          `【LSTA-Swin 历史预测 · ${monthLabel} 起报】暂无预置报告`,
          `平台目前仅预生成了 ${this.reportMonthsLabel} 两个起报月份的《ENSO 影响研判技术报告》与《提前期验证数据分析报告》。`,
          timelineLine,
          '切换到上述两个起报月份即可查看完整报告；也可以点击下方「让 AI 深入解读」，由模型基于当前起报轨迹临时组织解读框架。'
        ].join('\n')
      }

      const impact = payload.impact || {}
      const validation = payload.validation || {}
      const metrics = validation.metrics || {}
      return [
        `【LSTA-Swin 历史预测 · ${monthLabel} 起报】已载入预置报告。`,
        `① ENSO 影响研判技术报告（${impact.id || '—'}）：位相 ${impact.phase || '—'}（${impact.phaseValue || '—'}）｜预测展望 ${impact.horizon || '—'}｜置信度 ${impact.confidence || '—'}。${impact.summary || ''}`,
        `② 提前期验证数据分析报告（${validation.id || '—'}）：案例 MAE ${metrics.mae || '—'} ℃｜RMSE ${metrics.rmse || '—'} ℃｜平均偏差 ${metrics.bias || '—'} ℃｜三档状态命中率 ${metrics.stateAccuracy || '—'}%（${metrics.stateHits || '—'}/${metrics.count || '—'}）。评估结论：${validation.verdict || '—'}`,
        `${timelineLine}，完整指标、区域风险表与改进建议见下方明细。`
      ].join('\n')
    },
    askAiAboutBriefing() {
      const prompt = this.briefing.kind === 'model'
        ? this.modelBriefingPrompt()
        : this.eventBriefingPrompt()
      this.closeBriefing()
      this.handleOpenChat({ prompt })
    },
    eventBriefingPrompt() {
      const event = this.briefing.event || {}
      return [
        `请解读 ENSO 历史事件：${event.label || '未知事件'}。`,
        `事件类型：${event.type || '—'}；发生时段：${event.start || '—'} 至 ${event.end || '—'}；事件峰值：${event.peak || '—'}。`,
        `演变过程：${event.phase && event.phase !== '—' ? event.phase : '暂无记录'}。`,
        `事件描述：${event.description || '暂无描述'}。`,
        `当前时间轴位置：${this.briefing.timeline.month || '—'}，对应 Niño3.4 指数 ${this.briefing.timeline.value || '—'} ℃。`,
        '请分三部分回答：①该事件的物理机制与演变特征；②它对全球及中国气候遥相关的影响；③与同类事件的差异及可迁移的预测启示。'
      ].join('\n')
    },
    modelBriefingPrompt() {
      if (!this.briefing.available) {
        return `LSTA-Swin 在 ${this.briefingMonthLabel} 起报的历史预测尚无可用的预置报告（当前仅有 ${this.reportMonthsLabel}）。请说明在缺少预置报告的情况下，应如何从该起报月份的预测轨迹、提前期技巧衰减和 ENSO 位相出发，组织一份影响研判与验证分析的框架。`
      }
      const impact = this.briefing.impact
      const validation = this.briefing.validation
      return [
        `请对 LSTA-Swin 在 ${this.briefingMonthLabel} 起报的两份预置报告做深入解读。`,
        impact ? `【ENSO 影响研判技术报告】摘要：${impact.summary}；预测展望 ${impact.horizon}；位相 ${impact.phase}（${impact.phaseValue}）；置信度 ${impact.confidence}。` : '',
        validation ? `【提前期验证数据分析报告】摘要：${validation.summary}；案例 MAE ${validation.metrics.mae} ℃、RMSE ${validation.metrics.rmse} ℃、平均偏差 ${validation.metrics.bias} ℃、三档状态命中率 ${validation.metrics.stateAccuracy}%；评估结论：${validation.verdict}。` : '',
        '请回答：①两份报告结论是否一致，是否存在相互矛盾之处；②造成误差的主要物理与模型原因；③给业务使用者的四条可执行建议。'
      ].filter(Boolean).join('\n')
    }
  }
}
</script>

<style scoped>
/* ================= 悬浮图标 ================= */
.enso-ai{--ai-teal:#3ad0b8;--ai-gold:#e4b75c;--ai-panel:#0f1e25;--ai-line:rgba(94,158,168,.28);font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}
.ai-orb{position:fixed;z-index:3200;width:62px;height:62px;border-radius:50%;cursor:grab;user-select:none;-webkit-user-select:none;touch-action:none;display:flex;align-items:center;justify-content:center;transition:transform .18s ease,filter .18s ease}
.ai-orb:focus{outline:2px solid rgba(58,208,184,.55);outline-offset:3px}
.ai-orb:hover{transform:scale(1.05);filter:brightness(1.06)}
.ai-orb.is-dragging{cursor:grabbing;transform:scale(1.08)}
.orb-halo{position:absolute;inset:-9px;border-radius:50%;background:radial-gradient(circle,rgba(58,208,184,.32),transparent 68%);animation:orbPulse 3.4s ease-in-out infinite}
.orb-ring{position:absolute;inset:-4px;border-radius:50%;border:1px dashed rgba(228,183,92,.55);animation:orbSpin 14s linear infinite}
.ai-orb.is-open .orb-ring{border-style:solid;border-color:rgba(58,208,184,.8);animation-duration:5s}
.ai-orb.is-open .orb-halo{opacity:.95}
.orb-svg{position:relative;width:62px;height:62px;display:block;filter:drop-shadow(0 8px 18px rgba(0,0,0,.45))}
.orb-tip{position:absolute;right:74px;top:50%;transform:translateY(-50%);white-space:nowrap;padding:6px 10px;font-size:11px;color:#d8efe9;background:rgba(12,32,36,.94);border:1px solid var(--ai-line);opacity:0;pointer-events:none;transition:opacity .16s ease}
.ai-orb:hover .orb-tip,.ai-orb:focus .orb-tip{opacity:1}
@keyframes orbPulse{0%,100%{transform:scale(.92);opacity:.55}50%{transform:scale(1.12);opacity:.95}}
@keyframes orbSpin{to{transform:rotate(360deg)}}

/* ================= 弹片通用（锚定悬浮球 · 无遮罩 · 不模糊背景） ================= */
.ai-pop{position:fixed;z-index:3300;display:flex;flex-direction:column;box-sizing:border-box;background:linear-gradient(158deg,#132530,#0d1a21);border:1px solid rgba(94,158,168,.34);box-shadow:0 20px 48px rgba(2,10,14,.58);color:#dfeceb}
.ai-pop.right{transform-origin:0 50%}
.ai-pop.left{transform-origin:100% 50%}
.pop-caret{position:absolute;width:10px;height:10px;background:#16282f;border:1px solid rgba(94,158,168,.34);transform:translateY(-50%) rotate(45deg)}
.ai-pop.right .pop-caret{left:-6px;border-right:0;border-top:0}
.ai-pop.left .pop-caret{right:-6px;border-left:0;border-bottom:0;background:#16282f}
.ai-pop-enter-active,.ai-pop-leave-active{transition:opacity .16s ease,transform .16s ease}
.ai-pop-enter,.ai-pop-leave-to{opacity:0;transform:scale(.94)}
.ai-panel-head{display:flex;align-items:center;gap:8px;padding:9px 11px;border-bottom:1px solid var(--ai-line);background:rgba(17,40,46,.7)}
.head-mark{display:flex;align-items:center;justify-content:center;flex:none;width:28px;height:28px;border:1px solid rgba(58,208,184,.4);background:rgba(31,116,113,.18);color:var(--ai-teal);font-size:15px}
.head-mark.gold{border-color:rgba(228,183,92,.42);background:rgba(120,90,30,.16);color:var(--ai-gold)}
.head-text{flex:1;min-width:0;display:flex;flex-direction:column;gap:3px}
.head-text strong{font-size:13px;color:#eaf7f4;font-weight:600}
.head-text small{font-size:9.5px;color:#7f9ba3;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.conn-chip{display:inline-flex;align-items:center;gap:4px;flex:none;padding:3px 6px;font-size:9.5px;border:1px solid rgba(120,160,170,.28);color:#93aeb5}
.conn-chip i{width:6px;height:6px;border-radius:50%;background:#8fa5ad}
.conn-chip.ok{color:#79dcb6;border-color:rgba(80,184,146,.36)}.conn-chip.ok i{background:#51e0bd;box-shadow:0 0 8px rgba(81,224,189,.8)}
.conn-chip.checking{color:#e6c579;border-color:rgba(228,183,92,.36)}.conn-chip.checking i{background:#e4b75c}
.conn-chip.warn{color:#e6c579;border-color:rgba(228,183,92,.36)}.conn-chip.warn i{background:#e4b75c}
.conn-chip.error{color:#f09a8c;border-color:rgba(224,120,100,.4)}.conn-chip.error i{background:#ef8a76}
.icon-btn{flex:none;width:26px;height:26px;border:1px solid transparent;background:transparent;color:#8ea9b1;cursor:pointer;font-size:14px}
.icon-btn:hover{color:#e6f6f2;border-color:var(--ai-line);background:rgba(37,79,86,.35)}

/* ================= 对话 ================= */
.ai-messages{flex:1;overflow-y:auto;padding:11px 11px 4px;display:flex;flex-direction:column;gap:9px}
.ai-messages::-webkit-scrollbar{width:7px}
.ai-messages::-webkit-scrollbar-thumb{background:rgba(96,150,160,.35)}
.msg{display:flex;gap:9px;max-width:100%}
.msg.user{justify-content:flex-end}
.msg-avatar{flex:none;width:23px;height:23px;display:flex;align-items:center;justify-content:center;border:1px solid rgba(58,208,184,.38);background:rgba(30,104,102,.2);color:var(--ai-teal);font-size:12px}
.msg-body{max-width:84%;padding:8px 10px;font-size:12.5px;line-height:1.72;border:1px solid var(--ai-line);background:rgba(19,42,48,.72);color:#dcebe9}
.msg-body p{margin:0;white-space:pre-wrap;word-break:break-word}

/* ---- 助手回复：Markdown + LaTeX 渲染 ---- */
.md-body{white-space:normal;overflow-wrap:anywhere;word-break:break-word}
.md-body.is-plain{white-space:pre-wrap}
.md-body>:first-child{margin-top:0}
.md-body>:last-child{margin-bottom:0}
.md-body p{margin:0 0 9px;white-space:normal;line-height:1.8}
.md-body h1,.md-body h2,.md-body h3,.md-body h4,.md-body h5,.md-body h6{margin:13px 0 8px;font-weight:600;line-height:1.5;color:#eafbf7}
.md-body h1{font-size:15px}
.md-body h2{font-size:14px}
.md-body h3{font-size:13px;color:#c3e9e2}
.md-body h4,.md-body h5,.md-body h6{font-size:12.5px;color:#a9d5cf}
.md-body ul,.md-body ol{margin:0 0 9px;padding-left:20px}
.md-body li{margin:3px 0;line-height:1.78}
.md-body li>ul,.md-body li>ol{margin:3px 0}
.md-body strong{color:#fff8e6;font-weight:600}
.md-body em{color:#d3ece7}
.md-body a{color:#5fd8c4;text-decoration:underline;text-underline-offset:2px;word-break:break-all}
.md-body a:hover{color:#8df0dd}
.md-body hr{margin:12px 0;border:0;border-top:1px solid var(--ai-line)}
.md-body blockquote{margin:0 0 9px;padding:6px 11px;border-left:2px solid rgba(58,208,184,.5);background:rgba(28,62,66,.42);color:#c2e0da}
.md-body blockquote p:last-child{margin-bottom:0}
.md-body code{font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace;font-size:12px;padding:1px 5px;background:rgba(8,20,25,.85);border:1px solid var(--ai-line);color:#9fe8d8}
.md-body pre{margin:0 0 9px;padding:10px 12px;overflow-x:auto;background:rgba(8,20,25,.9);border:1px solid var(--ai-line)}
.md-body pre code{padding:0;border:0;background:none;color:#d3eae4;font-size:11.5px;line-height:1.7}
.md-body table{display:block;width:100%;margin:0 0 10px;border-collapse:collapse;font-size:11.5px;overflow-x:auto}
.md-body th,.md-body td{padding:6px 9px;border:1px solid var(--ai-line);text-align:left;vertical-align:top;line-height:1.65}
.md-body thead th{background:rgba(31,74,79,.72);color:#d7f5ee;font-weight:600;white-space:nowrap}
.md-body tbody tr:nth-child(even){background:rgba(20,48,54,.5)}
.md-body img{max-width:100%;height:auto;border:1px solid var(--ai-line)}
.md-body .katex{font-size:1.02em;color:#eaf7f4}
.md-body .katex-display{margin:10px 0;padding:8px 6px;overflow-x:auto;overflow-y:hidden;background:rgba(9,24,29,.6);border:1px solid var(--ai-line)}
.md-body .katex-display>.katex{white-space:normal}
.md-body .katex-error{color:#f0a08f}
.md-body.is-streaming>:last-child::after{content:'▍';margin-left:1px;color:var(--ai-teal);animation:caretBlink 1s steps(2) infinite}
.md-body.is-streaming>table:last-child::after{content:''}
.msg.user .msg-body{background:rgba(32,92,90,.42);border-color:rgba(58,208,184,.34);color:#e8fbf7}
.msg.is-error .msg-body{border-color:rgba(224,120,100,.45);background:rgba(66,28,24,.5)}
.msg-error{display:flex;align-items:center;gap:5px;margin-top:7px;color:#ef9c8c;font-size:10.5px}
.typing-caret{color:var(--ai-teal);animation:caretBlink 1s steps(2) infinite}
.msg-thinking{display:inline-flex;align-items:center;gap:7px;color:#9fbcc2;font-size:12px;font-style:normal}
.think-dots{display:inline-flex;gap:3px}
.think-dots i{width:4px;height:4px;border-radius:50%;background:var(--ai-teal);animation:thinkBounce .9s ease-in-out infinite}
.think-dots i:nth-child(2){animation-delay:.15s}
.think-dots i:nth-child(3){animation-delay:.3s}
@keyframes thinkBounce{0%,80%,100%{opacity:.25;transform:translateY(0)}40%{opacity:1;transform:translateY(-3px)}}
@keyframes caretBlink{0%,100%{opacity:1}50%{opacity:0}}
.quick-prompts{display:flex;flex-wrap:wrap;gap:6px;padding:2px 0 8px}
.quick-title{width:100%;font-size:9.5px;letter-spacing:.1em;color:#6d8a92}
.quick-prompts button{padding:5px 8px;font-size:11px;color:#a8d7d2;background:rgba(24,56,62,.6);border:1px solid var(--ai-line);cursor:pointer;text-align:left}
.quick-prompts button:hover{border-color:var(--ai-teal);color:#e6fff9;background:rgba(30,84,80,.6)}
.ai-composer{border-top:1px solid var(--ai-line);padding:9px 11px 10px;background:rgba(13,29,35,.85)}
.composer-input{width:100%;box-sizing:border-box;resize:none;padding:8px 9px;font-size:12.5px;line-height:1.65;color:#e3f2f0;background:rgba(9,22,27,.85);border:1px solid var(--ai-line);outline:none;font-family:inherit}
.composer-input:focus{border-color:rgba(58,208,184,.55)}
.composer-input::placeholder{color:#6b868d}
.composer-actions{display:flex;align-items:center;justify-content:flex-end;gap:8px;margin-top:9px}
.ghost-btn{padding:6px 10px;font-size:11.5px;color:#9db6bd;background:transparent;border:1px solid var(--ai-line);cursor:pointer}
.ghost-btn:hover{color:#e2f5f1;border-color:rgba(58,208,184,.45)}
.ghost-btn:disabled{opacity:.45;cursor:not-allowed}
.ghost-btn.danger{color:#f0a294;border-color:rgba(224,120,100,.42)}
.send-btn{display:inline-flex;align-items:center;gap:5px;padding:7px 13px;font-size:12px;font-weight:600;color:#052a28;background:linear-gradient(135deg,#5fe0c9,#33bda9);border:0;cursor:pointer}
.send-btn:hover{filter:brightness(1.07)}
.send-btn:disabled{opacity:.5;cursor:not-allowed;filter:none}

/* ================= 报告解读 ================= */
.briefing-body{flex:1;overflow-y:auto;padding:13px 15px 15px}
.briefing-body::-webkit-scrollbar{width:8px}
.briefing-body::-webkit-scrollbar-thumb{background:rgba(96,150,160,.35)}

/* ---- 消息提醒式流式气泡 ---- */
.stream-bubble{position:relative;display:flex;gap:8px;align-items:flex-start;padding:9px 10px 11px;margin-bottom:10px;border:1px solid rgba(58,208,184,.3);background:linear-gradient(135deg,rgba(22,58,60,.72),rgba(17,38,45,.72))}
.stream-mark{flex:none;width:22px;height:22px;display:flex;align-items:center;justify-content:center;border:1px solid rgba(58,208,184,.42);background:rgba(30,104,102,.22);color:var(--ai-teal);font-size:11px}
.stream-text{flex:1;margin:0;font-size:12px;line-height:1.8;color:#dcf0ec;white-space:pre-wrap;word-break:break-word}
.stream-progress{position:absolute;left:0;right:0;bottom:0;height:2px;background:rgba(94,158,168,.16)}
.stream-progress>span{display:block;height:100%;width:0;background:linear-gradient(90deg,#3ad0b8,#e4b75c);transition:width .1s linear}
.briefing-detail{animation:detailIn .3s ease both}
@keyframes detailIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}

/* ---- 弹片宽度有限，内部栅格改为两列/单列 ---- */
.ai-pop .briefing-body{padding:10px 11px 12px}
.ai-pop .briefing-facts{grid-template-columns:repeat(2,minmax(0,1fr))}
.ai-pop .briefing-facts div:nth-child(2n){border-right:0}
.ai-pop .indicator-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
.ai-pop .metric-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
.ai-pop .metric{border-bottom:1px solid var(--ai-line)}
.ai-pop .thesis{grid-template-columns:1fr;gap:5px;padding:10px 12px;margin-bottom:10px}
.ai-pop .event-hero h2{font-size:15px}
.ai-pop .report-title-row{flex-direction:column;gap:9px}
.ai-pop .report-phase{align-self:flex-start}
.ai-pop .report-title-row h3{font-size:13.5px}
.ai-pop .briefing-tabs{flex-direction:column;gap:6px}
.ai-pop .briefing-tabs button{width:100%;justify-content:center}
.ai-pop .risk-table{font-size:11px}
.ai-pop .risk-table th,.ai-pop .risk-table td{padding:7px 8px}
.ai-pop .briefing-foot{flex-wrap:wrap;padding:8px 11px;justify-content:flex-end}
.ai-pop .empty-report{padding:20px 14px}
.event-hero{padding:12px 13px;margin-bottom:11px;border:1px solid var(--ai-line);background:radial-gradient(ellipse at 88% 4%,rgba(38,118,124,.22),transparent 46%),linear-gradient(150deg,#152c34,#101f27)}
.event-hero h2{margin:8px 0 6px;font-size:17px;color:#eaf8f5;font-weight:600}
.event-range{margin:0;font-size:11px;color:#84a3ab}
.event-range .dot{margin:0 6px;color:#4d737d}
.event-type{display:inline-block;padding:3px 9px;font-size:10.5px;font-weight:700;letter-spacing:.06em;color:#0d3a36;background:#6ee0c4}
.event-type.cool{color:#04303f;background:#7fd0f2}
.event-type.model-type{color:#3a2a06;background:#e8c46e}
.briefing-facts{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));border:1px solid var(--ai-line);margin-bottom:11px}
.briefing-facts div{padding:8px 10px;border-right:1px solid var(--ai-line)}
.briefing-facts div:last-child{border-right:0}
.briefing-facts span{display:block;font-size:9.5px;color:#7e9aa2;margin-bottom:4px}
.briefing-facts strong{font-size:12px;color:#e2f2ef;font-weight:600}
.briefing-block{margin-bottom:11px;padding:11px 12px;border:1px solid var(--ai-line);background:rgba(18,40,47,.6)}
.briefing-block.subtle{background:rgba(14,32,38,.5)}
.briefing-block h3{display:flex;align-items:center;gap:6px;margin:0 0 7px;font-size:12.5px;color:#bfe7e0;font-weight:600}
.briefing-block h3 i{color:var(--ai-teal)}
.briefing-block p{margin:0;font-size:12px;line-height:1.82;color:#cbdcdb}
.briefing-block p b{color:#7fe8d4}
.briefing-tabs{display:flex;gap:7px;margin:11px 0 12px;border-bottom:1px solid var(--ai-line);padding-bottom:9px}
.briefing-tabs button{display:inline-flex;align-items:center;gap:6px;padding:7px 12px;font-size:12px;color:#a3c2c4;background:rgba(19,44,50,.7);border:1px solid var(--ai-line);cursor:pointer}
.briefing-tabs button.active{color:#0a2c2a;background:linear-gradient(135deg,#63ddc6,#38bda9);border-color:transparent;font-weight:600}
.report-title-row{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;margin-bottom:10px}
.report-title-row h3{margin:5px 0 4px;font-size:15px;color:#eaf8f5;font-weight:600}
.report-kicker{margin:0;font-size:9.5px;letter-spacing:.14em;color:#63c5d2;font-weight:700}
.report-meta{margin:0;font-size:10.5px;color:#7e9aa2}
.report-meta span{margin:0 5px;color:#4d737d}
.report-phase{flex:none;padding:5px 8px;font-size:10.5px;color:#f0d9a3;border:1px solid rgba(228,183,92,.35);background:rgba(120,92,30,.16);white-space:nowrap}
.thesis{display:grid;grid-template-columns:78px 1fr;gap:12px;padding:11px 13px;margin-bottom:11px;border-left:3px solid var(--ai-teal);background:rgba(22,58,60,.45)}
.thesis span{font-size:10.5px;font-weight:700;color:#6fe0cd;letter-spacing:.05em}
.thesis p{margin:0;font-size:12px;line-height:1.82;color:#d3e6e4}
.indicator-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:7px;margin-bottom:10px}
.indicator{display:flex;flex-direction:column;gap:4px;padding:9px 10px;border:1px solid var(--ai-line);background:rgba(16,36,43,.7)}
.indicator span{font-size:9.5px;color:#7e9aa2}
.indicator strong{font-size:14px;color:#e7f6f3}
.indicator small{font-size:9.5px;color:#7f9aa2}
.indicator.cyan{border-left:3px solid #59cfe0}.indicator.gold{border-left:3px solid #e4b75c}
.indicator.violet{border-left:3px solid #9d8cf0}.indicator.green{border-left:3px solid #4bd6a4}
.chip-row{display:flex;flex-wrap:wrap;gap:7px;margin-bottom:11px}
.chip{display:inline-flex;align-items:center;gap:6px;padding:5px 9px;font-size:11px;color:#d6e8e6;border:1px solid var(--ai-line);background:rgba(20,45,52,.6)}
.chip b{color:#8fd9cd;font-weight:600}
.chip.warning{border-color:rgba(228,183,92,.4)}.chip.warm{border-color:rgba(232,142,110,.42)}.chip.cool{border-color:rgba(114,186,232,.4)}
.risk-table-wrap{overflow-x:auto}
.risk-table{width:100%;border-collapse:collapse;font-size:11.5px}
.risk-table th{padding:8px 10px;text-align:left;color:#8fb0b6;background:rgba(11,28,34,.7);border-bottom:1px solid var(--ai-line);font-weight:600;white-space:nowrap}
.risk-table td{padding:9px 10px;color:#cddedd;border-bottom:1px solid rgba(94,158,168,.14);vertical-align:top}
.risk-table tr:last-child td{border-bottom:0}
.risk-table td.region{color:#e6f5f2;font-weight:600;white-space:nowrap}
.level{display:inline-block;padding:2px 8px;font-size:10.5px;white-space:nowrap}
.level.high{color:#ffd9cf;background:rgba(196,86,62,.32)}
.level.medium{color:#f6e3b0;background:rgba(168,132,44,.28)}
.level.low{color:#bfe6cd;background:rgba(56,132,90,.26)}
.bullet-list{margin:0;padding-left:16px}
.bullet-list li{font-size:12px;line-height:1.82;color:#cbdcdb;margin-bottom:5px}
.bullet-list li:last-child{margin-bottom:0}
.action-row{display:grid;grid-template-columns:28px 1fr;gap:9px;padding:8px 0;border-top:1px solid rgba(94,158,168,.16)}
.action-row:first-of-type{border-top:0;padding-top:0}
.action-row>b{font-size:12px;color:#6fe0cd;font-weight:700}
.action-row strong{display:block;font-size:12.5px;color:#e0f0ee;margin-bottom:3px}
.action-row p{margin:0;font-size:11.5px;line-height:1.8;color:#a9c2c3}
.numbered-row{display:grid;grid-template-columns:24px 1fr;gap:8px;padding:7px 0;border-top:1px solid rgba(94,158,168,.16)}
.numbered-row:first-of-type{border-top:0;padding-top:0}
.numbered-row>span{font-size:11px;color:#63c5d2;font-weight:700;padding-top:2px}
.numbered-row>span i{font-size:11px}
.numbered-row.plain>span{color:#5b8d99}
.numbered-row p{margin:0;font-size:12px;line-height:1.8;color:#cbdcdb}
.reco-row{padding:7px 0;border-top:1px solid rgba(94,158,168,.16)}
.reco-row:first-of-type{border-top:0;padding-top:0}
.reco-row b{display:block;font-size:11.5px;color:#9fdfd3;margin-bottom:3px}
.reco-row p{margin:0;font-size:11.5px;line-height:1.8;color:#adc6c6}
.metric-grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));margin-bottom:10px;border:1px solid var(--ai-line);background:rgba(11,28,34,.55)}
.metric{display:flex;flex-direction:column;gap:4px;padding:9px 10px;border-right:1px solid var(--ai-line)}
.metric:last-child{border-right:0}
.metric span{font-size:9.5px;color:#7e9aa2}
.metric strong{font-size:15px;color:#e7f6f3;font-weight:650}
.metric strong em{font-size:9.5px;font-style:normal;color:#8aa5ac;margin-left:2px}
.metric strong.cold{color:#8fd8f2}.metric strong.warm{color:#f5b98c}
.metric small{font-size:9px;color:#7b939a}
.verdict{padding:11px 13px;margin-bottom:10px;border:1px solid rgba(228,183,92,.32);background:radial-gradient(ellipse at 92% 0%,rgba(150,112,32,.2),transparent 52%),rgba(28,28,18,.6)}
.verdict span{display:block;font-size:9.5px;letter-spacing:.14em;color:#e4b75c;margin-bottom:5px}
.verdict strong{display:block;font-size:13px;color:#f6ecd2;margin-bottom:5px}
.verdict p{margin:0;font-size:11.5px;line-height:1.8;color:#c9c2a6}
.uncertainty{display:grid;grid-template-columns:18px 1fr;gap:8px;padding:10px 12px;margin-bottom:10px;border-left:3px solid #e4b75c;background:rgba(46,42,22,.45)}
.uncertainty i{color:#e4b75c;font-size:15px;padding-top:2px}
.uncertainty b{display:block;font-size:12px;color:#f0e0b4;margin-bottom:4px}
.uncertainty p{margin:0;font-size:11.5px;line-height:1.85;color:#cbc3a4}
.report-footnote{margin:10px 0 0;font-size:10.5px;line-height:1.75;color:#7c959c}
.empty-report{display:flex;flex-direction:column;align-items:center;gap:9px;padding:30px 18px;text-align:center;border:1px dashed var(--ai-line);background:rgba(16,36,43,.55)}
.empty-report i{font-size:28px;color:#6d959c}
.empty-report h3{margin:0;font-size:14px;color:#e5f4f1}
.empty-report p{margin:0;max-width:600px;font-size:12.5px;line-height:1.9;color:#a9c1c2}
.empty-report p b{color:#8fdccb}
.briefing-foot{display:flex;align-items:center;gap:8px;padding:9px 12px;border-top:1px solid var(--ai-line);background:rgba(13,29,35,.9)}
.foot-note{flex:1;font-size:10.5px;color:#7c959c;display:inline-flex;align-items:center;gap:6px}

/* ================= 响应式 ================= */
@media (max-width:860px){
  .briefing-facts,.indicator-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
  .metric-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
  .metric{border-bottom:1px solid var(--ai-line)}
  .briefing-body{padding:14px 13px}
  .orb-tip{display:none}
  .ai-pop{box-shadow:0 14px 32px rgba(2,10,14,.6)}
}
</style>
