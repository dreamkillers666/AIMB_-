import demoCases from './ensoDemo'
import { isLand } from './landMask'

const monthKey = (year, month) => `${year}-${String(month).padStart(2, '0')}`

const addMonths = (value, amount) => {
  const [year, month] = value.split('-').map(Number)
  const date = new Date(Date.UTC(year, month - 1 + amount, 1))
  return monthKey(date.getUTCFullYear(), date.getUTCMonth() + 1)
}

const makeSeries = (start, values) => values.map((value, index) => ({
  month: addMonths(start, index),
  value: Number(value.toFixed(2)),
  observed: Number(value.toFixed(2)),
  leadMonth: index + 1
}))

// Event intensity is reported on the warm or cold side of the index. For a
// model trajectory, use the largest absolute excursion so the headline value
// continues to follow the selected start month.
const peakValue = (values, type = '') => {
  const numeric = values
    .filter(value => value !== null && value !== undefined && value !== '')
    .map(value => Number(value))
    .filter(value => Number.isFinite(value))
  if (!numeric.length) return null
  const peak = type.indexOf('拉尼娜') > -1
    ? Math.min(...numeric)
    : type.indexOf('厄尔尼诺') > -1
      ? Math.max(...numeric)
      : numeric.reduce((best, value) => Math.abs(value) > Math.abs(best) ? value : best, numeric[0])
  return Number(peak.toFixed(2))
}

const formatPeak = value => {
  if (!Number.isFinite(Number(value))) return '—'
  const peak = Number(value)
  return `${peak > 0 ? '+' : ''}${peak.toFixed(1)} ℃`
}

// CPC ONI v6 seasonal values are used as a compact, citable event index fixture.
// The UI labels the values as ONI proxies because they are aligned to calendar months here.
const eventFixtures = [
  {
    id: '1982-83',
    label: '1982-1983 强厄尔尼诺',
    type: '强厄尔尼诺',
    start: '1982-01',
    source: 'NOAA CPC ONI v6 事件参考 · 本地演示序列',
    sourceUrl: 'https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso/oni/v6/index.php',
    peak: '+2.1 ℃',
    phase: '1982 年秋季快速发展，1983 年初达到峰值后衰减',
    description: '太平洋中东部暖异常在秋季迅速增强，次年春季转入衰减，适合观察强暖事件的“发展-峰值-衰减”过程。',
    values: [0.0, 0.0, 0.1, 0.3, 0.7, 0.7, 0.8, 1.0, 1.5, 1.8, 2.0, 2.1, 2.1, 1.9, 1.5, 1.2, 1.0, 0.6, 0.3, -0.1, -0.4, -0.8, -1.0, -0.9]
  },
  {
    id: '1997-98',
    label: '1997-1998 强厄尔尼诺',
    type: '强厄尔尼诺',
    start: '1997-01',
    source: 'NOAA CPC ONI v6 事件参考 · 本地演示序列',
    sourceUrl: 'https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso/oni/v6/index.php',
    peak: '+2.4 ℃',
    phase: '1997 年下半年爆发，1998 年春季转入快速冷却',
    description: '1997-1998 事件暖异常集中在年末达到极值，随后快速转冷，可用于观察强厄尔尼诺到拉尼娜转换的时间差。',
    values: [-0.4, -0.3, 0.0, 0.3, 0.7, 1.1, 1.5, 1.8, 2.0, 2.2, 2.3, 2.4, 2.2, 1.9, 1.4, 1.0, 0.4, -0.1, -0.6, -0.8, -1.0, -1.1, -1.3, -1.5]
  },
  {
    id: '2015-16',
    label: '2015-2016 强厄尔尼诺',
    type: '强厄尔尼诺',
    start: '2015-01',
    source: 'NOAA CPC ONI v6 事件参考 · 本地演示序列',
    sourceUrl: 'https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso/oni/v6/index.php',
    peak: '+2.6 ℃',
    phase: '2015 年秋冬达到峰值，2016 年春夏快速衰减',
    description: '2015-2016 事件是平台论文图示的典型案例。移动到 2015 年 10 月，可以同步查看暖异常扩展、指数上升和当前阶段说明。',
    values: [0.7, 0.7, 0.7, 0.9, 1.0, 1.2, 1.4, 1.7, 2.0, 2.3, 2.5, 2.6, 2.5, 2.2, 1.7, 1.1, 0.6, 0.1, -0.2, -0.3, -0.4, -0.5, -0.5, -0.4]
  },
  {
    id: '2020-23',
    label: '2020-2023 持续性拉尼娜',
    type: '持续性拉尼娜',
    start: '2020-01',
    source: 'NOAA CPC ONI v6 事件参考 · 本地演示序列',
    sourceUrl: 'https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso/oni/v6/index.php',
    peak: '-1.1 ℃',
    phase: '2020 年下半年建立，冷位相持续至 2023 年初后转暖',
    description: '三年拉尼娜事件呈现冷异常反复维持、缓慢衰减和末期转暖的特征，适合查看持续性事件与短期强事件的差异。',
    values: [0.5, 0.3, 0.1, 0.0, -0.2, -0.4, -0.6, -0.7, -0.9, -1.0, -1.1, -1.0, -0.9, -0.7, -0.3, -0.1, -0.2, -0.3, -0.4, -0.5, -0.8, -1.0, -1.0, -1.0, -0.9, -0.8, -0.7, -0.5, -0.3, -0.2, -0.4, -0.7, -0.8, -0.9, -0.9, -0.8, -0.5, -0.2, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.3, 1.6, 1.8, 1.9]
  }
].map(fixture => {
  const series = makeSeries(fixture.start, fixture.values)
  return {
    ...fixture,
    hasData: true,
    peak: formatPeak(peakValue(series.map(point => point.value), fixture.type)),
    series
  }
})

