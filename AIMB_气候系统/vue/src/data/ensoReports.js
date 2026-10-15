/**
 * 预置的 ENSO 历史预测报告数据
 *
 * 目前仅预生成 2025 年 1 月与 2 月两个起报月份，各含两份报告：
 *  1) ENSO 影响研判技术报告（impactReports）
 *  2) 提前期验证数据分析报告（validationReports）
 *
 * 提前期验证报告中的指标占位符（{{mae}} 等）在模块加载时按
 * ensoDemo 的回报序列 + 汇总验证结果计算填充，与「提前期验证」页面口径一致。
 */
import demoCases from './ensoDemo'

/** 已预置报告的起报月份 */
export const REPORT_MONTHS = ['2025-01', '2025-02']

/** ① ENSO 影响研判技术报告 */
export const impactReports = Object.freeze({
  '2025-01': {
    id: 'ENSO-IMPACT-202501',
    title: '2025 年 1 月 ENSO 影响研判技术报告',
    generatedAt: '2025-01-06 09:30',
    phase: '弱拉尼娜衰减段',
    phaseValue: '−0.36 ℃',
    confidence: '中高',
    horizon: '2025 年 2–4 月',
    summary: '历史案例指向赤道中东太平洋冷却信号逐步减弱，西太平洋暖池与副热带高压偏强的组合仍可能维持。未来一季中国气候风险以南方阶段性水汽偏弱、北方温度偏高和春季农业干旱暴露上升为主，需关注冷空气过程与暖湿输送的快速切换。',
    indicators: [
      { label: 'Niño3.4 预测中心', value: '−0.36 ℃', detail: '较基准偏冷 · 衰减', tone: 'cyan' },
      { label: '中国降水倾向', value: '南方偏少', detail: '华南约 −10%～−20%', tone: 'gold' },
      { label: '中国温度倾向', value: '北暖南平', detail: '北方 +0.3～+0.8 ℃', tone: 'violet' },
      { label: '研判置信度', value: '0.78', detail: '中高 · 多源一致', tone: 'green' }
    ],
    highlights: [
      { label: '水分', value: '华南春旱风险上升', tone: 'warning' },
      { label: '温度', value: '北方偏暖概率偏高', tone: 'warm' },
      { label: '农业', value: '冬小麦返青水分敏感', tone: 'cool' }
    ],
    regionalRisks: [
      { region: '华南—江南', precipitation: '偏少 10%～20%', temperature: '偏高 0.1～0.5 ℃', risk: '春旱、森林火险', level: '关注', levelTone: 'medium' },
      { region: '华北—黄淮', precipitation: '偏少 5%～15%', temperature: '偏高 0.3～0.8 ℃', risk: '土壤墒情下降、霜冻窗口提前', level: '关注', levelTone: 'medium' },
      { region: '长江中下游', precipitation: '接近常年', temperature: '接近常年', risk: '阶段性低温阴雨转折', level: '一般', levelTone: 'low' },
      { region: '西南东部', precipitation: '偏少 10%～25%', temperature: '偏高 0.2～0.6 ℃', risk: '山地旱情、林草火险', level: '较高', levelTone: 'high' }
    ],
    agriculture: [
      '冬小麦：返青至拔节期有效降水不足概率升高，建议将灌溉窗口前移 7–10 天，并优先保障群体偏弱田块。',
      '油菜与蔬菜：华南局地蒸散增强，需结合墒情监测安排小水勤灌，避免一次性过量灌溉造成病害。',
      '林草与设施农业：暖干背景叠加大风过程，重点防范林火和设施棚膜风损，建立 48 小时滚动预警。'
    ],
    actions: [
      { tag: '01', title: '水资源调度', text: '对华南、滇东开展旬尺度墒情复核，预留春灌及抗旱水源。' },
      { tag: '02', title: '农业气象服务', text: '以县域为单元更新返青期干旱指数，向冬小麦和油菜经营主体推送分区建议。' },
      { tag: '03', title: '过程复核', text: '每周融合 Niño3.4、850 hPa 水汽通量与副高位置，若暖湿输送增强则下调旱险等级。' }
    ],
    uncertainty: '不确定性主要来自春季季节内振荡和中纬度阻塞形势。若 850 hPa 西南水汽通量较基准增强 15% 以上，华南降水偏少信号需下调一级。'
  },
  '2025-02': {
    id: 'ENSO-IMPACT-202502',
    title: '2025 年 2 月 ENSO 影响研判技术报告',
    generatedAt: '2025-02-06 09:30',
    phase: '中性偏冷 · 拉尼娜尾段',
    phaseValue: '−0.22 ℃',
    confidence: '中等',
    horizon: '2025 年 3–5 月',
    summary: '2025 年 2 月历史案例显示冷异常接近阈值，海气耦合逐步转弱，ENSO 对中国气候的直接约束减小。未来春季呈现“北方偏暖、南方降水时空不均”的概率结构，前期干旱风险仍在，但暖湿季风启动后华南局地强对流和短时洪涝风险会快速抬升。',
    indicators: [
      { label: 'Niño3.4 预测中心', value: '−0.22 ℃', detail: '接近中性 · 转暖迹象', tone: 'cyan' },
      { label: '中国降水倾向', value: '南北分化', detail: '华南 −5%～+10%', tone: 'gold' },
      { label: '中国温度倾向', value: '整体偏暖', detail: '北方 +0.4～+1.0 ℃', tone: 'violet' },
      { label: '研判置信度', value: '0.69', detail: '中等 · 季节内扰动强', tone: 'green' }
    ],
    highlights: [
      { label: '水分', value: '南方转折风险加大', tone: 'warning' },
      { label: '温度', value: '北方暖干概率偏高', tone: 'warm' },
      { label: '农业', value: '播期与病虫害窗口提前', tone: 'cool' }
    ],
    regionalRisks: [
      { region: '华南沿海', precipitation: '前少后多', temperature: '偏高 0.2～0.7 ℃', risk: '旱涝急转、强对流', level: '较高', levelTone: 'high' },
      { region: '江南—华中', precipitation: '偏多 5%～15%', temperature: '偏高 0.1～0.5 ℃', risk: '阶段性渍涝、病害扩散', level: '关注', levelTone: 'medium' },
      { region: '华北—东北', precipitation: '偏少 5%～15%', temperature: '偏高 0.4～1.0 ℃', risk: '春旱、早春霜冻错配', level: '关注', levelTone: 'medium' },
      { region: '西南地区', precipitation: '偏少 5%～20%', temperature: '偏高 0.2～0.8 ℃', risk: '旱情延续、山火风险', level: '关注', levelTone: 'medium' }
    ],
    agriculture: [
      '冬小麦：北方积温偏高可能推动生育期提前，需将拔节期冻害防控和灌浆水管理纳入同一调度方案。',
      '水稻与春玉米：南方播栽窗口受旱涝转换影响，建议采用分批播种和短周期品种，降低集中暴露。',
      '果蔬与茶园：暖湿转换有利于病虫害扩散，重点监测稻瘟病、霜霉病及茶园假眼小绿叶蝉。'
    ],
    actions: [
      { tag: '01', title: '转折监测', text: '将华南 7–15 天降水和对流潜势纳入日更新看板，识别旱涝急转窗口。' },
      { tag: '02', title: '播期优化', text: '根据积温和土壤墒情动态调整春播批次，避免在单一强降水过程前集中下种。' },
      { tag: '03', title: '滚动校正', text: '若 Niño3.4 连续两月回升超过 0.3 ℃，重新估计季风启动日期并更新风险权重。' }
    ],
    uncertainty: '本月报告对季节内转折较敏感。ENSO 信号减弱后，MJO、印度洋偶极子及中纬度环流的相对贡献上升，区域降水结论应以旬尺度环流监测校正。'
  }
})

