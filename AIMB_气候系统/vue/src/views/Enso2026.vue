<template>
  <main class="enso-workspace">
    <header class="topbar">
      <div class="brand"><span class="brand-mark">ENSO</span><span>长提前期预测与可信验证</span></div>
      <nav aria-label="ENSO 页面导航">
        <router-link v-for="item in pages" :key="item.path" :to="item.path" :class="{ active: $route.path === item.path || (item.path === '/enso/models' && isModelPage) }">{{ item.label }}</router-link>
      </nav>
      <el-button class="legacy-button" icon="el-icon-s-grid" @click="$router.push('/usermanage/introduce')">旧版系统</el-button>
    </header>

    <section class="page-heading">
      <div><p class="eyebrow">2026 AIMB · ENSO</p><h1>{{ currentPage.label }}</h1><p class="subtitle">聚焦 Niño3.4 / ONI 的长提前期预测、滚动回放与可复现验证</p></div>
    </section>

    <section v-if="pageKey === 'overview'" class="overview-view">
      <div class="metric-strip">
        <article><span>预测目标</span><strong>Niño3.4 / ONI</strong><small>指数产品 · ℃</small></article>
        <article><span>预测跨度</span><strong>20 <small>个月</small></strong><small>长提前期预测</small></article>
        <article><span>模型设计</span><strong>12 + 3 <small>个月</small></strong><small>长期窗口 + 近期窗口</small></article>
        <article><span>起报月份</span><strong>{{ forecastStart }}</strong><small>预测产品</small></article>
      </div>
      <div class="content-grid">
        <article class="panel chart-panel"><div class="panel-heading"><div><h2>Niño3.4 预测轨迹</h2><p>指数距平（℃）</p></div><el-button size="mini" icon="el-icon-refresh" :loading="loading" @click="loadForecast">重新加载</el-button></div><div ref="chart" class="chart"></div></article>
        <article class="panel"><div class="panel-heading"><div><h2>预测配置</h2><p>长短期信息聚合</p></div></div><div class="status-list"><div><i class="dot cyan"></i><span>预测目标</span><b>Niño3.4 / ONI</b></div><div><i class="dot gold"></i><span>预测长度</span><b>20 个月</b></div><div><i class="dot green"></i><span>输入变量</span><b>SSTA · SSTA150 · τx · τy</b></div></div><el-button type="primary" plain class="full-button" @click="$router.push('/enso/validation')">查看提前期验证 <i class="el-icon-right"></i></el-button></article>
      </div>
      <div class="section-row"><div><h2>工作流程</h2><p>产品生成、历史回放与结果验证</p></div><div class="workflow"><button v-for="p in pages.slice(1)" :key="p.path" @click="$router.push(p.path)"><span>{{ p.number }}</span><b>{{ p.label }}</b><i class="el-icon-arrow-right"></i></button></div></div>
    </section>

    <section v-else-if="pageKey === 'showcase'" class="legacy-showcase">
      <legacy-intro />
    </section>

    <section v-else-if="pageKey === 'monitor'" class="legacy-showcase">
      <forecast-enso-screen />
    </section>

    <section v-else-if="pageKey === 'introduction'" class="legacy-showcase">
      <enso-introduce />
    </section>

    <section v-else-if="pageKey === 'results'" class="legacy-showcase">
      <enso-forecast />
    </section>

    <section v-else-if="pageKey === 'data'" class="legacy-showcase">
      <enso-data />
    </section>

    <section v-else-if="pageKey === 'resources'" class="legacy-showcase">
      <enso-resource />
    </section>

    <section v-else-if="pageKey === 'models'" class="legacy-showcase">
      <div class="model-tabs" role="tablist" aria-label="预测模型结果">
        <router-link v-for="model in models" :key="model.key" :to="model.path" class="model-tab" :class="{ active: modelKey === model.key }" role="tab">{{ model.label }}</router-link>
      </div>
      <prediction-page v-if="modelKey === 'mepm'" :embedded="true" />
      <convlstm-prediction v-else :embedded="true" />
    </section>

    <section v-else-if="pageKey === 'forecast'" class="content-grid">
      <article class="panel controls-panel"><div class="panel-heading"><div><h2>预测设置</h2><p>LSTA-Swin</p></div></div><label>起报月份</label><el-date-picker v-model="forecastStart" type="month" value-format="yyyy-MM" placeholder="选择月份" :clearable="false" /> <label>预测长度</label><el-select v-model="leadMonths"><el-option v-for="n in [10, 20]" :key="n" :label="n + ' 个月'" :value="n" /></el-select><div class="model-spec"><b>模型配置</b><span>长期窗口 12 个月</span><span>近期窗口 3 个月</span><span>SSTA · SSTA150 · τx · τy</span></div><el-button type="primary" :loading="loading" @click="loadForecast">生成预测</el-button></article>
      <article class="panel chart-panel"><div class="panel-heading"><div><h2>未来 Niño3.4 / ONI 曲线</h2><p>{{ forecastStart }} 起报 · 单位 ℃</p></div><div><el-button size="mini" icon="el-icon-download" @click="exportJson">JSON</el-button><el-button size="mini" icon="el-icon-document" @click="exportCsv">CSV</el-button></div></div><div ref="chart" class="chart"></div></article>
      <article class="panel map-panel"><div class="panel-heading"><div><h2>赤道太平洋空间场</h2><p>{{ selectedVariable }}</p></div><el-select v-model="selectedVariable" size="mini" class="variable-select"><el-option v-for="v in variables" :key="v" :label="v" :value="v" /></el-select></div><div class="heatmap"><div v-for="(row, yi) in heatmap" :key="yi" class="heat-row"><span v-for="(cell, xi) in row" :key="xi" :title="cell.toFixed(2) + ' ℃'" :style="heatStyle(cell)"></span></div></div><div class="map-axis"><span>120°E</span><span>180°</span><span>80°W</span></div></article>
    </section>

    <section v-else-if="pageKey === 'replay'" class="content-grid">
      <article class="panel controls-panel"><div class="panel-heading"><div><h2>2025-2026 历史回放</h2><p>选择起报月份</p></div></div><el-select v-model="replayStart"><el-option v-for="m in replayOptions" :key="m" :label="m" :value="m" /></el-select><el-button type="primary" :loading="loading" @click="loadReplay">加载回放</el-button></article>
      <article class="panel chart-panel"><div class="panel-heading"><div><h2>{{ replayStart }} 起报回放轨迹</h2><p>Niño3.4 指数距平（℃）</p></div><div><el-button size="mini" icon="el-icon-download" @click="exportJson">JSON</el-button><el-button size="mini" icon="el-icon-document" @click="exportCsv">CSV</el-button></div></div><div ref="chart" class="chart"></div></article>
    </section>

    <section v-else-if="pageKey === 'validation'" class="panel table-panel">
      <div class="panel-heading">
        <div><h2>逐提前期验证与基线比较</h2><p v-if="comparisonReplayStart">整体验证与 {{ comparisonReplayStart }} 回放案例</p></div>
        <div class="validation-actions">
          <el-button size="mini" type="primary" plain icon="el-icon-data-analysis" @click="openComparisonDialog">选择历史回放对比</el-button>
          <el-button v-if="comparisonReplayStart" size="mini" icon="el-icon-video-play" @click="openReplayComparison">查看回放</el-button>
          <el-button size="mini" icon="el-icon-document" @click="exportCsv">导出 CSV</el-button>
        </div>
      </div>
      <el-table v-if="comparisonReplayStart" :data="comparisonRows" stripe>
        <el-table-column prop="lead" label="提前期" width="100"><template slot-scope="s">{{ s.row.lead }} 个月</template></el-table-column>
        <el-table-column prop="pcc" label="整体 PCC" width="100"><template slot-scope="s">{{ formatValidationValue(s.row.pcc) }}</template></el-table-column>
        <el-table-column prop="mae" label="整体 MAE" width="100"><template slot-scope="s">{{ formatValidationValue(s.row.mae) }}</template></el-table-column>
        <el-table-column prop="rmse" label="整体 RMSE" width="100"><template slot-scope="s">{{ formatValidationValue(s.row.rmse) }}</template></el-table-column>
        <el-table-column prop="sampleCount" label="样本数" width="85"/>
        <el-table-column prop="replayMonth" :label="comparisonReplayStart + ' 目标月'" width="125"/>
        <el-table-column prop="replayForecast" label="案例预测值" width="110"><template slot-scope="s">{{ formatValidationValue(s.row.replayForecast) }}</template></el-table-column>
        <el-table-column prop="replayObserved" label="对应观测值" width="110"><template slot-scope="s">{{ formatValidationValue(s.row.replayObserved) }}</template></el-table-column>
        <el-table-column prop="replayError" label="预测误差" width="100"><template slot-scope="s">{{ formatValidationValue(s.row.replayError) }}</template></el-table-column>
      </el-table>
      <el-table v-else :data="leadRows" stripe>
        <el-table-column prop="lead" label="提前期" width="110"><template slot-scope="s">{{ s.row.lead }} 个月</template></el-table-column>
        <el-table-column prop="pcc" label="PCC"><template slot-scope="s">{{ formatValidationValue(s.row.pcc) }}</template></el-table-column>
        <el-table-column prop="mae" label="MAE"><template slot-scope="s">{{ formatValidationValue(s.row.mae) }}</template></el-table-column>
        <el-table-column prop="rmse" label="RMSE"><template slot-scope="s">{{ formatValidationValue(s.row.rmse) }}</template></el-table-column>
        <el-table-column prop="sampleCount" label="样本数" width="100"/>
      </el-table>
      <div class="subsection"><h3>论文消融结果</h3><el-table :data="paperMetrics" size="mini"><el-table-column prop="variant" label="模型变体"/><el-table-column prop="pcc" label="PCC"/></el-table></div>
      <el-dialog title="选择历史回放案例" :visible.sync="comparisonDialogVisible" width="420px" append-to-body>
        <div class="comparison-picker"><span>回放起报月份</span><el-select v-model="validationReplayStart" aria-label="历史回放起报月份"><el-option v-for="m in replayOptions" :key="m" :label="m" :value="m" /></el-select></div>
        <span slot="footer"><el-button size="small" @click="comparisonDialogVisible = false">取消</el-button><el-button size="small" type="primary" @click="applyReplayComparison">开始比较</el-button></span>
      </el-dialog>
    </section>

    <section v-else-if="pageKey === 'explainability'" class="content-grid explainability-view">
      <article class="panel paper-gallery-panel">
        <div class="panel-heading"><div><h2>论文可解释性验证</h2><p>{{ activeExplainabilityFigure.label }}</p></div><el-select v-model="selectedFigure" size="mini" class="variable-select"><el-option v-for="figure in explainabilityFigures" :key="figure.key" :label="figure.label" :value="figure.key" /></el-select></div>
        <div class="paper-image-frame"><img :src="activeExplainabilityFigure.src" :alt="activeExplainabilityFigure.label"></div>
        <p class="paper-caption">{{ activeExplainabilityFigure.caption }}</p>
      </article>
      <article class="panel paper-index-panel"><div class="panel-heading"><div><h2>验证图集</h2><p>LSTA-Swin 论文图</p></div></div><button v-for="figure in explainabilityFigures" :key="figure.key" class="paper-index-item" :class="{ active: selectedFigure === figure.key }" @click="selectedFigure = figure.key"><img :src="figure.src" :alt="figure.label"><span>{{ figure.label }}</span><i class="el-icon-arrow-right"></i></button></article>
    </section>

    <section v-else-if="pageKey === 'climate-data'" class="climate-data-page">
      <user-grid-data :visualization-only="true" />
    </section>

    <section v-else class="data-layout">
      <article class="panel"><div class="panel-heading"><div><h2>模型配置</h2></div><el-button size="mini" icon="el-icon-refresh" :loading="loading" @click="loadMetadata">刷新</el-button></div><el-descriptions :column="2" border><el-descriptions-item label="预测模型">LSTA-Swin</el-descriptions-item><el-descriptions-item label="预测目标">Niño3.4 / ONI</el-descriptions-item><el-descriptions-item label="长期 / 短期窗口">12 个月 / 近 3 个月</el-descriptions-item><el-descriptions-item label="输入变量">SSTA、SSTA150、τx、τy</el-descriptions-item><el-descriptions-item label="预测长度">20 个月</el-descriptions-item><el-descriptions-item label="空间范围">热带太平洋</el-descriptions-item></el-descriptions><el-button class="climate-entry-button" type="primary" plain icon="el-icon-picture-outline" @click="$router.push('/enso/climate-data')">打开气候 NC 可视化</el-button></article>
    </section>

    <footer>2026 AIMB ENSO</footer>
  </main>
