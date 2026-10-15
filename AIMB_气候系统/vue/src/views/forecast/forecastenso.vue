<template>
  <div class="enso-screen" :class="layerChoice + '-mode'">
    <div class="ambient-glow ambient-glow-left"></div>
    <div class="ambient-glow ambient-glow-right"></div>

    <header class="screen-header">
      <div class="brand-lockup">
        <span class="brand-symbol"><i class="el-icon-heavy-rain"></i></span>
        <div><strong>CLIMATE INTELLIGENCE</strong><small>全球气候遥感与预测中心</small></div>
      </div>
      <div class="screen-title">
        <span class="title-kicker">PACIFIC OCEAN · ENSO WATCH</span>
        <h1>ENSO 预测分析与监测平台</h1>
      </div>
      <div class="header-status">
        <span class="live-indicator"><i></i>系统运行正常</span>
        <span class="header-date">{{ nowDate }}</span>
        <button
          type="button"
          class="realtime-status-button"
          :class="{ active: liveStatusVisible }"
          :aria-expanded="liveStatusVisible ? 'true' : 'false'"
          aria-controls="realtime-status-panel"
          @click="toggleLiveStatus"
        ><i class="el-icon-data-line"></i>实时状态</button>
        <strong class="clock">{{ nowTime }}</strong>
      </div>
      <section v-if="liveStatusVisible" id="realtime-status-panel" class="realtime-status-panel" aria-label="ENSO 实时状态">
        <div class="realtime-status-heading"><div><span class="panel-kicker">LIVE ENSO STATUS</span><h2>ENSO 实时状态</h2></div><button type="button" class="realtime-status-close" aria-label="关闭实时状态" @click="liveStatusVisible = false"><i class="el-icon-close"></i></button></div>
        <div class="realtime-status-phase"><span class="live-indicator"><i></i>当前状态</span><strong>{{ realtimeStatus.status }}</strong><small>{{ realtimeStatus.caption }}</small></div>
        <div class="realtime-status-metrics"><div><span>Niño3.4</span><strong>{{ realtimeStatus.nino34 }}<em>°C</em></strong></div><div><span>ONI</span><strong>{{ realtimeStatus.oni }}<em>°C</em></strong></div><div><span>SOI</span><strong>{{ realtimeStatus.soi }}</strong></div></div>
        <div class="realtime-status-footer"><span>数据更新于 {{ dataUpdatedAt }}</span><button type="button" :disabled="liveStatusLoading" @click="refreshLiveStatus"><i :class="liveStatusLoading ? 'el-icon-loading' : 'el-icon-refresh'"></i>{{ liveStatusLoading ? '更新中' : '刷新数据' }}</button></div>
      </section>
    </header>

    <section class="history-control panel-card" aria-label="历史 ENSO 数据选择">
      <div class="history-control-fields">
        <div class="history-type-switch" role="tablist" aria-label="历史数据类型">
          <button type="button" role="tab" :aria-selected="historyType === 'event'" :class="{ active: historyType === 'event' }" @click="selectHistoryType('event')"><i class="el-icon-time"></i>ENSO 历史事件</button>
          <button type="button" role="tab" :aria-selected="historyType === 'model'" :class="{ active: historyType === 'model' }" @click="selectHistoryType('model')"><i class="el-icon-data-analysis"></i>LSTA-Swin 历史预测</button>
        </div>
        <el-select v-if="historyType === 'event'" v-model="selectedHistoryEventId" class="history-select" filterable aria-label="选择 ENSO 历史事件" placeholder="选择历史事件" @change="syncHistoryQuery">
          <el-option v-for="event in historyEvents" :key="event.id" :label="event.label" :value="event.id"><span class="history-option"><i :class="event.type.indexOf('拉尼娜') > -1 ? 'cool' : 'warm'"></i>{{ event.label }}</span></el-option>
        </el-select>
        <el-select v-else v-model="selectedHistoryModelStart" class="history-select" filterable aria-label="选择 LSTA-Swin 历史预测起报月份" placeholder="选择起报月份" @change="syncHistoryQuery">
          <el-option v-for="option in modelStartOptions" :key="option.id" :label="option.label" :value="option.id" />
        </el-select>
        <div class="history-current-summary"><span>当前月份</span><strong>{{ historyCurrentMonthLabel }}</strong><small>{{ historyCurrentPointLabel }} · {{ historyCurrentRange }}</small></div>
        <div class="history-context-summary"><span>{{ historyActiveItem.type }}</span><small>{{ historyEventTimeLabel }}</small><b>峰值 {{ historyPeakLabel }}</b></div>
      </div>
    </section>

    <section class="kpi-strip" aria-label="关键监测指标">
      <article class="kpi-item">
        <span class="kpi-icon warm-icon"><i class="el-icon-sunny"></i></span>
        <div><small>Niño3.4 海温距平</small><strong class="warm-text">{{ displayMonitor.nino34 }}<em>°C</em></strong></div>
        <span class="kpi-delta up"><i class="el-icon-top"></i> {{ historyMode ? historyCurrentMonthLabel : '+0.2' }}</span>
      </article>
      <article class="kpi-item">
        <span class="kpi-icon cyan-icon"><i class="el-icon-data-line"></i></span>
        <div><small>ONI · 3月滑动平均</small><strong>{{ displayMonitor.oni }}<em>°C</em></strong></div>
        <span class="kpi-delta up">{{ historyMode ? historyPhase : '持续偏暖' }}</span>
      </article>
      <article class="kpi-item">
        <span class="kpi-icon blue-icon"><i class="el-icon-odometer"></i></span>
        <div><small>南方涛动指数 SOI</small><strong class="blue-text">{{ displayMonitor.soi }}</strong></div>
        <span class="kpi-delta">近30日</span>
      </article>
      <article class="kpi-item phase-kpi">
        <span class="kpi-icon gold-icon"><i class="el-icon-aim"></i></span>
        <div><small>当前 ENSO 阶段</small><strong class="phase-text">{{ displayMonitor.status }}</strong></div>
        <span class="phase-chip"><i></i>{{ historyMode ? historyPhase : '发展期' }}</span>
      </article>
    </section>

    <main class="screen-body">
      <aside class="side-column left-column">
        <section class="panel-card phase-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">{{ historyMode ? 'HISTORICAL MONITORING' : 'LIVE OBSERVATION' }}</span><h2>{{ historyMode ? '海气历史监测' : '海气实时监测' }}</h2></div>
            <span class="data-age"><i></i>更新于 {{ dataUpdatedAt }}</span>
          </div>
          <div class="phase-reading">
            <div class="phase-orbit"><div class="orbit-track"></div><div class="orbit-core"><span>ONI</span><strong>{{ displayMonitor.oni }}</strong><small>°C</small></div><i class="orbit-satellite"></i></div>
            <div class="phase-copy"><span class="phase-label">当前状态</span><strong>{{ displayPhaseTitle }}</strong><span class="phase-caption">{{ displayPhaseCaption }}</span><div class="threshold-track"><i :style="{ width: displayThreshold + '%' }"></i></div><div class="threshold-caption"><span>中性</span><span>+0.5°C 阈值</span></div></div>
          </div>
          <div class="observation-grid">
            <div><span>东太平洋 SST</span><strong class="warm-text">{{ displayObservation.east }}</strong></div>
            <div><span>信风距平</span><strong class="cyan-text">{{ displayObservation.wind }}<span v-if="displayObservation.wind !== '—'"> m/s</span></strong></div>
            <div><span>暖水体积</span><strong>{{ displayObservation.volume }}<span v-if="displayObservation.volume !== '—'">%</span></strong></div>
          </div>
        </section>

        <section class="panel-card intensity-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">OCEAN HEAT CONTENT</span><h2>海洋热含量变化</h2></div>
            <span class="unit-label">℃ · 0–300m</span>
          </div>
          <div class="intensity-meter">
            <div class="intensity-scale"><span>+2.0</span><span>+1.0</span><span>0</span><span>−1.0</span></div>
            <div class="intensity-bars">
              <div v-for="item in displayHeatContent" :key="item.name" class="heat-row">
                <span>{{ item.name }}</span><div class="heat-track"><i :style="{ width: item.value + '%' }"></i></div><b>{{ item.label }}</b>
              </div>
            </div>
          </div>
          <div class="panel-footnote"><span class="pulse-dot"></span>暖水向东扩展，次表层正距平增强</div>
        </section>

        <section class="panel-card event-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">RECENT SIGNALS</span><h2>关键气候信号</h2></div>
            <button class="quiet-action" type="button" @click="refreshData"><i class="el-icon-refresh"></i>刷新</button>
          </div>
          <div class="signal-list">
            <div v-for="signal in displaySignals" :key="signal.time + signal.title" class="signal-row">
              <span class="signal-marker" :class="signal.tone"></span><div><strong>{{ signal.title }}</strong><small>{{ signal.detail }}</small></div><time>{{ signal.time }}</time>
            </div>
          </div>
        </section>
      </aside>

      <section class="center-column">
        <section class="map-card panel-card" :class="{ 'map-card-maximized': mapMaximized }">
          <div class="map-heading">
            <div class="map-title"><span class="map-live-icon"><i></i></span><div><span class="panel-kicker">{{ historyMode ? 'GLOBAL · HISTORICAL SSTA / TRADE WINDS' : 'TROPICAL PACIFIC · SSTA / TRADE WINDS' }}</span><h2>{{ historyMode ? '全球海温异常与信风场' : '热带太平洋海温距平与信风场' }}</h2></div></div>
            <span class="map-heading-note">拖动旋转 · 滚轮缩放 · Niño3.4 核心区追踪</span>
          </div>

          <enso-map-panel :key="historyMode ? 'history-map' : 'live-map'" :sst-field="historyMode && historyHasData ? historyCurrentField : null" :sst-mode="historyMode && historyHasData ? 'anomaly' : 'absolute'" initial-layer="both" :maximized="mapMaximized" @toggle-maximize="toggleMapMaximize" />
          <div class="map-bottomline"><span><i></i>{{ historyMode ? (historyHasData ? historyCurrentMonthLabel + ' · 历史场' : historyActiveItem.label) : '遥感反演场正常' }}</span><span>{{ historyMode ? historyDataSource : 'NOAA OISST v2.1 · ERSST v5' }}</span><span>{{ historyMode && historyHasData ? '固定距平色标 −3 至 +3 ℃' : '距平基准期 1991–2020' }}</span></div>
        </section>
        <div class="map-stat-row">
          <article v-for="stat in displayMapStats" :key="stat.title"><span class="stat-glyph" :class="stat.tone"><i :class="stat.icon"></i></span><div><small>{{ stat.title }}</small><strong :class="stat.tone === 'warm-glyph' ? 'warm-text' : ''">{{ stat.value }}</strong></div><b>{{ stat.metric }}</b></article>
        </div>
      </section>

      <aside class="side-column right-column">
        <section class="panel-card probability-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">IRI MULTI-MODEL OUTLOOK</span><h2>未来 ENSO 状态概率</h2></div>
            <span class="small-badge">6 季展望</span>
          </div>
          <div ref="probabilityChart" class="probability-chart"></div>
          <div class="chart-legend"><span><i class="legend-el-nino"></i>厄尔尼诺</span><span><i class="legend-neutral-dot"></i>中性</span><span><i class="legend-la-nina"></i>拉尼娜</span></div>
        </section>
        <section class="panel-card forecast-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">{{ historyMode ? 'NINO3.4 INDEX · HISTORICAL' : 'NINO3.4 INDEX · OBS / FCST' }}</span><h2>指数演变趋势</h2></div>
            <span class="unit-label">°C</span>
          </div>
          <div ref="indexChart" class="index-chart"></div>
          <div class="forecast-legend"><span><i class="observed-line"></i>{{ historyMode ? (historyType === 'event' ? '事件指数' : 'LSTA-Swin 预测') : '观测' }}</span><span><i class="forecast-line"></i>{{ historyMode && historyType === 'model' ? '观测代理' : '预测' }}</span><span><i class="threshold-line"></i>±0.5 阈值</span></div>
        </section>
        <section class="panel-card impact-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">GLOBAL TELECONNECTIONS</span><h2>区域影响信号</h2></div>
            <span class="risk-status"><i></i>{{ impactStatus }}</span>
          </div>
          <div class="impact-list">
            <div v-for="risk in displayRiskLevels" :key="risk.name" class="impact-row">
              <div class="impact-copy"><span>{{ risk.name }}</span><strong :class="risk.tone">{{ risk.label }}</strong></div>
              <div class="impact-track"><i :class="risk.tone" :style="{ width: risk.value + '%' }"></i></div>
            </div>
          </div>
          <div class="impact-note"><i class="el-icon-warning-outline"></i>信号强度基于季节预测与历史响应关系</div>
        </section>
      </aside>
    </main>

    <footer class="time-control">
      <div class="timeline-label"><span class="panel-kicker">ENSO SEASONAL TIMELINE</span><strong>演变时间轴</strong></div>
      <button class="play-button" type="button" :aria-label="isPlaying ? '暂停演变' : '播放演变'" @click="onPlayClick"><i :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i></button>
      <div class="timeline-track-wrap">
        <div class="timeline-labels"><span v-for="period in timelineLabels" :key="period.index" :class="{ selected: period.index === currentIndexSafe }" @click="selectTimelineIndex(period.index)">{{ period.label }}</span></div>
        <input v-model.number="currentIndex" class="timeline-slider" type="range" min="0" :max="Math.max(activePeriods.length - 1, 0)" step="1" aria-label="选择 ENSO 历史月份" :style="{ '--progress': timelineProgress + '%' }" @change="onTimelineChange">
      </div>
      <div class="timeline-current"><span>当前时次</span><strong>{{ activePeriod }}</strong><small>{{ historyMode ? historyCurrentPointLabel : (currentIndex < 3 ? '观测场' : '预测场') }}</small></div>
      <button type="button" class="timeline-ai-button" :class="{ ready: historyMode }" title="点击演变时间轴即唤起 AI 助手解读；也可由此按钮主动打开" @click="openAiBriefingFromTimeline('button')"><i class="el-icon-cpu"></i>AI 解读</button>
    </footer>

    <section class="module-workbench" aria-labelledby="moduleWorkbenchTitle">
      <div class="workbench-heading">
        <div><span class="panel-kicker">ENSO KNOWLEDGE &amp; TOOLS</span><h2 id="moduleWorkbenchTitle">专题分析工作台</h2><p>从实时监测继续进入背景知识、模型产品与可复核分析</p></div>
        <span class="workbench-count"><b>07</b> 个功能模块</span>
      </div>
      <nav class="module-quick-nav" aria-label="专题模块导航">
        <button v-for="module in modules" :key="module.id" type="button" @click="scrollToModule(module.id)"><span>{{ module.number }}</span>{{ module.title }}</button>
      </nav>
      <div class="module-grid">
        <article v-for="module in modules" :id="module.id" :key="module.id + '-card'" class="module-card" :class="module.tone">
          <div class="module-card-top"><span class="module-icon"><i :class="module.icon"></i></span><span class="module-number">{{ module.number }}</span></div>
          <span class="module-eyebrow">{{ module.eyebrow }}</span>
          <h3>{{ module.title }}</h3>
          <p>{{ module.description }}</p>
          <div class="module-highlights"><span v-for="item in module.highlights" :key="item">{{ item }}</span></div>
          <button v-if="module.path" type="button" class="module-action" @click="$router.push(module.path)">{{ module.action || '进入模块' }}<i class="el-icon-arrow-right"></i></button>
          <span v-else class="module-action module-action-static">平台概览 <i class="el-icon-check"></i></span>
        </article>
      </div>
    </section>
    <div class="screen-watermark">AIMB CLIMATE SYSTEM <span>·</span> ENSO MONITORING</div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import EnsoMapPanel from '@/components/EnsoMapPanel.vue'