// ---------------------------------------------------------------------------
// 提前期验证数据分析报告
// ---------------------------------------------------------------------------

const aggregateRows = demoCases.metrics.systemResults

const stateClass = value => value <= -0.5 ? 'cold' : value >= 0.5 ? 'warm' : 'neutral'

/** 与「提前期验证」页一致的案例指标汇总 */
export const buildCaseSummary = start => {
  const series = demoCases.replay(start).series
  const points = series.filter(point => Number.isFinite(point.value) && Number.isFinite(point.observedNino34))
  if (!points.length) return null

  const errors = points.map(point => point.value - point.observedNino34)
  const mae = errors.reduce((sum, error) => sum + Math.abs(error), 0) / errors.length
  const rmse = Math.sqrt(errors.reduce((sum, error) => sum + error * error, 0) / errors.length)
  const bias = errors.reduce((sum, error) => sum + error, 0) / errors.length

  const forecastMean = points.reduce((sum, point) => sum + point.value, 0) / points.length
  const observedMean = points.reduce((sum, point) => sum + point.observedNino34, 0) / points.length
  const covariance = points.reduce((sum, point) => sum + (point.value - forecastMean) * (point.observedNino34 - observedMean), 0)
  const forecastVariance = points.reduce((sum, point) => sum + Math.pow(point.value - forecastMean, 2), 0)
  const observedVariance = points.reduce((sum, point) => sum + Math.pow(point.observedNino34 - observedMean, 2), 0)
  const pccRaw = points.length >= 3 && forecastVariance && observedVariance
    ? covariance / Math.sqrt(forecastVariance * observedVariance)
    : null
  const pcc = Number.isFinite(pccRaw) ? Math.max(-1, Math.min(1, pccRaw)) : null

  const stateHits = points.filter(point => stateClass(point.value) === stateClass(point.observedNino34)).length
  const leadMetrics = points
    .map(point => aggregateRows.find(metric => metric.lead === point.leadMonth))
    .filter(metric => metric && Number.isFinite(metric.mae) && Number.isFinite(metric.sampleCount) && metric.sampleCount > 0)
  const aggregateWeight = leadMetrics.reduce((sum, metric) => sum + metric.sampleCount, 0)
  const aggregateMae = aggregateWeight
    ? leadMetrics.reduce((sum, metric) => sum + metric.mae * metric.sampleCount, 0) / aggregateWeight
    : null

  return {
    start,
    count: points.length,
    stateHits,
    stateAccuracy: Math.round(stateHits / points.length * 100),
    mae,
    rmse,
    bias,
    pcc,
    aggregateMae,
    leadRows: points.map(point => ({
      leadMonth: point.leadMonth,
      month: point.month,
      forecast: point.value,
      observed: point.observedNino34,
      error: Number((point.value - point.observedNino34).toFixed(2))
    }))
  }
}