</template>

<script>
import demoCases from '@/data/ensoDemo'
import LegacyIntro from './introducePage.vue'
import ForecastEnsoScreen from './forecast/forecastenso.vue'
import EnsoIntroduce from './ENSO_introduce.vue'
import EnsoForecast from './ENSO/ENSO_forecast.vue'
import EnsoData from './ENSO/ENSO_data.vue'
import EnsoResource from './ENSO/ENSO_resource.vue'
import PredictionPage from './enso_prediction.vue'
import ConvlstmPrediction from './convlstm_prediction.vue'
import UserGridData from './User_GridData.vue'

export default {
  name: 'Enso2026',
  data() {
    const replayOptions = demoCases.replayOptions
    const queryStart = Array.isArray(this.$route.query.start) ? this.$route.query.start[0] : this.$route.query.start
    const replayStart = replayOptions.includes(queryStart) ? queryStart : '2025-01'
    return {
      pages: [
        { path: '/enso/overview', label: '总览', number: '01' }, { path: '/enso/showcase', label: '简介大屏', number: '02' },
        { path: '/enso/monitor', label: 'ENSO监测', number: '03' }, { path: '/enso/introduction', label: 'ENSO介绍', number: '04' },
        { path: '/enso/models', label: '预测模型', number: '05' },
        { path: '/enso/replay', label: '历史回放', number: '06' }, { path: '/enso/validation', label: '提前期验证', number: '07' },
        { path: '/enso/explainability', label: '物理解释', number: '08' }, { path: '/enso/data-model', label: '数据与模型', number: '09' },
        { path: '/enso/climate-data', label: '气候 NC', number: '10' }
      ],
      models: [
        { key: 'mepm', path: '/enso/models/mepm', label: 'LSTA-Swin预测结果' },
        { key: 'convlstm', path: '/enso/models/convlstm', label: 'ConvLSTM预测结果' }
      ],
      variables: ['SSTA', 'SSTA150', 'τx', 'τy'], selectedVariable: 'SSTA', selectedFigure: 'spatiotemporal', forecastStart: '2023-03', leadMonths: 20,
      replayOptions, replayStart, validationReplayStart: replayStart, comparisonReplayStart: '', comparisonRows: [], comparisonDialogVisible: false, data: {}, metadata: demoCases.metadata, paperMetrics: demoCases.metrics.ablation, leadRows: demoCases.metrics.systemResults, heatmap: demoCases.explainability.heatmap, loading: false,
      chart: null,
      explainabilityFigures: [
        { key: 'correlation', label: '图 2 · 提前期相关性', src: require('@/assets/LSTA-Swin/figure-2-correlation.png'), caption: '不同模型在 1–20 个月提前期上的相关系数对比。' },
        { key: 'oni', label: '图 3 · ONI 预测对比', src: require('@/assets/LSTA-Swin/figure-3-oni-comparison.png'), caption: '2015–2016 ENSO 事件中不同起报月份的 ONI 轨迹对比。' },
        { key: 'spatiotemporal', label: '图 4 · 时空演化', src: require('@/assets/LSTA-Swin/figure-4-spatiotemporal.png'), caption: '海温异常与风应力场在 ENSO 生命周期中的时空演化。' },
        { key: 'saliency', label: '图 5 · 预测显著性', src: require('@/assets/LSTA-Swin/figure-5-saliency.png'), caption: '模型预测相关显著性分布及 Niño3.4 区域贡献。' }
      ]
    }
  },
  computed: {
    isModelPage() { return this.$route.path === '/enso/models' || this.$route.path.indexOf('/enso/models/') === 0 },
    modelKey() {
      const key = this.$route.path.split('/').pop()
      return this.models.some(model => model.key === key) ? key : 'mepm'
    },
    pageKey() { return this.isModelPage ? 'models' : this.$route.path.split('/').pop() },
    activeExplainabilityFigure() { return this.explainabilityFigures.find(figure => figure.key === this.selectedFigure) || this.explainabilityFigures[0] },
    currentPage() {
      if (this.isModelPage) return this.pages.find(p => p.path === '/enso/models')
      const page = this.pages.find(p => p.path === this.$route.path)
      if (page) return page
      const nestedLabels = {
        '/enso/results': 'ENSO预测结果',
        '/enso/data': 'ENSO数据',
        '/enso/resources': 'ENSO更多资源'
      }
      return { path: this.$route.path, label: nestedLabels[this.$route.path] || this.pages[0].label }
    }
  },
  components: { LegacyIntro, ForecastEnsoScreen, EnsoIntroduce, EnsoForecast, EnsoData, EnsoResource, PredictionPage, ConvlstmPrediction, UserGridData },
  watch: {
    '$route.path'() { this.$nextTick(() => this.loadForPage()) }
  },
  created() { this.loadForPage() },
  mounted() { window.addEventListener('resize', this.resizeChart) },
  beforeDestroy() { window.removeEventListener('resize', this.resizeChart); if (this.chart) this.chart.dispose() },
  methods: {
    async loadForPage() {
      const key = this.pageKey
      if (key === 'data-model') return this.loadMetadata()
      if (key === 'validation') return this.loadMetrics()
      if (key === 'replay') return this.loadReplay()
      if (key === 'explainability') return this.loadExplainability()
      if (key === 'overview' || key === 'forecast') return this.loadForecast()
    },
    wait(ms) { return new Promise(resolve => setTimeout(resolve, ms)) },
    async loadForecast() {
      this.loading = true
      try { await this.wait(1600); this.data = demoCases.forecast(this.forecastStart, this.leadMonths); this.heatmap = this.data.spatialMaps || []; this.$nextTick(() => this.renderChart(this.data.series || [], true)) }
      catch (e) { this.showError(e) } finally { this.loading = false }
    },
    openReplayComparison() {
      const start = this.comparisonReplayStart || this.validationReplayStart
      this.replayStart = start
      this.$router.push({ path: '/enso/replay', query: { start } })
    },
    openComparisonDialog() {
      this.validationReplayStart = this.comparisonReplayStart || this.replayStart
      this.comparisonDialogVisible = true
    },
    applyReplayComparison() {
      const replay = demoCases.replay(this.validationReplayStart)
      const metricsByLead = new Map(this.leadRows.map(row => [row.lead, row]))
      this.comparisonRows = replay.series.map(point => ({
        ...(metricsByLead.get(point.leadMonth) || {
          lead: point.leadMonth,
          pcc: null,
          mae: null,
          rmse: null,
          sampleCount: null
        }),
        ...this.replayPointForLead(replay.series, point.leadMonth)
      }))
      this.comparisonReplayStart = this.validationReplayStart
      this.comparisonDialogVisible = false
    },
    replayPointForLead(series, lead) {
      const point = series.find(item => item.leadMonth === lead)
      const hasObservation = point && Number.isFinite(point.observed)
      return {
        replayMonth: point ? point.month : '',
        replayForecast: point ? point.value : null,
        replayObserved: hasObservation ? point.observed : null,
        replayError: hasObservation ? Number((point.value - point.observed).toFixed(2)) : null
      }
    },
    formatValidationValue(value) { return Number.isFinite(value) ? value.toFixed(2) : '—' },
    async loadReplay() {
      this.loading = true
      try { await this.wait(1300); this.data = demoCases.replay(this.replayStart); this.$nextTick(() => this.renderChart(this.data.series || [], false, true)) }
      catch (e) { this.showError(e) } finally { this.loading = false }
    },
    async loadMetrics() {
      this.loading = true
      try { await this.wait(1100); this.data = demoCases.metrics; this.paperMetrics = this.data.ablation || []; this.leadRows = this.data.systemResults || [] }
      catch (e) { this.showError(e) } finally { this.loading = false }
    },
    async loadExplainability() {
      this.loading = true
      try { await this.wait(1000); this.data = demoCases.explainability; this.heatmap = this.data.heatmap || [] }
      catch (e) { this.showError(e) } finally { this.loading = false }
    },
    async loadMetadata() {
      this.loading = true
      try { await this.wait(800); this.metadata = demoCases.metadata }
      catch (e) { this.showError(e) } finally { this.loading = false }
    },
    renderChart(series, interval, replay) {
      if (!this.$refs.chart || !series.length) return
      if (this.chart) this.chart.dispose()
      this.chart = this.$echarts.init(this.$refs.chart)
      const option = {
        backgroundColor: 'transparent', animation: false,
        tooltip: { trigger: 'axis', valueFormatter: value => value + ' ℃' },
        legend: { data: replay ? ['预测值', '参考 ONI'] : (interval ? ['预测值', '下界', '上界'] : ['预测值']), textStyle: { color: '#bcc8d1' }, top: 4 },
        grid: { left: 48, right: 22, top: 42, bottom: 36 },
        xAxis: { type: 'category', data: series.map(p => p.month), axisLabel: { color: '#a4b0ba', interval: 2 }, axisLine: { lineStyle: { color: '#41505b' } } },
        yAxis: { type: 'value', name: '℃', nameTextStyle: { color: '#a4b0ba' }, axisLabel: { color: '#a4b0ba' }, splitLine: { lineStyle: { color: 'rgba(150,170,180,.12)' } } },
        series: [{ name: '预测值', type: 'line', smooth: true, symbol: 'none', data: series.map(p => p.value), lineStyle: { color: '#38c6b0', width: 3 }, areaStyle: { color: 'rgba(56,198,176,.12)' } }]
      }
      if (interval) option.series.push({ name: '下界', type: 'line', symbol: 'none', lineStyle: { type: 'dashed', color: '#e4b75c' }, data: series.map(p => p.lower) }, { name: '上界', type: 'line', symbol: 'none', lineStyle: { type: 'dashed', color: '#e4b75c' }, data: series.map(p => p.upper) })
      if (replay) option.series.push({ name: '参考 ONI', type: 'line', connectNulls: false, symbol: 'circle', symbolSize: 5, data: series.map(p => p.observed), lineStyle: { color: '#e4b75c', width: 2, type: 'dashed' }, itemStyle: { color: '#e4b75c' } })
      this.chart.setOption(option)
    },
    heatStyle(value) { const t = Math.max(0, Math.min(1, (value + 0.2) / 1.65)); return { backgroundColor: `hsl(${190 - 178 * t}, ${34 + 38 * t}%, ${24 + 31 * t}%)` } },
    resizeChart() { if (this.chart) this.chart.resize() },
    downloadFile(content, type, extension) { const blob = new Blob([content], { type }); const link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = `enso-${this.pageKey}-${this.data.forecastStart || this.forecastStart}.${extension}`; link.click(); URL.revokeObjectURL(link.href) },
    exportJson() { this.downloadFile(JSON.stringify(this.data, null, 2), 'application/json', 'json') },
    exportCsv() {
      const rows = this.pageKey === 'validation' ? (this.comparisonReplayStart ? this.comparisonRows : this.leadRows) : (this.data.series || [])
      const columns = Array.from(new Set(rows.reduce((keys, row) => keys.concat(Object.keys(row)), [])))
      const quote = value => `"${String(value == null ? '' : value).replace(/"/g, '""')}"`
      const csv = [columns.map(quote).join(','), ...rows.map(row => columns.map(key => quote(row[key])).join(','))].join('\r\n')
      this.downloadFile('\ufeff' + csv, 'text/csv;charset=utf-8', 'csv')
    },
    showError(error) { this.$message.error(`ENSO 接口加载失败：${error.message || '请检查后端服务'}`) }
  }
}
</script>

