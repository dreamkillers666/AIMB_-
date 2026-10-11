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
        <strong class="clock">{{ nowTime }}</strong>
      </div>
    </header>

    <section class="kpi-strip" aria-label="关键监测指标">
      <article class="kpi-item">
        <span class="kpi-icon warm-icon"><i class="el-icon-sunny"></i></span>
        <div><small>Niño3.4 海温距平</small><strong class="warm-text">{{ monitor.nino34 }}<em>°C</em></strong></div>
        <span class="kpi-delta up"><i class="el-icon-top"></i> +0.2</span>
      </article>
      <article class="kpi-item">
        <span class="kpi-icon cyan-icon"><i class="el-icon-data-line"></i></span>
        <div><small>ONI · 3月滑动平均</small><strong>{{ monitor.oni }}<em>°C</em></strong></div>
        <span class="kpi-delta up">持续偏暖</span>
      </article>
      <article class="kpi-item">
        <span class="kpi-icon blue-icon"><i class="el-icon-odometer"></i></span>
        <div><small>南方涛动指数 SOI</small><strong class="blue-text">{{ monitor.soi }}</strong></div>
        <span class="kpi-delta">近30日</span>
      </article>
      <article class="kpi-item phase-kpi">
        <span class="kpi-icon gold-icon"><i class="el-icon-aim"></i></span>
        <div><small>当前 ENSO 阶段</small><strong class="phase-text">{{ monitor.status }}</strong></div>
        <span class="phase-chip"><i></i>发展期</span>
      </article>
    </section>

    <main class="screen-body">
      <aside class="side-column left-column">
        <section class="panel-card phase-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">LIVE OBSERVATION</span><h2>海气实时监测</h2></div>
            <span class="data-age"><i></i>更新于 {{ dataUpdatedAt }}</span>
          </div>
          <div class="phase-reading">
            <div class="phase-orbit"><div class="orbit-track"></div><div class="orbit-core"><span>ONI</span><strong>{{ monitor.oni }}</strong><small>°C</small></div><i class="orbit-satellite"></i></div>
            <div class="phase-copy"><span class="phase-label">当前状态</span><strong>厄尔尼诺发展</strong><span class="phase-caption">赤道中东太平洋持续增暖</span><div class="threshold-track"><i></i></div><div class="threshold-caption"><span>中性</span><span>+0.5°C 阈值</span></div></div>
          </div>
          <div class="observation-grid">
            <div><span>东太平洋 SST</span><strong class="warm-text">+1.2°</strong></div>
            <div><span>信风距平</span><strong class="cyan-text">−0.6<span> m/s</span></strong></div>
            <div><span>暖水体积</span><strong>+14<span>%</span></strong></div>
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
              <div v-for="item in heatContent" :key="item.name" class="heat-row">
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
            <div v-for="signal in signals" :key="signal.time" class="signal-row">
              <span class="signal-marker" :class="signal.tone"></span><div><strong>{{ signal.title }}</strong><small>{{ signal.detail }}</small></div><time>{{ signal.time }}</time>
            </div>
          </div>
        </section>
      </aside>

      <section class="center-column">
        <section class="map-card panel-card" :class="{ 'map-card-maximized': mapMaximized }">
          <div class="map-heading">
            <div class="map-title"><span class="map-live-icon"><i></i></span><div><span class="panel-kicker">TROPICAL PACIFIC · SSTA / TRADE WINDS</span><h2>热带太平洋海温距平与信风场</h2></div></div>
            <span class="map-heading-note">拖动旋转 · 滚轮缩放 · Niño3.4 核心区追踪</span>
          </div>

          <enso-map-panel :maximized="mapMaximized" @toggle-maximize="toggleMapMaximize" />
          <div class="map-bottomline"><span><i></i>遥感反演场正常</span><span>NOAA OISST v2.1 · ERSST v5</span><span>距平基准期 1991–2020</span></div>
        </section>
        <div class="map-stat-row">
          <article><span class="stat-glyph warm-glyph"><i class="el-icon-sunny"></i></span><div><small>赤道中东太平洋</small><strong class="warm-text">持续增暖</strong></div><b>↑ 0.3°</b></article>
          <article><span class="stat-glyph cyan-glyph"><i class="el-icon-sort"></i></span><div><small>低层信风强度</small><strong>较常年偏弱</strong></div><b>−0.6 m/s</b></article>
          <article><span class="stat-glyph blue-glyph"><i class="el-icon-ship"></i></span><div><small>暖水向东输送</small><strong>传播信号活跃</strong></div><b>+14%</b></article>
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
            <div><span class="panel-kicker">NINO3.4 INDEX · OBS / FCST</span><h2>指数演变趋势</h2></div>
            <span class="unit-label">°C</span>
          </div>
          <div ref="indexChart" class="index-chart"></div>
          <div class="forecast-legend"><span><i class="observed-line"></i>观测</span><span><i class="forecast-line"></i>预测</span><span><i class="threshold-line"></i>±0.5 阈值</span></div>
        </section>
        <section class="panel-card impact-card">
          <div class="panel-heading">
            <div><span class="panel-kicker">GLOBAL TELECONNECTIONS</span><h2>区域影响信号</h2></div>
            <span class="risk-status"><i></i>关注</span>
          </div>
          <div class="impact-list">
            <div v-for="risk in riskLevels" :key="risk.name" class="impact-row">
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
      <button class="play-button" type="button" :aria-label="isPlaying ? '暂停演变回放' : '播放演变回放'" @click="togglePlayback"><i :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i></button>
      <div class="timeline-track-wrap">
        <div class="timeline-labels"><span v-for="(period, index) in periods" :key="period" :class="{ selected: index === currentIndex }" @click="currentIndex = index">{{ period }}</span></div>
        <input v-model.number="currentIndex" class="timeline-slider" type="range" min="0" :max="periods.length - 1" step="1" aria-label="选择 ENSO 预测季节" :style="{ '--progress': (currentIndex / (periods.length - 1) * 100) + '%' }">
      </div>
      <div class="timeline-current"><span>当前时次</span><strong>{{ activePeriod }}</strong><small>{{ currentIndex < 3 ? '观测场' : '预测场' }}</small></div>
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