/** 提前期验证报告的故事线模板（数值占位符由 buildCaseSummary 结果填充） */
const validationTemplates = {
  '2025-01': {
    id: 'LSTA-SWIN-VERIFY-202501',
    title: '弱拉尼娜衰减与暖位相转折漏报诊断',
    generatedAt: '2025-01-06 09:30',
    summary: '本次历史案例能够维持弱冷位相的背景判断，案例相关性为 {{pcc}}，但振幅与转折时点误差突出：平均偏差 {{bias}}℃，三档状态命中率 {{accuracy}}%。预测序列持续停留在弱冷区间，未能跟随 2025 年春夏实际序列向中性及暖侧跃迁；误差随提前期扩大，说明当前外推对海气耦合转换信号响应不足。',
    findings: [
      '有效配对覆盖 {{count}} 个月，案例 MAE {{mae}}℃、RMSE {{rmse}}℃；案例 PCC {{pcc}} 反映整体共变仍有一定一致性，但不能替代逐月状态命中率。',
      '冷侧背景延续被过度平滑，预测振幅明显收缩；后段暖侧增强未被提前捕捉，误差由相位偏差与振幅低估共同主导。',
      '三档状态命中 {{hits}}/{{count}} 个月（{{accuracy}}%）。单月分档用于历史案例诊断，不能解释为官方 ONI 事件准确率。'
    ],
    causes: [
      '递减的单一趋势项难以表达“弱冷衰减—中性过渡—暖异常发展”的非线性转换，远期预测逐步向平滑背景态收缩。',
      'Niño3.4 指数序列缺少对西风爆发、暖水体积与次表层热含量重建的显式约束，关键先导信号可能未被充分传递到长提前期输出。',
      '单案例有效月份有限，事件转折样本及季节位相覆盖不足；以逐月观测作参照时，月际噪声也会放大局地误差。'
    ],
    recommendations: [
      { title: '模型结构｜显式建模相位转换', detail: '在 LSTA-Swin 主干上增加 ENSO 状态转移或多任务相位头，同时预测 Niño3.4 连续值、变化率和冷/中/暖概率；对 3–9 个月转折窗口采用门控残差解码，避免远期输出过度趋平。' },
      { title: '数据处理｜补齐海气先导信号', detail: '加入暖水体积 / 上层海洋热含量、赤道西风爆发、海表风应力及次表层温度等特征；按起报时刻严格进行时间切分和标准化，核验缺测填补与异常值处理是否引入相位滞后。' },
      { title: '验证设计｜按事件和季节分层', detail: '在滚动起报回报中报告事件转折月误差、振幅误差、PCC、MAE、RMSE 与三档命中率，并给出 bootstrap 置信区间；对 6–12 个月提前期开展独立校准。' }
    ],
    verdict: '背景位相有参考价值，转折预报能力不足',
    nextStep: '优先补强海洋热含量与风爆发先导信息，并针对转折样本开展分层回报和概率校准。'
  },
  '2025-02': {
    id: 'LSTA-SWIN-VERIFY-202502',
    title: '冷位相维持偏差与暖异常发展低估诊断',
    generatedAt: '2025-02-06 09:30',
    summary: '该起报轨迹呈现持续冷偏差，平均偏差 {{bias}}℃，三档状态命中率 {{accuracy}}%；案例 MAE {{mae}}℃、RMSE {{rmse}}℃。案例 PCC {{pcc}} 显示序列变化方向仍有一定同步性，但预测暖侧振幅显著不足，分类命中随观测由中性转向暖侧而下降。结果提示模型对冷背景的惯性保持强于对后续暖异常发展的响应。',
    findings: [
      '有效配对覆盖 {{count}} 个月，三档状态命中 {{hits}}/{{count}} 个月（{{accuracy}}%）；低命中主要集中在观测进入暖侧后的后段目标月。',
      '案例偏差为 {{bias}}℃，整体偏冷；RMSE 与 MAE 的差异表明，后段少数大幅暖异常漏报对总误差贡献较大。',
      '案例 PCC {{pcc}} 与类别命中率并不等价：连续序列仍可能共同变化，但幅度校准和关键状态转类仍存在明显缺口。'
    ],
    causes: [
      '模型对最近观测的持续性依赖与趋势衰减共同形成冷位相“记忆”，当真实海洋蓄热释放并推动暖异常增强时，输出容易滞后。',
      '若训练集中快速转暖与强暖事件比例偏低，均方误差优化会偏向振幅收缩，降低少数高振幅样本对参数更新的影响。',
      'Niño3.4 单指标验证不能区分海洋初始场误差、季节锁相误差和大气噪声；需要结合空间海温与风应力场进行归因。'
    ],
    recommendations: [
      { title: '模型结构｜改善远期振幅与概率表达', detail: '采用多尺度时序解码与状态条件门控，增加对变化率、振幅和状态概率的联合约束；对暖异常发展阶段使用分位数或异方差输出，减少均值回归造成的振幅低估。' },
      { title: '数据处理｜提高转暖样本代表性', detail: '按 ENSO 生命周期阶段重采样或使用事件加权损失，保持训练、验证按年份分块，避免同一事件泄漏；对海温距平、热含量和风场统一基准期、掩膜及标准化口径。' },
      { title: '评估闭环｜定位误差来源', detail: '补充逐提前期偏差、振幅比、转类提前量和可靠性图；采用按事件留一验证，并分别评估 Niño3.4 指数及赤道太平洋空间场，检验改进是否来自真实可预测信号。' }
    ],
    verdict: '趋势共变尚可，状态转换与振幅技巧偏弱',
    nextStep: '重点检查暖水体积与风应力先导信号，并通过事件加权训练和分位数输出改善暖侧振幅。'
  }
}

