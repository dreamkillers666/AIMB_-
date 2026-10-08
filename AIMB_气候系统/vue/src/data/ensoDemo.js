const leadMonths = Array.from({ length: 20 }, (_, index) => index + 1)

const makeSeries = (start, count = 20) => leadMonths.slice(0, count).map((leadMonth) => {
  const value = 1.55 * Math.exp(-Math.pow((leadMonth - 8) / 6.2, 2)) - 0.18
  return {
    leadMonth,
    month: addMonths(start, leadMonth),
    value: Number(value.toFixed(2)),
    lower: Number((value - 0.28).toFixed(2)),
    upper: Number((value + 0.28).toFixed(2))
  }
})

const spatialMaps = Array.from({ length: 9 }, (_, y) => Array.from({ length: 15 }, (_, x) => {
  const equatorial = Math.exp(-Math.pow((y - 4) / 1.5, 2))
  const pacific = Math.exp(-Math.pow((x - 8) / 4, 2))
  return Number((1.4 * equatorial * pacific - 0.12).toFixed(2))
}))

const metadata = {
  model: 'LSTA-Swin',
  windows: { longTermMonths: 12, shortTermMonths: 3 },
  variables: ['SSTA', 'SSTA150', 'τx', 'τy']
}

const replayOptions = Array.from({ length: 24 }, (_, index) => {
  const year = 2025 + Math.floor(index / 12)
  const month = String((index % 12) + 1).padStart(2, '0')
  return `${year}-${month}`
})

// CPC ONI seasonal values are keyed by their center month (DJF -> January).
// The published ERSSTv6 table currently ends at JAS 2026, centered on August.
const observedOni = {
  '2024-01': 1.8, '2024-02': 1.5, '2024-03': 1.2, '2024-04': 0.8,
  '2024-05': 0.4, '2024-06': 0.2, '2024-07': 0.1, '2024-08': 0.0,
  '2024-09': -0.1, '2024-10': -0.2, '2024-11': -0.3, '2024-12': -0.4,
  '2025-01': -0.5, '2025-02': -0.2, '2025-03': -0.1, '2025-04': 0.0,
  '2025-05': 0.0, '2025-06': 0.0, '2025-07': -0.1, '2025-08': -0.3,
  '2025-09': -0.4, '2025-10': -0.6, '2025-11': -0.6, '2025-12': -0.6,
  '2026-01': -0.4, '2026-02': -0.2, '2026-03': 0.1, '2026-04': 0.5,
  '2026-05': 0.9, '2026-06': 1.4, '2026-07': 1.8, '2026-08': 2.2
}

const toMonthDate = (value) => {
  const [year, month] = value.split('-').map(Number)
  return new Date(Date.UTC(year, month - 1, 1))
}

const monthKey = (date) => `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`

const addMonths = (value, amount) => {
  const date = toMonthDate(value)
  date.setUTCMonth(date.getUTCMonth() + amount)
  return monthKey(date)
}

const monthDistance = (from, to) => {
  const start = toMonthDate(from)
  const end = toMonthDate(to)
  return (end.getUTCFullYear() - start.getUTCFullYear()) * 12 + end.getUTCMonth() - start.getUTCMonth()
}

const referenceOni = (value) => Object.prototype.hasOwnProperty.call(observedOni, value) ? observedOni[value] : null
const isObservedMonth = (value) => Object.prototype.hasOwnProperty.call(observedOni, value)

const latestObservationAtOrBefore = (value) => Object.keys(observedOni)
  .filter(month => month <= value)
  .sort()
  .slice(-1)
  .map(month => ({ month, value: observedOni[month] }))[0] || null

const estimateTendency = (throughMonth) => {
  const history = Object.keys(observedOni)
    .filter(month => month <= throughMonth)
    .sort()
    .slice(-4)
    .map(month => ({ month, value: observedOni[month] }))
  if (history.length < 2) return 0

  const xMean = (history.length - 1) / 2
  const yMean = history.reduce((sum, point) => sum + point.value, 0) / history.length
  const covariance = history.reduce((sum, point, index) => sum + (index - xMean) * (point.value - yMean), 0)
  const variance = history.reduce((sum, point, index) => sum + Math.pow(index - xMean, 2), 0)
  return Math.max(-0.3, Math.min(0.3, covariance / variance))
}