<style scoped>
.enso-workspace{--bg:#10191d;--panel:#172328;--line:#2d4146;--text:#e2ece8;--muted:#9aada9;--teal:#38c6b0;--gold:#e4b75c;min-height:100vh;background:radial-gradient(ellipse at 84% 0%,#1b3433 0,transparent 38%),#10191d;color:var(--text);padding:0 4.2%;box-sizing:border-box;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}.topbar{height:68px;display:flex;align-items:center;border-bottom:1px solid var(--line);gap:28px}.brand{display:flex;align-items:center;gap:11px;white-space:nowrap;font-size:14px;font-weight:600}.brand-mark{background:#d4efe6;color:#15342e;padding:7px 9px;font-size:12px;font-weight:800;border-radius:3px}.topbar nav{display:flex;align-self:stretch;align-items:stretch;gap:4px;flex:1}.topbar nav a{display:flex;align-items:center;color:var(--muted);text-decoration:none;padding:0 13px;font-size:13px;border-bottom:2px solid transparent}.topbar nav a.active,.topbar nav a:hover{color:var(--text);border-color:var(--teal)}.source-button{color:#bcd0ca;background:transparent;border-color:var(--line)}.page-heading{display:flex;justify-content:space-between;align-items:center;padding:30px 0 18px}.eyebrow{font-size:11px;color:var(--teal);margin:0 0 8px;text-transform:uppercase}.page-heading h1{font-size:25px;margin:0;font-weight:650}.subtitle{margin:8px 0 0;color:var(--muted);font-size:13px}.page-heading .el-tag{background:#483a20;border:0;color:#f0c76f}.notice{display:flex;align-items:center;gap:10px;padding:11px 14px;background:#28281e;border-left:3px solid var(--gold);color:#e1d5ad;font-size:12px;margin:0 0 20px}.notice button{margin-left:auto;background:none;border:0;color:#f0c76f;cursor:pointer}.metric-strip{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));border:1px solid var(--line);background:#142024;margin-bottom:18px}.metric-strip article{padding:16px 19px;border-right:1px solid var(--line)}.metric-strip article:last-child{border:0}.metric-strip span,.metric-strip small{display:block;color:var(--muted);font-size:11px}.metric-strip strong{display:block;font-size:22px;margin:8px 0 5px;color:#e4f1eb}.metric-strip strong small{display:inline;color:var(--muted);font-size:13px}.content-grid{display:grid;grid-template-columns:minmax(230px, .82fr) minmax(0,2fr);gap:16px}.panel{background:rgba(23,35,40,.92);border:1px solid var(--line);padding:18px;min-width:0}.chart-panel{grid-column:span 1}.overview-view .content-grid{grid-template-columns:minmax(0,1.8fr) minmax(260px,.8fr)}.panel-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;margin-bottom:14px}.panel-heading h2,.section-row h2,.panel h2{font-size:15px;margin:0;font-weight:600}.panel-heading p,.section-row p{color:var(--muted);font-size:11px;margin:6px 0 0}.chart{height:295px;width:100%}.chart-caption{color:#9eb2ac;font-size:10px;border-top:1px solid rgba(130,160,150,.13);padding-top:9px;margin-top:9px}.overview-view .chart{height:270px}.status-list{padding:10px 0 25px}.status-list div{display:flex;align-items:center;gap:9px;padding:12px 0;border-bottom:1px solid rgba(130,160,150,.13);font-size:12px}.status-list b{margin-left:auto;font-size:11px;color:#d5c184;font-weight:500}.dot{width:7px;height:7px;border-radius:50%}.cyan{background:#54c9d1}.gold{background:#e4b75c}.green{background:#38c6b0}.full-button{width:100%;background:transparent;border-color:#42655f;color:#9ddbd0}.section-row{display:grid;grid-template-columns:210px 1fr;gap:20px;padding:25px 0}.workflow{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));border:1px solid var(--line)}.workflow button{display:flex;align-items:center;gap:8px;text-align:left;padding:13px 10px;background:transparent;border:0;border-right:1px solid var(--line);color:var(--text);cursor:pointer}.workflow button span{color:var(--teal);font-size:10px}.workflow button b{font-size:11px;font-weight:500}.workflow button i{margin-left:auto;color:var(--muted)}.controls-panel label{display:block;color:var(--muted);font-size:11px;margin:14px 0 6px}.controls-panel .el-date-editor,.controls-panel .el-select{width:100%;margin-bottom:7px}.controls-panel>.el-button{width:100%;margin-top:12px}.model-spec{display:flex;flex-direction:column;gap:10px;padding:16px 0;border-top:1px solid var(--line);border-bottom:1px solid var(--line);margin:18px 0;color:#c2d1cc;font-size:11px}.model-spec b{color:var(--text);font-size:12px}.unavailable{color:#e4b75c}.map-panel{grid-column:1/-1}.variable-select{width:120px}.heatmap{padding:12px 10px 4px;max-width:780px;margin:auto}.heat-row{display:grid;grid-template-columns:repeat(15,1fr);gap:4px;margin-bottom:4px}.heat-row span{height:17px;border-radius:2px}.large-heatmap .heat-row span{height:23px}.map-axis{max-width:780px;margin:4px auto;display:flex;justify-content:space-between;color:var(--muted);font-size:10px}.table-panel{margin-bottom:20px}.table-panel>>>.el-table,.table-panel>>>.el-table th,.table-panel>>>.el-table tr{background:transparent;color:#d2dfda}.table-panel>>>.el-table td,.table-panel>>>.el-table th.is-leaf{border-color:var(--line)}.table-panel>>>.el-table::before{background:var(--line)}.subsection{margin-top:25px}.subsection h3{font-size:13px;margin-bottom:5px}.subsection>p{font-size:11px;color:var(--muted)}.limitation,.source-warning{padding:12px;background:#24251f;color:#d5c184;font-size:11px;line-height:1.7;margin-top:12px}.stage-list>div{display:flex;gap:15px;padding:17px 0;border-bottom:1px solid var(--line)}.stage-list>div>b{color:var(--teal);font-size:12px}.stage-list strong{font-size:12px}.stage-list p{color:var(--muted);font-size:11px;line-height:1.6;margin:6px 0 0}.data-layout{display:grid;grid-template-columns:1.6fr 1fr;gap:16px}.data-layout>>>.el-descriptions__body,.data-layout>>>.el-descriptions__table,.data-layout>>>.el-descriptions-item__label,.data-layout>>>.el-descriptions-item__content{background:#172328!important;border-color:var(--line)!important;color:#d7e2dd!important}.checklist p{font-size:11px;color:#c0d0ca;margin:16px 0}.checklist p i{color:#e4b75c;margin-right:6px}.checklist p:last-child i{color:var(--teal)}footer{padding:20px 0 25px;border-top:1px solid var(--line);color:#879994;font-size:10px}footer span{float:right}@media(max-width:1100px){.enso-workspace{padding:0 22px}.topbar{height:auto;min-height:64px;flex-wrap:wrap;gap:10px;padding:10px 0}.topbar nav{order:3;flex-basis:100%;overflow:auto;min-height:42px}.topbar nav a{white-space:nowrap;padding:0 10px}.metric-strip{grid-template-columns:repeat(2,1fr)}.metric-strip article:nth-child(2){border-right:0}.metric-strip article:nth-child(-n+2){border-bottom:1px solid var(--line)}.workflow{grid-template-columns:repeat(3,1fr)}.workflow button:nth-child(3){border-right:0}.workflow button:nth-child(-n+3){border-bottom:1px solid var(--line)}}@media(max-width:720px){.page-heading{align-items:flex-start;gap:12px}.page-heading h1{font-size:21px}.page-heading>.el-tag{white-space:nowrap}.overview-view .content-grid,.content-grid,.data-layout{grid-template-columns:1fr}.chart-panel,.map-panel{grid-column:auto}.section-row{grid-template-columns:1fr;gap:12px}.workflow{grid-template-columns:repeat(2,1fr)}.workflow button:nth-child(3){border-right:1px solid var(--line)}.workflow button:nth-child(2n){border-right:0}.workflow button:nth-child(-n+4){border-bottom:1px solid var(--line)}.chart{height:250px}.notice{align-items:flex-start}.notice button{white-space:nowrap}.topbar .source-button{margin-left:auto}}
.legacy-button{color:#dcebe5;background:#213733;border-color:#45645c}.legacy-button:hover{color:#fff;background:#2b4b43;border-color:#5c9183}.showcase-notice{display:flex;align-items:center;gap:9px;margin-bottom:14px;padding:10px 13px;background:#28281e;border-left:3px solid #e4b75c;color:#e1d5ad;font-size:12px;line-height:1.6}.model-tabs{display:flex;flex-wrap:wrap;gap:8px;margin:0 0 16px;padding-bottom:12px;border-bottom:1px solid var(--line)}.model-tab{padding:8px 12px;border:1px solid #38514d;color:#a9c4bd;text-decoration:none;font-size:12px;background:#142024}.model-tab:hover,.model-tab.active{color:#e8fff8;border-color:var(--teal);background:#1d3935}.legacy-showcase>>>.page-container{margin:0!important}.legacy-showcase>>>.hero{margin-top:0!important}
.validation-actions{display:flex;align-items:center;justify-content:flex-end;gap:8px;flex-wrap:wrap}.validation-actions .el-select{width:125px}@media(max-width:720px){.table-panel .panel-heading{flex-wrap:wrap}.validation-actions{justify-content:flex-start}}
.comparison-picker{display:flex;align-items:center;gap:14px}.comparison-picker .el-select{flex:1}
.explainability-view{grid-template-columns:minmax(0,2fr) minmax(260px,.8fr)}.paper-gallery-panel{min-width:0}.paper-image-frame{display:flex;align-items:center;justify-content:center;min-height:330px;padding:12px;background:#10191d;border:1px solid var(--line);overflow:auto}.paper-image-frame img{display:block;max-width:100%;max-height:560px;width:auto;height:auto}.paper-caption{margin:10px 0 0;color:var(--muted);font-size:11px;line-height:1.7}.paper-index-panel{display:flex;flex-direction:column;gap:8px}.paper-index-item{display:grid;grid-template-columns:78px 1fr auto;align-items:center;gap:10px;width:100%;padding:8px;border:1px solid var(--line);background:#142024;color:var(--text);text-align:left;cursor:pointer}.paper-index-item img{display:block;width:78px;height:48px;object-fit:cover}.paper-index-item span{font-size:11px}.paper-index-item i{color:var(--muted)}.paper-index-item.active,.paper-index-item:hover{border-color:var(--teal);background:#1d3935}.climate-data-page{margin:0 -2%;background:#f5f7fa;min-height:560px}.climate-entry-button{margin-top:18px}@media(max-width:720px){.explainability-view{grid-template-columns:1fr}.paper-image-frame{min-height:220px}.climate-data-page{margin:0 -22px}}
</style>
