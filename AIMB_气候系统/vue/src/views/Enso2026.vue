<template>
  <main :class="['enso-workspace', { 'monitor-mode': pageKey === 'monitor' }]">
    <header v-if="pageKey !== 'monitor'" class="topbar">
      <div class="brand"><span class="brand-mark">ENSO</span><span>长提前期预测与可信验证</span></div>
      <nav aria-label="ENSO 页面导航">
        <router-link v-for="item in pages" :key="item.path" :to="item.path" :class="{ active: $route.path === item.path || (item.path === '/enso/models' && isModelPage) }">{{ item.label }}</router-link>
      </nav>
      <el-button class="legacy-button" icon="el-icon-s-grid" @click="$router.push('/usermanage/introduce')">旧版系统</el-button>
    </header>

    <section v-if="pageKey !== 'monitor'" class="page-heading">
      <div><p class="eyebrow">2026 AIMB · ENSO</p><h1>{{ currentPage.label }}</h1><p v-if="pageKey === 'overview'" class="subtitle">从热带太平洋海气信号到 ENSO 预测、验证与气候风险研判</p></div>
    </section>

    <section v-if="pageKey === 'overview'" class="system-introduction-page">
      <div class="system-intro-hero">
        <div class="system-intro-copy">
          <p class="system-intro-kicker">AIMB · CLIMATE INTELLIGENCE PLATFORM</p>
          <h2>面向 ENSO 年际预测与气候风险研判的一体化工作台</h2>
          <p class="system-intro-lead">本平台围绕热带太平洋海气耦合过程，整合海温距平、次表层海温、风应力和气候指数等信息，在同一工作台内贯通“实时观测 — 历史事件回放 — 模型历史预测”三种数据模式，提供 Niño3.4 / ONI 长提前期预测、探针读数 / 框选统计 / 纬向剖面等交互分析、2D/3D 海温风场联动与 AI 影响研判，帮助用户从“看见信号”走到“解释信号、评估信号、服务决策”。</p>
          <div class="system-intro-actions">
            <el-button type="primary" icon="el-icon-monitor" @click="$router.push('/enso/monitor')">进入 ENSO 监测</el-button>
            <el-button plain icon="el-icon-data-analysis" @click="$router.push('/enso/models')">查看预测模型</el-button>
          </div>
          <div class="system-intro-tags"><span><i class="el-icon-monitor"></i>实时 · 事件 · 模型三态联动</span><span><i class="el-icon-data-line"></i>20 个月预测跨度</span><span><i class="el-icon-odometer"></i>1–20 个月逐提前期评估</span><span><i class="el-icon-cpu"></i>AI 影响研判</span></div>
        </div>
        <div class="system-intro-signal" aria-label="平台能力概览">
          <div class="signal-orbit signal-orbit-one"></div><div class="signal-orbit signal-orbit-two"></div>
          <div class="signal-core"><span>ENSO</span><strong>DATA → SIGNAL</strong><small>从监测到研判</small></div>
          <div class="signal-node node-top"><i class="el-icon-sunny"></i><span>海温距平</span></div>
          <div class="signal-node node-right"><i class="el-icon-data-analysis"></i><span>模型预测</span></div>
          <div class="signal-node node-bottom"><i class="el-icon-s-check"></i><span>可信验证</span></div>
          <div class="signal-node node-left"><i class="el-icon-warning-outline"></i><span>风险研判</span></div>
        </div>
      </div>

      <section class="system-intro-section">
        <div class="system-section-heading"><div><p class="system-intro-kicker">SYSTEM PROFILE</p><h2>系统简介</h2></div><span>从海气观测到区域气候影响</span></div>
        <div class="system-profile-grid">
          <article class="system-profile-main"><p>ENSO 是连接热带太平洋海洋状态与全球大气环流的重要年际信号。平台以 Niño3.4 / ONI 为核心预测对象，将热带太平洋海温距平、次表层热状态、海表风应力等关键变量组织为可追溯的预测输入，并用长短期窗口提取海气演变中的持续性与转折信息。</p><p>用户可以在同一套工作流中切换“实时状态、ENSO 历史事件、模型历史预测”三种数据模式：监测端提供 2D/3D 海温与风场地图，内置探针读数、点位时序、框选统计、异常热点、等值线、卷帘对比、纬向剖面与鹰眼导航等交互分析工具，并以 Niño3.4 阈值告警提示位相跨越；回放端按月联动空间场、指数与风场，选择回放对象确认后即可逐月播放，并对照实际观测评估提前期技巧。预测结果进一步衔接中国区域降水、温度和农业风险研判，悬浮 AI 助手与影响研判 Agent 全程随行，为气候服务提供结构化证据。</p></article>
          <div class="system-profile-facts"><div><span>预测对象</span><strong>Niño3.4 / ONI</strong><small>月尺度海温指数距平</small></div><div><span>主要输入</span><strong>SSTA · SSTA150 · τx · τy</strong><small>海温、次表层与风应力场</small></div><div><span>输出形态</span><strong>轨迹 · 空间场 · 风险</strong><small>预测、解释与影响研判</small></div></div>
        </div>
      </section>

      <section class="system-intro-section">
        <div class="system-section-heading"><div><p class="system-intro-kicker">CORE CAPABILITIES</p><h2>平台能力地图</h2></div><span>每个模块都对应一类可复核的业务动作</span></div>
        <div class="system-capability-grid">
          <article v-for="capability in systemCapabilities" :key="capability.title" class="system-capability-card" :class="capability.tone"><span class="capability-icon"><i :class="capability.icon"></i></span><div><h3>{{ capability.title }}</h3><p>{{ capability.description }}</p><router-link v-if="capability.path" :to="capability.path">{{ capability.action }}<i class="el-icon-arrow-right"></i></router-link></div></article>
        </div>
      </section>

      <section class="system-intro-section system-workflow-section">
        <div class="system-section-heading"><div><p class="system-intro-kicker">ANALYSIS WORKFLOW</p><h2>从信号到结论</h2></div><span>让每一步分析都能回到数据和证据</span></div>
        <div class="system-workflow"><div v-for="step in systemWorkflow" :key="step.number" class="system-workflow-step"><span>{{ step.number }}</span><div><strong>{{ step.title }}</strong><p>{{ step.description }}</p></div><i class="el-icon-arrow-right"></i></div></div>
      </section>
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
      <article class="panel controls-panel">
        <div class="panel-heading"><div><h2>2025-2026 历史回放</h2><p>选择起报月份</p></div></div>
        <el-select v-model="replayStart" aria-label="选择历史回放起报月份" @change="loadReplay"><el-option v-for="m in replayOptions" :key="m" :label="m" :value="m" /></el-select>
        <el-button type="primary" :loading="loading" @click="loadReplay">加载回放</el-button>
        <div class="replay-agent-hint"><i class="el-icon-cpu"></i><span>ENSO 影响研判 Agent</span><small>随回放月份联动生成区域风险与农业影响分析</small></div>
      </article>
      <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>{{ replayStart }} 起报回放轨迹</h2><p>预测 Niño3.4 月指数 vs 观测 Niño3.4 月指数（℃）</p></div><div><el-button size="mini" icon="el-icon-download" @click="exportJson">JSON</el-button><el-button size="mini" icon="el-icon-document" @click="exportCsv">CSV</el-button></div></div>
        <div ref="chart" class="chart"></div>
      </article>
      <article class="panel impact-report-panel">
        <div class="impact-report-topline"><span class="agent-status"><i></i> ENSO IMPACT INTELLIGENCE</span><span class="report-period">回放起报月份 <b>{{ replayStart }}</b></span></div>
        <template v-if="activeReplayImpactReport">
          <div class="impact-report-heading">
            <div><p class="report-kicker">ENSO 影响研判技术报告 · {{ activeReplayImpactReport.id }}</p><h2>{{ activeReplayImpactReport.title }}</h2><p class="report-meta">预测展望 {{ activeReplayImpactReport.horizon }} <span>·</span> 生成时间 {{ activeReplayImpactReport.generatedAt }}</p></div>
            <el-button class="generate-report-button" size="small" icon="el-icon-refresh" :loading="reportGenerating" @click="generateReplayImpactReport">{{ reportGenerating ? 'Agent 正在研判' : '重新研判' }}</el-button>
          </div>
          <div class="report-thesis"><span class="thesis-mark">研判摘要</span><p>{{ activeReplayImpactReport.summary }}</p></div>
          <div class="impact-indicator-grid"><div v-for="item in activeReplayImpactReport.indicators" :key="item.label" :class="['impact-indicator', item.tone]"><span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.detail }}</small></div></div>
          <div class="report-highlights"><span v-for="item in activeReplayImpactReport.highlights" :key="item.label" :class="['highlight-chip', item.tone]"><b>{{ item.label }}</b>{{ item.value }}</span></div>
          <div class="report-section-heading"><div><span>01 / REGIONAL OUTLOOK</span><h3>中国区域气候风险研判</h3></div><small>基于 ENSO 遥相关型与季节尺度气候倾向</small></div>
          <div class="regional-risk-table-wrap"><table class="regional-risk-table"><thead><tr><th>区域</th><th>降水倾向</th><th>温度倾向</th><th>主要风险</th><th>等级</th></tr></thead><tbody><tr v-for="row in activeReplayImpactReport.regionalRisks" :key="row.region"><td class="region-name">{{ row.region }}</td><td>{{ row.precipitation }}</td><td>{{ row.temperature }}</td><td>{{ row.risk }}</td><td><span :class="['risk-level', row.levelTone]">{{ row.level }}</span></td></tr></tbody></table></div>
          <div class="report-detail-grid">
            <section class="report-detail-card agriculture-card"><div class="detail-card-title"><span>02 / AGRICULTURE</span><h3>农业与产业影响</h3></div><ul><li v-for="(item, index) in activeReplayImpactReport.agriculture" :key="index">{{ item }}</li></ul></section>
            <section class="report-detail-card actions-card"><div class="detail-card-title"><span>03 / RESPONSE</span><h3>建议关注与行动</h3></div><div v-for="action in activeReplayImpactReport.actions" :key="action.tag" class="action-row"><b>{{ action.tag }}</b><div><strong>{{ action.title }}</strong><p>{{ action.text }}</p></div></div></section>
          </div>
          <div class="uncertainty-note"><i class="el-icon-warning-outline"></i><div><b>不确定性与更新条件</b><p>{{ activeReplayImpactReport.uncertainty }}</p></div></div>
          <div class="report-footer"><span>分析框架：ENSO 位相识别 · 遥相关诊断 · 区域风险映射 · 农业暴露评估</span><span>置信度：{{ activeReplayImpactReport.confidence }}</span></div>
        </template>
        <div v-else class="report-empty-state"><div class="empty-agent-icon"><i class="el-icon-cpu"></i></div><span class="report-kicker">ENSO IMPACT AGENT</span><h2>{{ replayStart }} 影响研判报告</h2><p>该月份尚未预置专属研判。Agent 将结合当前回放曲线、ENSO 位相和区域气候响应，生成中国降水、温度及农业风险分析。</p><el-button type="primary" size="small" icon="el-icon-magic-stick" :loading="reportGenerating" @click="generateReplayImpactReport">{{ reportGenerating ? '正在生成技术报告…' : '生成影响研判报告' }}</el-button></div>
      </article>
    </section>

    <section v-else-if="pageKey === 'validation'" class="panel table-panel">
      <div class="panel-heading">
        <div><h2>逐提前期验证与实际比较</h2><p v-if="comparisonReplayStart">整体验证与 {{ comparisonReplayStart }} 历史案例</p></div>
        <div class="validation-actions">
          <el-button size="mini" type="primary" plain icon="el-icon-data-analysis" @click="openComparisonDialog">选择历史案例对比</el-button>
          <el-button v-if="comparisonReplayStart" size="mini" icon="el-icon-video-play" @click="openReplayComparison">查看历史回放</el-button>
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
      <article v-if="comparisonReplayStart && validationCaseSummary" class="validation-report">
        <div class="validation-report-header">
          <div class="report-agent-mark"><i class="el-icon-cpu"></i></div>
          <div class="report-title-block">
            <div class="report-kicker"><span class="report-live-dot"></span> LEADSCOPE AI · HINDCAST DIAGNOSTICS <span class="report-version">v1.0</span></div>
            <h2>{{ activeValidationReport.title }}</h2>
            <p>{{ comparisonReplayStart }} 起报 · Niño3.4 月指数逐提前期回报诊断 · {{ validationCaseSummary.count }} 个有效配对目标月</p>
          </div>
          <span class="report-status"><i class="el-icon-circle-check"></i> 分析完成</span>
        </div>
        <div class="report-metrics">
          <div class="report-metric"><span>案例 MAE</span><strong>{{ formatValidationValue(validationCaseSummary.mae) }} <small>℃</small></strong><em>单次历史轨迹</em></div>
          <div class="report-metric"><span>案例 RMSE</span><strong>{{ formatValidationValue(validationCaseSummary.rmse) }} <small>℃</small></strong><em>强调大误差惩罚</em></div>
          <div class="report-metric"><span>平均偏差</span><strong :class="validationCaseSummary.bias < 0 ? 'cold-bias' : 'warm-bias'">{{ validationCaseSummary.bias > 0 ? '+' : '' }}{{ formatValidationValue(validationCaseSummary.bias) }} <small>℃</small></strong><em>{{ validationCaseSummary.bias < 0 ? '整体偏冷' : validationCaseSummary.bias > 0 ? '整体偏暖' : '无明显系统偏差' }}</em></div>
          <div class="report-metric"><span>三档状态命中率</span><strong>{{ validationCaseSummary.stateAccuracy }}<small>%</small></strong><em>{{ validationCaseSummary.stateHits }} / {{ validationCaseSummary.count }} 目标月</em></div>
          <div class="report-metric"><span>总体验证 MAE</span><strong>{{ formatValidationValue(validationCaseSummary.aggregateMae) }} <small>℃</small></strong><em>相同提前期汇总基准</em></div>
        </div>
        <div class="report-abstract"><span class="abstract-label">研判摘要</span><p>{{ formatValidationReportText(activeValidationReport.summary) }}</p></div>
        <div class="report-analysis-grid">
          <section class="report-section">
            <h3><i class="el-icon-data-line"></i> 关键发现</h3>
            <div v-for="(item, index) in activeValidationReport.findings" :key="'finding-' + index" class="report-list-item"><span class="report-index">0{{ index + 1 }}</span><p>{{ formatValidationReportText(item) }}</p></div>
          </section>
          <section class="report-section">
            <h3><i class="el-icon-warning-outline"></i> 误差成因诊断</h3>
            <div v-for="(item, index) in activeValidationReport.causes" :key="'cause-' + index" class="report-list-item"><span class="cause-mark"><i class="el-icon-arrow-right"></i></span><p>{{ formatValidationReportText(item) }}</p></div>
          </section>
          <section class="report-section recommendations-section">
            <h3><i class="el-icon-s-opportunity"></i> 模型与数据改进建议</h3>
            <div v-for="(item, index) in activeValidationReport.recommendations" :key="'recommendation-' + index" class="recommendation-item"><b>{{ item.title }}</b><p>{{ formatValidationReportText(item.detail) }}</p></div>
          </section>
          <aside class="report-conclusion"><span>评估结论</span><strong>{{ activeValidationReport.verdict }}</strong><p>{{ formatValidationReportText(activeValidationReport.nextStep) }}</p></aside>
        </div>
        <div class="report-footnote"><i class="el-icon-info"></i> 命中率按单月 Niño3.4 距平分为冷侧（≤−0.5℃）、中性（>−0.5℃且<+0.5℃）和暖侧（≥+0.5℃）三档，仅表示本历史轨迹的状态分档一致率，不等同于官方 ONI 事件准确率。案例分数基于可用配对月份；总体验证 MAE 按同提前期的历史样本汇总，二者统计口径不同。</div>
      </article>
      <div class="subsection"><h3>论文消融结果</h3><el-table :data="paperMetrics" size="mini"><el-table-column prop="variant" label="模型变体"/><el-table-column prop="pcc" label="PCC"/></el-table></div>
      <el-dialog title="选择历史案例" :visible.sync="comparisonDialogVisible" width="420px" append-to-body>
        <div class="comparison-picker"><span>起报月份</span><el-select v-model="validationReplayStart" aria-label="历史案例起报月份"><el-option v-for="m in replayOptions" :key="m" :label="m" :value="m" /></el-select></div>
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

    <footer v-if="pageKey !== 'monitor'">2026 AIMB ENSO</footer>
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
import { impactReports as replayImpactReports } from '@/data/ensoReports'