import { historyEvents, modelStartOptions, modelReplay, fieldForPoint } from '@/data/ensoHistory'
import { openAiBriefing } from '@/utils/aiBus'
import { getModelReports } from '@/data/ensoReports'

export default {
  name: 'ForecastENSOScreen',
  components: { EnsoMapPanel },
  data() {
    const queryStart = this.$route && this.$route.query
      ? (Array.isArray(this.$route.query.start) ? this.$route.query.start[0] : this.$route.query.start)
      : ''
    const hasModelStart = modelStartOptions.some(option => option.id === queryStart)
    return {
      nowTime: '',
      nowDate: '',
      dataUpdatedAt: '08:30',
      liveStatusLoading: false,
      clockTimer: null,
      playTimer: null,
      isPlaying: false,
      charts: {},
      currentIndex: 3,
      historyType: hasModelStart ? 'model' : 'event',
      selectedHistoryEventId: '2015-16',
      selectedHistoryModelStart: hasModelStart ? queryStart : '2025-01',
      historyEvents,
      modelStartOptions,
      mapMaximized: false,
      liveStatusVisible: false,
      layerChoice: 'combined',
      periods: ['2026 JJA', '2026 JAS', '2026 ASO', '2026 SON', '2026 OND', '2027 NDJ'],
      monitor: { nino34: '+0.8', oni: '+0.7', soi: '−0.4', status: '厄尔尼诺' },
      heatContent: [
        { name: '东部', value: 84, label: '+1.6°' },
        { name: '中东部', value: 72, label: '+1.2°' },
        { name: '中部', value: 60, label: '+0.8°' },
        { name: '西部', value: 34, label: '+0.3°' }
      ],
      signals: [
        { time: '08:30', title: '赤道东太平洋增暖', detail: 'Niño3.4 距平较上旬上升 0.2°C', tone: 'warm' },
        { time: '06:00', title: '信风异常偏弱', detail: '西风爆发信号持续向东传播', tone: 'cyan' },
        { time: '昨日', title: '次表层暖水增强', detail: '120°W–160°E 暖水体积高于常年', tone: 'blue' }
      ],
      riskLevels: [
        { name: '东亚夏季风', value: 72, label: '偏强信号', tone: 'warm' },
        { name: '南美西岸降水', value: 63, label: '偏多倾向', tone: 'cyan' },
        { name: '澳洲北部干旱', value: 58, label: '中等关注', tone: 'gold' }
      ],
      modules: [
        { id: 'system-introduction', number: '01', title: '系统介绍', eyebrow: 'CLIMATE INTELLIGENCE', icon: 'el-icon-monitor', tone: 'cyan-tone', description: '了解平台如何整合海气输入、ENSO 长提前期预测、历史数据联动和可信验证，并进入完整系统说明。', highlights: ['数据 · 预测 · 验证', '点击进入系统介绍'], path: '/enso/overview', action: '进入系统介绍' },
        { id: 'enso-introduction', number: '02', title: 'ENSO 介绍', eyebrow: 'OCEAN–ATMOSPHERE', icon: 'el-icon-help', tone: 'blue-tone', description: '了解厄尔尼诺、拉尼娜与中性位相，以及海气耦合如何驱动热带太平洋的年际变化。', highlights: ['ENSO 位相', '海气耦合机制'], path: '/enso/introduction' },
        { id: 'prediction-models', number: '03', title: '预测模型', eyebrow: 'MODEL LAB', icon: 'el-icon-data-analysis', tone: 'gold-tone', description: '对比 LSTA-Swin 与 ConvLSTM 产品，查看 Niño3.4 / ONI 预测轨迹和赤道太平洋空间场。', highlights: ['长提前期预测', '多模型产品'], path: '/enso/models', action: '查看模型' },
        { id: 'historical-replay', number: '04', title: '历史数据', eyebrow: 'HISTORICAL DATA', icon: 'el-icon-time', tone: 'violet-tone', description: '在 ENSO 监测平台中选择历史事件或 LSTA-Swin 起报月份，地图、指数和其他面板同步联动。', highlights: ['事件与起报月份', '地图 · 指数 · 风场联动'], path: '/enso/monitor?view=history' },
        { id: 'lead-validation', number: '05', title: '提前期验证', eyebrow: 'SKILL ASSESSMENT', icon: 'el-icon-aim', tone: 'cyan-tone', description: '查看 1–20 个月提前期的相关系数、平均绝对误差与均方根误差，评估模型技巧衰减。', highlights: ['PCC · MAE · RMSE', '逐提前期统计'], path: '/enso/validation' },
        { id: 'physical-explanation', number: '06', title: '物理解释', eyebrow: 'PHYSICAL INSIGHT', icon: 'el-icon-view', tone: 'gold-tone', description: '通过时空演变与显著性图，追踪模型关注区域和海温、风应力信号之间的物理联系。', highlights: ['时空演变', '预测显著性'], path: '/enso/explainability' },
        { id: 'climate-nc', number: '07', title: '气候 NC', eyebrow: 'CLIMATE DATA EXPLORER', icon: 'el-icon-picture-outline', tone: 'blue-tone', description: '载入 NetCDF 气候格点数据，选择变量和时次，查看空间分布并开展区域分析。', highlights: ['NetCDF 格点场', '变量 · 时次 · 区域'], path: '/enso/climate-data', action: '打开数据视图' }
      ]
    }
  },
  computed: {
    historyMode() {
      const view = this.$route && this.$route.query ? this.$route.query.view : ''
      const normalizedView = Array.isArray(view) ? view[0] : view
      return normalizedView === 'history'
    },
    historyActiveItem() {
      if (this.historyType === 'model') return modelReplay(this.selectedHistoryModelStart)
      return this.historyEvents.find(event => event.id === this.selectedHistoryEventId) || this.historyEvents[0]
    },
    historySeries() {
      return this.historyActiveItem && Array.isArray(this.historyActiveItem.series) ? this.historyActiveItem.series : []
    },
    historyHasData() {
      return Boolean(this.historyActiveItem && this.historyActiveItem.hasData && this.historySeries.some(point => {
        if (!point || point.value === null || point.value === undefined || point.value === '') return false
        return Number.isFinite(Number(point.value))
      }))
    },
    currentIndexSafe() {
      const max = this.activePeriods.length - 1
      if (max < 0) return 0
      const value = Number(this.currentIndex)
      if (!Number.isFinite(value)) return 0
      return Math.min(Math.max(Math.trunc(value), 0), max)
    },
    activePeriods() {
      if (!this.historyMode) return this.periods
      return this.historySeries.map(point => point.month)
    },
    timelineLabels() {
      const periods = this.activePeriods
      if (periods.length <= 9) return periods.map((label, index) => ({ label, index }))
      const indexes = Array.from(new Set([0, Math.floor((periods.length - 1) / 2), periods.length - 1]))
      return indexes.map(index => ({ label: periods[index], index }))
    },
    activePeriod() {
      if (!this.historyMode) return this.periods[this.currentIndexSafe] || this.periods[0]
      return this.historyCurrentMonthLabel
    },
    historyCurrentPoint() {
      return this.historySeries[this.currentIndexSafe] || { month: '', value: null, observed: null }
    },
    historyCurrentField() {
      return this.historyHasData ? fieldForPoint(this.historyCurrentPoint, this.historyType, this.currentIndexSafe) : null
    },
    historyCurrentMonthLabel() {
      if (!this.historyMode) return '—'
      const month = this.historyCurrentPoint.month
      if (!month) return '—'
      const [year, value] = month.split('-')
      return `${year} 年 ${Number(value)} 月`
    },
    historyCurrentPointLabel() {
      if (!this.historyMode || !this.historySeries.length) return '—'
      return `时间游标 ${this.currentIndexSafe + 1} / ${this.historySeries.length}`
    },
    historyCurrentValue() {
      if (this.historyCurrentPoint.value === null || this.historyCurrentPoint.value === undefined || this.historyCurrentPoint.value === '') return null
      const value = Number(this.historyCurrentPoint.value)
      return Number.isFinite(value) ? value : null
    },
    historyCurrentRange() {
      if (!this.historyCurrentField || !Array.isArray(this.historyCurrentField.data)) return '—'
      const values = this.historyCurrentField.data.filter(value => Number.isFinite(value))
      if (!values.length) return '—'
      const min = Math.min(...values)
      const max = Math.max(...values)
      return `${this.signedValue(min)} / ${this.signedValue(max)} ℃`
    },
    historyPhase() {
      if (!this.historyHasData) return this.historyActiveItem && this.historyActiveItem.type ? this.historyActiveItem.type : '历史事件'
      const value = this.historyCurrentValue || 0
      if (value >= 1.5) return '暖异常核心'
      if (value >= 0.5) return '暖位相发展'
      if (value <= -1) return '冷异常核心'
      if (value <= -0.5) return '冷位相维持'
      return '中性过渡'
    },
    historyDataSource() {
      return this.historyActiveItem && this.historyActiveItem.source ? this.historyActiveItem.source : '历史数据目录'
    },
    historyEventTimeLabel() {
      const item = this.historyActiveItem
      if (!item) return '—'
      if (this.historyType === 'model') return `${item.start || '—'} 起报`
      return `${item.start || '—'} 至 ${item.end || (this.historySeries.length ? this.historySeries[this.historySeries.length - 1].month : '—')}`
    },
    historyPeakLabel() {
      return this.historyActiveItem && this.historyActiveItem.peak ? this.historyActiveItem.peak : '—'
    },
    historyEmptyText() {
      if (!this.historyMode) return ''
      const item = this.historyActiveItem
      if (!item) return '历史事件'
      return `${item.label} · ${item.start || '—'} 至 ${item.end || '—'}`
    },
    historyPointDescription() {
      if (!this.historyMode || !this.historyHasData) return this.historyEmptyText
      const value = this.historyCurrentValue
      const month = this.historyCurrentMonthLabel
      if (value === null) return `${month}指数记录未绑定。`
      if (value >= 1.5) return `${month}暖异常达到核心强度，Niño3.4 为 ${this.signedValue(value)} ℃。`
      if (value >= 0.5) return `${month}暖异常正在发展，Niño3.4 为 ${this.signedValue(value)} ℃。`
      if (value <= -1) return `${month}冷异常达到核心强度，Niño3.4 为 ${this.signedValue(value)} ℃。`
      if (value <= -0.5) return `${month}冷异常维持，Niño3.4 为 ${this.signedValue(value)} ℃。`
      return `${month}指数接近中性，Niño3.4 为 ${this.signedValue(value)} ℃。`
    },
    displayMonitor() {
      if (!this.historyMode) return this.monitor
      const value = this.historyCurrentValue
      if (value === null) return { nino34: '—', oni: '—', soi: '—', status: this.historyActiveItem && this.historyActiveItem.type ? this.historyActiveItem.type : '历史事件' }
      const signed = this.signedValue(value)
      return { nino34: signed, oni: signed, soi: '—', status: this.historyActiveItem.type }
    },
    realtimeStatus() {
      const status = this.monitor.status || '中性'
      const value = Number.parseFloat(String(this.monitor.nino34).replace(/[+℃]/g, ''))
      const caption = status.indexOf('厄尔尼诺') > -1
        ? '赤道中东太平洋偏暖，暖异常仍在发展'
        : status.indexOf('拉尼娜') > -1
          ? '赤道中东太平洋偏冷，冷异常正在维持'
          : '赤道中东太平洋接近常年状态'
      return {
        status,
        caption,
        nino34: this.monitor.nino34 || '—',
        oni: this.monitor.oni || '—',
        soi: this.monitor.soi || '—',
        value: Number.isFinite(value) ? value : 0
      }
    },
    displayPhaseTitle() {
      return this.historyMode ? this.historyPhase : '厄尔尼诺发展'
    },
    displayPhaseCaption() {
      if (!this.historyMode) return '赤道中东太平洋持续增暖'
      return this.historyPointDescription
    },
    displayThreshold() {
      if (!this.historyMode) return 62
      if (this.historyCurrentValue === null) return 0
      return Math.max(5, Math.min(100, 50 + this.historyCurrentValue * 22))
    },
    displayObservation() {
      if (!this.historyMode) return { east: '+1.2°', wind: '−0.6', volume: '+14' }
      if (this.historyCurrentValue === null) return { east: '—', wind: '—', volume: '—' }
      const value = this.historyCurrentValue
      return { east: this.signedValue(value * 0.85) + '°', wind: this.signedValue(value * -0.35), volume: this.signedValue(value * 9) }
    },
    displayHeatContent() {
      if (!this.historyMode) return this.heatContent
      if (this.historyCurrentValue === null) return this.heatContent.map(item => ({ ...item, value: 0, label: '—' }))
      const value = this.historyCurrentValue
      const sign = value >= 0 ? '+' : '−'
      const magnitude = Math.abs(value)
      return [
        { name: '东部', value: Math.min(100, Math.max(3, 50 + value * 24)), label: `${sign}${magnitude.toFixed(1)}°` },
        { name: '中东部', value: Math.min(100, Math.max(3, 45 + value * 19)), label: `${sign}${(magnitude * 0.8).toFixed(1)}°` },
        { name: '中部', value: Math.min(100, Math.max(3, 40 + value * 15)), label: `${sign}${(magnitude * 0.6).toFixed(1)}°` },
        { name: '西部', value: Math.min(100, Math.max(3, 35 - value * 7)), label: `${value >= 0 ? '+' : '−'}${(magnitude * 0.3).toFixed(1)}°` }
      ]
    },
    displayMapStats() {
      if (!this.historyMode) return [
        { title: '赤道中东太平洋', value: '持续增暖', metric: '↑ 0.3°', tone: 'warm-glyph', icon: 'el-icon-sunny' },
        { title: '低层信风强度', value: '较常年偏弱', metric: '−0.6 m/s', tone: 'cyan-glyph', icon: 'el-icon-sort' },
        { title: '暖水向东输送', value: '传播信号活跃', metric: '+14%', tone: 'blue-glyph', icon: 'el-icon-ship' }
      ]
      if (this.historyCurrentValue === null) return [
        { title: '赤道中东太平洋', value: this.historyActiveItem && this.historyActiveItem.type ? this.historyActiveItem.type : '历史事件', metric: this.historyPeakLabel, tone: 'blue-glyph', icon: 'el-icon-sunny' },
        { title: '低层信风强度', value: '历史监测', metric: '—', tone: 'cyan-glyph', icon: 'el-icon-sort' },
        { title: '暖水向东输送', value: '事件信息', metric: this.historyEventTimeLabel, tone: 'blue-glyph', icon: 'el-icon-ship' }
      ]
      const value = this.historyCurrentValue
      return [
        { title: '赤道中东太平洋', value: value >= 0.5 ? '暖异常' : value <= -0.5 ? '冷异常' : '接近中性', metric: `${value > 0 ? '↑' : value < 0 ? '↓' : '·'} ${this.signedValue(value)}°`, tone: value >= 0 ? 'warm-glyph' : 'blue-glyph', icon: 'el-icon-sunny' },
        { title: '低层信风强度', value: value >= 0.5 ? '偏弱' : value <= -0.5 ? '偏强' : '接近常年', metric: this.signedValue(value * -0.35) + ' m/s', tone: 'cyan-glyph', icon: 'el-icon-sort' },
        { title: '暖水向东输送', value: value >= 0.5 ? '传播信号活跃' : value <= -0.5 ? '冷水输送' : '信号平稳', metric: this.signedValue(value * 9) + '%', tone: 'blue-glyph', icon: 'el-icon-ship' }
      ]
    },
    displaySignals() {
      if (!this.historyMode) return this.signals
      const value = this.historyCurrentValue
      if (value === null) {
        const item = this.historyActiveItem || {}
        return [
          { time: '事件', title: item.label || '历史事件', detail: `${item.type || 'ENSO'} · ${this.historyEventTimeLabel}`, tone: item.type && item.type.indexOf('拉尼娜') > -1 ? 'blue' : 'warm' },
          { time: '强度', title: '事件指数', detail: `峰值 ${this.historyPeakLabel}`, tone: 'cyan' },
          { time: '监测', title: '当前选择已更新', detail: '事件类型与发生时间已同步到监测面板', tone: 'blue' }
        ]
      }
      const month = this.historyCurrentMonthLabel
      const signed = this.signedValue(value)
      const magnitude = Math.abs(value)
      const direction = value >= 0.5 ? '暖' : value <= -0.5 ? '冷' : '中性'
      const wind = this.displayObservation.wind
      const volume = this.displayObservation.volume
      const isModel = this.historyType === 'model'
      return [
        {
          time: month.replace(' 年 ', '/').replace(' 月', ''),
          title: `${isModel ? '模型预测' : '赤道海温'}${direction}${direction === '中性' ? '过渡' : '异常'}`,
          detail: `${month} Niño3.4 ${signed} ℃，${this.historyPhase}`,
          tone: value >= 0.5 ? 'warm' : value <= -0.5 ? 'blue' : 'cyan'
        },
        {
          time: `游标 ${this.currentIndexSafe + 1}`,
          title: `信风${value >= 0.5 ? '偏弱' : value <= -0.5 ? '偏强' : '接近常年'}`,
          detail: `低层风场距平 ${wind} m/s，随当前海温场同步更新`,
          tone: 'cyan'
        },
        {
          time: `距平 ${magnitude.toFixed(1)}°`,
          title: `${value >= 0.5 ? '暖水' : value <= -0.5 ? '冷水' : '次表层'}输送信号${magnitude >= 1 ? '增强' : '平稳'}`,
          detail: `暖水体积代理 ${volume}% · 当前场 ${this.historyCurrentRange}`,
          tone: value >= 0.5 ? 'warm' : 'blue'
        }
      ]
    },
    displayRiskLevels() {
      if (!this.historyMode) return this.riskLevels
      const value = this.historyCurrentValue
      if (value === null) {
        return this.riskLevels.map(risk => ({ ...risk, value: 18, label: '参考' }))
      }
      const magnitude = Math.min(2.6, Math.abs(value))
      const phaseWeight = this.historySeries.length > 1 ? this.currentIndexSafe / (this.historySeries.length - 1) : 0
      const seasonalPulse = 5 * Math.sin((this.currentIndexSafe + 1) * Math.PI / 6)
      const scaled = magnitude / 2.6 * 48
      const warm = value >= 0.5
      const cool = value <= -0.5
      return [
        {
          name: '东亚夏季风',
          value: Math.round(Math.max(12, Math.min(96, 27 + scaled + (warm ? 10 : cool ? 5 : 0) + seasonalPulse * 0.35))),
          label: warm ? '偏弱信号' : cool ? '偏强信号' : '中性倾向',
          tone: warm ? 'warm' : 'cyan'
        },
        {
          name: '南美西岸降水',
          value: Math.round(Math.max(12, Math.min(96, 28 + scaled * 0.82 + (warm ? 12 : cool ? 6 : 0) + seasonalPulse * 0.25))),
          label: warm ? '偏多倾向' : cool ? '偏少倾向' : '季节信号弱',
          tone: warm ? 'cyan' : 'blue'
        },
        {
          name: '澳洲北部干旱',
          value: Math.round(Math.max(12, Math.min(96, 24 + scaled * 0.72 + (warm ? 16 : cool ? 2 : 0) + phaseWeight * 8))),
          label: warm ? '中等关注' : cool ? '风险减弱' : '中性关注',
          tone: warm ? 'gold' : cool ? 'cyan' : 'gold'
        }
      ]
    },
    impactStatus() {
      if (!this.historyMode) return '关注'
      if (this.historyCurrentValue === null) return '参考'
      const magnitude = Math.abs(this.historyCurrentValue)
      return magnitude >= 1.5 ? '高关注' : magnitude >= 0.5 ? '关注' : '平稳'
    },
    probabilityOutlook() {
      if (!this.historyMode) {
        return {
          labels: ['SON', 'OND', 'NDJ', 'DJF', 'JFM', 'FMA'],
          elNino: [60, 58, 53, 47, 40, 32],
          neutral: [25, 27, 30, 34, 37, 39],
          laNina: [15, 15, 17, 19, 23, 29]
        }
      }
      const values = this.historySeries
        .map(point => point.value)
        .filter(value => value !== null && value !== undefined && value !== '' && Number.isFinite(Number(value)))
        .map(Number)
      if (!values.length) {
        return {
          labels: ['当前', '下月', '两月后', '三月后', '四月后', '五月后'],
          elNino: [33, 33, 33, 33, 33, 33],
          neutral: [34, 34, 34, 34, 34, 34],
          laNina: [33, 33, 33, 33, 33, 33]
        }
      }
      const points = Array.from({ length: 6 }, (_, offset) => values[Math.min(this.currentIndexSafe + offset * 2, values.length - 1)])
      const labels = Array.from({ length: 6 }, (_, offset) => {
        const point = this.historySeries[Math.min(this.currentIndexSafe + offset * 2, this.historySeries.length - 1)]
        if (!point || !point.month) return offset === 0 ? '当前' : `${offset}月后`
        const [year, month] = point.month.split('-')
        return offset === 0 ? `${year}/${Number(month)}` : `${Number(month)}月`
      })
      const elNino = []
      const neutral = []
      const laNina = []
      points.forEach(value => {
        const warmProbability = Math.max(8, Math.min(82, Math.round(42 + value * 23)))
        const neutralProbability = Math.max(10, Math.min(58, Math.round(36 - Math.abs(value) * 13)))
        const coolProbability = Math.max(8, 100 - warmProbability - neutralProbability)
        const total = warmProbability + neutralProbability + coolProbability
        elNino.push(Math.round(warmProbability / total * 100))
        neutral.push(Math.round(neutralProbability / total * 100))
        laNina.push(100 - elNino[elNino.length - 1] - neutral[neutral.length - 1])
      })
      return { labels, elNino, neutral, laNina }
    },
    timelineProgress() {
      const max = Math.max(this.activePeriods.length - 1, 1)
      return Math.max(0, Math.min(100, this.currentIndexSafe / max * 100))
    }
  },
  watch: {
    '$route.query.view': {
      immediate: true,
      handler() {
        if (!this.historyMode) {
          this.stopPlayback()
          this.mapMaximized = false
          this.historyType = 'event'
          this.selectedHistoryEventId = '2015-16'
        }
        this.currentIndex = this.historyMode ? (this.historyType === 'event' ? 9 : 0) : Math.min(this.currentIndex, this.periods.length - 1)
        this.$nextTick(() => this.renderCharts())
      }
    },
    '$route.query.start': {
      handler(value) {
        const start = Array.isArray(value) ? value[0] : value
        if (!this.historyMode) return
        if (!modelStartOptions.some(option => option.id === start)) {
          if (this.historyType === 'model') this.historyType = 'event'
          return
        }
        this.historyType = 'model'
        this.selectedHistoryModelStart = start
      }
    },
    historyType() { this.resetHistorySelection() },
    selectedHistoryEventId() { this.resetHistorySelection() },
    selectedHistoryModelStart() { this.resetHistorySelection() },
    currentIndex() {
      if (this.currentIndex !== this.currentIndexSafe) this.currentIndex = this.currentIndexSafe
      this.$nextTick(() => {
        this.renderIndexChart()
        this.renderProbabilityChart()
      })
    }
  },
  mounted() {
    this.updateClock()
    this.clockTimer = setInterval(this.updateClock, 1000)
    this.$nextTick(() => {
      this.initCharts()
      window.addEventListener('resize', this.handleResize)
      window.addEventListener('keydown', this.handleKeydown)
    })
  },
  beforeDestroy() {
    clearInterval(this.clockTimer)
    clearInterval(this.playTimer)
    window.removeEventListener('resize', this.handleResize)
    window.removeEventListener('keydown', this.handleKeydown)
    document.body.style.overflow = ''
    Object.keys(this.charts).forEach(key => this.charts[key] && this.charts[key].dispose())
  },
  methods: {
    updateClock() {
      const now = new Date()
      this.nowTime = now.toLocaleTimeString('zh-CN', { hour12: false })
      this.nowDate = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '.')
    },
    refreshData() {
      this.dataUpdatedAt = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
      this.renderCharts()
    },
    toggleLiveStatus() {
      if (this.historyMode && this.$router && this.$route) {
        const query = { ...(this.$route.query || {}) }
        delete query.view
        delete query.start
        this.$router.replace({ query }).catch(() => {})
        this.liveStatusVisible = true
        this.refreshLiveStatus()
        return
      }
      this.liveStatusVisible = !this.liveStatusVisible
      if (this.liveStatusVisible) this.refreshLiveStatus()
    },
    refreshLiveStatus() {
      if (this.liveStatusLoading || typeof fetch !== 'function') return
      this.liveStatusLoading = true
      const controller = typeof AbortController === 'function' ? new AbortController() : null
      const timeout = controller ? setTimeout(() => controller.abort(), 5000) : null
      const options = controller ? { signal: controller.signal } : undefined
      fetch('http://localhost:8000/wind', options)
        .then(response => {
          if (!response.ok) throw new Error(`HTTP ${response.status}`)
          return response.json()
        })
        .then(raw => {
          const nino = raw && raw.nino34
          if (!nino || typeof nino !== 'object') throw new Error('实时 ENSO 指数不可用')
          const anomaly = Number(nino.anomaly)
          const statusMap = { 'El Niño': '厄尔尼诺', 'La Niña': '拉尼娜', Neutral: '中性' }
          const hasAnomaly = Number.isFinite(anomaly)
          const formatted = hasAnomaly ? `${anomaly >= 0 ? '+' : ''}${anomaly.toFixed(2)}` : null
          this.monitor = {
            ...this.monitor,
            nino34: formatted || this.monitor.nino34,
            oni: formatted || this.monitor.oni,
            status: statusMap[nino.status] || this.monitor.status
          }
          this.dataUpdatedAt = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
        })
        .catch(() => {})
        .then(() => {
          if (timeout) clearTimeout(timeout)
          this.liveStatusLoading = false
        })
    },
    toggleMapMaximize() {
      this.mapMaximized = !this.mapMaximized
      document.body.style.overflow = this.mapMaximized ? 'hidden' : ''
      this.$nextTick(() => this.handleResize())
    },
    handleKeydown(event) {
      if (event.key === 'Escape' && this.mapMaximized) this.toggleMapMaximize()
    },
    scrollToModule(id) {
      const target = document.getElementById(id)
      if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    togglePlayback() {
      if (this.isPlaying) {
        this.stopPlayback()
        return
      }
      const length = this.activePeriods.length
      if (length < 2) return
      this.isPlaying = true
      this.playTimer = setInterval(() => {
        this.currentIndex = (this.currentIndexSafe + 1) % length
      }, 900)
    },
    selectHistoryType(type) {
      this.historyType = type
      // The selector is also available in live mode; selecting a tab enters
      // the historical view and keeps the query in sync.
      this.syncHistoryQuery()
    },
    // ---- 演变时间轴 × AI 助手联动 ----
    // 选中 ENSO 历史事件或 LSTA-Swin 历史预测后，只要操作演变时间轴
    // （点标签 / 拖滑杆 / 播放），就唤起全局 AI 助手的报告解读弹窗。
    onPlayClick() {
      const wasPlaying = this.isPlaying
      this.togglePlayback()
      if (!wasPlaying && this.isPlaying) this.openAiBriefingFromTimeline('play')
    },
    selectTimelineIndex(index) {
      this.currentIndex = index
      this.openAiBriefingFromTimeline('label')
    },
    onTimelineChange() {
      this.openAiBriefingFromTimeline('slider')
    },
    historyTimelineContext() {
      return {
        month: this.historyCurrentMonthLabel,
        value: this.historyCurrentValue === null ? '—' : `${this.signedValue(this.historyCurrentValue)}`,
        phase: this.historyPhase,
        range: this.historyCurrentRange,
        cursor: this.currentIndexSafe + 1,
        total: this.historySeries.length,
        hasData: this.historyHasData
      }
    },
    openAiBriefingFromTimeline(trigger) {
      if (!this.historyMode) {
        this.$message({
          type: 'info',
          showClose: true,
          duration: 3000,
          message: '请先选择「ENSO 历史事件」或「LSTA-Swin 历史预测」，再点击演变时间轴查看 AI 解读'
        })
        return
      }
      const timeline = this.historyTimelineContext()
      if (this.historyType === 'event') {
        const item = this.historyActiveItem || {}
        openAiBriefing({
          kind: 'event',
          trigger,
          timeline,
          event: {
            id: item.id,
            label: item.label,
            type: item.type,
            start: item.start,
            end: item.end,
            peak: item.peak,
            phase: item.phase,
            description: item.description,
            source: item.source,
            sourceUrl: item.sourceUrl,
            hasData: item.hasData
          }
        })
        return
      }
      const month = this.selectedHistoryModelStart
      const reports = getModelReports(month)
      openAiBriefing({
        kind: 'model',
        trigger,
        month,
        timeline,
        available: reports.available,
        impact: reports.impact,
        validation: reports.validation
      })
    },
    syncHistoryQuery() {
      if (!this.$route || !this.$router) return
      const query = { ...(this.$route.query || {}), view: 'history' }
      if (this.historyType === 'model') query.start = this.selectedHistoryModelStart
      else delete query.start
      const current = this.$route.query || {}
      const sameStart = (Array.isArray(current.start) ? current.start[0] : current.start) === query.start
      if (current.view === query.view && sameStart) return
      this.$router.replace({ query }).catch(() => {})
    },
    resetHistorySelection() {
      if (!this.historyMode) return
      this.stopPlayback()
      if (!this.historyHasData) this.mapMaximized = false
      const defaultIndex = this.historyType === 'event' && this.selectedHistoryEventId === '2015-16' ? 9 : 0
      this.currentIndex = Math.min(defaultIndex, Math.max(this.historySeries.length - 1, 0))
      this.$nextTick(() => this.renderCharts())
    },
    stopPlayback() {
      if (this.playTimer) clearInterval(this.playTimer)
      this.playTimer = null
      this.isPlaying = false
    },
    signedValue(value) {
      if (value === null || value === undefined || value === '') return '—'
      const number = Number(value)
      if (!Number.isFinite(number)) return '—'
      return `${number > 0 ? '+' : ''}${number.toFixed(2)}`
    },
    initCharts() {
      if (!this.$refs.probabilityChart || !this.$refs.indexChart) return
      this.charts.probability = echarts.init(this.$refs.probabilityChart)
      this.charts.index = echarts.init(this.$refs.indexChart)
      this.renderCharts()
    },
    renderCharts() {
      this.renderProbabilityChart()
      this.renderIndexChart()
    },
    renderProbabilityChart() {
      if (!this.charts.probability) return
      const textColor = '#8aa8bc'
      const gridColor = 'rgba(75, 136, 164, .16)'
      const outlook = this.probabilityOutlook
      this.charts.probability.setOption({
        animationDuration: 1100,
        animationEasing: 'cubicOut',
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(3, 20, 35, .96)', borderColor: '#1d718b', textStyle: { color: '#e3f6ff', fontSize: 11 }, formatter: params => `${params[0].axisValue}<br/>${params.map(item => `${item.marker} ${item.seriesName}：${item.value}%`).join('<br/>')}` },
        legend: { show: false },
        grid: { left: 34, right: 10, top: 12, bottom: 28 },
        xAxis: { type: 'category', data: outlook.labels, axisTick: { show: false }, axisLine: { lineStyle: { color: '#29465b' } }, axisLabel: { color: textColor, fontSize: 9, margin: 9 } },
        yAxis: { type: 'value', max: 100, interval: 50, axisLabel: { color: textColor, fontSize: 9, formatter: '{value}%' }, axisTick: { show: false }, axisLine: { show: false }, splitLine: { lineStyle: { color: gridColor, type: 'dashed' } } },
        series: [
          { name: '厄尔尼诺', type: 'bar', stack: 'total', barWidth: 17, itemStyle: { color: '#ff9b5e', borderRadius: [3, 3, 0, 0] }, data: outlook.elNino },
          { name: '中性', type: 'bar', stack: 'total', barWidth: 17, itemStyle: { color: '#25c8bd' }, data: outlook.neutral },
          { name: '拉尼娜', type: 'bar', stack: 'total', barWidth: 17, itemStyle: { color: '#378bce', borderRadius: [0, 0, 3, 3] }, data: outlook.laNina }
        ]
      }, true)
    },
    renderIndexChart() {
      if (!this.charts.index) return
      if (this.historyMode) {
        const labels = this.historySeries.map(point => point.month)
        const values = this.historySeries.map(point => point.value === null || point.value === undefined || point.value === '' ? null : (Number.isFinite(Number(point.value)) ? Number(point.value) : null))
        const observed = this.historySeries.map(point => point.observed === null || point.observed === undefined || point.observed === '' ? null : (Number.isFinite(Number(point.observed)) ? Number(point.observed) : null))
        const allValues = values.concat(observed).filter(value => value !== null)
        const min = allValues.length ? Math.floor(Math.min(-0.5, ...allValues) - 0.25) : -1
        const max = allValues.length ? Math.ceil(Math.max(0.5, ...allValues) + 0.25) : 2
        const series = []
        if (this.historyHasData) {
          series.push({
            name: this.historyType === 'event' ? '事件 ONI' : 'LSTA-Swin 预测', type: 'line', smooth: .25, showSymbol: false,
            data: values, lineStyle: { width: 2.5, color: '#ffad64', type: this.historyType === 'event' ? 'solid' : 'dashed' },
            itemStyle: { color: '#ffb36c' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255, 153, 87, .18)' }, { offset: 1, color: 'rgba(255, 153, 87, 0)' }]) },
            markPoint: this.historyCurrentValue === null ? undefined : { symbol: 'circle', symbolSize: 9, itemStyle: { color: '#fff0c5', borderColor: '#ff9e5f', borderWidth: 2 }, label: { show: false }, data: [{ coord: [labels[this.currentIndexSafe] || labels[0], this.historyCurrentValue] }] },
            markLine: {
              symbol: 'none',
              silent: true,
              label: { show: false },
              lineStyle: { color: 'rgba(220, 180, 125, .5)', type: 'dotted', width: 1 },
              data: [
                { yAxis: .5 },
                { yAxis: -.5 },
                ...(this.historyCurrentValue === null || !labels[this.currentIndexSafe] ? [] : [{ xAxis: labels[this.currentIndexSafe], lineStyle: { color: 'rgba(255, 214, 139, .72)', type: 'dashed', width: 1 } }])
              ]
            }
          })
        }
        if (this.historyType === 'model' && observed.some(value => value !== null)) {
          series.push({ name: '观测代理', type: 'line', smooth: .25, showSymbol: false, data: observed, lineStyle: { width: 2, color: '#42d8e5' }, itemStyle: { color: '#72f2eb' } })
        }
        this.charts.index.setOption({
          animationDuration: 500,
          tooltip: { trigger: 'axis', backgroundColor: 'rgba(3, 20, 35, .96)', borderColor: '#1d718b', textStyle: { color: '#e3f6ff', fontSize: 11 }, formatter: params => `${params[0] ? params[0].axisValue : ''}<br/>${params.filter(item => item.value != null).map(item => `${item.marker} ${item.seriesName}：${Number(item.value).toFixed(2)}°C`).join('<br/>')}` },
          grid: { left: 34, right: 10, top: 14, bottom: 32 },
          xAxis: { type: 'category', boundaryGap: false, data: labels, axisTick: { show: false }, axisLine: { lineStyle: { color: '#29465b' } }, axisLabel: { color: '#839fb4', fontSize: 8, interval: Math.max(0, Math.ceil(labels.length / 7) - 1), margin: 8 } },
          yAxis: { type: 'value', min, max, interval: 1, axisTick: { show: false }, axisLine: { show: false }, axisLabel: { color: '#839fb4', fontSize: 9, formatter: '{value}°' }, splitLine: { lineStyle: { color: 'rgba(75, 136, 164, .15)', type: 'dashed' } } },
          series
        }, true)
        return
      }
      const labels = ['MAM', 'AMJ', 'MJJ', 'JJA', 'JAS', 'ASO', 'SON', 'OND', 'NDJ']
      const baseAxis = { axisTick: { show: false }, axisLine: { lineStyle: { color: '#29465b' } }, axisLabel: { color: '#839fb4', fontSize: 8, interval: 1, margin: 8 } }
      this.charts.index.setOption({
        animationDuration: 1300,
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(3, 20, 35, .96)', borderColor: '#1d718b', textStyle: { color: '#e3f6ff', fontSize: 11 }, formatter: params => `${params[0].axisValue}<br/>${params.filter(item => item.value != null).map(item => `${item.marker} ${item.seriesName}：${item.value.toFixed(1)}°C`).join('<br/>')}` },
        grid: { left: 34, right: 10, top: 14, bottom: 32 },
        xAxis: { type: 'category', boundaryGap: false, data: labels, ...baseAxis },
        yAxis: { type: 'value', min: -1, max: 2, interval: 1, axisTick: { show: false }, axisLine: { show: false }, axisLabel: { color: '#839fb4', fontSize: 9, formatter: '{value}°' }, splitLine: { lineStyle: { color: 'rgba(75, 136, 164, .15)', type: 'dashed' } } },
        series: [
          { name: '观测', type: 'line', smooth: .35, showSymbol: true, symbolSize: 5, connectNulls: false, data: [.2, .3, .5, .6, .7, .8, null, null, null], lineStyle: { width: 2.5, color: '#42d8e5' }, itemStyle: { color: '#72f2eb', borderColor: '#092638', borderWidth: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(33, 201, 217, .2)' }, { offset: 1, color: 'rgba(33, 201, 217, 0)' }]) } },
          { name: '预测', type: 'line', smooth: .35, showSymbol: true, symbolSize: 4, connectNulls: false, data: [null, null, null, null, null, .8, .82, .75, .63], lineStyle: { width: 2, type: 'dashed', color: '#ffad64' }, itemStyle: { color: '#ffb36c' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255, 153, 87, .18)' }, { offset: 1, color: 'rgba(255, 153, 87, 0)' }]) }, markLine: { symbol: 'none', silent: true, label: { show: false }, lineStyle: { color: 'rgba(255, 176, 107, .45)', type: 'dashed', width: 1 }, data: [{ yAxis: .5 }, { yAxis: -.5 }] } }
        ]
      }, true)
    },
    handleResize() {
      Object.keys(this.charts).forEach(key => this.charts[key] && this.charts[key].resize())
    }
  }
}
</script>

<style scoped>
.enso-screen{--bg:#041522;--panel:rgba(7,31,48,.88);--line:rgba(61,144,172,.34);--cyan:#46dce5;--teal:#43dfcc;--text:#e3f5fb;--muted:#819bad;position:relative;isolation:isolate;display:flex;flex-direction:column;gap:10px;width:100%;height:100vh;min-height:650px;padding:14px 18px 10px;box-sizing:border-box;overflow:hidden;color:var(--text);background:radial-gradient(ellipse at 50% 42%,rgba(8,64,86,.44),transparent 39%),radial-gradient(ellipse at 7% 52%,rgba(0,109,133,.12),transparent 31%),linear-gradient(135deg,#041321 0%,#061c2d 52%,#041422 100%);font-family:Inter,"PingFang SC","Microsoft YaHei",sans-serif}
.enso-screen:before{position:absolute;z-index:-2;inset:0;content:"";opacity:.34;background-image:linear-gradient(rgba(66,159,182,.035) 1px,transparent 1px),linear-gradient(90deg,rgba(66,159,182,.035) 1px,transparent 1px);background-size:36px 36px;mask-image:linear-gradient(90deg,transparent,#000 15%,#000 85%,transparent)}
.enso-screen:after{position:absolute;z-index:-1;inset:0;pointer-events:none;content:"";background:linear-gradient(180deg,rgba(1,10,20,.38),transparent 17%,transparent 82%,rgba(1,9,17,.58))}
.ambient-glow{position:absolute;z-index:-1;width:330px;height:330px;border-radius:50%;filter:blur(100px);pointer-events:none;opacity:.14}.ambient-glow-left{left:12%;top:34%;background:#00c8db}.ambient-glow-right{right:6%;top:16%;background:#156bd5;opacity:.1}
.screen-header{position:relative;z-index:20;display:grid;grid-template-columns:1fr 1.15fr 1fr;align-items:center;flex:0 0 54px;min-width:0;border-bottom:1px solid rgba(58,144,175,.32)}
.screen-header:before,.screen-header:after{position:absolute;bottom:-1px;width:14%;height:2px;content:"";background:linear-gradient(90deg,transparent,var(--cyan),transparent);filter:drop-shadow(0 0 5px var(--cyan))}.screen-header:before{left:0}.screen-header:after{right:0}
.history-control{display:flex;flex:0 0 auto;flex-direction:column;gap:10px;padding:10px 13px;overflow:visible;clip-path:none}
.history-control-fields{display:flex;align-items:center;gap:9px;min-width:0}.history-type-switch{display:flex;flex:0 0 auto;gap:3px;padding:3px;border:1px solid rgba(63,142,166,.24);border-radius:5px;background:rgba(2,17,28,.5)}.history-type-switch button{padding:7px 9px;border:0;border-radius:3px;color:#7794a7;background:transparent;font:inherit;font-size:9px;white-space:nowrap;cursor:pointer}.history-type-switch button i{margin-right:4px}.history-type-switch button.active{color:#c8fbf1;background:linear-gradient(135deg,rgba(28,142,160,.45),rgba(28,98,126,.3));box-shadow:inset 0 0 0 1px rgba(75,210,216,.27)}.history-select{flex:1;min-width:200px;max-width:440px}.history-select .el-input__inner{height:31px;border-color:rgba(74,163,181,.38);color:#cceaf0;background:rgba(4,25,39,.78);font-size:10px}.history-option{display:flex;align-items:center;gap:6px}.history-option i{width:6px;height:6px;border-radius:50%;background:#ffad69;box-shadow:0 0 7px #ffad69}.history-option i.cool{background:#4fd4dc;box-shadow:0 0 7px #4fd4dc}.history-option small{margin-left:auto;color:#7693a2;font-size:8px}.history-current-summary{display:flex;flex:1;min-width:170px;flex-direction:column;align-items:flex-end;gap:2px;color:#7894a5;font-size:8px;text-align:right}.history-current-summary strong{color:#e7f8fa;font-size:13px;font-weight:600}.history-current-summary small{color:#63d5ce;font-size:8px;white-space:nowrap}.history-map-empty{display:flex;flex:1;min-height:230px;align-items:center;justify-content:center;flex-direction:column;gap:7px;margin:9px 10px 4px;border:1px dashed rgba(86,167,181,.3);border-radius:7px;color:#7897a5;background:rgba(3,20,34,.55);text-align:center}.history-map-empty i,.history-chart-empty i{color:#60c9d1;font-size:25px}.history-map-empty strong{color:#cce9ee;font-size:13px}.history-map-empty span{max-width:280px;color:#7897a5;font-size:9px;line-height:1.6}.history-chart-empty{position:absolute;inset:57px 0 25px;display:flex;align-items:center;justify-content:center;flex-direction:column;gap:7px;color:#7897a5;font-size:9px;background:rgba(3,20,34,.72);pointer-events:none}.history-chart-empty i{font-size:22px}
.brand-lockup{display:flex;align-items:center;gap:10px}.brand-symbol{display:grid;width:35px;height:35px;place-items:center;border:1px solid rgba(71,221,225,.45);border-radius:10px;color:#68eeea;background:linear-gradient(145deg,rgba(30,171,198,.24),rgba(9,55,78,.2));box-shadow:0 0 20px rgba(53,218,224,.11),inset 0 0 12px rgba(53,218,224,.13);font-size:17px}.brand-lockup strong,.brand-lockup small{display:block}.brand-lockup strong{font-size:10px;letter-spacing:1.65px;color:#bfe6ed}.brand-lockup small{margin-top:4px;color:#718da1;font-size:10px;letter-spacing:.5px}
.screen-title{text-align:center}.title-kicker{font-size:8px;letter-spacing:3px;color:#44c9d8}.screen-title h1{margin:3px 0 0;color:#f0fbff;font-size:20px;font-weight:650;letter-spacing:2.5px;text-shadow:0 0 20px rgba(69,214,230,.2)}
.header-status{display:flex;align-items:center;justify-content:flex-end;gap:14px;color:#829eb1;font-size:10px}.live-indicator{display:flex;align-items:center;gap:6px;color:#76dfc7;white-space:nowrap}.live-indicator i,.data-age i,.risk-status i{width:6px;height:6px;border-radius:50%;background:#45e4c1;box-shadow:0 0 8px #3ae2c1;animation:statusPulse 1.8s ease-in-out infinite}.clock{color:#e3f5fb;font-size:15px;font-variant-numeric:tabular-nums;letter-spacing:1px}.header-date{white-space:nowrap}.realtime-status-button{display:flex;align-items:center;gap:5px;padding:6px 9px;border:1px solid rgba(72,195,204,.36);border-radius:4px;color:#9be8e1;background:rgba(18,97,112,.22);font:inherit;font-size:9px;white-space:nowrap;cursor:pointer;transition:all .2s}.realtime-status-button:hover,.realtime-status-button.active{border-color:rgba(92,228,218,.78);color:#e5fffb;background:rgba(30,135,139,.34);box-shadow:0 0 15px rgba(62,213,204,.16)}.realtime-status-button i{font-size:11px}.realtime-status-panel{position:absolute;z-index:30;top:calc(100% + 8px);right:0;width:286px;padding:12px;border:1px solid rgba(73,187,198,.48);border-radius:7px;background:linear-gradient(145deg,rgba(7,37,53,.98),rgba(3,22,36,.98));box-shadow:0 18px 42px rgba(0,0,0,.4),0 0 20px rgba(51,192,196,.12)}.realtime-status-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:8px;padding-bottom:9px;border-bottom:1px solid rgba(67,135,160,.2)}.realtime-status-heading h2{margin:3px 0 0;color:#e0f5f4;font-size:13px;font-weight:600}.realtime-status-close{width:22px;height:22px;padding:0;border:1px solid rgba(91,178,191,.27);border-radius:3px;color:#85aeb8;background:rgba(16,71,87,.25);cursor:pointer}.realtime-status-close:hover{color:#eaffff;border-color:rgba(105,226,221,.65)}.realtime-status-phase{display:grid;grid-template-columns:auto 1fr;align-items:center;gap:5px 9px;padding:11px 0 9px}.realtime-status-phase .live-indicator{grid-column:1/-1;font-size:8px}.realtime-status-phase strong{color:#ffc77e;font-size:17px;font-weight:600}.realtime-status-phase small{color:#8ca9b5;font-size:8px;line-height:1.5}.realtime-status-metrics{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:6px}.realtime-status-metrics>div{padding:8px 7px;border:1px solid rgba(71,143,164,.2);background:rgba(8,40,56,.65)}.realtime-status-metrics span,.realtime-status-metrics strong{display:block}.realtime-status-metrics span{color:#7897a6;font-size:8px}.realtime-status-metrics strong{margin-top:4px;color:#dff5f2;font-size:13px;font-weight:600}.realtime-status-metrics em{margin-left:2px;color:#86aab3;font-size:8px;font-style:normal}.realtime-status-footer{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-top:10px;padding-top:8px;border-top:1px solid rgba(67,135,160,.16);color:#7896a4;font-size:8px}.realtime-status-footer button{padding:0;border:0;color:#6ddbd3;background:transparent;font:inherit;font-size:8px;cursor:pointer}.realtime-status-footer button:hover{color:#c8fffa}.realtime-status-footer button i{margin-right:3px}
.kpi-strip{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));flex:0 0 72px;border:1px solid rgba(55,134,161,.32);background:linear-gradient(105deg,rgba(9,39,58,.87),rgba(7,31,49,.7));box-shadow:inset 0 0 28px rgba(34,131,154,.055);clip-path:polygon(0 0,calc(100% - 15px) 0,100% 15px,100% 100%,15px 100%,0 calc(100% - 15px))}
.kpi-item{position:relative;display:flex;align-items:center;gap:12px;min-width:0;padding:0 16px}.kpi-item+.kpi-item:before{position:absolute;left:0;top:19px;bottom:19px;width:1px;content:"";background:linear-gradient(transparent,rgba(77,159,183,.3),transparent)}.kpi-icon{display:grid;flex:0 0 35px;width:35px;height:35px;place-items:center;border:1px solid rgba(80,174,190,.24);border-radius:50%;background:rgba(18,84,104,.27);font-size:17px}.warm-icon{color:#ffb167}.cyan-icon{color:#50e0d5}.blue-icon{color:#5ba9e8}.gold-icon{color:#ffd47b}.kpi-item div{min-width:0}.kpi-item small{display:block;color:#8ba7b7;font-size:10px;white-space:nowrap}.kpi-item strong{display:block;margin-top:3px;color:#ecfbff;font-size:23px;font-weight:650;line-height:1;letter-spacing:.25px;font-variant-numeric:tabular-nums}.kpi-item strong em{margin-left:3px;color:#9cc0cf;font-size:11px;font-style:normal;font-weight:500}.kpi-delta{margin-left:auto;color:#7895a7;font-size:9px;white-space:nowrap}.kpi-delta.up{color:#ffad69}.phase-kpi .phase-text{font-size:18px;color:#ffc077}.phase-chip{display:flex;align-items:center;gap:5px;margin-left:auto;padding:5px 8px;border:1px solid rgba(255,176,104,.22);border-radius:3px;color:#ffbd78;background:rgba(173,89,35,.12);font-size:9px;white-space:nowrap}.phase-chip i{width:5px;height:5px;border-radius:50%;background:#ffac61;box-shadow:0 0 6px #ff9f51}
.screen-body{display:grid;grid-template-columns:minmax(220px,22%) minmax(400px,1fr) minmax(255px,25%);gap:10px;flex:1;min-height:0}.side-column{display:grid;gap:10px;min-width:0;min-height:0}.left-column{grid-template-rows:1.12fr .97fr .96fr}.right-column{grid-template-rows:1fr 1.05fr .94fr}.panel-card{position:relative;min-width:0;min-height:0;overflow:hidden;border:1px solid var(--line);background:linear-gradient(135deg,rgba(9,37,55,.92),rgba(5,26,42,.87));box-shadow:inset 0 0 23px rgba(32,126,155,.055),0 8px 22px rgba(0,0,0,.12);clip-path:polygon(0 0,calc(100% - 10px) 0,100% 10px,100% 100%,0 100%);transition:border-color .24s,box-shadow .24s}.panel-card:hover{border-color:rgba(74,203,220,.62);box-shadow:inset 0 0 26px rgba(32,174,190,.09),0 0 18px rgba(15,178,205,.08)}.panel-card:before{position:absolute;top:0;left:0;width:34px;height:2px;content:"";background:linear-gradient(90deg,#69eee6,rgba(105,238,230,0));filter:drop-shadow(0 0 5px #52dfe2)}.panel-heading{position:relative;z-index:1;display:flex;align-items:center;justify-content:space-between;gap:8px;padding:12px 12px 8px}.panel-kicker{display:block;margin-bottom:3px;color:#58c6d2;font-size:7px;letter-spacing:1.4px;white-space:nowrap}.panel-heading h2,.map-heading h2{margin:0;color:#e1f4f8;font-size:13px;font-weight:600;letter-spacing:.4px}.data-age{display:flex;align-items:center;gap:5px;color:#7591a5;font-size:8px;white-space:nowrap}.data-age i{width:5px;height:5px}.quiet-action{padding:4px 7px;border:1px solid rgba(83,162,181,.25);border-radius:3px;color:#8fb7c5;background:rgba(14,65,84,.2);font-size:9px;cursor:pointer}.quiet-action:hover{color:#e4fbff;border-color:#49d7dc}.unit-label{color:#7793a7;font-size:9px}
.phase-reading{display:flex;align-items:center;gap:13px;padding:3px 12px 8px}.phase-orbit{position:relative;display:grid;flex:0 0 94px;width:94px;height:94px;place-items:center}.orbit-track{position:absolute;inset:6px;border:1px solid rgba(67,191,207,.3);border-radius:50%;background:conic-gradient(from 205deg,rgba(25,209,216,.12),rgba(45,219,214,.94) 34%,rgba(255,169,88,.92) 72%,rgba(255,169,88,.14) 77%,rgba(29,76,101,.12) 78%);mask:radial-gradient(farthest-side,transparent calc(100% - 4px),#000 calc(100% - 3px))}.phase-orbit:before,.phase-orbit:after{position:absolute;inset:0;border:1px solid rgba(58,171,193,.1);border-radius:50%;content:""}.phase-orbit:after{inset:13px;border-style:dashed;animation:orbit 24s linear infinite}.orbit-core{z-index:1;text-align:center}.orbit-core span,.orbit-core small{display:block;color:#7c9bac;font-size:8px;letter-spacing:1px}.orbit-core strong{display:inline-block;margin-top:3px;color:#ffb06a;font-size:25px;font-weight:600;font-variant-numeric:tabular-nums}.orbit-core small{display:inline;margin-left:2px}.orbit-satellite{position:absolute;top:7px;right:18px;width:6px;height:6px;border-radius:50%;background:#ffc977;box-shadow:0 0 10px #ffab61}.phase-copy{flex:1;min-width:0}.phase-label{display:block;color:#7695a8;font-size:9px}.phase-copy>strong{display:block;margin:4px 0;color:#ffd08a;font-size:14px}.phase-caption{display:block;color:#86a2b3;font-size:9px;white-space:nowrap}.threshold-track{position:relative;height:4px;margin-top:12px;border-radius:4px;background:#173d50}.threshold-track:before{position:absolute;left:25%;top:-3px;height:10px;border-left:1px dashed #ffcf7e;content:""}.threshold-track i{display:block;width:62%;height:100%;border-radius:4px;background:linear-gradient(90deg,#40d5dc,#ffb563);box-shadow:0 0 8px rgba(255,177,95,.48)}.threshold-caption{display:flex;justify-content:space-between;margin-top:4px;color:#647f92;font-size:8px}.observation-grid{display:grid;grid-template-columns:repeat(3,1fr);margin:0 12px;padding:8px 0 9px;border-top:1px solid rgba(67,135,160,.19)}.observation-grid>div+div{padding-left:8px;border-left:1px solid rgba(67,135,160,.18)}.observation-grid span{display:block;color:#7692a3;font-size:8px;white-space:nowrap}.observation-grid strong{display:block;margin-top:4px;color:#dceff5;font-size:13px;font-variant-numeric:tabular-nums}.observation-grid strong span{display:inline;color:#7895a8;font-size:8px}.warm-text{color:#ffb36b!important}.cyan-text{color:#48dfd3!important}.blue-text{color:#74b8ef!important}
.intensity-meter{display:grid;grid-template-columns:34px minmax(0,1fr);gap:8px;padding:5px 12px 0}.intensity-scale{display:flex;flex-direction:column;justify-content:space-between;padding:0 0 4px;color:#667f92;font-size:8px}.intensity-bars{display:flex;flex-direction:column;gap:8px}.heat-row{display:grid;grid-template-columns:40px minmax(0,1fr) 40px;align-items:center;gap:6px;color:#86a1b2;font-size:9px}.heat-row b{text-align:right;color:#ffb16b;font-size:9px;font-weight:500;font-variant-numeric:tabular-nums}.heat-track{height:8px;border:1px solid rgba(57,130,155,.18);border-radius:8px;background:rgba(5,19,32,.8);overflow:hidden}.heat-track i{display:block;height:100%;border-radius:8px;background:linear-gradient(90deg,#1ec9d3,#56e0c8 42%,#ffc16d 83%,#ff8b62);box-shadow:0 0 8px rgba(77,220,206,.25);animation:barIn 1.2s ease-out both}.heat-row:nth-child(2) .heat-track i{animation-delay:.08s}.heat-row:nth-child(3) .heat-track i{animation-delay:.16s}.heat-row:nth-child(4) .heat-track i{animation-delay:.24s}.panel-footnote{display:flex;align-items:center;gap:6px;margin:10px 12px 0;padding-top:8px;border-top:1px solid rgba(67,135,160,.16);color:#83a0ae;font-size:8px;white-space:nowrap}.pulse-dot,.caption-dot{width:5px;height:5px;flex:0 0 5px;border-radius:50%;background:#52e0d0;box-shadow:0 0 8px #52e0d0;animation:statusPulse 1.7s infinite}
.signal-list{padding:0 12px}.signal-row{display:grid;grid-template-columns:8px minmax(0,1fr) auto;align-items:center;gap:7px;padding:7px 0;border-top:1px solid rgba(67,135,160,.15)}.signal-marker{width:6px;height:6px;border-radius:50%;box-shadow:0 0 8px currentColor}.signal-marker.warm{color:#ffad69;background:#ffad69}.signal-marker.cyan{color:#47dfd2;background:#47dfd2}.signal-marker.blue{color:#61b5ee;background:#61b5ee}.signal-row strong,.signal-row small{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.signal-row strong{color:#d3eaf0;font-size:9px;font-weight:500}.signal-row small{margin-top:3px;color:#7691a4;font-size:8px}.signal-row time{color:#638094;font-size:8px;white-space:nowrap}
.center-column{display:flex;flex-direction:column;gap:10px;min-width:0;min-height:0}.map-card{display:flex;flex:1;flex-direction:column}.map-heading{display:flex;align-items:center;justify-content:space-between;gap:12px;min-height:50px;padding:0 13px;border-bottom:1px solid rgba(67,135,160,.18)}.map-title{display:flex;align-items:center;gap:9px;min-width:0}.map-live-icon{position:relative;display:grid;flex:0 0 27px;width:27px;height:27px;place-items:center;border:1px solid rgba(61,199,211,.3);border-radius:50%;background:rgba(17,92,113,.3)}.map-live-icon:before,.map-live-icon:after,.map-live-icon i{position:absolute;inset:5px;border:1px solid rgba(59,218,219,.33);border-radius:50%;content:""}.map-live-icon:after{inset:9px;background:#53e0db;box-shadow:0 0 8px #57e0df}.map-heading h2{font-size:12px}.layer-switch{display:flex;align-items:center;gap:3px;padding:3px;border:1px solid rgba(63,142,166,.22);border-radius:5px;background:rgba(2,17,28,.5)}.layer-switch button{padding:5px 7px;border:0;border-radius:3px;color:#7896a8;background:transparent;font-size:8px;white-space:nowrap;cursor:pointer;transition:all .2s}.layer-switch button i{margin-right:4px}.layer-switch button.active{color:#bff6f2;background:linear-gradient(135deg,rgba(28,142,160,.45),rgba(28,98,126,.3));box-shadow:inset 0 0 0 1px rgba(75,210,216,.27)}
.pacific-visual{position:relative;flex:1;min-height:0;margin:9px 10px 4px;border:1px solid rgba(31,121,149,.26);border-radius:7px;overflow:hidden;background:radial-gradient(ellipse at 56% 50%,rgba(18,93,116,.15),transparent 55%),linear-gradient(180deg,rgba(5,27,44,.52),rgba(4,20,35,.67))}.pacific-visual:before{position:absolute;z-index:2;inset:0;pointer-events:none;content:"";background:linear-gradient(180deg,rgba(24,103,126,.06),transparent 12%,transparent 80%,rgba(4,19,32,.35))}.pacific-visual:after{position:absolute;z-index:1;inset:0;pointer-events:none;content:"";opacity:.24;background:linear-gradient(180deg,transparent 0,rgba(54,215,220,.055) 50%,transparent 100%);background-size:100% 180px;animation:scan 8s linear infinite}.pacific-map{position:absolute;inset:0;width:100%;height:100%}.map-corner{position:absolute;z-index:3;top:10px;display:flex;align-items:center;gap:7px;color:#789bad;font-size:8px}.map-corner-tl{left:12px;flex-direction:column;align-items:flex-start;gap:3px}.map-corner-tl span{color:#5f899d}.map-corner-tl b{color:#d7eff5;font-weight:500;letter-spacing:.5px}.map-corner-tr{right:12px}.map-corner-tr i{width:5px;height:5px;border-radius:50%;background:#48dfd1;box-shadow:0 0 7px #48dfd1}.graticule path{fill:none;stroke:#6dd2df;stroke-opacity:.19;stroke-width:1;stroke-dasharray:3 5}.graticule .equator{stroke:#5adce1;stroke-opacity:.47;stroke-width:1.2;stroke-dasharray:7 4}.sst-anomaly{transition:opacity .45s}.heat-bloom{animation:heatBreath 5s ease-in-out infinite;transform-origin:570px 316px}.isotherm{fill:none;stroke:#66ecdf;stroke-opacity:.56;stroke-width:1.1;stroke-dasharray:5 7;animation:contourDrift 26s linear infinite}.isotherm-warm{stroke:#ffc175;stroke-opacity:.76;animation-direction:reverse}.wind-streams{transition:opacity .45s}.wind-line{stroke-width:1.25;stroke-dasharray:5 9;animation:windFlow 9s linear infinite}.wind-b{animation-duration:11s;animation-delay:-4s}.wind-c{animation-duration:13s;animation-delay:-3s}.wind-d{animation-duration:10s;animation-delay:-6s}.landmass{fill:#173647;stroke:#367388;stroke-width:1.2;stroke-linejoin:round;filter:drop-shadow(0 0 6px rgba(25,119,145,.26))}.land-shine{stroke:#59a3ad;stroke-opacity:.49;stroke-width:1;stroke-linecap:round;stroke-linejoin:round}.map-labels{fill:#b7d4dc;font-size:12px;letter-spacing:.5px}.map-labels .label-sub{fill:#64879b;font-size:8px;letter-spacing:1.2px}.map-labels .sea-label{fill:#62b8c3;font-size:11px;letter-spacing:2px}.map-labels .latitude-label,.map-labels .longitude-label{fill:#638ca0;font-size:9px}.nino-zone rect:first-child{fill:rgba(255,174,95,.07);stroke:#ffbe72;stroke-width:1;stroke-dasharray:4 4}.nino-zone path{fill:none;stroke:#ffd08a;stroke-width:1.8}.zone-point{fill:#fff1c9;filter:drop-shadow(0 0 5px #ffc277)}.zone-ripple{fill:none;stroke:#ffd18b;stroke-width:1;opacity:.7;animation:ripple 2.4s ease-out infinite}.zone-label-bg{fill:rgba(5,29,45,.9);stroke:rgba(255,185,111,.45);stroke-width:1}.nino-zone text{fill:#ffd49a;font-size:10px}.current-marker{fill:none;stroke:#fff0bf;stroke-width:1;filter:drop-shadow(0 0 4px #ffb266);animation:markerPulse 2.4s ease-in-out infinite}.current-marker circle:first-child{fill:#ffd38a;stroke:#fff1cb}.current-marker circle:nth-child(2){stroke:#ffb76d;opacity:.5}.map-legend{position:absolute;z-index:3;left:12px;bottom:11px;display:flex;align-items:center;gap:6px;color:#7e9cac;font-size:8px}.legend-title{margin-right:2px;color:#b6d4de}.legend-cold,.legend-neutral,.legend-warm{display:block;width:19px;height:5px;border-radius:4px}.legend-cold{background:#22c6df}.legend-neutral{width:32px;background:linear-gradient(90deg,#22c6df,#3ed8bd,#ffba67)}.legend-warm{display:none;background:#ff9d5d}.legend-divider{height:12px;margin:0 3px;border-left:1px solid rgba(91,147,163,.32)}.wind-legend{display:flex;align-items:center;gap:4px}.wind-legend i{display:block;width:14px;border-top:1px dashed #55dfe2}.map-scale{position:absolute;z-index:3;right:12px;bottom:13px;display:flex;align-items:center;gap:6px;color:#718d9f;font-size:8px}.map-scale i{width:35px;border-top:1px solid #a1cbd0}.map-caption{position:absolute;z-index:3;right:12px;bottom:38px;display:flex;align-items:center;gap:7px;padding:7px 9px;border:1px solid rgba(244,172,96,.32);border-radius:4px;background:rgba(4,26,41,.86);box-shadow:0 0 16px rgba(244,159,80,.08)}.map-caption>div strong,.map-caption>div small{display:block}.map-caption>div strong{color:#ffe0b1;font-size:9px;font-weight:600}.map-caption>div small{margin-top:3px;color:#8ea9b5;font-size:7px}.map-caption>b{margin-left:4px;color:#ffbb74;font-size:13px;font-weight:600}.caption-dot{background:#ffb16b;box-shadow:0 0 8px #ffad65}.map-bottomline{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:5px 13px 8px;color:#708b9c;font-size:8px}.map-bottomline span:first-child{display:flex;align-items:center;gap:5px;color:#75cfbf}.map-bottomline span:first-child i{width:5px;height:5px;border-radius:50%;background:#4de0c2;box-shadow:0 0 6px #4de0c2}
.sst-mode .wind-streams{opacity:.12}.wind-mode .sst-anomaly{opacity:.14}.wind-mode .wind-streams{opacity:1}.combined-mode .sst-anomaly,.combined-mode .wind-streams{opacity:1}.map-stat-row{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px;flex:0 0 54px}.map-stat-row article{display:flex;align-items:center;gap:8px;min-width:0;padding:0 9px;border:1px solid rgba(58,134,157,.27);background:linear-gradient(100deg,rgba(8,41,59,.86),rgba(6,30,47,.75));clip-path:polygon(0 0,calc(100% - 8px) 0,100% 8px,100% 100%,0 100%)}.stat-glyph{display:grid;flex:0 0 27px;width:27px;height:27px;place-items:center;border-radius:7px;background:rgba(49,157,169,.15);font-size:13px}.warm-glyph{color:#ffb069}.cyan-glyph{color:#55ddd1}.blue-glyph{color:#6db8ea}.map-stat-row article div{min-width:0}.map-stat-row small,.map-stat-row strong{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.map-stat-row small{color:#7593a4;font-size:8px}.map-stat-row strong{margin-top:4px;color:#d8edf2;font-size:10px;font-weight:500}.map-stat-row article>b{margin-left:auto;color:#9dd3da;font-size:9px;white-space:nowrap}
.probability-chart{height:calc(100% - 76px);min-height:90px;margin:0 4px}.chart-legend{display:flex;justify-content:center;gap:11px;padding:0 0 8px;color:#839daf;font-size:8px}.chart-legend span,.forecast-legend span{display:flex;align-items:center;gap:4px;white-space:nowrap}.chart-legend i{display:block;width:6px;height:6px;border-radius:2px}.legend-el-nino{background:#ff9b5e}.legend-neutral-dot{background:#25c8bd}.legend-la-nina{background:#378bce}.index-chart{height:calc(100% - 76px);min-height:94px;margin:0 4px}.forecast-legend{display:flex;justify-content:center;gap:10px;padding:0 0 8px;color:#829daf;font-size:8px}.forecast-legend i{display:block;width:11px;border-top:2px solid}.observed-line{border-color:#42d8e5}.forecast-line{border-color:#ffad64;border-top-style:dashed}.threshold-line{border-color:#dcb47d;border-top-style:dotted}.small-badge{padding:4px 6px;border:1px solid rgba(68,177,188,.25);border-radius:3px;color:#77c8d0;background:rgba(28,104,119,.15);font-size:8px;white-space:nowrap}
.impact-list{padding:0 12px}.impact-row{margin-bottom:9px}.impact-copy{display:flex;justify-content:space-between;gap:6px;margin-bottom:5px;color:#a8c0cb;font-size:9px}.impact-copy strong{font-size:8px;font-weight:500}.impact-copy strong.warm{color:#ffb16b}.impact-copy strong.cyan{color:#5addcf}.impact-copy strong.gold{color:#ffd27c}.impact-track{height:5px;border-radius:4px;background:#123247;overflow:hidden}.impact-track i{display:block;height:100%;border-radius:4px;box-shadow:0 0 8px currentColor;animation:barIn 1.1s ease-out both}.impact-track i.warm{color:#ffad69;background:linear-gradient(90deg,#d77650,#ffb86e)}.impact-track i.cyan{color:#54ded0;background:linear-gradient(90deg,#1bb0b9,#58e2d1)}.impact-track i.gold{color:#f2c66f;background:linear-gradient(90deg,#d3974f,#f3ce78)}.risk-status{display:flex;align-items:center;gap:5px;color:#f1c274;font-size:8px}.risk-status i{background:#f0bb61;box-shadow:0 0 7px #efb54f}.impact-note{margin:7px 12px 0;padding-top:6px;border-top:1px solid rgba(67,135,160,.15);color:#768f9e;font-size:7px;white-space:nowrap}.impact-note i{margin-right:4px;color:#e5b66d}
.time-control{display:flex;align-items:center;gap:12px;flex:0 0 47px;padding:0 12px;border:1px solid rgba(61,143,170,.29);background:linear-gradient(90deg,rgba(6,32,49,.9),rgba(8,39,56,.72),rgba(6,32,49,.9));box-shadow:inset 0 0 20px rgba(40,126,148,.04)}.timeline-label{display:flex;flex:0 0 112px;flex-direction:column;gap:2px}.timeline-label .panel-kicker{margin:0;font-size:6px;letter-spacing:1px}.timeline-label strong{color:#d4ebf0;font-size:10px;font-weight:500}.play-button{display:grid;flex:0 0 28px;width:28px;height:28px;place-items:center;border:1px solid rgba(72,195,204,.35);border-radius:50%;color:#71e5e0;background:rgba(25,113,131,.18);font-size:14px;cursor:pointer;transition:all .2s}.play-button:hover{border-color:#65e7e1;color:#fff;box-shadow:0 0 13px rgba(54,219,220,.25)}.timeline-track-wrap{flex:1;min-width:0}.timeline-labels{display:flex;justify-content:space-between;margin:0 1px 5px;color:#7895a7;font-size:8px}.timeline-labels span{position:relative;cursor:pointer;transition:color .2s}.timeline-labels span:hover,.timeline-labels span.selected{color:#ffd092}.timeline-labels span.selected:after{position:absolute;left:50%;bottom:-8px;width:5px;height:5px;border:1px solid #ffcf8e;border-radius:50%;background:#ffaf66;box-shadow:0 0 8px #ffaf66;content:"";transform:translateX(-50%)}.timeline-slider{display:block;width:100%;height:3px;margin:0;outline:0;appearance:none;border-radius:3px;background:linear-gradient(90deg,#47dcd4 var(--progress),#1c465d var(--progress));cursor:pointer}.timeline-slider::-webkit-slider-thumb{width:9px;height:9px;appearance:none;border:2px solid #d7ffed;border-radius:50%;background:#42ddcf;box-shadow:0 0 10px rgba(69,231,214,.8)}.timeline-slider::-moz-range-thumb{width:7px;height:7px;border:2px solid #d7ffed;border-radius:50%;background:#42ddcf;box-shadow:0 0 10px rgba(69,231,214,.8)}.timeline-current{display:flex;flex:0 0 120px;flex-direction:column;align-items:flex-end;gap:2px;padding-left:10px;border-left:1px solid rgba(71,143,165,.24)}.timeline-current span,.timeline-current small{color:#6f8b9d;font-size:7px}.timeline-current strong{color:#e9f9fa;font-size:10px;font-weight:600}.timeline-current small{color:#58d6cf}.screen-watermark{position:absolute;right:20px;bottom:2px;color:rgba(90,133,151,.31);font-size:6px;letter-spacing:1.1px;pointer-events:none}.screen-watermark span{color:rgba(82,212,212,.4)}
@keyframes statusPulse{0%,100%{opacity:.7;box-shadow:0 0 4px currentColor}50%{opacity:1;box-shadow:0 0 10px currentColor}}@keyframes orbit{to{transform:rotate(360deg)}}@keyframes heatBreath{0%,100%{transform:scale(.96);opacity:.82}50%{transform:scale(1.04);opacity:1}}@keyframes contourDrift{to{stroke-dashoffset:-100}}@keyframes windFlow{to{stroke-dashoffset:-140}}@keyframes ripple{0%{transform:scale(.6);opacity:.85}100%{transform:scale(2.4);opacity:0}}@keyframes markerPulse{0%,100%{opacity:.75;transform:scale(.96)}50%{opacity:1;transform:scale(1.06)}}@keyframes scan{0%{background-position:0 -180px}100%{background-position:0 calc(100% + 180px)}}@keyframes barIn{from{width:0}to{width:var(--bar-width,100%)}}
@media(max-width:1200px){.enso-screen{padding-right:12px;padding-left:12px}.screen-body{grid-template-columns:minmax(195px,22%) minmax(340px,1fr) minmax(225px,25%);gap:8px}.side-column,.center-column{gap:8px}.panel-heading{padding:9px 9px 6px}.phase-reading{gap:6px;padding-right:8px;padding-left:8px}.phase-orbit{flex-basis:80px;width:80px;height:80px}.observation-grid{margin-right:8px;margin-left:8px}.signal-list,.impact-list{padding-right:9px;padding-left:9px}.map-heading{padding-right:9px;padding-left:9px}.map-stat-row article{gap:5px;padding:0 6px}.map-stat-row article>b{font-size:8px}.kpi-item{gap:8px;padding:0 10px}}
@media(max-width:900px){.history-control{padding:10px}.history-control-fields{align-items:stretch;flex-wrap:wrap}.history-type-switch{flex:1 1 100%}.history-type-switch button{flex:1}.history-select{min-width:0;max-width:none;flex:1 1 55%}.history-current-summary{flex:1 1 40%;min-width:130px}}
@media(max-height:760px) and (min-width:901px){.enso-screen{min-height:580px;gap:7px;padding-top:9px;padding-bottom:7px}.screen-header{flex-basis:46px}.kpi-strip{flex-basis:62px}.screen-body{gap:7px}.side-column,.center-column{gap:7px}.panel-heading{padding-top:7px;padding-bottom:5px}.phase-orbit{width:76px;height:76px;flex-basis:76px}.phase-reading{padding-bottom:4px}.observation-grid{padding-top:5px;padding-bottom:5px}.intensity-bars{gap:6px}.panel-footnote{margin-top:6px;padding-top:5px}.signal-row{padding:5px 0}.map-stat-row{flex-basis:46px}.time-control{flex-basis:41px}.map-heading{min-height:44px}}
@media(max-width:900px){.enso-screen{height:auto;min-height:100vh;overflow:visible}.screen-header{grid-template-columns:1fr auto;flex-basis:auto;min-height:58px;padding-bottom:7px}.screen-title{grid-column:1/-1;grid-row:2;padding:7px 0 4px}.screen-title h1{font-size:17px}.header-status{grid-column:2;grid-row:1;gap:8px}.header-status .header-date{display:none}.brand-lockup strong{font-size:8px}.kpi-strip{flex-basis:auto;min-height:68px}.kpi-item{padding:9px 8px}.kpi-item strong{font-size:18px}.kpi-icon{flex-basis:29px;width:29px;height:29px;font-size:14px}.kpi-delta{display:none}.screen-body{grid-template-columns:minmax(0,1fr) minmax(250px,.78fr);grid-template-rows:minmax(480px,56vh) auto;flex:none}.center-column{grid-column:1/-1;grid-row:1}.map-card{min-height:410px}.left-column{grid-column:1;grid-row:2;grid-template-rows:repeat(3,minmax(175px,auto))}.right-column{grid-column:2;grid-row:2;grid-template-rows:repeat(3,minmax(175px,auto))}.time-control{flex-basis:55px}.screen-watermark{display:none}}
@media(max-width:620px){.enso-screen{gap:8px;padding:9px}.screen-header{grid-template-columns:minmax(0,1fr) auto}.brand-lockup small{font-size:8px}.brand-symbol{width:30px;height:30px}.header-status{gap:7px}.live-indicator{font-size:8px}.clock{font-size:12px}.screen-title{padding:6px 0}.title-kicker{font-size:7px;letter-spacing:2px}.screen-title h1{font-size:15px;letter-spacing:1.5px}.kpi-strip{grid-template-columns:repeat(2,minmax(0,1fr));clip-path:none}.kpi-item{min-height:54px;padding:6px 8px}.kpi-item:nth-child(3):before{display:none}.kpi-item:nth-child(n+3){border-top:1px solid rgba(77,159,183,.18)}.kpi-item small{font-size:8px}.kpi-item strong{font-size:17px}.kpi-item .phase-text{font-size:14px}.phase-chip{font-size:7px;padding:4px 5px}.history-control-fields{gap:7px}.history-type-switch button{padding:7px 5px;font-size:8px}.history-select{flex-basis:100%}.history-current-summary{align-items:flex-start;flex-basis:100%;text-align:left}.history-current-summary strong{font-size:12px}.screen-body{display:flex;flex-direction:column}.center-column{order:-1}.map-card{min-height:380px}.map-heading{align-items:flex-start;flex-direction:column;padding:9px;gap:7px}.layer-switch{align-self:flex-end;margin-top:-2px}.layer-switch button{padding:5px 6px}.map-title h2{font-size:10px}.pacific-visual{min-height:270px;margin:7px 6px 3px}.pacific-map{width:132%;max-width:none;left:-16%;right:auto}.map-labels{font-size:10px}.map-labels .label-sub,.map-labels .latitude-label,.map-labels .longitude-label{font-size:7px}.map-legend{left:7px;bottom:7px;gap:4px;font-size:7px}.map-caption{right:6px;bottom:30px;padding:5px 6px}.map-caption>div small{font-size:6px}.map-caption>b{font-size:11px}.map-scale{right:7px;bottom:8px}.map-bottomline{padding:4px 7px 6px;font-size:7px}.map-stat-row{grid-template-columns:1fr;flex-basis:auto}.map-stat-row article{min-height:46px}.side-column{display:grid;grid-template-columns:1fr 1fr;grid-template-rows:auto auto;gap:8px}.left-column,.right-column{grid-column:auto;grid-row:auto}.left-column .phase-card,.right-column .probability-card{grid-column:1/-1;min-height:185px}.left-column .intensity-card,.right-column .forecast-card{min-height:165px}.left-column .event-card,.right-column .impact-card{min-height:165px}.phase-orbit{width:90px;height:90px;flex-basis:90px}.intensity-card .panel-footnote{white-space:normal}.signal-row{grid-template-columns:7px minmax(0,1fr);gap:5px}.signal-row time{display:none}.signal-row strong{font-size:8px}.signal-row small{font-size:7px}.probability-card .probability-chart{height:125px}.forecast-card .index-chart{height:110px}.time-control{flex-wrap:wrap;gap:7px;padding:8px}.timeline-label{flex-basis:82px}.timeline-label .panel-kicker{font-size:5px}.timeline-current{flex-basis:76px;padding-left:6px}.timeline-current strong{font-size:8px}.timeline-labels span{font-size:7px}.timeline-labels span:nth-child(2),.timeline-labels span:nth-child(4){display:none}.timeline-track-wrap{min-width:150px}.play-button{width:25px;height:25px;flex-basis:25px}}
@media(prefers-reduced-motion:reduce){.enso-screen *, .enso-screen *:before,.enso-screen *:after{animation-duration:.01ms!important;animation-iteration-count:1!important;scroll-behavior:auto!important;transition-duration:.01ms!important}}
.heat-track i,.impact-track i{transform-origin:left;animation:barIn 1.2s ease-out both}@keyframes barIn{from{transform:scaleX(0)}to{transform:scaleX(1)}}

@media(max-width:620px){.realtime-status-button{padding:5px 6px;font-size:8px}.realtime-status-button i{font-size:10px}.realtime-status-panel{position:fixed;top:58px;right:9px;left:9px;width:auto;max-width:none}}

/* Keep the dashboard as the first viewport while allowing the knowledge modules below it to scroll into view. */
.enso-screen{height:auto;min-height:100vh;overflow:visible}
.screen-body{flex:1 0 auto;min-height:clamp(560px,66vh,820px)}
.map-card-maximized{position:fixed!important;z-index:10000;inset:12px!important;display:flex!important;width:auto!important;height:auto!important;min-height:0!important;padding:12px!important;overflow:hidden!important;border:1px solid rgba(71,211,220,.52)!important;border-radius:10px!important;background:linear-gradient(145deg,#041522,#061c2c)!important;box-shadow:0 30px 100px rgba(0,0,0,.68),0 0 45px rgba(38,201,217,.18)!important;clip-path:none!important;animation:mapExpand .24s cubic-bezier(.2,.8,.2,1) both}
.map-card-maximized:before{display:none}
.map-card-maximized .map-heading{flex:0 0 54px}
.map-card-maximized .enso-map-panel{flex:1;min-height:0}
.map-card-maximized .map-bottomline{flex:0 0 28px}

.module-workbench{position:relative;z-index:1;margin:12px 0 22px;padding:20px;border:1px solid rgba(61,144,172,.3);border-radius:12px;background:linear-gradient(135deg,rgba(7,31,48,.94),rgba(4,22,37,.92));box-shadow:inset 0 0 36px rgba(32,155,177,.055),0 18px 44px rgba(0,0,0,.18)}
.workbench-heading{display:flex;align-items:flex-end;justify-content:space-between;gap:16px;margin-bottom:16px}
.workbench-heading h2{margin:4px 0 0;color:#e8f7fa;font-size:19px;letter-spacing:.5px}
.workbench-heading p{margin:6px 0 0;color:#829eaf;font-size:11px}
.workbench-count{display:flex;align-items:center;gap:8px;color:#7895a7;font-size:10px}
.workbench-count b{color:#53dfd7;font-size:20px;font-weight:600;text-shadow:0 0 14px rgba(66,225,218,.28)}
.module-quick-nav{position:sticky;top:8px;z-index:6;display:flex;gap:4px;margin:0 -5px 14px;padding:5px;overflow:auto;border:1px solid rgba(69,152,174,.2);border-radius:7px;background:rgba(4,21,35,.92);backdrop-filter:blur(12px);scrollbar-width:thin}
.module-quick-nav button{display:flex;align-items:center;gap:6px;flex:0 0 auto;padding:8px 10px;border:1px solid transparent;border-radius:4px;color:#90adbb;background:transparent;font-family:inherit;font-size:10px;font-weight:500;line-height:1;white-space:nowrap;cursor:pointer;transition:all .2s}
.module-quick-nav button span{color:#51c9d2;font-size:8px;letter-spacing:.5px}
.module-quick-nav button:hover{border-color:rgba(67,205,216,.28);color:#d9f8f5;background:rgba(19,104,123,.24)}
.module-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:10px}
.module-card{--accent:#4bd8d4;position:relative;display:flex;min-height:190px;flex-direction:column;padding:15px;border:1px solid rgba(70,143,166,.23);border-radius:8px;overflow:hidden;background:linear-gradient(145deg,rgba(9,39,58,.91),rgba(5,25,41,.89));transition:transform .22s,border-color .22s,box-shadow .22s}
.module-card:before{position:absolute;top:0;right:0;width:76px;height:76px;border-radius:50%;content:"";background:radial-gradient(circle,var(--accent),transparent 70%);opacity:.11;transform:translate(24px,-28px)}
.module-card:hover{z-index:1;transform:translateY(-3px);border-color:color-mix(in srgb,var(--accent) 55%,transparent);box-shadow:0 12px 30px rgba(0,0,0,.22),0 0 20px color-mix(in srgb,var(--accent) 12%,transparent)}
.module-card.cyan-tone{--accent:#4fe1d2}.module-card.blue-tone{--accent:#69baff}.module-card.gold-tone{--accent:#ffc47a}.module-card.violet-tone{--accent:#b695ff}
.module-card-top{display:flex;align-items:center;justify-content:space-between;margin-bottom:11px}
.module-icon{display:grid;width:33px;height:33px;place-items:center;border:1px solid color-mix(in srgb,var(--accent) 35%,transparent);border-radius:8px;color:var(--accent);background:color-mix(in srgb,var(--accent) 10%,transparent);font-size:15px;box-shadow:inset 0 0 13px color-mix(in srgb,var(--accent) 8%,transparent)}
.module-number{color:rgba(144,183,198,.45);font-size:11px;letter-spacing:1px}
.module-eyebrow{color:var(--accent);font-size:7px;letter-spacing:1.3px}
.module-card h3{margin:5px 0 6px;color:#e1f3f7;font-size:14px;font-weight:600}
.module-card p{min-height:38px;margin:0;color:#8ba6b4;font-size:9px;line-height:1.7}
.module-highlights{display:flex;flex-wrap:wrap;gap:5px;margin-top:10px}
.module-highlights span{padding:4px 6px;border:1px solid rgba(94,150,169,.2);border-radius:3px;color:#8eaab7;background:rgba(21,59,76,.36);font-size:8px}
.module-action{display:flex;align-items:center;justify-content:space-between;width:100%;margin-top:auto;padding:12px 0 0;border:0;color:var(--accent);background:transparent;font-family:inherit;font-size:9px;font-weight:600;line-height:1;text-align:left;cursor:pointer}
.module-action i{transition:transform .18s}.module-card:hover .module-action i{transform:translateX(3px)}
.module-action-static{color:#7798a4;cursor:default}
@media(max-width:1050px){.module-grid{grid-template-columns:repeat(3,minmax(0,1fr))}}
@media(max-width:900px){.screen-body{min-height:0}.module-workbench{margin-top:14px;padding:15px}}
@media(max-width:680px){.module-workbench{padding:13px}.workbench-heading{align-items:flex-start}.workbench-heading h2{font-size:16px}.workbench-count{gap:5px;font-size:8px}.workbench-count b{font-size:17px}.module-quick-nav{margin-bottom:10px}.module-quick-nav button{padding:7px 8px;font-size:9px}.module-grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:8px}.module-card{min-height:195px;padding:11px}.module-card h3{font-size:13px}.module-card p{font-size:8px}.map-card-maximized{inset:0!important;padding:7px!important;border-radius:0!important}.map-card-maximized .map-heading{align-items:center;flex-direction:row;min-height:42px;padding:0 5px;gap:6px}.map-card-maximized .map-title h2{font-size:10px}.map-card-maximized .map-heading-note{display:none}}
@media(max-width:390px){.module-grid{grid-template-columns:1fr}.module-card{min-height:170px}}
@media(prefers-reduced-motion:reduce){.module-card,.module-quick-nav button,.module-action i{transition:none!important}}
@keyframes mapExpand{from{opacity:.88;transform:scale(.985)}to{opacity:1;transform:scale(1)}}
@media(prefers-reduced-motion:reduce){.map-card-maximized{animation:none}}
.history-context-summary{display:flex;flex:0 1 220px;min-width:150px;flex-direction:column;align-items:flex-end;gap:2px;color:#d6edf2;font-size:9px;text-align:right}.history-context-summary span{color:#ffc77d;font-weight:600}.history-context-summary small,.history-context-summary b{color:#7898a7;font-size:8px;font-weight:400;white-space:nowrap}@media(max-width:620px){.history-context-summary{align-items:flex-start;flex-basis:100%;text-align:left}}
/* ---- 演变时间轴上的 AI 解读入口 ---- */
.timeline-ai-button{display:flex;align-items:center;gap:5px;flex:0 0 auto;padding:6px 10px;border:1px solid rgba(72,146,166,.34);border-radius:5px;color:#8fb6c0;background:rgba(13,52,66,.55);font-family:inherit;font-size:9px;letter-spacing:.4px;cursor:pointer;transition:color .18s,border-color .18s,background .18s}
.timeline-ai-button i{font-size:11px}
.timeline-ai-button:hover{color:#d9f8f5;border-color:rgba(67,205,216,.55);background:rgba(20,95,110,.5)}
.timeline-ai-button.ready{color:#6fe0cd;border-color:rgba(75,224,193,.42);box-shadow:0 0 12px rgba(75,224,193,.14)}
.timeline-ai-button.ready:hover{color:#e9fffb;background:rgba(22,110,104,.55)}
@media(max-width:1200px){.timeline-ai-button{padding:5px 8px;font-size:8px}.timeline-ai-button i{font-size:10px}}
@media(max-width:680px){.timeline-ai-button{padding:5px 7px}.timeline-ai-button i{font-size:10px}}
</style>