// NOAA/CPC 常用的历史事件目录。目录项只提供导航元数据；只有上面的四个
// fixture 绑定了本地月度序列和空间场，避免把演示数据误用于其它事件。
const catalogEntries = [
  ['1950-51', '1950-1951 拉尼娜', '拉尼娜', '1950-01', '1951-12'],
  ['1951-52', '1951-1952 厄尔尼诺', '厄尔尼诺', '1951-01', '1952-12'],
  ['1953-54', '1953-1954 厄尔尼诺', '厄尔尼诺', '1953-01', '1954-12'],
  ['1954-56', '1954-1956 持续性拉尼娜', '持续性拉尼娜', '1954-01', '1956-12'],
  ['1957-58', '1957-1958 厄尔尼诺', '厄尔尼诺', '1957-01', '1958-12'],
  ['1963-64', '1963-1964 厄尔尼诺', '厄尔尼诺', '1963-01', '1964-12'],
  ['1964-65', '1964-1965 拉尼娜', '拉尼娜', '1964-01', '1965-12'],
  ['1965-66', '1965-1966 厄尔尼诺', '厄尔尼诺', '1965-01', '1966-12'],
  ['1967-68', '1967-1968 拉尼娜', '拉尼娜', '1967-01', '1968-12'],
  ['1968-69', '1968-1969 厄尔尼诺', '厄尔尼诺', '1968-01', '1969-12'],
  ['1969-70', '1969-1970 厄尔尼诺', '厄尔尼诺', '1969-01', '1970-12'],
  ['1970-72', '1970-1972 持续性拉尼娜', '持续性拉尼娜', '1970-01', '1972-12'],
  ['1972-73', '1972-1973 厄尔尼诺', '厄尔尼诺', '1972-01', '1973-12'],
  ['1973-76', '1973-1976 持续性拉尼娜', '持续性拉尼娜', '1973-01', '1976-12'],
  ['1976-77', '1976-1977 厄尔尼诺', '厄尔尼诺', '1976-01', '1977-12'],
  ['1977-78', '1977-1978 厄尔尼诺', '厄尔尼诺', '1977-01', '1978-12'],
  ['1979-80', '1979-1980 厄尔尼诺', '厄尔尼诺', '1979-01', '1980-12'],
  ['1982-83', '1982-1983 强厄尔尼诺', '强厄尔尼诺', '1982-01', '1983-12'],
  ['1984-85', '1984-1985 拉尼娜', '拉尼娜', '1984-01', '1985-12'],
  ['1986-87', '1986-1987 厄尔尼诺', '厄尔尼诺', '1986-01', '1987-12'],
  ['1987-88', '1987-1988 厄尔尼诺', '厄尔尼诺', '1987-01', '1988-12'],
  ['1988-89', '1988-1989 拉尼娜', '拉尼娜', '1988-01', '1989-12'],
  ['1991-92', '1991-1992 厄尔尼诺', '厄尔尼诺', '1991-01', '1992-12'],
  ['1994-95', '1994-1995 厄尔尼诺', '厄尔尼诺', '1994-01', '1995-12'],
  ['1995-96', '1995-1996 拉尼娜', '拉尼娜', '1995-01', '1996-12'],
  ['1997-98', '1997-1998 强厄尔尼诺', '强厄尔尼诺', '1997-01', '1998-12'],
  ['1998-01', '1998-2001 持续性拉尼娜', '持续性拉尼娜', '1998-01', '2001-12'],
  ['2002-03', '2002-2003 厄尔尼诺', '厄尔尼诺', '2002-01', '2003-12'],
  ['2004-05', '2004-2005 厄尔尼诺', '厄尔尼诺', '2004-01', '2005-12'],
  ['2006-07', '2006-2007 厄尔尼诺', '厄尔尼诺', '2006-01', '2007-12'],
  ['2007-08', '2007-2008 拉尼娜', '拉尼娜', '2007-01', '2008-12'],
  ['2009-10', '2009-2010 厄尔尼诺', '厄尔尼诺', '2009-01', '2010-12'],
  ['2010-12', '2010-2012 持续性拉尼娜', '持续性拉尼娜', '2010-01', '2012-12'],
  ['2014-16', '2014-2016 强厄尔尼诺', '强厄尔尼诺', '2014-01', '2016-12'],
  ['2015-16', '2015-2016 强厄尔尼诺', '强厄尔尼诺', '2015-01', '2016-12'],
  ['2016-17', '2016-2017 拉尼娜', '拉尼娜', '2016-01', '2017-12'],
  ['2017-18', '2017-2018 拉尼娜', '拉尼娜', '2017-01', '2018-12'],
  ['2018-19', '2018-2019 厄尔尼诺', '厄尔尼诺', '2018-01', '2019-12'],
  ['2020-23', '2020-2023 持续性拉尼娜', '持续性拉尼娜', '2020-01', '2023-12'],
  ['2023-24', '2023-2024 厄尔尼诺', '厄尔尼诺', '2023-01', '2024-12'],
  ['2024-25', '2024-2025 拉尼娜', '拉尼娜', '2024-01', '2025-12']
].map(([id, label, type, start, end]) => ({ id, label, type, start, end }))

