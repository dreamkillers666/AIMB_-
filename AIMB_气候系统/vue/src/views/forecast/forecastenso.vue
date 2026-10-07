<template>
  <div class="enso-screen">
    <!-- 顶部 -->
    <div class="screen-header">
      <div class="header-title">ENSO 预测分析与监测平台</div>
      <div class="header-right">
        <span class="time">{{ nowTime }}</span>
      </div>
    </div>

    <!-- 主体 -->
    <div class="screen-body">
      <!-- 左侧 -->
      <div class="left-panel">
        <div class="panel-card">
          <div class="panel-title">实时监测</div>
          <div class="gauge-box">
            <div ref="gaugeChart" class="mini-chart"></div>
            <div class="metrics">
              <div class="metric-item">
                <div class="metric-label">Niño3.4</div>
                <div class="metric-value">{{ monitor.nino34 }}</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">ONI</div>
                <div class="metric-value">{{ monitor.oni }}</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">SOI</div>
                <div class="metric-value">{{ monitor.soi }}</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">状态</div>
                <div class="metric-value status">{{ monitor.status }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div class="panel-title">预测类型</div>
          <div ref="barChart" class="small-panel-chart"></div>
        </div>

        <div class="panel-card">
          <div class="panel-title">历史统计</div>
          <div class="history-list">
            <div class="history-row" v-for="item in historyStats" :key="item.name">
              <span>{{ item.name }}</span>
              <el-progress
                  :percentage="item.value"
                  :show-text="false"
                  :stroke-width="10"
                  color="#2f8bff"
              ></el-progress>
              <span class="history-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间 -->
      <div class="center-panel">
        <div class="map-wrapper">
          <div ref="chinaMap" class="china-map"></div>

          <div class="center-overlay">
            <div class="overlay-title">中国区域 ENSO 影响监测</div>
            <div class="overlay-sub">
              基于 ENSO 预测结果的区域影响监测
            </div>
          </div>
        </div>

        <div class="bottom-summary">
          <div class="summary-card">
            <div class="summary-label">IRI 发布时间</div>
            <div class="summary-value">{{ data.iriPublishedAt }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-label">CPC 发布时间</div>
            <div class="summary-value">{{ data.cpcFetchedAt }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-label">主导信号</div>
            <div class="summary-value highlight">{{ dominantSignal }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-label">系统状态</div>
            <div class="summary-value ok">{{ errorMsg ? '异常' : '正常' }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧 -->
      <div class="right-panel">
        <div class="panel-card">
          <div class="panel-title">核心指数</div>
          <div class="index-box">
            <div ref="donutChart" class="mini-chart"></div>
            <div class="index-info">
              <div class="index-row">
                <span>Neutral 最高</span>
                <span class="green">{{ maxNeutral }}</span>
              </div>
              <div class="index-row">
                <span>El Niño 最高</span>
                <span class="orange">{{ maxElNino }}</span>
              </div>
              <div class="index-row">
                <span>La Niña 最高</span>
                <span class="blue">{{ maxLaNina }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div class="panel-title">IRI 概率演变</div>
          <div ref="lineChart" class="small-panel-chart"></div>
        </div>

        <div class="panel-card">
          <div class="panel-title">风险等级</div>
          <div class="risk-list">
            <div class="risk-row" v-for="item in riskLevels" :key="item.name">
              <span>{{ item.name }}</span>
              <el-progress
                  :percentage="item.value"
                  :show-text="true"
                  :stroke-width="10"
                  :color="item.color"
              ></el-progress>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import chinaJson from '@/assets/map/china.json'

export default {
  name: 'ForecastENSOScreen',
  data() {
    return {
      baseUrl: 'http://localhost:9090',
      data: {
        iriPublishedAt: '2026/8/15 08:00:00',
        cpcFetchedAt: '2026/8/15 08:00:00',
        iri: [],
        cpcStrengths: []
      },
      errorMsg: '',
      nowTime: '',
      timer: null,
      charts: {},

      monitor: {
        nino34: '+0.8',
        oni: '+0.7',
        soi: '-0.4',
        status: '偏暖'
      },

      historyStats: [
        { name: 'El Niño 历史占比', value: 62 },
        { name: 'La Niña 历史占比', value: 45 },
        { name: 'Neutral 历史占比', value: 78 },
        { name: '强事件频率', value: 36 },
        { name: '近十年活跃度', value: 58 }
      ],

      riskLevels: [
        { name: '华南降水风险', value: 68, color: '#2f8bff' },
        { name: '长江流域风险', value: 55, color: '#35c2a1' },
        { name: '华东高温风险', value: 61, color: '#ffb347' },
        { name: '北方异常风险', value: 39, color: '#8a7dff' }
      ],

      mapPoints: [
        { name: '广东', value: [113.27, 23.13, 78] },
        { name: '福建', value: [119.30, 26.08, 62] },
        { name: '江西', value: [115.89, 28.68, 55] },
        { name: '湖南', value: [112.98, 28.20, 58] },
        { name: '湖北', value: [114.31, 30.52, 51] },
        { name: '河南', value: [113.62, 34.75, 44] },
        { name: '北京', value: [116.40, 39.90, 36] },
        { name: '四川', value: [104.06, 30.67, 28] }
      ]
    }
  },
  computed: {
    iriRows() {
      return this.data.iri || []
    },
    cpcRows() {
      return this.data.cpcStrengths || []
    },
    dominantSignal() {
      if (!this.iriRows.length) return '-'
      const avgNeutral = this.avg(this.iriRows.map(i => i.neutral))
      const avgElNino = this.avg(this.iriRows.map(i => i.elNino))
      const avgLaNina = this.avg(this.iriRows.map(i => i.laNina))
      if (avgElNino >= avgNeutral && avgElNino >= avgLaNina) return 'El Niño 偏强'
      if (avgNeutral >= avgElNino && avgNeutral >= avgLaNina) return 'Neutral 主导'
      return 'La Niña 偏强'
    },
    maxNeutral() {
      return this.iriRows.length ? Math.max.apply(null, this.iriRows.map(i => i.neutral)) + '%' : '-'
    },
    maxElNino() {
      return this.iriRows.length ? Math.max.apply(null, this.iriRows.map(i => i.elNino)) + '%' : '-'
    },
    maxLaNina() {
      return this.iriRows.length ? Math.max.apply(null, this.iriRows.map(i => i.laNina)) + '%' : '-'
    }
  },
  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
    this.$nextTick(() => {
      this.initCharts()
      this.reload()
      window.addEventListener('resize', this.handleResize)
    })
  },
  beforeDestroy() {
    clearInterval(this.timer)
    window.removeEventListener('resize', this.handleResize)
    Object.keys(this.charts).forEach(k => {
      if (this.charts[k]) this.charts[k].dispose()
    })
  },
  methods: {
    avg(arr) {
      if (!arr || !arr.length) return 0
      return arr.reduce((a, b) => a + b, 0) / arr.length
    },
    updateTime() {
      const d = new Date()
      this.nowTime = d.toLocaleString()
    },
    async reload() {
      this.data = {
        iriPublishedAt: '2026/8/15 08:00:00',
        cpcFetchedAt: '2026/8/15 08:00:00',
        iri: [
          { season: '2025 JJA', laNina: 15, neutral: 25, elNino: 60 },
          { season: '2025 JAS', laNina: 16, neutral: 24, elNino: 60 },
          { season: '2025 ASO', laNina: 18, neutral: 22, elNino: 60 },
          { season: '2025 SON', laNina: 20, neutral: 21, elNino: 59 },
          { season: '2025 OND', laNina: 22, neutral: 20, elNino: 58 },
          { season: '2025 NDJ', laNina: 24, neutral: 19, elNino: 57 }
        ],
        cpcStrengths: []
      }
      this.renderAllCharts()
    },
    initCharts() {
      echarts.registerMap('china', chinaJson)
      this.charts.gauge = echarts.init(this.$refs.gaugeChart)
      this.charts.bar = echarts.init(this.$refs.barChart)
      this.charts.map = echarts.init(this.$refs.chinaMap)
      this.charts.donut = echarts.init(this.$refs.donutChart)
      this.charts.line = echarts.init(this.$refs.lineChart)
      this.renderAllCharts()
    },
    renderAllCharts() {
      this.renderGauge()
      this.renderBar()
      this.renderMap()
      this.renderDonut()
      this.renderLine()
    },
    renderGauge() {
      this.charts.gauge.setOption({
        series: [{
          type: 'gauge',
          startAngle: 220,
          endAngle: -40,
          min: -2,
          max: 2,
          progress: { show: true, width: 8 },
          axisLine: { lineStyle: { width: 8 } },
          splitLine: { show: false },
          axisTick: { show: false },
          axisLabel: { show: false },
          pointer: { show: false },
          detail: { formatter: 'ONI', color: '#fff', fontSize: 14, offsetCenter: [0, '60%'] },
          data: [{ value: 0.7 }]
        }]
      })
    },
    renderBar() {
      const seasons = this.iriRows.map(i => i.season)
      const en = this.iriRows.map(i => i.elNino)
      const ne = this.iriRows.map(i => i.neutral)

      this.charts.bar.setOption({
        backgroundColor: 'transparent',
        grid: { left: 30, right: 10, top: 30, bottom: 25 },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: seasons,
          axisLine: { lineStyle: { color: '#7aa6d8' } },
          axisLabel: { color: '#cfe6ff', fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
          axisLabel: { color: '#cfe6ff', fontSize: 10 }
        },
        series: [
          { name: 'Neutral', type: 'bar', data: ne, barWidth: 10 },
          { name: 'El Niño', type: 'bar', data: en, barWidth: 10 }
        ]
      })
    },
    renderMap() {
      this.charts.map.setOption({
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            if (params.value && params.value.length) {
              return `${params.name}<br/>影响等级：${params.value[2]}`
            }
            return params.name
          }
        },
        geo: {
          map: 'china',
          roam: false,
          zoom: 1.15,
          itemStyle: {
            areaColor: '#d9efff',
            borderColor: '#8dbbe8'
          },
          emphasis: {
            itemStyle: { areaColor: '#6eb8ff' }
          }
        },
        series: [
          {
            type: 'map',
            map: 'china',
            geoIndex: 0,
            data: []
          },
          {
            type: 'effectScatter',
            coordinateSystem: 'geo',
            rippleEffect: { scale: 4 },
            symbolSize: function(val) {
              return Math.max(8, val[2] / 6)
            },
            itemStyle: {
              color: '#00e5ff'
            },
            data: this.mapPoints
          }
        ]
      })
    },
    renderDonut() {
      const n = this.avg(this.iriRows.map(i => i.neutral))
      const e = this.avg(this.iriRows.map(i => i.elNino))
      const l = this.avg(this.iriRows.map(i => i.laNina))

      this.charts.donut.setOption({
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: ['55%', '75%'],
          label: { color: '#fff' },
          data: [
            { value: n, name: 'Neutral' },
            { value: e, name: 'El Niño' },
            { value: l, name: 'La Niña' }
          ]
        }]
      })
    },
    renderLine() {
      this.charts.line.setOption({
        grid: { left: 35, right: 10, top: 25, bottom: 30 },
        tooltip: { trigger: 'axis' },
        legend: {
          top: 0,
          textStyle: { color: '#dbefff', fontSize: 10 }
        },
        xAxis: {
          type: 'category',
          data: this.iriRows.map(i => i.season),
          axisLabel: { color: '#cfe6ff' },
          axisLine: { lineStyle: { color: '#6fa4d9' } }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: { color: '#cfe6ff' },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
        },
        series: [
          { name: 'La Niña', type: 'line', smooth: true, data: this.iriRows.map(i => i.laNina) },
          { name: 'Neutral', type: 'line', smooth: true, data: this.iriRows.map(i => i.neutral) },
          { name: 'El Niño', type: 'line', smooth: true, data: this.iriRows.map(i => i.elNino) }
        ]
      })
    },
    handleResize() {
      Object.keys(this.charts).forEach(k => {
        if (this.charts[k]) this.charts[k].resize()
      })
    }
  }
}
</script>

<style scoped>
.enso-screen {
  height: 100vh;
  background: linear-gradient(180deg, #061a3a 0%, #0c2f60 8%, #0b1630 100%);
  color: #fff;
  overflow: hidden;
  padding: 8px 12px 12px;
  box-sizing: border-box;
}
.screen-header {
  height: 64px;
  background: linear-gradient(90deg, rgba(18,102,255,0.8), rgba(0,164,255,0.45), rgba(18,102,255,0.8));
  border: 1px solid rgba(98,182,255,0.35);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 0 18px rgba(0,140,255,0.25) inset;
}
.header-title {
  width: 100%;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 2px;
}
.header-right {
  position: absolute;
  right: 26px;
  top: 26px;
  font-size: 14px;
  color: #d8efff;
}
.screen-body {
  display: flex;
  gap: 14px;
  margin-top: 12px;
  height: calc(100vh - 92px);
}
.left-panel, .right-panel {
  width: 22%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.center-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.panel-card {
  background: rgba(8, 31, 68, 0.82);
  border: 1px solid rgba(85, 173, 255, 0.22);
  box-shadow: inset 0 0 20px rgba(54, 145, 255, 0.08);
  border-radius: 8px;
  padding: 14px;
}
.panel-title {
  font-size: 18px;
  font-weight: 700;
  color: #eef7ff;
  margin-bottom: 12px;
  border-left: 4px solid #2fa7ff;
  padding-left: 10px;
}
.gauge-box, .index-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.mini-chart {
  width: 140px;
  height: 140px;
}
.metrics, .index-info {
  flex: 1;
  padding-left: 10px;
}
.metric-item, .index-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}
.metric-value {
  font-weight: 700;
  color: #ffffff;
}
.metric-value.status {
  color: #ffd76a;
}
.green { color: #40d990; }
.orange { color: #ffb347; }
.blue { color: #4da3ff; }

.small-panel-chart {
  width: 100%;
  height: 220px;
}
.history-list, .risk-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.history-row, .risk-row {
  display: grid;
  grid-template-columns: 110px 1fr 40px;
  gap: 10px;
  align-items: center;
  font-size: 13px;
}
.history-value {
  text-align: right;
  color: #d9eeff;
}
.map-wrapper {
  flex: 1;
  position: relative;
  background: rgba(9, 30, 63, 0.85);
  border: 1px solid rgba(85,173,255,0.22);
  border-radius: 8px;
  overflow: hidden;
}
.china-map {
  width: 100%;
  height: 100%;
  min-height: 540px;
}
.center-overlay {
  position: absolute;
  left: 20px;
  bottom: 16px;
  background: rgba(4, 18, 42, 0.45);
  padding: 10px 14px;
  border-radius: 6px;
}
.overlay-title {
  font-size: 22px;
  color: #ffbfdc;
  font-weight: 700;
}
.overlay-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #d5ecff;
}
.bottom-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.summary-card {
  background: rgba(8, 31, 68, 0.82);
  border: 1px solid rgba(85, 173, 255, 0.22);
  border-radius: 8px;
  padding: 14px;
}
.summary-label {
  color: #a6d2ff;
  font-size: 13px;
}
.summary-value {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 700;
}
.summary-value.highlight {
  color: #ffd56a;
}
.summary-value.ok {
  color: #44e39b;
}
</style>