const hindcastValue = (start, targetMonth) => {
  // ONI is centered on a three-month season, so use observations published by the issue month.
  const latest = latestObservationAtOrBefore(addMonths(start, -2)) || latestObservationAtOrBefore('2026-08')
  if (!latest) return null

  let value = latest.value
  let tendency = estimateTendency(latest.month)
  const steps = Math.max(0, monthDistance(latest.month, targetMonth))
  for (let step = 0; step < steps; step += 1) {
    value = (value + tendency) * 0.95
    tendency *= 0.65
  }
  return Number(value.toFixed(2))
}

const makeReplaySeries = (start) => {
  return leadMonths.map((leadMonth) => {
    const targetMonth = addMonths(start, leadMonth)
    const value = hindcastValue(start, targetMonth)
    const spread = 0.28 + leadMonth * 0.055
    return {
      leadMonth,
      month: targetMonth,
      value,
      observed: isObservedMonth(targetMonth) ? referenceOni(targetMonth) : null,
      lower: value == null ? null : Number((value - spread).toFixed(2)),
      upper: value == null ? null : Number((value + spread).toFixed(2))
    }
  })
}

const calculateMetrics = (points, lead) => {
  const evaluated = points.filter(point => Number.isFinite(point.value) && Number.isFinite(point.observed))
  const predicted = evaluated.map(point => point.value)
  const reference = evaluated.map(point => point.observed)
  if (!evaluated.length) return { lead, pcc: null, mae: null, rmse: null, sampleCount: 0 }

  const meanPredicted = predicted.reduce((sum, value) => sum + value, 0) / predicted.length
  const meanReference = reference.reduce((sum, value) => sum + value, 0) / reference.length
  const covariance = predicted.reduce((sum, value, index) => sum + (value - meanPredicted) * (reference[index] - meanReference), 0)
  const predictedVariance = predicted.reduce((sum, value) => sum + Math.pow(value - meanPredicted, 2), 0)
  const referenceVariance = reference.reduce((sum, value) => sum + Math.pow(value - meanReference, 2), 0)
  const pcc = evaluated.length >= 3 && predictedVariance && referenceVariance
    ? covariance / Math.sqrt(predictedVariance * referenceVariance)
    : null
  const mae = predicted.reduce((sum, value, index) => sum + Math.abs(value - reference[index]), 0) / predicted.length
  const rmse = Math.sqrt(predicted.reduce((sum, value, index) => sum + Math.pow(value - reference[index], 2), 0) / predicted.length)
  return {
    lead,
    pcc: pcc == null ? null : Number(Math.max(-1, Math.min(1, pcc)).toFixed(2)),
    mae: Number(mae.toFixed(2)),
    rmse: Number(rmse.toFixed(2)),
    sampleCount: evaluated.length
  }
}

const verificationLeads = leadMonths
const aggregateReplayMetrics = verificationLeads.map(lead => {
  const points = replayOptions
    .map(start => makeReplaySeries(start).find(point => point.leadMonth === lead))
    .filter(Boolean)
  return calculateMetrics(points, lead)
})

const demoMetrics = {
  leadMonths: verificationLeads,
  systemResults: aggregateReplayMetrics,
  ablation: [
    { variant: '原始 Swin', pcc: 0.59 },
    { variant: 'LST-IAM + MLP', pcc: 0.65 },
    { variant: 'LSTA-Swin', pcc: 0.69 },
    { variant: '去除 MLP', pcc: 0.62 },
    { variant: '替换 Swin Block', pcc: 0.62 }
  ]
}

export default {
  metadata,
  replayOptions,
  forecast(start, lead) {
    return {
      ...metadata,
      forecastStart: start,
      leadMonths: lead,
      indexName: 'Niño3.4 / ONI',
      series: makeSeries(start, lead),
      spatialMaps
    }
  },
  replay(start) {
    const replayStart = replayOptions.includes(start) ? start : '2025-01'
    return {
      ...metadata,
      forecastStart: replayStart,
      leadMonths: 20,
      indexName: 'Niño3.4 / ONI',
      series: makeReplaySeries(replayStart),
      referenceSource: 'NOAA CPC ONI',
      observedThrough: '2026-08'
    }
  },
  metrics: demoMetrics,
  explainability: {
    ...metadata,
    stages: ['充电', '发展', '衰减'],
    heatmap: spatialMaps
  }
}