const formatNumber = value => Number.isFinite(value) ? value.toFixed(2) : '—'
const formatSigned = value => Number.isFinite(value) ? `${value > 0 ? '+' : ''}${value.toFixed(2)}` : '—'

const fillPlaceholders = (text, values) => String(text).replace(/\{\{(pcc|bias|accuracy|count|hits|mae|rmse)\}\}/g,
  (match, key) => (values[key] === null || values[key] === undefined ? '—' : values[key]))

const buildValidationReport = month => {
  const template = validationTemplates[month]
  if (!template) return null
  const summary = buildCaseSummary(month) || {}
  const values = {
    pcc: Number.isFinite(summary.pcc) ? summary.pcc.toFixed(2) : '样本不足',
    bias: formatSigned(summary.bias),
    accuracy: summary.stateAccuracy === undefined ? '—' : summary.stateAccuracy,
    count: summary.count === undefined ? 0 : summary.count,
    hits: summary.stateHits === undefined ? 0 : summary.stateHits,
    mae: formatNumber(summary.mae),
    rmse: formatNumber(summary.rmse)
  }
  return {
    ...template,
    title: `${month.slice(0, 4)} 年 ${Number(month.slice(5, 7))} 月 · ${template.title}`,
    summary: fillPlaceholders(template.summary, values),
    findings: template.findings.map(item => fillPlaceholders(item, values)),
    causes: template.causes.map(item => fillPlaceholders(item, values)),
    recommendations: template.recommendations.map(item => ({ ...item, detail: fillPlaceholders(item.detail, values) })),
    nextStep: fillPlaceholders(template.nextStep, values),
    metrics: {
      mae: formatNumber(summary.mae),
      rmse: formatNumber(summary.rmse),
      bias: formatSigned(summary.bias),
      biasTone: Number.isFinite(summary.bias) ? (summary.bias < 0 ? 'cold' : 'warm') : '',
      biasNote: Number.isFinite(summary.bias)
        ? (summary.bias < 0 ? '整体偏冷' : summary.bias > 0 ? '整体偏暖' : '无明显系统偏差')
        : '—',
      stateAccuracy: summary.stateAccuracy === undefined ? '—' : summary.stateAccuracy,
      stateHits: summary.stateHits === undefined ? 0 : summary.stateHits,
      count: summary.count === undefined ? 0 : summary.count,
      aggregateMae: formatNumber(summary.aggregateMae)
    },
    leadRows: (summary.leadRows || []).slice(0, 20)
  }
}

/** ② 提前期验证数据分析报告 */
export const validationReports = Object.freeze(REPORT_MONTHS.reduce((result, month) => {
  const report = buildValidationReport(month)
  if (report) result[month] = report
  return result
}, {}))

/** 某个起报月份是否已有预置报告 */
export const hasReportsFor = month => REPORT_MONTHS.indexOf(month) > -1

/** 取某个起报月份的两份预置报告 */
export const getModelReports = month => ({
  month,
  available: hasReportsFor(month),
  impact: impactReports[month] || null,
  validation: validationReports[month] || null
})

export const availableReplayMonths = demoCases.replayOptions

export default { REPORT_MONTHS, impactReports, validationReports, hasReportsFor, getModelReports, buildCaseSummary }
