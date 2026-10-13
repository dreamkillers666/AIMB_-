const SST_MIN = -2
const SST_MAX = 32
const ANOMALY_MIN = -3
const ANOMALY_MAX = 3

export const SST_COLOR_STOPS = [
  { value: -2, rgb: [15, 45, 120] },
  { value: 4, rgb: [30, 90, 170] },
  { value: 10, rgb: [40, 140, 190] },
  { value: 16, rgb: [45, 180, 170] },
  { value: 20, rgb: [110, 200, 120] },
  { value: 24, rgb: [220, 210, 90] },
  { value: 28, rgb: [235, 150, 60] },
  { value: 32, rgb: [210, 60, 40] }
]

export const SST_COLOR_GRADIENT = 'linear-gradient(90deg, #0f2d78 0%, #1e5aaa 17.65%, #288cbe 35.3%, #2db4aa 52.95%, #6ec878 64.7%, #dcd25a 76.5%, #eb963c 88.2%, #d23c28 100%)'

export const ensoFieldColor = (value, isAnomaly = false) => {
  const sourceMin = isAnomaly ? ANOMALY_MIN : SST_MIN
  const sourceMax = isAnomaly ? ANOMALY_MAX : SST_MAX
  const bounded = Math.max(sourceMin, Math.min(sourceMax, Number(value)))
  const scaleValue = SST_MIN + ((bounded - sourceMin) / (sourceMax - sourceMin)) * (SST_MAX - SST_MIN)
  const upperIndex = SST_COLOR_STOPS.findIndex(stop => stop.value >= scaleValue)

  if (upperIndex <= 0) return SST_COLOR_STOPS[0].rgb
  const lower = SST_COLOR_STOPS[upperIndex - 1]
  const upper = SST_COLOR_STOPS[upperIndex]
  const ratio = (scaleValue - lower.value) / (upper.value - lower.value)
  return lower.rgb.map((channel, index) => Math.round(channel + (upper.rgb[index] - channel) * ratio))
}