export default {
  name: 'Enso2026',
  data() {
    const replayOptions = demoCases.replayOptions
    const queryStart = Array.isArray(this.$route.query.start) ? this.$route.query.start[0] : this.$route.query.start
    const replayStart = replayOptions.includes(queryStart) ? queryStart : '2025-01'
    return {
      pages: [
        { path: '/enso/overview', label: '系统介绍', number: '01' }, { path: '/enso/showcase', label: '简介大屏', number: '02' },
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
      systemCapabilities: [
        { title: 'ENSO 监测', description: 'Niño3.4 / ONI / SOI 与海洋热含量动态监测；2D/3D 海温风场地图支持探针读数、框选统计、异常热点、等值线、卷帘对比、纬向剖面与鹰眼导航。', icon: 'el-icon-monitor', tone: 'cyan', path: '/enso/monitor', action: '打开监测平台' },
        { title: '预测模型', description: '对比 LSTA-Swin 与 ConvLSTM 的长提前期预测轨迹和空间场输出。', icon: 'el-icon-data-analysis', tone: 'gold', path: '/enso/models', action: '查看模型产品' },
        { title: '历史数据', description: '实时观测、ENSO 历史事件与模型历史预测三态切换，确认回放对象后逐月联动空间场、指数与风场。', icon: 'el-icon-time', tone: 'violet', path: '/enso/replay', action: '进入历史数据' },
        { title: '提前期验证', description: '从 PCC、MAE、RMSE 和状态分档等角度评估 1–20 个月预测技巧，支持历史案例对比与 AI 诊断报告。', icon: 'el-icon-s-check', tone: 'green', path: '/enso/validation', action: '查看验证结果' },
        { title: '物理解释', description: '通过时空演变、相关性和显著性图，追踪模型关注的海气信号。', icon: 'el-icon-view', tone: 'blue', path: '/enso/explainability', action: '分析物理信号' },
        { title: '气候 NC', description: '浏览 NetCDF 格点变量与时次，为区域气候影响分析提供数据底座。', icon: 'el-icon-picture-outline', tone: 'purple', path: '/enso/climate-data', action: '打开数据浏览' },
        { title: '智能研判助手', description: '悬浮 AI 助手提供面板级问询、阈值告警解读与历史事件 / 模型预测报告解读；影响研判 Agent 联动生成中国区域降水、温度与农业风险分析。', icon: 'el-icon-cpu', tone: 'cyan', path: '/enso/monitor', action: '体验 AI 助手' },
        { title: '导出中心', description: '一键导出监测简报（PNG / PDF，地图与图表样式完整保留）与指数时序、监测数据包（CSV / JSON），导出过程带进度与失败提示。', icon: 'el-icon-download', tone: 'gold', path: '/enso/monitor', action: '打开导出中心' }
      ],
      systemWorkflow: [
        { number: '01', title: '汇聚海气输入', description: '接入 NOAA OISST 海温、GFS 风场与历史指数序列，统一时空口径与 1991–2020 距平基准。' },
        { number: '02', title: '实时监测与信号识别', description: '2D/3D 地图联动绝对海温与信风场，借助探针、剖面、框选统计定位关键信号，阈值告警提示位相跨越。' },
        { number: '03', title: '生成 ENSO 预测', description: 'LSTA-Swin 提取长短期演变信息，输出 1–20 个月 Niño3.4 / ONI 轨迹与赤道太平洋空间异常场。' },
        { number: '04', title: '历史回放与可信验证', description: '历史事件与模型起报双回放逐月联动，对照观测评估 PCC / MAE / RMSE 与状态命中，识别偏差与转折。' },
        { number: '05', title: '形成气候服务', description: '结合遥相关与区域资料生成中国降水、温度与农业风险研判，AI 助手随行解读并支撑决策。' }
      ],
      variables: ['SSTA', 'SSTA150', 'τx', 'τy'], selectedVariable: 'SSTA', selectedFigure: 'spatiotemporal', forecastStart: '2023-03', leadMonths: 20,
      replayOptions, replayStart, replayReportOverrides: {}, reportGenerating: false, validationReplayStart: replayStart, comparisonReplayStart: '', comparisonRows: [], comparisonDialogVisible: false, data: {}, metadata: demoCases.metadata, paperMetrics: demoCases.metrics.ablation, leadRows: demoCases.metrics.systemResults, heatmap: demoCases.explainability.heatmap, loading: false,
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
    activeReplayImpactReport() { return this.replayReportOverrides[this.replayStart] || replayImpactReports[this.replayStart] || null },
    validationCaseSummary() {
      if (!this.comparisonReplayStart || !this.comparisonRows.length) return null
      const points = this.comparisonRows.filter(row => Number.isFinite(row.replayForecast) && Number.isFinite(row.replayObserved))
      if (!points.length) return null
      const errors = points.map(row => row.replayForecast - row.replayObserved)
      const mae = errors.reduce((sum, error) => sum + Math.abs(error), 0) / errors.length
      const rmse = Math.sqrt(errors.reduce((sum, error) => sum + error * error, 0) / errors.length)
      const bias = errors.reduce((sum, error) => sum + error, 0) / errors.length
      const forecastMean = points.reduce((sum, row) => sum + row.replayForecast, 0) / points.length
      const observedMean = points.reduce((sum, row) => sum + row.replayObserved, 0) / points.length
      const covariance = points.reduce((sum, row) => sum + (row.replayForecast - forecastMean) * (row.replayObserved - observedMean), 0)
      const forecastVariance = points.reduce((sum, row) => sum + Math.pow(row.replayForecast - forecastMean, 2), 0)
      const observedVariance = points.reduce((sum, row) => sum + Math.pow(row.replayObserved - observedMean, 2), 0)
      const pccRaw = points.length >= 3 && forecastVariance && observedVariance ? covariance / Math.sqrt(forecastVariance * observedVariance) : null
      const pcc = Number.isFinite(pccRaw) ? Math.max(-1, Math.min(1, pccRaw)) : null
      const stateClass = value => value <= -0.5 ? 'cold' : value >= 0.5 ? 'warm' : 'neutral'
      const stateHits = points.filter(row => stateClass(row.replayForecast) === stateClass(row.replayObserved)).length
      const leadMetrics = points.map(row => this.leadRows.find(metric => metric.lead === row.lead)).filter(metric => metric && Number.isFinite(metric.mae) && Number.isFinite(metric.sampleCount) && metric.sampleCount > 0)
      const aggregateWeight = leadMetrics.reduce((sum, metric) => sum + metric.sampleCount, 0)
      const aggregateMae = aggregateWeight ? leadMetrics.reduce((sum, metric) => sum + metric.mae * metric.sampleCount, 0) / aggregateWeight : null
      return { count: points.length, stateHits, stateAccuracy: Math.round(stateHits / points.length * 100), mae, rmse, bias, pcc, aggregateMae }
    },
    activeValidationReport() {
      const month = this.comparisonReplayStart
      const summary = this.validationCaseSummary || {}
      const bias = Number.isFinite(summary.bias) ? `${summary.bias > 0 ? '+' : ''}${summary.bias.toFixed(2)}` : '—'
      const common = {
        pcc: Number.isFinite(summary.pcc) ? summary.pcc.toFixed(2) : '样本不足',
        bias,
        accuracy: summary.stateAccuracy == null ? '—' : summary.stateAccuracy,
        count: summary.count == null ? 0 : summary.count,
        hits: summary.stateHits == null ? 0 : summary.stateHits,
        mae: Number.isFinite(summary.mae) ? summary.mae.toFixed(2) : '—',
        rmse: Number.isFinite(summary.rmse) ? summary.rmse.toFixed(2) : '—'
      }
      const reports = {
        '2025-01': {
          title: '弱拉尼娜衰减与暖位相转折漏报诊断',
          summary: '本次历史案例能够维持弱冷位相的背景判断，案例相关性为 {{pcc}}，但振幅与转折时点误差突出：平均偏差 {{bias}}℃，三档状态命中率 {{accuracy}}%。预测序列持续停留在弱冷区间，未能跟随 2025 年春夏实际序列向中性及暖侧跃迁；误差随提前期扩大，说明当前外推对海气耦合转换信号响应不足。',
          findings: ['有效配对覆盖 {{count}} 个月，案例 MAE {{mae}}℃、RMSE {{rmse}}℃；案例 PCC {{pcc}}反映整体共变仍有一定一致性，但不能替代逐月状态命中率。', '冷侧背景延续被过度平滑，预测振幅明显收缩；后段暖侧增强未被提前捕捉，误差由相位偏差与振幅低估共同主导。', '三档状态命中 {{hits}}/{{count}} 个月（{{accuracy}}%）。单月分档用于历史案例诊断，不能解释为官方 ONI 事件准确率。'],
          causes: ['递减的单一趋势项难以表达“弱冷衰减—中性过渡—暖异常发展”的非线性转换，远期预测逐步向平滑背景态收缩。', 'Niño3.4 指数序列缺少对西风爆发、暖水体积与次表层热含量重建的显式约束，关键先导信号可能未被充分传递到长提前期输出。', '单案例有效月份有限，事件转折样本及季节位相覆盖不足；以逐月观测作参照时，月际噪声也会放大局地误差。'],
          recommendations: [{ title: '模型结构｜显式建模相位转换', detail: '在 LSTA-Swin 主干上增加 ENSO 状态转移或多任务相位头，同时预测 Niño3.4 连续值、变化率和冷/中/暖概率；对 3–9 个月转折窗口采用门控残差解码，避免远期输出过度趋平。' }, { title: '数据处理｜补齐海气先导信号', detail: '加入暖水体积 / 上层海洋热含量、赤道西风爆发、海表风应力及次表层温度等特征；按起报时刻严格进行时间切分和标准化，核验缺测填补与异常值处理是否引入相位滞后。' }, { title: '验证设计｜按事件和季节分层', detail: '在滚动起报回报中报告事件转折月误差、振幅误差、PCC、MAE、RMSE 与三档命中率，并给出 bootstrap 置信区间；对 6–12 个月提前期开展独立校准。' }],
          verdict: '背景位相有参考价值，转折预报能力不足',
          nextStep: '优先补强海洋热含量与风爆发先导信息，并针对转折样本开展分层回报和概率校准。'
        },
        '2025-02': {
          title: '冷位相维持偏差与暖异常发展低估诊断',
          summary: '该起报轨迹呈现持续冷偏差，平均偏差 {{bias}}℃，三档状态命中率 {{accuracy}}%；案例 MAE {{mae}}℃、RMSE {{rmse}}℃。案例 PCC {{pcc}}显示序列变化方向仍有一定同步性，但预测暖侧振幅显著不足，分类命中随观测由中性转向暖侧而下降。结果提示模型对冷背景的惯性保持强于对后续暖异常发展的响应。',
          findings: ['有效配对覆盖 {{count}} 个月，三档状态命中 {{hits}}/{{count}} 个月（{{accuracy}}%）；低命中主要集中在观测进入暖侧后的后段目标月。', '案例偏差为 {{bias}}℃，整体偏冷；RMSE 与 MAE 的差异表明，后段少数大幅暖异常漏报对总误差贡献较大。', '案例 PCC {{pcc}}与类别命中率并不等价：连续序列仍可能共同变化，但幅度校准和关键状态转类仍存在明显缺口。'],
          causes: ['模型对最近观测的持续性依赖与趋势衰减共同形成冷位相“记忆”，当真实海洋蓄热释放并推动暖异常增强时，输出容易滞后。', '若训练集中快速转暖与强暖事件比例偏低，均方误差优化会偏向振幅收缩，降低少数高振幅样本对参数更新的影响。', 'Niño3.4 单指标验证不能区分海洋初始场误差、季节锁相误差和大气噪声；需要结合空间海温与风应力场进行归因。'],
          recommendations: [{ title: '模型结构｜改善远期振幅与概率表达', detail: '采用多尺度时序解码与状态条件门控，增加对变化率、振幅和状态概率的联合约束；对暖异常发展阶段使用分位数或异方差输出，减少均值回归造成的振幅低估。' }, { title: '数据处理｜提高转暖样本代表性', detail: '按 ENSO 生命周期阶段重采样或使用事件加权损失，保持训练、验证按年份分块，避免同一事件泄漏；对海温距平、热含量和风场统一基准期、掩膜及标准化口径。' }, { title: '评估闭环｜定位误差来源', detail: '补充逐提前期偏差、振幅比、转类提前量和可靠性图；采用按事件留一验证，并分别评估 Niño3.4 指数及赤道太平洋空间场，检验改进是否来自真实可预测信号。' }],
          verdict: '趋势共变尚可，状态转换与振幅技巧偏弱',
          nextStep: '重点检查暖水体积与风应力先导信号，并通过事件加权训练和分位数输出改善暖侧振幅。'
        }
      }
      const report = reports[month]
      if (!report) return { title: `${month} 历史轨迹定量诊断`, summary: '本案例覆盖 {{count}} 个有效目标月，MAE {{mae}}℃、RMSE {{rmse}}℃，平均偏差 {{bias}}℃；三档状态命中率 {{accuracy}}%。请结合逐提前期误差与整体验证基准识别误差随提前期的变化。', findings: ['案例指标由当前所选历史月份的逐提前期预测与观测配对计算。', '整体技巧由历史回报样本汇总，代表长期技巧背景，不等同于单次轨迹效果。', '建议结合空间场、事件阶段与起报季节检查误差是否具有稳定结构。'], causes: ['单个起报案例无法独立区分初始场误差、海气耦合噪声和模式结构误差。', '远期误差受 ENSO 相位转变、季节锁相和初始海洋热含量不确定性共同影响。', '观测覆盖或有效样本变化会影响指标稳定性，需要在滚动回报中持续核验。'], recommendations: [{ title: '模型结构｜建立多目标诊断', detail: '联合预测 Niño3.4 值、变化率与状态概率，并对不同提前期设置独立校准。' }, { title: '数据处理｜加强时序与样本审查', detail: '采用按年份分组的严格回报切分，统一距平基准、缺测掩膜和输入标准化口径。' }, { title: '验证设计｜按事件生命周期分层', detail: '补充事件级误差、状态转类命中、置信区间和空间场验证，区分偶然偏差与系统性问题。' }], verdict: '单案例诊断需结合滚动验证与物理场证据', nextStep: '优先复核相同提前期、相近季节及相同 ENSO 阶段的多起报回报结果。' }
      return { ...report, title: `${month.slice(0, 4)} 年 ${Number(month.slice(5, 7))} 月 · ${report.title}`, summary: this.fillValidationReport(report.summary, common), findings: report.findings.map(item => this.fillValidationReport(item, common)) }
    },
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
      if (key === 'forecast') return this.loadForecast()
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
      const hasObservation = point && Number.isFinite(point.observedNino34)
      return {
        replayMonth: point ? point.month : '',
        replayForecast: point ? point.value : null,
        replayObserved: hasObservation ? point.observedNino34 : null,
        replayError: hasObservation ? Number((point.value - point.observedNino34).toFixed(2)) : null
      }
    },
    fillValidationReport(text, values) {
      return text.replace(/\{\{(pcc|bias|accuracy|count|hits|mae|rmse)\}\}/g, (match, key) => values[key] == null ? '—' : values[key])
    },
    formatValidationReportText(text) {
      const summary = this.validationCaseSummary || {}
      return this.fillValidationReport(text, {
        pcc: Number.isFinite(summary.pcc) ? summary.pcc.toFixed(2) : '样本不足',
        bias: Number.isFinite(summary.bias) ? `${summary.bias > 0 ? '+' : ''}${summary.bias.toFixed(2)}` : '—',
        accuracy: summary.stateAccuracy == null ? '—' : summary.stateAccuracy,
        count: summary.count == null ? 0 : summary.count,
        hits: summary.stateHits == null ? 0 : summary.stateHits,
        mae: Number.isFinite(summary.mae) ? summary.mae.toFixed(2) : '—',
        rmse: Number.isFinite(summary.rmse) ? summary.rmse.toFixed(2) : '—'
      })
    },
    formatValidationValue(value) { return Number.isFinite(value) ? value.toFixed(2) : '—' },
    async loadReplay() {
      this.loading = true
      try { await this.wait(1300); this.data = demoCases.replay(this.replayStart); this.$nextTick(() => this.renderChart(this.data.series || [], false, true)) }
      catch (e) { this.showError(e) } finally { this.loading = false }
    },
    async generateReplayImpactReport() {
      if (this.reportGenerating) return
      const month = this.replayStart
      this.reportGenerating = true
      try {
        await this.wait(1250)
        const seed = replayImpactReports[month] || (Number(month.slice(-2)) >= 2 && Number(month.slice(-2)) <= 5 ? replayImpactReports['2025-02'] : replayImpactReports['2025-01'])
        const year = Number(month.slice(0, 4))
        const monthNumber = Number(month.slice(5, 7))
        const addMonth = (amount) => {
          const date = new Date(Date.UTC(year, monthNumber - 1 + amount, 1))
          return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`
        }
        const now = new Date()
        const generatedAt = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
        const report = JSON.parse(JSON.stringify(seed))
        report.id = `ENSO-IMPACT-${month.replace('-', '')}`
        report.title = `${year} 年 ${monthNumber} 月 ENSO 影响研判技术报告`
        report.generatedAt = generatedAt
        report.horizon = `${addMonth(1)}–${addMonth(3)}`
        if (!replayImpactReports[month]) {
          report.summary = `${year} 年 ${monthNumber} 月历史案例显示，${seed.phase}对应的热带太平洋海温异常仍可为未来季节气候倾向提供背景约束。以下研判结合当前 Niño3.4 历史轨迹、ENSO 遥相关型及区域季节信号，重点识别中国降水、温度与农业生产中的风险窗口，并建议随月度观测滚动更新。`
          const replay = demoCases.replay(month)
          const center = replay.series && replay.series[0] && replay.series[0].value
          report.indicators[0].value = Number.isFinite(center) ? `${center > 0 ? '+' : ''}${center.toFixed(2)} ℃` : seed.phaseValue
          report.uncertainty = `当前月份未配置专属预置报告，Agent 已按相近季节背景生成研判框架。区域降水与灾害风险需结合 ${month} 后续实况、季节预测和当地监测资料持续订正。${seed.uncertainty}`
        }
        this.$set(this.replayReportOverrides, month, report)
        this.$message.success(`${month} 影响研判报告已生成`)
      } catch (e) { this.showError(e) } finally { this.reportGenerating = false }
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
        legend: { data: replay ? ['预测 Niño3.4 月指数', '观测 Niño3.4 月指数'] : (interval ? ['预测值', '下界', '上界'] : ['预测值']), textStyle: { color: '#bcc8d1' }, top: 4 },
        grid: { left: 48, right: 22, top: 42, bottom: 36 },
        xAxis: { type: 'category', data: series.map(p => p.month), axisLabel: { color: '#a4b0ba', interval: 2 }, axisLine: { lineStyle: { color: '#41505b' } } },
        yAxis: { type: 'value', name: '℃', nameTextStyle: { color: '#a4b0ba' }, axisLabel: { color: '#a4b0ba' }, splitLine: { lineStyle: { color: 'rgba(150,170,180,.12)' } } },
        series: [{ name: replay ? '预测 Niño3.4 月指数' : '预测值', type: 'line', smooth: true, symbol: 'none', data: series.map(p => p.value), lineStyle: { color: '#38c6b0', width: 3 }, areaStyle: { color: 'rgba(56,198,176,.12)' } }]
      }
      if (interval) option.series.push({ name: '下界', type: 'line', symbol: 'none', lineStyle: { type: 'dashed', color: '#e4b75c' }, data: series.map(p => p.lower) }, { name: '上界', type: 'line', symbol: 'none', lineStyle: { type: 'dashed', color: '#e4b75c' }, data: series.map(p => p.upper) })
      if (replay) option.series.push({ name: '观测 Niño3.4 月指数', type: 'line', connectNulls: false, symbol: 'circle', symbolSize: 5, data: series.map(p => p.observedNino34), lineStyle: { color: '#e4b75c', width: 2, type: 'dashed' }, itemStyle: { color: '#e4b75c' } })
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
.enso-workspace{--bg:#10191d;--panel:#172328;--line:#2d4146;--text:#e2ece8;--muted:#9aada9;--teal:#38c6b0;--gold:#e4b75c;min-height:100vh;background:radial-gradient(ellipse at 84% 0%,#1b3433 0,transparent 38%),#10191d;color:var(--text);padding:0 4.2%;box-sizing:border-box;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}.topbar{height:68px;display:flex;align-items:center;border-bottom:1px solid var(--line);gap:28px}.brand{display:flex;align-items:center;gap:11px;white-space:nowrap;font-size:14px;font-weight:600}.brand-mark{background:#d4efe6;color:#15342e;padding:7px 9px;font-size:12px;font-weight:800;border-radius:3px}.topbar nav{display:flex;align-self:stretch;align-items:stretch;gap:4px;flex:1}.topbar nav a{display:flex;align-items:center;color:var(--muted);text-decoration:none;padding:0 13px;font-size:13px;border-bottom:2px solid transparent}.topbar nav a.active,.topbar nav a:hover{color:var(--text);border-color:var(--teal)}.source-button{color:#bcd0ca;background:transparent;border-color:var(--line)}.page-heading{display:flex;justify-content:space-between;align-items:center;padding:30px 0 18px}.eyebrow{font-size:11px;color:var(--teal);margin:0 0 8px;text-transform:uppercase}.page-heading h1{font-size:25px;margin:0;font-weight:650}.subtitle{margin:8px 0 0;color:var(--muted);font-size:13px}.page-heading .el-tag{background:#483a20;border:0;color:#f0c76f}.notice{display:flex;align-items:center;gap:10px;padding:11px 14px;background:#28281e;border-left:3px solid var(--gold);color:#e1d5ad;font-size:12px;margin:0 0 20px}.notice button{margin-left:auto;background:none;border:0;color:#f0c76f;cursor:pointer}.metric-strip{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));border:1px solid var(--line);background:#142024;margin-bottom:18px}.metric-strip article{padding:16px 19px;border-right:1px solid var(--line)}.metric-strip article:last-child{border:0}.metric-strip span,.metric-strip small{display:block;color:var(--muted);font-size:11px}.metric-strip strong{display:block;font-size:22px;margin:8px 0 5px;color:#e4f1eb}.metric-strip strong small{display:inline;color:var(--muted);font-size:13px}.content-grid{display:grid;grid-template-columns:minmax(230px, .82fr) minmax(0,2fr);gap:16px}.panel{background:rgba(23,35,40,.92);border:1px solid var(--line);padding:18px;min-width:0}.chart-panel{grid-column:span 1}.overview-view .content-grid{grid-template-columns:minmax(0,1.8fr) minmax(260px,.8fr)}.panel-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;margin-bottom:14px}.panel-heading h2,.section-row h2,.panel h2{font-size:15px;margin:0;font-weight:600}.panel-heading p,.section-row p{color:var(--muted);font-size:11px;margin:6px 0 0}.chart{height:295px;width:100%}.chart-caption{color:#9eb2ac;font-size:10px;border-top:1px solid rgba(130,160,150,.13);padding-top:9px;margin-top:9px}.overview-view .chart{height:270px}.status-list{padding:10px 0 25px}.status-list div{display:flex;align-items:center;gap:9px;padding:12px 0;border-bottom:1px solid rgba(130,160,150,.13);font-size:12px}.status-list b{margin-left:auto;font-size:11px;color:#d5c184;font-weight:500}.dot{width:7px;height:7px;border-radius:50%}.cyan{background:#54c9d1}.gold{background:#e4b75c}.green{background:#38c6b0}.full-button{width:100%;background:transparent;border-color:#42655f;color:#9ddbd0}.section-row{display:grid;grid-template-columns:210px 1fr;gap:20px;padding:25px 0}.workflow{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));border:1px solid var(--line)}.workflow button{display:flex;align-items:center;gap:8px;text-align:left;padding:13px 10px;background:transparent;border:0;border-right:1px solid var(--line);color:var(--text);cursor:pointer}.workflow button span{color:var(--teal);font-size:10px}.workflow button b{font-size:11px;font-weight:500}.workflow button i{margin-left:auto;color:var(--muted)}.controls-panel label{display:block;color:var(--muted);font-size:11px;margin:14px 0 6px}.controls-panel .el-date-editor,.controls-panel .el-select{width:100%;margin-bottom:7px}.controls-panel>.el-button{width:100%;margin-top:12px}.model-spec{display:flex;flex-direction:column;gap:10px;padding:16px 0;border-top:1px solid var(--line);border-bottom:1px solid var(--line);margin:18px 0;color:#c2d1cc;font-size:11px}.model-spec b{color:var(--text);font-size:12px}.unavailable{color:#e4b75c}.map-panel{grid-column:1/-1}.variable-select{width:120px}.heatmap{padding:12px 10px 4px;max-width:780px;margin:auto}.heat-row{display:grid;grid-template-columns:repeat(15,1fr);gap:4px;margin-bottom:4px}.heat-row span{height:17px;border-radius:2px}.large-heatmap .heat-row span{height:23px}.map-axis{max-width:780px;margin:4px auto;display:flex;justify-content:space-between;color:var(--muted);font-size:10px}.table-panel{margin-bottom:20px}.table-panel>>>.el-table,.table-panel>>>.el-table th,.table-panel>>>.el-table tr,.table-panel>>>.el-table td{background:transparent;color:#d2dfda}.table-panel>>>.el-table .el-table__header-wrapper th.el-table__cell{background:#1a2b31;color:#b9ccc9}.table-panel>>>.el-table .el-table__body tr.el-table__row--striped td.el-table__cell{background:#1b2a30}.table-panel>>>.el-table .el-table__body tr:hover>td.el-table__cell,.table-panel>>>.el-table .el-table__body tr.current-row>td.el-table__cell{background:#243b40!important}.table-panel>>>.el-table .el-table__fixed,.table-panel>>>.el-table .el-table__fixed-right{background:#172328}.table-panel>>>.el-table td.el-table__cell,.table-panel>>>.el-table th.el-table__cell{border-color:var(--line)}.table-panel>>>.el-table::before,.table-panel>>>.el-table__fixed::before,.table-panel>>>.el-table__fixed-right::before{background:var(--line)}.subsection{margin-top:25px}.subsection h3{font-size:13px;margin-bottom:5px}.subsection>p{font-size:11px;color:var(--muted)}.limitation,.source-warning{padding:12px;background:#24251f;color:#d5c184;font-size:11px;line-height:1.7;margin-top:12px}.stage-list>div{display:flex;gap:15px;padding:17px 0;border-bottom:1px solid var(--line)}.stage-list>div>b{color:var(--teal);font-size:12px}.stage-list strong{font-size:12px}.stage-list p{color:var(--muted);font-size:11px;line-height:1.6;margin:6px 0 0}.data-layout{display:grid;grid-template-columns:1.6fr 1fr;gap:16px}.data-layout>>>.el-descriptions__body,.data-layout>>>.el-descriptions__table,.data-layout>>>.el-descriptions-item__label,.data-layout>>>.el-descriptions-item__content{background:#172328!important;border-color:var(--line)!important;color:#d7e2dd!important}.checklist p{font-size:11px;color:#c0d0ca;margin:16px 0}.checklist p i{color:#e4b75c;margin-right:6px}.checklist p:last-child i{color:var(--teal)}footer{padding:20px 0 25px;border-top:1px solid var(--line);color:#879994;font-size:10px}footer span{float:right}@media(max-width:1100px){.enso-workspace{padding:0 22px}.topbar{height:auto;min-height:64px;flex-wrap:wrap;gap:10px;padding:10px 0}.topbar nav{order:3;flex-basis:100%;overflow:auto;min-height:42px}.topbar nav a{white-space:nowrap;padding:0 10px}.metric-strip{grid-template-columns:repeat(2,1fr)}.metric-strip article:nth-child(2){border-right:0}.metric-strip article:nth-child(-n+2){border-bottom:1px solid var(--line)}.workflow{grid-template-columns:repeat(3,1fr)}.workflow button:nth-child(3){border-right:0}.workflow button:nth-child(-n+3){border-bottom:1px solid var(--line)}}@media(max-width:720px){.page-heading{align-items:flex-start;gap:12px}.page-heading h1{font-size:21px}.page-heading>.el-tag{white-space:nowrap}.overview-view .content-grid,.content-grid,.data-layout{grid-template-columns:1fr}.chart-panel,.map-panel{grid-column:auto}.section-row{grid-template-columns:1fr;gap:12px}.workflow{grid-template-columns:repeat(2,1fr)}.workflow button:nth-child(3){border-right:1px solid var(--line)}.workflow button:nth-child(2n){border-right:0}.workflow button:nth-child(-n+4){border-bottom:1px solid var(--line)}.chart{height:250px}.notice{align-items:flex-start}.notice button{white-space:nowrap}.topbar .source-button{margin-left:auto}}
.legacy-button{color:#dcebe5;background:#213733;border-color:#45645c}.legacy-button:hover{color:#fff;background:#2b4b43;border-color:#5c9183}.showcase-notice{display:flex;align-items:center;gap:9px;margin-bottom:14px;padding:10px 13px;background:#28281e;border-left:3px solid #e4b75c;color:#e1d5ad;font-size:12px;line-height:1.6}.model-tabs{display:flex;flex-wrap:wrap;gap:8px;margin:0 0 16px;padding-bottom:12px;border-bottom:1px solid var(--line)}.model-tab{padding:8px 12px;border:1px solid #38514d;color:#a9c4bd;text-decoration:none;font-size:12px;background:#142024}.model-tab:hover,.model-tab.active{color:#e8fff8;border-color:var(--teal);background:#1d3935}.legacy-showcase>>>.page-container{margin:0!important}.legacy-showcase>>>.hero{margin-top:0!important}
.validation-actions{display:flex;align-items:center;justify-content:flex-end;gap:8px;flex-wrap:wrap}.validation-actions .el-select{width:125px}@media(max-width:720px){.table-panel .panel-heading{flex-wrap:wrap}.validation-actions{justify-content:flex-start}}
.comparison-picker{display:flex;align-items:center;gap:14px}.comparison-picker .el-select{flex:1}
.explainability-view{grid-template-columns:minmax(0,2fr) minmax(260px,.8fr)}.paper-gallery-panel{min-width:0}.paper-image-frame{display:flex;align-items:center;justify-content:center;min-height:330px;padding:12px;background:#10191d;border:1px solid var(--line);overflow:auto}.paper-image-frame img{display:block;max-width:100%;max-height:560px;width:auto;height:auto}.paper-caption{margin:10px 0 0;color:var(--muted);font-size:11px;line-height:1.7}.paper-index-panel{display:flex;flex-direction:column;gap:8px}.paper-index-item{display:grid;grid-template-columns:78px 1fr auto;align-items:center;gap:10px;width:100%;padding:8px;border:1px solid var(--line);background:#142024;color:var(--text);text-align:left;cursor:pointer}.paper-index-item img{display:block;width:78px;height:48px;object-fit:cover}.paper-index-item span{font-size:11px}.paper-index-item i{color:var(--muted)}.paper-index-item.active,.paper-index-item:hover{border-color:var(--teal);background:#1d3935}.climate-data-page{margin:0 -2%;background:#f5f7fa;min-height:560px}.climate-entry-button{margin-top:18px}@media(max-width:720px){.explainability-view{grid-template-columns:1fr}.paper-image-frame{min-height:220px}.climate-data-page{margin:0 -22px}}
.enso-workspace.monitor-mode{width:100%;height:100vh;min-height:100vh;padding:0;overflow:hidden;background:#041522}.monitor-mode .legacy-showcase{height:100%;margin:0;padding:0}
@media(max-width:900px){.enso-workspace.monitor-mode{height:auto;min-height:100vh;overflow:visible}.monitor-mode .legacy-showcase{height:auto;min-height:100vh}}
.enso-workspace.monitor-mode{height:auto;min-height:100vh;overflow:visible}
.monitor-mode .legacy-showcase{height:auto;min-height:100vh;overflow:visible}
.content-grid:has(.impact-report-panel){align-items:start}.replay-agent-hint{display:grid;grid-template-columns:20px 1fr;align-items:center;gap:5px 8px;padding:12px;margin-top:20px;border:1px solid rgba(73,177,204,.24);background:linear-gradient(135deg,rgba(18,63,80,.42),rgba(19,37,47,.65));color:#a8ddec}.replay-agent-hint i{grid-row:span 2;color:#63d8e4;font-size:17px}.replay-agent-hint span{font-size:11px;font-weight:650;letter-spacing:.04em}.replay-agent-hint small{grid-column:2;color:#829daa;font-size:10px;line-height:1.55}.impact-report-panel{grid-column:1/-1;position:relative;overflow:hidden;padding:22px 24px 17px;background:radial-gradient(ellipse at 88% 0%,rgba(26,105,117,.2),transparent 36%),linear-gradient(155deg,rgba(18,36,46,.98),rgba(13,26,35,.98));border-color:#294a55;box-shadow:0 15px 38px rgba(0,0,0,.18)}.impact-report-panel:before{content:"";position:absolute;left:0;top:0;bottom:0;width:2px;background:linear-gradient(180deg,#5bd5dc,#4087b6 58%,transparent)}.impact-report-topline{display:flex;align-items:center;justify-content:space-between;gap:12px;padding-bottom:12px;border-bottom:1px solid rgba(125,169,183,.14)}.agent-status{display:flex;align-items:center;gap:8px;color:#87cbd5;font-size:9px;letter-spacing:.17em;font-weight:700}.agent-status i{width:7px;height:7px;border-radius:50%;background:#4be0c1;box-shadow:0 0 12px rgba(75,224,193,.8)}.report-period{color:#748d98;font-size:10px}.report-period b{margin-left:5px;color:#d5e9e8;font-weight:600}.impact-report-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;padding:20px 0 14px}.impact-report-heading h2{margin:5px 0 7px;color:#e7f3f3;font-size:20px;letter-spacing:.01em}.report-kicker{margin:0;color:#63c5d2;font-size:9px;font-weight:700;letter-spacing:.14em}.report-meta{margin:0;color:#7e98a3;font-size:10px}.report-meta span{margin:0 7px;color:#4d717e}.generate-report-button{flex:none;color:#a7dbe0;background:rgba(37,94,107,.18);border-color:#356b76}.generate-report-button:hover{color:#e8ffff;background:rgba(41,132,146,.28);border-color:#55bac4}.report-thesis{display:grid;grid-template-columns:76px 1fr;gap:14px;padding:14px 16px;background:linear-gradient(90deg,rgba(36,88,93,.2),rgba(27,54,64,.24));border:1px solid rgba(76,149,156,.2);border-left:2px solid #54c1c7}.thesis-mark{padding-top:2px;color:#72d2d0;font-size:10px;font-weight:700;letter-spacing:.08em}.report-thesis p{margin:0;color:#bed0d3;font-size:12px;line-height:1.85}.impact-indicator-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:10px;margin-top:13px}.impact-indicator{position:relative;display:flex;flex-direction:column;gap:6px;min-height:87px;padding:12px 14px;background:rgba(16,34,43,.86);border:1px solid rgba(105,142,155,.16);overflow:hidden}.impact-indicator:after{content:"";position:absolute;width:54px;height:54px;right:-22px;top:-24px;border:1px solid currentColor;border-radius:50%;opacity:.12}.impact-indicator>span{color:#829ba5;font-size:10px}.impact-indicator strong{color:#d9eeee;font-size:18px;font-weight:650;letter-spacing:.02em}.impact-indicator small{color:#78909a;font-size:9px}.impact-indicator.cyan{color:#56d0d4;border-top-color:#3994a1}.impact-indicator.gold{color:#e5bd6b;border-top-color:#a17d3b}.impact-indicator.violet{color:#aaa0ec;border-top-color:#7064ad}.impact-indicator.green{color:#67d7b1;border-top-color:#3d987a}.report-highlights{display:flex;flex-wrap:wrap;gap:8px;margin:11px 0 24px}.highlight-chip{display:flex;align-items:center;gap:8px;padding:7px 10px;border:1px solid rgba(114,150,158,.19);background:rgba(20,43,51,.62);color:#a8bec2;font-size:10px}.highlight-chip b{font-size:9px;color:#78a0a8}.highlight-chip.warning{border-color:rgba(229,181,91,.28)}.highlight-chip.warning b{color:#e3b85f}.highlight-chip.warm{border-color:rgba(221,135,101,.25)}.highlight-chip.warm b{color:#e0926e}.highlight-chip.cool{border-color:rgba(90,179,202,.24)}.highlight-chip.cool b{color:#75c4d2}.report-section-heading{display:flex;align-items:flex-end;justify-content:space-between;gap:12px;margin:0 0 10px}.report-section-heading span,.detail-card-title span{color:#5bbfc7;font-size:8px;font-weight:700;letter-spacing:.15em}.report-section-heading h3,.detail-card-title h3{margin:5px 0 0;color:#d5e7e8;font-size:13px;font-weight:620}.report-section-heading>small{color:#6f8994;font-size:9px}.regional-risk-table-wrap{width:100%;overflow-x:auto;border:1px solid rgba(100,146,160,.17);background:rgba(11,27,36,.46)}.regional-risk-table{width:100%;min-width:650px;border-collapse:collapse;text-align:left}.regional-risk-table th{padding:10px 12px;color:#7c9ca6;background:rgba(36,69,80,.3);font-size:9px;font-weight:600;letter-spacing:.04em;border-bottom:1px solid rgba(100,146,160,.2)}.regional-risk-table td{padding:11px 12px;color:#a8bec2;font-size:10px;line-height:1.5;border-bottom:1px solid rgba(100,146,160,.11)}.regional-risk-table tr:last-child td{border-bottom:0}.regional-risk-table tbody tr:hover{background:rgba(46,99,108,.12)}.regional-risk-table td.region-name{color:#d1e4e6;font-weight:600;white-space:nowrap}.risk-level{display:inline-block;padding:3px 7px;border-radius:2px;font-size:9px;white-space:nowrap}.risk-level.low{color:#79d1ae;background:rgba(70,152,116,.12)}.risk-level.medium{color:#e5c478;background:rgba(183,139,58,.14)}.risk-level.high{color:#f09678;background:rgba(188,82,61,.16)}.report-detail-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:13px}.report-detail-card{padding:15px 16px;background:rgba(16,34,43,.72);border:1px solid rgba(100,146,160,.16)}.report-detail-card ul{list-style:none;padding:0;margin:10px 0 0}.report-detail-card li{position:relative;padding:8px 0 8px 13px;color:#aabdc1;font-size:10px;line-height:1.7;border-bottom:1px solid rgba(100,146,160,.1)}.report-detail-card li:before{content:"";position:absolute;left:0;top:14px;width:4px;height:4px;border-radius:50%;background:#52c4c2}.report-detail-card li:last-child{border-bottom:0;padding-bottom:0}.action-row{display:grid;grid-template-columns:27px 1fr;gap:9px;padding:9px 0;border-bottom:1px solid rgba(100,146,160,.1)}.action-row:last-child{border-bottom:0;padding-bottom:0}.action-row>b{display:flex;align-items:center;justify-content:center;width:24px;height:24px;color:#74cbd0;background:rgba(53,132,140,.14);font-size:9px}.action-row strong{color:#c9dfe1;font-size:10px;font-weight:600}.action-row p{margin:4px 0 0;color:#8ea6ad;font-size:9px;line-height:1.65}.uncertainty-note{display:grid;grid-template-columns:20px 1fr;gap:10px;margin-top:13px;padding:12px 14px;background:rgba(112,86,38,.12);border:1px solid rgba(186,148,75,.19)}.uncertainty-note>i{padding-top:1px;color:#d7b466;font-size:15px}.uncertainty-note b{color:#d6bd81;font-size:10px}.uncertainty-note p{margin:5px 0 0;color:#a99d7d;font-size:9px;line-height:1.65}.report-footer{display:flex;justify-content:space-between;gap:12px;margin-top:14px;padding-top:10px;border-top:1px solid rgba(100,146,160,.13);color:#65808b;font-size:8px;letter-spacing:.02em}.report-footer span:last-child{color:#8ebcc0;white-space:nowrap}.report-empty-state{display:flex;flex-direction:column;align-items:flex-start;max-width:590px;padding:40px 12px 32px}.empty-agent-icon{display:flex;align-items:center;justify-content:center;width:48px;height:48px;margin-bottom:18px;border:1px solid rgba(89,193,201,.3);background:rgba(42,111,119,.14);color:#67d4d6;font-size:21px;box-shadow:0 0 28px rgba(43,170,178,.08)}.report-empty-state h2{margin:8px 0;color:#dcebed;font-size:19px}.report-empty-state>p{margin:0 0 18px;color:#91aab2;font-size:11px;line-height:1.8}.report-empty-state>>>.el-button--primary{color:#e9ffff;background:#237f83;border-color:#2d9e9e}.report-empty-state>>>.el-button--primary:hover{background:#2c9b9b}
@media(max-width:760px){.impact-report-panel{padding:17px 14px}.impact-report-heading{flex-direction:column}.impact-report-heading h2{font-size:17px}.impact-indicator-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.report-thesis{grid-template-columns:1fr;gap:7px}.report-detail-grid{grid-template-columns:1fr}.report-section-heading{align-items:flex-start;flex-direction:column}.report-footer{flex-direction:column}.report-period{font-size:9px}.replay-agent-hint{margin-top:14px}}

.validation-report{position:relative;margin:22px 0 6px;padding:22px 24px 18px;overflow:hidden;border:1px solid #284751;background:radial-gradient(ellipse at 94% 0%,rgba(35,126,139,.15),transparent 34%),linear-gradient(145deg,#142630,#101e27);box-shadow:0 16px 36px rgba(0,0,0,.16)}.validation-report:before{content:"";position:absolute;left:0;top:0;bottom:0;width:2px;background:linear-gradient(180deg,#53d2cf,#508cb7 60%,transparent)}.validation-report-header{display:flex;align-items:center;gap:15px;padding-bottom:17px;border-bottom:1px solid rgba(112,162,174,.16)}.report-agent-mark{display:flex;align-items:center;justify-content:center;flex:none;width:42px;height:42px;border:1px solid rgba(77,195,203,.33);background:rgba(35,119,131,.15);color:#65d7d6;font-size:20px;box-shadow:0 0 24px rgba(37,161,171,.11)}.report-title-block{flex:1;min-width:0}.validation-report .report-kicker{display:flex;align-items:center;gap:7px;color:#66c9d2;font-size:9px;font-weight:700;letter-spacing:.13em}.report-live-dot{width:6px;height:6px;border-radius:50%;background:#51e0bd;box-shadow:0 0 9px rgba(81,224,189,.7)}.report-version{padding:2px 5px;border:1px solid rgba(110,157,169,.19);color:#809ca6;font-size:8px;letter-spacing:.05em}.validation-report-header h2{margin:7px 0 5px;color:#e5f2f2;font-size:18px;font-weight:620}.validation-report-header p{margin:0;color:#7e9ba5;font-size:10px}.report-status{display:flex;align-items:center;gap:6px;padding:6px 9px;border:1px solid rgba(80,184,146,.2);background:rgba(41,117,91,.1);color:#7bd2b2;font-size:9px;white-space:nowrap}.report-metrics{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));margin:15px 0 13px;border:1px solid rgba(105,146,158,.15);background:rgba(9,23,30,.35)}.report-metric{display:flex;flex-direction:column;gap:6px;min-height:79px;padding:12px 14px;border-right:1px solid rgba(105,146,158,.14)}.report-metric:last-child{border-right:0}.report-metric>span{color:#829da6;font-size:9px}.report-metric strong{color:#d9eded;font-size:20px;font-weight:650;letter-spacing:.01em}.report-metric strong small{color:#82a1a9;font-size:10px;font-weight:500}.report-metric strong.cold-bias{color:#7dc5e1}.report-metric strong.warm-bias{color:#efb574}.report-metric em{color:#6f8a94;font-size:8px;font-style:normal}.report-abstract{display:grid;grid-template-columns:66px 1fr;gap:12px;padding:13px 15px;border:1px solid rgba(69,150,158,.2);border-left:2px solid #51bdc4;background:linear-gradient(90deg,rgba(29,85,91,.19),rgba(18,45,55,.3))}.abstract-label{color:#78d0ce;font-size:10px;font-weight:700}.report-abstract p{margin:0;color:#bfd1d3;font-size:11px;line-height:1.85}.report-analysis-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:13px}.report-section{padding:15px 16px;background:rgba(14,30,39,.66);border:1px solid rgba(102,145,157,.15)}.report-section h3{display:flex;align-items:center;gap:8px;margin:0 0 11px;color:#d2e4e5;font-size:11px;font-weight:620}.report-section h3 i{color:#66c7cb;font-size:14px}.report-list-item{display:grid;grid-template-columns:23px 1fr;gap:8px;padding:8px 0;border-top:1px solid rgba(103,145,155,.1)}.report-list-item p,.recommendation-item p{margin:0;color:#9eb3b9;font-size:10px;line-height:1.75}.report-index{color:#63c2c7;font-size:9px;font-weight:700}.cause-mark{display:flex;align-items:flex-start;padding-top:2px;color:#d9b568;font-size:10px}.recommendations-section{grid-column:1/-1;display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:0 13px}.recommendations-section h3{grid-column:1/-1}.recommendation-item{padding:10px 11px;background:rgba(21,45,52,.52);border:1px solid rgba(99,145,153,.12)}.recommendation-item b{display:block;margin-bottom:6px;color:#a6d5d3;font-size:10px}.report-conclusion{grid-column:1/-1;display:grid;grid-template-columns:74px 1fr;gap:5px 12px;padding:13px 15px;border:1px solid rgba(195,158,87,.19);background:rgba(105,79,34,.1)}.report-conclusion>span{grid-row:span 2;color:#d5b76e;font-size:9px;font-weight:700}.report-conclusion strong{color:#e2d7b4;font-size:11px}.report-conclusion p{margin:0;color:#a99e80;font-size:9px;line-height:1.65}.report-footnote{display:flex;gap:7px;margin-top:12px;color:#728991;font-size:8px;line-height:1.65}.report-footnote i{flex:none;color:#6caeb7;font-size:12px}@media(max-width:760px){.validation-report{padding:16px 13px}.validation-report-header{align-items:flex-start;flex-wrap:wrap}.report-agent-mark{width:36px;height:36px}.report-status{margin-left:50px}.validation-report-header h2{font-size:15px}.report-metrics{grid-template-columns:repeat(2,minmax(0,1fr))}.report-metric{border-bottom:1px solid rgba(105,146,158,.14)}.report-metric:nth-child(2n){border-right:0}.report-metric:last-child{grid-column:1/-1;border-bottom:0}.report-analysis-grid{grid-template-columns:1fr}.recommendations-section{grid-column:auto;grid-template-columns:1fr}.recommendations-section h3{grid-column:auto}.report-conclusion{grid-column:auto;grid-template-columns:1fr}.report-conclusion>span{grid-row:auto}.report-abstract{grid-template-columns:1fr;gap:6px}}
 .system-introduction-page{max-width:1240px;margin:0 auto 28px}.system-intro-hero{display:grid;grid-template-columns:minmax(0,1.45fr) minmax(330px,.8fr);gap:24px;align-items:stretch;padding:30px 32px;border:1px solid #294951;background:radial-gradient(circle at 80% 18%,rgba(42,135,149,.2),transparent 36%),linear-gradient(135deg,#142930,#101d25 70%);box-shadow:0 18px 42px rgba(0,0,0,.16);overflow:hidden}.system-intro-copy{display:flex;flex-direction:column;align-items:flex-start;justify-content:center;min-width:0}.system-intro-kicker{margin:0;color:#63c7d0;font-size:9px;font-weight:700;letter-spacing:.15em}.system-intro-copy h2{max-width:730px;margin:11px 0 14px;color:#e9f5f2;font-size:31px;line-height:1.3;font-weight:650;letter-spacing:.01em}.system-intro-lead{max-width:720px;margin:0;color:#b4c9c9;font-size:13px;line-height:1.95}.system-intro-actions{display:flex;gap:10px;flex-wrap:wrap;margin-top:22px}.system-intro-actions .el-button{margin:0}.system-intro-actions .el-button--primary{color:#062a2a;background:#5bd3c7;border-color:#5bd3c7}.system-intro-actions .el-button--primary:hover{color:#062a2a;background:#79e2d7;border-color:#79e2d7}.system-intro-actions .el-button--default{color:#b9d8d5;background:rgba(27,58,65,.65);border-color:#42656d}.system-intro-tags{display:flex;flex-wrap:wrap;gap:8px 18px;margin-top:23px;color:#789aa0;font-size:10px}.system-intro-tags span{display:inline-flex;align-items:center;gap:5px}.system-intro-tags i{color:#5bd0c6}.system-intro-signal{position:relative;min-height:360px;display:flex;align-items:center;justify-content:center}.signal-orbit{position:absolute;border:1px solid rgba(88,203,207,.25);border-radius:50%;transform:rotate(-22deg)}.signal-orbit-one{width:280px;height:150px}.signal-orbit-two{width:210px;height:280px;transform:rotate(46deg);border-color:rgba(104,150,204,.22)}.signal-core{z-index:2;display:flex;align-items:center;justify-content:center;flex-direction:column;width:142px;height:142px;border:1px solid rgba(105,216,210,.55);border-radius:50%;background:radial-gradient(circle,#246f75 0,rgba(28,67,76,.96) 52%,rgba(16,33,41,.98) 72%);box-shadow:0 0 36px rgba(55,207,196,.2),inset 0 0 25px rgba(109,232,223,.14)}.signal-core span{color:#8be6db;font-size:19px;font-weight:750;letter-spacing:.14em}.signal-core strong{margin-top:7px;color:#d9f3ee;font-size:8px;letter-spacing:.14em}.signal-core small{margin-top:7px;color:#83afb1;font-size:9px}.signal-node{position:absolute;z-index:3;display:flex;align-items:center;gap:6px;padding:7px 9px;border:1px solid rgba(103,172,183,.3);background:rgba(13,36,45,.92);color:#b1d4d4;font-size:10px;box-shadow:0 6px 15px rgba(0,0,0,.18)}.signal-node i{color:#62d2d0;font-size:13px}.node-top{top:25px;left:50%;transform:translateX(-50%)}.node-right{right:0;top:50%;transform:translateY(-50%)}.node-bottom{bottom:24px;left:50%;transform:translateX(-50%)}.node-left{left:0;top:50%;transform:translateY(-50%)}.system-intro-section{margin-top:28px;padding:24px 26px;border:1px solid rgba(72,113,121,.4);background:rgba(18,33,40,.83)}.system-section-heading{display:flex;align-items:flex-end;justify-content:space-between;gap:18px;margin-bottom:18px}.system-section-heading h2{margin:6px 0 0;color:#e1efeb;font-size:20px;font-weight:620}.system-section-heading>span{color:#7f9a9f;font-size:10px}.system-profile-grid{display:grid;grid-template-columns:minmax(0,1.45fr) minmax(300px,.8fr);gap:25px}.system-profile-main{padding-right:24px;border-right:1px solid rgba(100,147,153,.16)}.system-profile-main p{margin:0 0 13px;color:#abc0c0;font-size:12px;line-height:1.95}.system-profile-main p:last-child{margin-bottom:0}.system-profile-facts{display:grid;gap:10px}.system-profile-facts>div{padding:13px 15px;background:rgba(19,49,56,.5);border-left:2px solid #4abfbd}.system-profile-facts span{display:block;color:#7ba0a4;font-size:9px}.system-profile-facts strong{display:block;margin-top:5px;color:#d2e8e4;font-size:14px;font-weight:620}.system-profile-facts small{display:block;margin-top:4px;color:#759196;font-size:9px}.system-capability-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:11px}.system-capability-card{display:grid;grid-template-columns:38px 1fr;gap:11px;min-height:145px;padding:15px;background:#14272e;border:1px solid rgba(84,131,140,.23)}.system-capability-card:hover{border-color:#57c9c3;background:#19343a;transform:translateY(-2px);transition:.2s ease}.capability-icon{display:flex;align-items:center;justify-content:center;width:36px;height:36px;border:1px solid currentColor;background:rgba(255,255,255,.03);font-size:17px}.system-capability-card h3{margin:1px 0 7px;color:#d9ebe7;font-size:13px}.system-capability-card p{min-height:42px;margin:0;color:#8ea8aa;font-size:10px;line-height:1.65}.system-capability-card a{display:inline-flex;align-items:center;gap:5px;margin-top:11px;color:#78d7d0;font-size:10px;text-decoration:none}.system-capability-card a:hover{color:#b4fff4}.system-capability-card.cyan{color:#61d3cd}.system-capability-card.gold{color:#dfb96e}.system-capability-card.violet{color:#b2a5e9}.system-capability-card.green{color:#6ed1aa}.system-capability-card.blue{color:#72bce1}.system-capability-card.purple{color:#c19be8}.system-workflow{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));border:1px solid rgba(91,143,148,.22)}.system-workflow-step{position:relative;display:grid;grid-template-columns:34px 1fr 14px;gap:9px;min-height:103px;padding:16px 14px;border-right:1px solid rgba(91,143,148,.22)}.system-workflow-step:last-child{border-right:0}.system-workflow-step>span{color:#64d1c8;font-size:11px;font-weight:700}.system-workflow-step strong{color:#d4e7e4;font-size:12px}.system-workflow-step p{margin:7px 0 0;color:#849ea0;font-size:10px;line-height:1.65}.system-workflow-step>i{align-self:center;color:#66888e}.system-workflow-step:hover{background:rgba(45,100,103,.17)}
@media(max-width:900px){.system-intro-hero{grid-template-columns:1fr;padding:25px}.system-intro-signal{min-height:290px;max-width:430px;width:100%;margin:0 auto}.system-profile-grid{grid-template-columns:1fr}.system-profile-main{padding-right:0;padding-bottom:16px;border-right:0;border-bottom:1px solid rgba(100,147,153,.16)}.system-capability-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.system-workflow{grid-template-columns:repeat(2,minmax(0,1fr))}.system-workflow-step:nth-child(2){border-right:0}.system-workflow-step:nth-child(-n+2){border-bottom:1px solid rgba(91,143,148,.22)}}
@media(max-width:620px){.system-intro-hero{padding:21px 17px}.system-intro-copy h2{font-size:24px}.system-intro-lead{font-size:12px}.system-intro-tags{display:grid;gap:8px}.system-intro-section{padding:19px 16px}.system-section-heading{align-items:flex-start;flex-direction:column;gap:6px}.system-capability-grid,.system-workflow{grid-template-columns:1fr}.system-workflow-step{border-right:0;border-bottom:1px solid rgba(91,143,148,.22)}.system-workflow-step:last-child{border-bottom:0}.system-intro-signal{min-height:250px;transform:scale(.88)}.signal-node{font-size:9px}.node-right{right:-4px}.node-left{left:-4px}}

/* 能力卡片为奇数末位时占满整行，避免尾行孤卡 */
.system-capability-grid .system-capability-card:last-child:nth-child(3n + 1){grid-column:1 / -1}
</style>
