<template>
  <div class="enso-page">
    <!-- 顶部栏 -->
    <div class="enso-topbar">
      <div class="left">
        <div class="enso-logo">ENSO</div>
        <div class="title">
          <div class="h1">ENSO 官方预测展示</div>
          <div class="sub">IRI 三分类概率 + NOAA CPC Strengths（来自后端接口）</div>
        </div>
      </div>

      <div class="right">
        <el-input
            v-model="baseUrl"
            size="medium"
            class="baseurl"
            placeholder="后端地址，如 http://localhost:9090"
            clearable
        >
          <template slot="prepend">API</template>
        </el-input>

        <el-button type="primary" :loading="loading" icon="el-icon-refresh" @click="reload">
          刷新
        </el-button>

        <el-button :loading="fetching" icon="el-icon-download" @click="manualFetch">
          触发抓取
        </el-button>
      </div>
    </div>

    <!-- 状态提示 -->
    <el-alert
        v-if="errorMsg"
        class="enso-alert"
        type="error"
        show-icon
        :title="errorMsg"
        @close="errorMsg=''"
    />

    <!-- 概览卡片 -->
    <el-row :gutter="16" class="enso-row">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="enso-card">
          <div slot="header" class="card-header">
            <span>IRI 最新发布时间</span>
            <el-tag type="success" effect="light">三分类概率</el-tag>
          </div>
          <div class="metric">
            <div class="metric-value">{{ formatTime(data.iriPublishedAt) }}</div>
            <div class="metric-hint">来源：IRI ENSO Forecast（current）</div>
          </div>
          <div class="links">
            <a :href="iriUrl" target="_blank" rel="noreferrer">打开 IRI 页面</a>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="enso-card">
          <div slot="header" class="card-header">
            <span>CPC 最新抓取时间</span>
            <el-tag type="warning" effect="light">Strengths</el-tag>
          </div>
          <div class="metric">
            <div class="metric-value">{{ formatTime(data.cpcFetchedAt) }}</div>
            <div class="metric-hint">来源：NOAA CPC ENSO Strengths</div>
          </div>
          <div class="links">
            <a :href="cpcUrl" target="_blank" rel="noreferrer">打开 CPC Strengths 页面</a>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="enso-card">
          <div slot="header" class="card-header">
            <span>接口状态</span>
            <el-tag :type="statusTagType" effect="light">{{ statusText }}</el-tag>
          </div>

          <div class="status-box">
            <div class="status-line">
              <span class="dot" :class="statusDotClass"></span>
              <span>{{ statusDetail }}</span>
            </div>
            <el-divider></el-divider>

            <div class="small">
              <div><b>GET</b> {{ baseUrl }}/enso/latest</div>
              <div><b>POST</b> {{ baseUrl }}/enso/fetch</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主体：IRI 图 + 表，CPC 表 -->
    <el-row :gutter="16" class="enso-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="enso-card">
          <div slot="header" class="card-header">
            <span>IRI 三分类概率（%）</span>
            <div class="right-note">
              <el-tag size="mini" effect="plain">La Niña / Neutral / El Niño</el-tag>
            </div>
          </div>

          <div class="chart-wrap">
            <div ref="chartEl" class="chart"></div>
          </div>

          <el-table
              :data="iriRows"
              stripe
              height="320"
              v-loading="loading"
              element-loading-text="加载中..."
              empty-text="暂无 IRI 数据（先点“触发抓取”或等待后端定时任务）"
              class="enso-table"
          >
            <el-table-column prop="season" label="Season" width="110"></el-table-column>
            <el-table-column prop="laNina" label="La Niña (%)" width="130" align="right"></el-table-column>
            <el-table-column prop="neutral" label="Neutral (%)" width="130" align="right"></el-table-column>
            <el-table-column prop="elNino" label="El Niño (%)" width="130" align="right"></el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="enso-card">
          <div slot="header" class="card-header">
            <span>CPC Strengths（阈值概率 %）</span>
            <div class="right-note">
              <el-tag size="mini" effect="plain">Niño3.4 阈值</el-tag>
            </div>
          </div>

          <div class="hint">
            ≤ 为偏冷（La Niña），≥ 为偏暖（El Niño）。数据来自 CPC Strengths 页面。
          </div>

          <el-table
              :data="cpcRows"
              stripe
              height="520"
              v-loading="loading"
              element-loading-text="加载中..."
              empty-text="暂无 CPC 数据（先点“触发抓取”或等待后端定时任务）"
              class="enso-table"
          >
            <el-table-column prop="season" label="Season" width="110" fixed></el-table-column>
            <el-table-column prop="leNeg2" label="≤ -2.0" width="110" align="right"></el-table-column>
            <el-table-column prop="leNeg15" label="≤ -1.5" width="110" align="right"></el-table-column>
            <el-table-column prop="leNeg1" label="≤ -1.0" width="110" align="right"></el-table-column>
            <el-table-column prop="leNeg05" label="≤ -0.5" width="110" align="right"></el-table-column>
            <el-table-column prop="gePos05" label="≥ 0.5" width="110" align="right"></el-table-column>
            <el-table-column prop="gePos1" label="≥ 1.0" width="110" align="right"></el-table-column>
            <el-table-column prop="gePos15" label="≥ 1.5" width="110" align="right"></el-table-column>
            <el-table-column prop="gePos2" label="≥ 2.0" width="110" align="right"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <div class="footer">
      <span>提示：如果请求失败，多半是后端跨域未放行（localhost:5173）或后端服务未启动。</span>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'

