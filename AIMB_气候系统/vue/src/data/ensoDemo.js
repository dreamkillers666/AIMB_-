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
  '2025-01': -0.5, '2025-02': -0.2, '2025-03': -0.1, '2025-04': 0.0,
  '2025-05': 0.0, '2025-06': 0.0, '2025-07': -0.1, '2025-08': -0.3,
  '2025-09': -0.4, '2025-10': -0.6, '2025-11': -0.6, '2025-12': -0.6,
  '2026-01': -0.4, '2026-02': -0.2, '2026-03': 0.1, '2026-04': 0.5,
  '2026-05': 0.9, '2026-06': 1.4, '2026-07': 1.8, '2026-08': 2.2
}

const extendedOni = [
  2.16, 2.27, 2.22, 2.03, 1.72, 1.36, 1.03, 0.67, 0.28, -0.02, -0.28, -0.46,
  -0.57, -0.54, -0.39, -0.19, 0.02, 0.17, 0.19, 0.08, -0.08, -0.20, -0.18, -0.03,
  0.13, 0.22, 0.16, 0.00, -0.16, -0.22, -0.12, 0.06, 0.20, 0.25, 0.16, 0.02
]

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

const hashStart = (value) => value.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)

const referenceOni = (value) => {
  if (Object.prototype.hasOwnProperty.call(observedOni, value)) return observedOni[value]
  const offset = monthDistance('2026-08', value)
  if (offset >= 0 && offset < extendedOni.length) return extendedOni[offset]
  return Number((0.18 * Math.sin((offset + 4) / 2.8) * Math.exp(-Math.max(offset, 0) / 30)).toFixed(2))
}

const isObservedMonth = (value) => Object.prototype.hasOwnProperty.call(observedOni, value)

const makeReplaySeries = (start) => {
  const seed = hashStart(start)
  return leadMonths.map((leadMonth) => {
    const targetMonth = addMonths(start, leadMonth)
    const reference = referenceOni(targetMonth)
    const spread = 0.28 + leadMonth * 0.055
    const phase = Math.sin((seed * 127.1 + leadMonth * 311.7) * Math.PI / 180) * (0.10 + leadMonth * 0.04)
    const drift = ((seed % 7) - 3) * 0.02 * Math.min(leadMonth / 8, 1)
    const value = reference + (phase + drift) * 2.5
    return {
      leadMonth,
      month: targetMonth,
      value: Number(value.toFixed(2)),
      observed: isObservedMonth(targetMonth) ? Number(reference.toFixed(2)) : null,
      lower: Number((value - spread).toFixed(2)),
      upper: Number((value + spread).toFixed(2))
    }
  })
}

const calculateMetrics = (points, lead) => {
  const evaluated = points.filter(point => Number.isFinite(point.observed))
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