export default {
  name: 'ForecastENSOScreen',
  components: { EnsoMapPanel },
  data() {
    return {
      nowTime: '',
      nowDate: '',
      dataUpdatedAt: '08:30',
      clockTimer: null,
      playTimer: null,
      isPlaying: false,
      charts: {},
      currentIndex: 3,
      mapMaximized: false,
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
        { id: 'system-introduction', number: '01', title: '系统介绍', eyebrow: 'CLIMATE INTELLIGENCE', icon: 'el-icon-monitor', tone: 'cyan-tone', description: '了解平台如何整合海气输入、ENSO 长提前期预测、历史回放和可信验证，并进入完整系统说明。', highlights: ['数据 · 预测 · 验证', '点击进入系统介绍'], path: '/enso/overview', action: '进入系统介绍' },
        { id: 'enso-introduction', number: '02', title: 'ENSO 介绍', eyebrow: 'OCEAN–ATMOSPHERE', icon: 'el-icon-help', tone: 'blue-tone', description: '了解厄尔尼诺、拉尼娜与中性位相，以及海气耦合如何驱动热带太平洋的年际变化。', highlights: ['ENSO 位相', '海气耦合机制'], path: '/enso/introduction' },
        { id: 'prediction-models', number: '03', title: '预测模型', eyebrow: 'MODEL LAB', icon: 'el-icon-data-analysis', tone: 'gold-tone', description: '对比 LSTA-Swin 与 ConvLSTM 产品，查看 Niño3.4 / ONI 预测轨迹和赤道太平洋空间场。', highlights: ['长提前期预测', '多模型产品'], path: '/enso/models', action: '查看模型' },
        { id: 'historical-replay', number: '04', title: '历史回放', eyebrow: 'HINDCAST REPLAY', icon: 'el-icon-video-play', tone: 'violet-tone', description: '选择历史起报月份，逐月回放预测轨迹，并与当时可获得的观测记录对照。', highlights: ['按起报月份检索', '预测与观测对比'], path: '/enso/replay' },
        { id: 'lead-validation', number: '05', title: '提前期验证', eyebrow: 'SKILL ASSESSMENT', icon: 'el-icon-aim', tone: 'cyan-tone', description: '查看 1–20 个月提前期的相关系数、平均绝对误差与均方根误差，评估模型技巧衰减。', highlights: ['PCC · MAE · RMSE', '逐提前期统计'], path: '/enso/validation' },
        { id: 'physical-explanation', number: '06', title: '物理解释', eyebrow: 'PHYSICAL INSIGHT', icon: 'el-icon-view', tone: 'gold-tone', description: '通过时空演变与显著性图，追踪模型关注区域和海温、风应力信号之间的物理联系。', highlights: ['时空演变', '预测显著性'], path: '/enso/explainability' },
        { id: 'climate-nc', number: '07', title: '气候 NC', eyebrow: 'CLIMATE DATA EXPLORER', icon: 'el-icon-picture-outline', tone: 'blue-tone', description: '载入 NetCDF 气候格点数据，选择变量和时次，查看空间分布并开展区域分析。', highlights: ['NetCDF 格点场', '变量 · 时次 · 区域'], path: '/enso/climate-data', action: '打开数据视图' }
      ]
    }
  },
  computed: {
    activePeriod() { return this.periods[this.currentIndex] || this.periods[0] }
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
        clearInterval(this.playTimer)
        this.isPlaying = false
        return
      }
      this.isPlaying = true
      this.playTimer = setInterval(() => {
        this.currentIndex = (this.currentIndex + 1) % this.periods.length
      }, 1800)
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
      this.charts.probability.setOption({
        animationDuration: 1100,
        animationEasing: 'cubicOut',
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(3, 20, 35, .96)', borderColor: '#1d718b', textStyle: { color: '#e3f6ff', fontSize: 11 }, formatter: params => `${params[0].axisValue}<br/>${params.map(item => `${item.marker} ${item.seriesName}：${item.value}%`).join('<br/>')}` },
        legend: { show: false },
        grid: { left: 34, right: 10, top: 12, bottom: 28 },
        xAxis: { type: 'category', data: ['SON', 'OND', 'NDJ', 'DJF', 'JFM', 'FMA'], axisTick: { show: false }, axisLine: { lineStyle: { color: '#29465b' } }, axisLabel: { color: textColor, fontSize: 9, margin: 9 } },
        yAxis: { type: 'value', max: 100, interval: 50, axisLabel: { color: textColor, fontSize: 9, formatter: '{value}%' }, axisTick: { show: false }, axisLine: { show: false }, splitLine: { lineStyle: { color: gridColor, type: 'dashed' } } },
        series: [
          { name: '厄尔尼诺', type: 'bar', stack: 'total', barWidth: 17, itemStyle: { color: '#ff9b5e', borderRadius: [3, 3, 0, 0] }, data: [60, 58, 53, 47, 40, 32] },
          { name: '中性', type: 'bar', stack: 'total', barWidth: 17, itemStyle: { color: '#25c8bd' }, data: [25, 27, 30, 34, 37, 39] },
          { name: '拉尼娜', type: 'bar', stack: 'total', barWidth: 17, itemStyle: { color: '#378bce', borderRadius: [0, 0, 3, 3] }, data: [15, 15, 17, 19, 23, 29] }
        ]
      }, true)
    },
    renderIndexChart() {
      if (!this.charts.index) return
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
.screen-header{position:relative;display:grid;grid-template-columns:1fr 1.15fr 1fr;align-items:center;flex:0 0 54px;min-width:0;border-bottom:1px solid rgba(58,144,175,.32)}
.screen-header:before,.screen-header:after{position:absolute;bottom:-1px;width:14%;height:2px;content:"";background:linear-gradient(90deg,transparent,var(--cyan),transparent);filter:drop-shadow(0 0 5px var(--cyan))}.screen-header:before{left:0}.screen-header:after{right:0}
.brand-lockup{display:flex;align-items:center;gap:10px}.brand-symbol{display:grid;width:35px;height:35px;place-items:center;border:1px solid rgba(71,221,225,.45);border-radius:10px;color:#68eeea;background:linear-gradient(145deg,rgba(30,171,198,.24),rgba(9,55,78,.2));box-shadow:0 0 20px rgba(53,218,224,.11),inset 0 0 12px rgba(53,218,224,.13);font-size:17px}.brand-lockup strong,.brand-lockup small{display:block}.brand-lockup strong{font-size:10px;letter-spacing:1.65px;color:#bfe6ed}.brand-lockup small{margin-top:4px;color:#718da1;font-size:10px;letter-spacing:.5px}
.screen-title{text-align:center}.title-kicker{font-size:8px;letter-spacing:3px;color:#44c9d8}.screen-title h1{margin:3px 0 0;color:#f0fbff;font-size:20px;font-weight:650;letter-spacing:2.5px;text-shadow:0 0 20px rgba(69,214,230,.2)}
.header-status{display:flex;align-items:center;justify-content:flex-end;gap:14px;color:#829eb1;font-size:10px}.live-indicator{display:flex;align-items:center;gap:6px;color:#76dfc7;white-space:nowrap}.live-indicator i,.data-age i,.risk-status i{width:6px;height:6px;border-radius:50%;background:#45e4c1;box-shadow:0 0 8px #3ae2c1;animation:statusPulse 1.8s ease-in-out infinite}.clock{color:#e3f5fb;font-size:15px;font-variant-numeric:tabular-nums;letter-spacing:1px}.header-date{white-space:nowrap}
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
@media(max-height:760px) and (min-width:901px){.enso-screen{min-height:580px;gap:7px;padding-top:9px;padding-bottom:7px}.screen-header{flex-basis:46px}.kpi-strip{flex-basis:62px}.screen-body{gap:7px}.side-column,.center-column{gap:7px}.panel-heading{padding-top:7px;padding-bottom:5px}.phase-orbit{width:76px;height:76px;flex-basis:76px}.phase-reading{padding-bottom:4px}.observation-grid{padding-top:5px;padding-bottom:5px}.intensity-bars{gap:6px}.panel-footnote{margin-top:6px;padding-top:5px}.signal-row{padding:5px 0}.map-stat-row{flex-basis:46px}.time-control{flex-basis:41px}.map-heading{min-height:44px}}
@media(max-width:900px){.enso-screen{height:auto;min-height:100vh;overflow:visible}.screen-header{grid-template-columns:1fr auto;flex-basis:auto;min-height:58px;padding-bottom:7px}.screen-title{grid-column:1/-1;grid-row:2;padding:7px 0 4px}.screen-title h1{font-size:17px}.header-status{grid-column:2;grid-row:1;gap:8px}.header-status .header-date{display:none}.brand-lockup strong{font-size:8px}.kpi-strip{flex-basis:auto;min-height:68px}.kpi-item{padding:9px 8px}.kpi-item strong{font-size:18px}.kpi-icon{flex-basis:29px;width:29px;height:29px;font-size:14px}.kpi-delta{display:none}.screen-body{grid-template-columns:minmax(0,1fr) minmax(250px,.78fr);grid-template-rows:minmax(480px,56vh) auto;flex:none}.center-column{grid-column:1/-1;grid-row:1}.map-card{min-height:410px}.left-column{grid-column:1;grid-row:2;grid-template-rows:repeat(3,minmax(175px,auto))}.right-column{grid-column:2;grid-row:2;grid-template-rows:repeat(3,minmax(175px,auto))}.time-control{flex-basis:55px}.screen-watermark{display:none}}
@media(max-width:620px){.enso-screen{gap:8px;padding:9px}.screen-header{grid-template-columns:minmax(0,1fr) auto}.brand-lockup small{font-size:8px}.brand-symbol{width:30px;height:30px}.header-status{gap:7px}.live-indicator{font-size:8px}.clock{font-size:12px}.screen-title{padding:6px 0}.title-kicker{font-size:7px;letter-spacing:2px}.screen-title h1{font-size:15px;letter-spacing:1.5px}.kpi-strip{grid-template-columns:repeat(2,minmax(0,1fr));clip-path:none}.kpi-item{min-height:54px;padding:6px 8px}.kpi-item:nth-child(3):before{display:none}.kpi-item:nth-child(n+3){border-top:1px solid rgba(77,159,183,.18)}.kpi-item small{font-size:8px}.kpi-item strong{font-size:17px}.kpi-item .phase-text{font-size:14px}.phase-chip{font-size:7px;padding:4px 5px}.screen-body{display:flex;flex-direction:column}.center-column{order:-1}.map-card{min-height:380px}.map-heading{align-items:flex-start;flex-direction:column;padding:9px;gap:7px}.layer-switch{align-self:flex-end;margin-top:-2px}.layer-switch button{padding:5px 6px}.map-title h2{font-size:10px}.pacific-visual{min-height:270px;margin:7px 6px 3px}.pacific-map{width:132%;max-width:none;left:-16%;right:auto}.map-labels{font-size:10px}.map-labels .label-sub,.map-labels .latitude-label,.map-labels .longitude-label{font-size:7px}.map-legend{left:7px;bottom:7px;gap:4px;font-size:7px}.map-caption{right:6px;bottom:30px;padding:5px 6px}.map-caption>div small{font-size:6px}.map-caption>b{font-size:11px}.map-scale{right:7px;bottom:8px}.map-bottomline{padding:4px 7px 6px;font-size:7px}.map-stat-row{grid-template-columns:1fr;flex-basis:auto}.map-stat-row article{min-height:46px}.side-column{display:grid;grid-template-columns:1fr 1fr;grid-template-rows:auto auto;gap:8px}.left-column,.right-column{grid-column:auto;grid-row:auto}.left-column .phase-card,.right-column .probability-card{grid-column:1/-1;min-height:185px}.left-column .intensity-card,.right-column .forecast-card{min-height:165px}.left-column .event-card,.right-column .impact-card{min-height:165px}.phase-orbit{width:90px;height:90px;flex-basis:90px}.intensity-card .panel-footnote{white-space:normal}.signal-row{grid-template-columns:7px minmax(0,1fr);gap:5px}.signal-row time{display:none}.signal-row strong{font-size:8px}.signal-row small{font-size:7px}.probability-card .probability-chart{height:125px}.forecast-card .index-chart{height:110px}.time-control{flex-wrap:wrap;gap:7px;padding:8px}.timeline-label{flex-basis:82px}.timeline-label .panel-kicker{font-size:5px}.timeline-current{flex-basis:76px;padding-left:6px}.timeline-current strong{font-size:8px}.timeline-labels span{font-size:7px}.timeline-labels span:nth-child(2),.timeline-labels span:nth-child(4){display:none}.timeline-track-wrap{min-width:150px}.play-button{width:25px;height:25px;flex-basis:25px}}
@media(prefers-reduced-motion:reduce){.enso-screen *, .enso-screen *:before,.enso-screen *:after{animation-duration:.01ms!important;animation-iteration-count:1!important;scroll-behavior:auto!important;transition-duration:.01ms!important}}
.heat-track i,.impact-track i{transform-origin:left;animation:barIn 1.2s ease-out both}@keyframes barIn{from{transform:scaleX(0)}to{transform:scaleX(1)}}

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
</style>
