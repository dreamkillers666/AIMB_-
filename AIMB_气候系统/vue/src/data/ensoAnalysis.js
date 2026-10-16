// ENSO 地图分析共享模块：Niño 分区定义与 marching squares 等值线提取，
// 供 2D 地图（EnsoMapPanel）与 3D 地球（EnsoGlobe）共用。

// A6 · Niño 指数分区定义（经度东正；west > east 表示跨 180° 日界线）
export const NINO_REGIONS = {
  nino12: { key: 'nino12', label: 'Niño1+2', desc: '南美沿岸', range: '0–10°S · 90°W–80°W', south: -10, north: 0, west: -90, east: -80, glowLon: -85, glowLat: -5, glowScale: 0.7 },
  nino3: { key: 'nino3', label: 'Niño3', desc: '赤道东太平洋', range: '5°N–5°S · 150°W–90°W', south: -5, north: 5, west: -150, east: -90, glowLon: -120, glowLat: 0, glowScale: 0.85 },
  nino34: { key: 'nino34', label: 'Niño3.4', desc: '赤道中东部太平洋', range: '5°N–5°S · 170°W–120°W', south: -5, north: 5, west: -170, east: -120, glowLon: -145, glowLat: 0, glowScale: 1 },
  nino4: { key: 'nino4', label: 'Niño4', desc: '赤道西太平洋', range: '5°N–5°S · 160°E–150°W', south: -5, north: 5, west: 160, east: -150, glowLon: 175, glowLat: 0, glowScale: 0.85 }
}

// A5 · marching squares：从格点场提取 iso 等值线段（格点坐标，浮点）
export const marchingSquares = (values, nx, ny, iso) => {
  const segments = []
  if (!values || nx < 2 || ny < 2) return segments
  const lerp = (a, b) => (iso - a) / (b - a)
  for (let j = 0; j < ny - 1; j += 1) {
    for (let i = 0; i < nx - 1; i += 1) {
      const tl = values[j * nx + i]
      const tr = values[j * nx + i + 1]
      const br = values[(j + 1) * nx + i + 1]
      const bl = values[(j + 1) * nx + i]
      if (![tl, tr, br, bl].every(Number.isFinite)) continue
      let code = 0
      if (tl > iso) code |= 8
      if (tr > iso) code |= 4
      if (br > iso) code |= 2
      if (bl > iso) code |= 1
      if (code === 0 || code === 15) continue
      const top = [i + lerp(tl, tr), j]
      const right = [i + 1, j + lerp(tr, br)]
      const bottom = [i + lerp(bl, br), j + 1]
      const left = [i, j + lerp(tl, bl)]
      const table = {
        1: [left, bottom], 2: [bottom, right], 3: [left, right], 4: [top, right],
        5: [left, top, bottom, right], 6: [top, bottom], 7: [left, top],
        8: [left, top], 9: [top, bottom], 10: [left, bottom, top, right],
        11: [top, right], 12: [left, right], 13: [bottom, right], 14: [left, bottom]
      }
      const points = table[code]
      if (!points) continue
      if (points.length === 4) {
        segments.push([points[0], points[1]])
        segments.push([points[2], points[3]])
      } else {
        segments.push([points[0], points[1]])
      }
    }
  }
  return segments
}