const replayById = eventFixtures.reduce((result, event) => {
  result[event.id] = event
  return result
}, {})

const noDataEvent = entry => ({
  ...entry,
  hasData: false,
  source: 'NOAA CPC ONI v6 · 历史事件目录',
  sourceUrl: 'https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso/oni/v6/index.php',
  peak: '—',
  phase: '—',
  description: '该事件已列入 ENSO 历史事件目录，可在此选择并查看事件元数据。'
})

export const historyEvents = catalogEntries.map(entry => replayById[entry.id]
  ? { ...entry, ...replayById[entry.id] }
  : noDataEvent(entry))

const wrapLongitude = value => {
  let result = value % 360
  if (result < 0) result += 360
  return result
}

const longitudeDistance = (a, b) => {
  const distance = Math.abs(wrapLongitude(a) - wrapLongitude(b))
  return Math.min(distance, 360 - distance)
}

// 陆地判断：landMask.js 提供基于 Natural Earth 50m 海岸线的 1° 位掩膜。
// 历史版本用矩形框近似陆地，会把南/北大西洋整片海域误判成陆地，
// 导致回放海温在大西洋中部大面积缺失。

// A low-resolution, deterministic anomaly field keeps the replay responsive while
// preserving the spatial pattern of an equatorial Pacific ENSO signal.
export const makeAnomalyField = (index, seed = 0) => {
  const nx = 72
  const ny = 37
  const data = []
  const amplitude = Number.isFinite(Number(index)) ? Number(index) : 0
  // Keep the event amplitude visible while moving both the core and an
  // equatorial wave between frames. The stronger phase shift makes playback
  // readable even when adjacent index values are close together.
  const phase = Number(seed) * 0.62
  const coreCenter = 214 + 18 * Math.sin(phase * 0.7)
  const waveCenter = 158 + ((Number(seed) * 12) % 86)
  for (let yi = 0; yi < ny; yi += 1) {
    const latitude = 90 - yi * 5
    for (let xi = 0; xi < nx; xi += 1) {
      const longitude = xi * 5
      // 高纬度海洋不合成数据：真实 OISST 距平在高纬海冰区基本缺测，
      // 强行铺值会在北冰洋/白令海形成与底图海冰冲突的块状马赛克
      if (Math.abs(latitude) >= 66 || isLand(longitude, latitude)) {
        data.push(null)
        continue
      }
      const equatorialEnvelope = Math.exp(-Math.pow(latitude / 13, 2))
      const core = equatorialEnvelope * Math.exp(-Math.pow(longitudeDistance(longitude, coreCenter) / 58, 2))
      const westPacific = Math.exp(-Math.pow(latitude / 18, 2)) * Math.exp(-Math.pow(longitudeDistance(longitude, 155) / 42, 2))
      const propagation = equatorialEnvelope * Math.exp(-Math.pow(longitudeDistance(longitude, waveCenter) / 31, 2))
      const texture = 0.22 * Math.sin((longitude + Number(seed) * 24) * Math.PI / 34) * Math.cos(latitude * Math.PI / 28)
      const travelingWave = 0.44 * Math.sin(phase + longitude * Math.PI / 38) * propagation
      const coastalPulse = 0.16 * Math.cos(phase * 0.8 + longitude * Math.PI / 24) * equatorialEnvelope
      const value = amplitude * (0.94 * core - 0.26 * westPacific) + travelingWave + coastalPulse + texture + 0.05 * Math.sin(Number(seed) + latitude)
      data.push(Number(Math.max(-3, Math.min(3, value)).toFixed(3)))
    }
  }
  return {
    header: { lo1: 0, la1: 90, dx: 5, dy: 5, nx, ny, refTime: 'historical-demo', forecastTime: index, gridDefinitionTemplate: 0 },
    data
  }
}

