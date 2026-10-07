const leadMonths = Array.from({ length: 20 }, (_, index) => index + 1)

const makeSeries = (offset = 0) => leadMonths.map((leadMonth) => {
  const value = 1.55 * Math.exp(-Math.pow((leadMonth - 8 - offset) / 6.2, 2)) - 0.18
  return {
    leadMonth,
    month: `${leadMonth}月`,
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

const demoMetrics = {
  leadMonths: [1, 3, 6, 9, 12, 18, 20],
  systemResults: [
    { lead: 1, pcc: 0.91, mae: 0.18, rmse: 0.23 },
    { lead: 3, pcc: 0.88, mae: 0.22, rmse: 0.29 },
    { lead: 6, pcc: 0.82, mae: 0.31, rmse: 0.39 },
    { lead: 9, pcc: 0.76, mae: 0.39, rmse: 0.48 },
    { lead: 12, pcc: 0.69, mae: 0.47, rmse: 0.56 },
    { lead: 18, pcc: 0.58, mae: 0.59, rmse: 0.68 },
    { lead: 20, pcc: 0.53, mae: 0.64, rmse: 0.74 }
  ],
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
  forecast(start, lead) {
    return {
      ...metadata,
      forecastStart: start,
      leadMonths: lead,
      indexName: 'Niño3.4 / ONI',
      series: makeSeries().slice(0, lead),
      spatialMaps
    }
  },
  replay(start) {
    return {
      ...metadata,
      forecastStart: start,
      leadMonths: 20,
      series: makeSeries(1)
    }
  },
  metrics: demoMetrics,
  explainability: {
    ...metadata,
    stages: ['充电', '发展', '衰减'],
    heatmap: spatialMaps
  }
}