export default {
  name: 'ForecastENSO',
  data() {
    return {
      baseUrl: 'http://localhost:9090',
      loading: false,
      fetching: false,
      errorMsg: '',
      data: {
        iriPublishedAt: null,
        cpcFetchedAt: null,
        iri: [],
        cpcStrengths: []
      },
      iriUrl: 'https://iri.columbia.edu/our-expertise/climate/forecasts/enso/current/',
      cpcUrl: 'https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso_advisory/strengths/index.php',
      chart: null
    }
  },
  computed: {
    iriRows() {
      return this.data.iri || []
    },
    cpcRows() {
      return this.data.cpcStrengths || []
    },
    statusText() {
      if (this.loading) return '加载中'
      if (this.errorMsg) return '异常'
      return '正常'
    },
    statusTagType() {
      if (this.errorMsg) return 'danger'
      if (this.loading) return 'info'
      return 'success'
    },
    statusDotClass() {
      if (this.errorMsg) return 'dotBad'
      if (this.loading) return 'dotLoading'
      return 'dotOk'
    },
    statusDetail() {
      if (this.loading) return '正在请求后端接口...'
      if (this.errorMsg) return '接口请求失败：请检查后端地址、跨域、服务状态'
      const iriCount = (this.iriRows || []).length
      const cpcCount = (this.cpcRows || []).length
      return `已获取 IRI ${iriCount} 行，CPC ${cpcCount} 行`
    }
  },
  mounted() {
    this.reload()
    window.addEventListener('resize', this.onResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    formatTime(v) {
      if (!v) return '-'
      // 兼容 LocalDateTime: 2026-02-19T00:00:00
      try {
        const d = new Date(v)
        if (isNaN(d.getTime())) return String(v)
        return d.toLocaleString()
      } catch (e) {
        return String(v)
      }
    },
    onResize() {
      if (this.chart) this.chart.resize()
    },
    ensureChart() {
      if (!this.$refs.chartEl) return
      if (!this.chart) this.chart = echarts.init(this.$refs.chartEl)
    },
    renderChart() {
      this.ensureChart()
      if (!this.chart) return

      const rows = this.iriRows || []
      const seasons = rows.map(r => r.season)
      const laNina = rows.map(r => r.laNina)
      const neutral = rows.map(r => r.neutral)
      const elNino = rows.map(r => r.elNino)

      this.chart.setOption({
        grid: { left: 40, right: 16, top: 24, bottom: 36 },
        tooltip: { trigger: 'axis' },
        legend: { top: 0, left: 0, data: ['La Niña', 'Neutral', 'El Niño'] },
        xAxis: { type: 'category', data: seasons, axisLabel: { interval: 0 } },
        yAxis: { type: 'value', min: 0, max: 100 },
        series: [
          { name: 'La Niña', type: 'line', smooth: true, data: laNina },
          { name: 'Neutral', type: 'line', smooth: true, data: neutral },
          { name: 'El Niño', type: 'line', smooth: true, data: elNino }
        ]
      })
    },
    async reload() {
      this.loading = true
      this.errorMsg = ''
      try {
        const res = await axios.get(`${this.baseUrl}/enso/latest`, { timeout: 20000 })
        this.data = res.data || this.data
        this.$nextTick(() => this.renderChart())
      } catch (e) {
        const msg = (e && e.response && e.response.data && e.response.data.message) || e.message || '请求失败'
        this.errorMsg = msg
      } finally {
        this.loading = false
      }
    },
    async manualFetch() {
      this.fetching = true
      try {
        await axios.post(`${this.baseUrl}/enso/fetch`, null, { timeout: 30000 })
        this.$message.success('已触发抓取，正在刷新...')
        await this.reload()
      } catch (e) {
        const msg = (e && e.response && e.response.data && e.response.data.message) || e.message || '触发抓取失败'
        this.$message.error(msg)
      } finally {
        this.fetching = false
      }
    }
  }
}
</script>

<style scoped>
.enso-page {
  min-height: 100vh;
  padding: 16px 16px 28px;
  background:
      radial-gradient(1200px 800px at 10% 10%, rgba(64, 158, 255, 0.12), transparent 40%),
      radial-gradient(1000px 600px at 90% 10%, rgba(103, 194, 58, 0.12), transparent 35%),
      #f6f8fb;
  color: #1f2937;
}

.enso-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 14px;
  border: 1px solid rgba(31, 41, 55, 0.08);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.06);
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.enso-logo {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.95), rgba(103, 194, 58, 0.95));
  color: white;
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.18);
}