export const historyEventById = historyEvents.reduce((result, event) => {
  result[event.id] = event
  return result
}, {})

export const modelStartOptions = demoCases.replayOptions.map(month => ({
  id: month,
  label: `${month.slice(0, 4)} 年 ${Number(month.slice(5, 7))} 月起报`,
  start: month
}))

export const modelReplay = start => {
  const normalizedStart = modelStartOptions.some(option => option.id === start) ? start : '2025-01'
  const replay = demoCases.replay(normalizedStart)
  const series = replay.series.map((point, index) => ({
    month: point.month,
    value: point.value === null || point.value === undefined || point.value === ''
      ? null
      : (Number.isFinite(Number(point.value)) ? Number(Number(point.value).toFixed(2)) : null),
    observed: Number.isFinite(point.observedNino34) ? point.observedNino34 : null,
    leadMonth: index + 1,
    lower: point.lower,
    upper: point.upper
  }))
  return {
    id: normalizedStart,
    label: `${normalizedStart.slice(0, 4)} 年 ${Number(normalizedStart.slice(5, 7))} 月起报`,
    type: 'LSTA-Swin 历史预测',
    hasData: true,
    start: normalizedStart,
    source: 'AIMB LSTA-Swin 历史预测演示序列 · 2025–2026 起报样本',
    peak: formatPeak(peakValue(series.map(point => point.value))),
    phase: '起报月份后的 20 个月预测轨迹',
    description: '该历史预测序列用于展示起报月份、预测指数与空间异常场的联动方式。',
    series
  }
}

export const fieldForPoint = (point, mode, index) => {
  const field = makeAnomalyField(point.value, mode === 'event' ? index : index + 30)
  field.header.forecastTime = mode === 'event' ? index : index + 30
  return field
}