.title .h1 {
  font-size: 18px;
  font-weight: 800;
  line-height: 1.2;
}
.title .sub {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(31, 41, 55, 0.65);
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.baseurl {
  width: 360px;
  max-width: 42vw;
}

.enso-row {
  margin-top: 16px;
}

.enso-card {
  border-radius: 14px;
  border: 1px solid rgba(31, 41, 55, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 650;
}

.metric {
  display: grid;
  gap: 6px;
  padding: 4px 0;
}
.metric-value {
  font-size: 20px;
  font-weight: 800;
}
.metric-hint {
  font-size: 12px;
  color: rgba(31, 41, 55, 0.62);
}
.links {
  margin-top: 10px;
}
.links a {
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
}
.links a:hover {
  text-decoration: underline;
}

.status-box {
  display: grid;
  gap: 10px;
}
.status-line {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}
.small {
  font-size: 12px;
  color: rgba(31, 41, 55, 0.7);
  display: grid;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dotOk { background: #22c55e; }
.dotBad { background: #ef4444; }
.dotLoading { background: #f59e0b; }

.chart-wrap {
  padding: 6px 0 10px;
}
.chart {
  width: 100%;
  height: 260px;
  border-radius: 12px;
  background: linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.35));
  border: 1px solid rgba(31, 41, 55, 0.06);
}

.hint {
  font-size: 12px;
  color: rgba(31, 41, 55, 0.62);
  margin-bottom: 10px;
}

.enso-alert {
  margin-top: 12px;
  border-radius: 12px;
}

.footer {
  margin-top: 18px;
  font-size: 12px;
  color: rgba(31, 41, 55, 0.55);
  text-align: center;
}
</style>