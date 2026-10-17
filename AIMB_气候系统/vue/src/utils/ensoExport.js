// B23 · 数据与图表导出中心
// 纯工具模块：payload 由 forecastenso.vue 组装，这里只负责简报排版合成与文件生成，
// 不依赖任何 Vue 组件。所有导出步骤的异常由调用方捕获并转为进度条上的失败状态。

export function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('图片资源加载失败'))
    img.src = src
  })
}

export function downloadText(text, filename, mime) {
  try {
    const blob = new Blob([text], { type: mime })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.setTimeout(() => URL.revokeObjectURL(url), 2000)
    return true
  } catch (error) {
    return false
  }
}

export function downloadDataUrl(dataUrl, filename) {
  const link = document.createElement('a')
  link.href = dataUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

export function timestampedName(prefix, ext) {
  const now = new Date()
  const pad = value => String(value).padStart(2, '0')
  return `${prefix}_${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}_${pad(now.getHours())}-${pad(now.getMinutes())}${ext}`
}

const FONT = '"PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif'

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.arcTo(x + w, y, x + w, y + h, r)
  ctx.arcTo(x + w, y + h, x, y + h, r)
  ctx.arcTo(x, y + h, x, y, r)
  ctx.arcTo(x, y, x + w, y, r)
  ctx.closePath()
}

function panel(ctx, x, y, w, h) {
  ctx.fillStyle = 'rgba(7, 31, 48, 0.94)'
  roundRect(ctx, x, y, w, h, 10)
  ctx.fill()
  ctx.strokeStyle = 'rgba(96, 214, 226, 0.35)'
  ctx.lineWidth = 1
  ctx.stroke()
}

function caption(ctx, text, x, y) {
  ctx.font = `11px ${FONT}`
  const w = ctx.measureText(text).width + 18
  ctx.fillStyle = 'rgba(3, 20, 34, 0.85)'
  roundRect(ctx, x, y, w, 22, 4)
  ctx.fill()
  ctx.fillStyle = '#9fd8e2'
  ctx.fillText(text, x + 9, y + 15)
}

function drawCover(ctx, img, x, y, w, h) {
  const iw = img.naturalWidth || img.width
  const ih = img.naturalHeight || img.height
  const scale = Math.max(w / iw, h / ih)
  const dw = iw * scale
  const dh = ih * scale
  ctx.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh)
}

function drawContain(ctx, img, x, y, w, h) {
  const iw = img.naturalWidth || img.width
  const ih = img.naturalHeight || img.height
  const scale = Math.min(w / iw, h / ih)
  const dw = iw * scale
  const dh = ih * scale
  ctx.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh)
}

function placeholder(ctx, x, y, w, h, text) {
  ctx.fillStyle = 'rgba(6, 30, 47, 0.72)'
  roundRect(ctx, x, y, w, h, 8)
  ctx.fill()
  ctx.fillStyle = '#6f8c9d'
  ctx.font = `12px ${FONT}`
  ctx.textAlign = 'center'
  ctx.fillText(text, x + w / 2, y + h / 2 + 4)
  ctx.textAlign = 'left'
}

// 纬向剖面小图：离屏 canvas，PNG 合成与打印页共用同一份渲染
export function renderProfileChart(points, isAnomaly, width = 472, height = 116) {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = 'rgba(6, 30, 47, 0.65)'
  roundRect(ctx, 0, 0, width, height, 8)
  ctx.fill()
  const valid = (points || []).filter(point => Number.isFinite(point.value))
  if (valid.length < 2) {
    ctx.fillStyle = '#6f8c9d'
    ctx.font = `12px ${FONT}`
    ctx.textAlign = 'center'
    ctx.fillText('纬向剖面未开启（工具栏「剖面」）', width / 2, height / 2 + 4)
    ctx.textAlign = 'left'
    return canvas
  }
  const left = 46
  const right = width - 16
  const top = 14
  const bottom = height - 24
  const lonMin = valid[0].lon
  const lonMax = valid[valid.length - 1].lon
  let min = Infinity
  let max = -Infinity
  valid.forEach(point => {
    min = Math.min(min, point.value)
    max = Math.max(max, point.value)
  })
  const rawMax = max
  const rawMin = min
  if (isAnomaly) { min = Math.min(min, -0.5); max = Math.max(max, 0.5) }
  const span = Math.max(0.4, max - min)
  min -= span * 0.15
  max += span * 0.15
  const px = lon => left + (lon - lonMin) / Math.max(1e-6, lonMax - lonMin) * (right - left)
  const py = value => bottom - (value - min) / (max - min) * (bottom - top)
  if (min < 0 && max > 0) {
    ctx.strokeStyle = 'rgba(140, 178, 196, 0.45)'
    ctx.setLineDash([5, 4])
    ctx.beginPath()
    ctx.moveTo(left, py(0))
    ctx.lineTo(right, py(0))
    ctx.stroke()
    ctx.setLineDash([])
  }
  ctx.strokeStyle = '#ff8f6b'
  ctx.lineWidth = 1.8
  ctx.beginPath()
  let pen = false
  ;(points || []).forEach(point => {
    if (!Number.isFinite(point.value)) { pen = false; return }
    const x = px(point.lon)
    const y = py(point.value)
    if (!pen) { ctx.moveTo(x, y); pen = true } else ctx.lineTo(x, y)
  })
  ctx.stroke()
  ctx.fillStyle = '#ffd9ac'
  ctx.font = `10px ${FONT}`
  ctx.textAlign = 'center'
  ctx.fillText(`峰值 ${rawMax > 0 ? '+' : ''}${rawMax.toFixed(1)}℃`, px(valid.reduce((a, b) => (b.value > a.value ? b : a)).lon), 12)
  ctx.fillStyle = '#7c99aa'
  const fmtLon = value => {
    const v = ((value % 360) + 360) % 360
    return v <= 180 ? `${Math.round(v)}°E` : `${Math.round(360 - v)}°W`
  }
  ;[lonMin, (lonMin + lonMax) / 2, lonMax].forEach(lon => {
    ctx.fillText(fmtLon(lon), Math.max(20, Math.min(width - 20, px(lon))), height - 6)
  })
  ctx.textAlign = 'left'
  return canvas
}

// 监测简报 PNG：1240×924 深色排版，图表以 getDataURL 位图嵌入，样式与页面一致
export async function composeBriefingCanvas(payload) {
  const W = 1240
  const H = 924
  const canvas = document.createElement('canvas')
  canvas.width = W
  canvas.height = H
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#041522'
  ctx.fillRect(0, 0, W, H)
  ctx.strokeStyle = 'rgba(96, 214, 226, 0.55)'
  ctx.beginPath()
  ctx.moveTo(0, 0.5)
  ctx.lineTo(W, 0.5)
  ctx.stroke()

  // 头部
  ctx.fillStyle = '#58c6d2'
  ctx.font = `10px ${FONT}`
  ctx.fillText('A I M B  ·  C L I M A T E  I N T E L L I G E N C E', 40, 42)
  ctx.fillStyle = '#F0FBFF'
  ctx.font = `600 24px ${FONT}`
  ctx.fillText('ENSO 监测简报', 40, 74)
  ctx.textAlign = 'right'
  ctx.fillStyle = '#819BAD'
  ctx.font = `12px ${FONT}`
  ctx.fillText(`生成时间 ${payload.generatedAt}`, W - 40, 44)
  ctx.font = `12px ${FONT}`
  const chipText = `当前数据 · ${payload.modeText}`
  const chipW = ctx.measureText(chipText).width + 34
  ctx.fillStyle = payload.accentFill
  roundRect(ctx, W - 40 - chipW, 54, chipW, 26, 13)
  ctx.fill()
  ctx.strokeStyle = payload.accent
  ctx.lineWidth = 1
  ctx.stroke()
  ctx.fillStyle = payload.accentBright
  ctx.fillText(chipText, W - 40 - chipW / 2, 71)
  ctx.textAlign = 'left'
  ctx.strokeStyle = 'rgba(61, 144, 172, 0.45)'
  ctx.beginPath()
  ctx.moveTo(40, 94)
  ctx.lineTo(W - 40, 94)
  ctx.stroke()

  // 地图快照
  panel(ctx, 40, 106, 660, 368)
  ctx.save()
  roundRect(ctx, 41, 107, 658, 366, 9)
  ctx.clip()
  if (payload.mapImage) {
    const img = await loadImage(payload.mapImage)
    drawCover(ctx, img, 41, 107, 658, 366)
  } else {
    placeholder(ctx, 41, 107, 658, 366, '地图快照不可用')
  }
  ctx.restore()
  caption(ctx, payload.mapCaption, 52, 116)

  // 右栏：关键指标 + 回放对象 + 钉子
  panel(ctx, 720, 106, 480, 368)
  ctx.fillStyle = '#8fd7e0'
  ctx.font = `10px ${FONT}`
  ctx.fillText('关 键 指 标', 738, 130)
  const cells = [[738, 140], [962, 140], [738, 214], [962, 214]]
  payload.kpi.forEach((item, index) => {
    const cell = cells[index]
    if (!cell) return
    ctx.fillStyle = '#7c99aa'
    ctx.font = `10px ${FONT}`
    ctx.fillText(item.name, cell[0], cell[1] + 10)
    ctx.fillStyle = item.color || '#E3F5FB'
    ctx.font = `${item.small ? '600 16px' : '600 22px'} ${FONT}`
    ctx.fillText(item.value, cell[0], cell[1] + (item.small ? 34 : 38))
  })
  if (payload.alert) {
    ctx.fillStyle = payload.alert.tone === 'cool' ? '#bcd9ff' : '#ffd0a2'
    ctx.font = `600 12px ${FONT}`
    ctx.fillText(`${payload.alert.title} · ${payload.alert.value}`, 738, 306)
  } else {
    ctx.fillStyle = '#6f8c9d'
    ctx.font = `11px ${FONT}`
    ctx.fillText('Niño3.4 未越过 ±0.5℃ 阈值 · 无告警', 738, 306)
  }
  ctx.strokeStyle = 'rgba(67, 135, 160, 0.25)'
  ctx.beginPath()
  ctx.moveTo(738, 324)
  ctx.lineTo(1182, 324)
  ctx.stroke()
  ctx.fillStyle = '#cfe6ee'
  ctx.font = `11px ${FONT}`
  ctx.fillText(payload.scopeLabel, 738, 344)
  ctx.fillStyle = '#7c99aa'
  ctx.font = `10px ${FONT}`
  ctx.fillText(payload.regionLabel, 738, 362)
  ctx.fillText(payload.meta.source, 738, 378)
  ctx.fillText(`${payload.meta.colorScale} · ${payload.meta.baseline}`, 738, 394)
  ctx.fillStyle = '#8fd7e0'
  ctx.font = `10px ${FONT}`
  ctx.fillText('点 位 时 序 探 针', 738, 416)
  if (payload.pins.length) {
    payload.pins.slice(0, 4).forEach((pin, index) => {
      const y = 432 + index * 14
      ctx.fillStyle = '#ffd694'
      ctx.font = `600 10px ${FONT}`
      ctx.fillText(String(pin.index), 738, y)
      ctx.fillStyle = '#cfe6ee'
      ctx.font = `10px ${FONT}`
      ctx.fillText(`${pin.coord} — ${pin.currentText}`, 754, y)
    })
  } else {
    ctx.fillStyle = '#6f8c9d'
    ctx.font = `10px ${FONT}`
    ctx.fillText('未落钉（地图 / 地球上单击可添加，最多 4 个）', 738, 432)
  }

  // 图表区
  panel(ctx, 40, 494, 660, 300)
  caption(ctx, 'Niño3.4 指数演变趋势（观测 / 预测）', 52, 504)
  ctx.save()
  roundRect(ctx, 41, 495, 658, 298, 9)
  ctx.clip()
  if (payload.indexImage) {
    const img = await loadImage(payload.indexImage)
    drawContain(ctx, img, 44, 520, 652, 266)
  } else {
    placeholder(ctx, 44, 520, 652, 266, '指数图表未就绪')
  }
  ctx.restore()
  panel(ctx, 720, 494, 480, 142)
  caption(ctx, '未来 ENSO 状态概率（%）', 732, 504)
  ctx.save()
  roundRect(ctx, 721, 495, 478, 140, 9)
  ctx.clip()
  if (payload.probImage) {
    const img = await loadImage(payload.probImage)
    drawContain(ctx, img, 724, 518, 472, 114)
  } else {
    placeholder(ctx, 724, 518, 472, 114, '概率图表未就绪')
  }
  ctx.restore()
  panel(ctx, 720, 648, 480, 146)
  caption(ctx, payload.profileCaption, 732, 658)
  ctx.save()
  roundRect(ctx, 721, 649, 478, 144, 9)
  ctx.clip()
  if (payload.profileImage) {
    const img = await loadImage(payload.profileImage)
    drawContain(ctx, img, 724, 672, 472, 118)
  } else {
    placeholder(ctx, 724, 672, 472, 118, '剖面数据未就绪')
  }
  ctx.restore()

  // 页脚
  ctx.strokeStyle = 'rgba(61, 144, 172, 0.35)'
  ctx.beginPath()
  ctx.moveTo(40, 820)
  ctx.lineTo(W - 40, 820)
  ctx.stroke()
  ctx.fillStyle = '#7c99aa'
  ctx.font = `11px ${FONT}`
  ctx.fillText(`数据来源：${payload.meta.source} · ${payload.meta.colorScale} · ${payload.meta.baseline}`, 40, 846)
  if (payload.footerNote) {
    ctx.fillStyle = '#c9a86a'
    ctx.fillText(payload.footerNote, 40, 868)
  }
  ctx.textAlign = 'right'
  ctx.fillStyle = '#58c6d2'
  ctx.font = `10px ${FONT}`
  ctx.fillText('AIMB 气候系统 · ENSO 预测分析与监测平台', W - 40, 846)
  if (payload.cursorText) {
    ctx.fillStyle = '#7c99aa'
    ctx.font = `10px ${FONT}`
    ctx.fillText(payload.cursorText, W - 40, 868)
  }
  ctx.textAlign = 'left'
  return canvas
}

// 监测简报打印页：PDF 导出走浏览器「打印 → 另存为 PDF」，排版与 PNG 同构
export function buildBriefingHtml(payload) {
  const esc = value => String(value == null ? '' : value)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const imgTag = src => (src ? `<img src="${src}" alt="">` : `<div class="ph">暂无快照</div>`)
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title>ENSO 监测简报</title>
<style>
  @page { size: A4 landscape; margin: 8mm; }
  body { margin: 0; background: #041522; color: #e3f5fb; font-family: "PingFang SC", "Microsoft YaHei", sans-serif; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
  .page { width: 1240px; margin: 0 auto; padding: 24px 40px; box-sizing: border-box; }
  .kicker { color: #58c6d2; font-size: 10px; letter-spacing: 2px; }
  .head { display: flex; align-items: flex-end; justify-content: space-between; border-bottom: 1px solid rgba(61,144,172,.45); padding-bottom: 12px; }
  .head h1 { margin: 6px 0 0; font-size: 24px; font-weight: 600; }
  .head .meta { text-align: right; color: #819bad; font-size: 12px; }
  .chip { display: inline-block; margin-top: 8px; padding: 4px 14px; border: 1px solid ${payload.accent}; border-radius: 13px; background: ${payload.accentFill}; color: ${payload.accentBright}; font-size: 12px; }
  .row { display: flex; gap: 20px; margin-top: 12px; }
  .panel { background: rgba(7,31,48,.94); border: 1px solid rgba(96,214,226,.35); border-radius: 10px; padding: 10px; box-sizing: border-box; }
  .panel .cap { color: #9fd8e2; font-size: 11px; background: rgba(3,20,34,.85); display: inline-block; padding: 3px 9px; border-radius: 4px; margin-bottom: 6px; }
  .map { flex: 0 0 660px; } .map img, .map .ph { width: 640px; height: 356px; object-fit: cover; border-radius: 8px; display: block; }
  .side { flex: 1; }
  .kpis { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 6px; }
  .kpi { padding: 8px 10px; border: 1px solid rgba(74,140,166,.26); border-radius: 6px; background: rgba(6,30,47,.6); }
  .kpi span { display: block; color: #7c99aa; font-size: 10px; }
  .kpi b { display: block; margin-top: 4px; font-size: 20px; font-weight: 600; }
  .alert { margin-top: 8px; font-size: 12px; color: ${payload.alert && payload.alert.tone === 'cool' ? '#bcd9ff' : '#ffd0a2'}; }
  .scope { margin-top: 8px; font-size: 11px; color: #cfe6ee; }
  .meta-line { font-size: 10px; color: #7c99aa; margin-top: 3px; }
  .pins { margin-top: 8px; font-size: 10px; color: #cfe6ee; }
  .pins div { margin-top: 3px; }
  .charts { margin-top: 12px; }
  .charts .panel img, .charts .panel .ph { width: 100%; border-radius: 8px; display: block; }
  .footer { margin-top: 12px; border-top: 1px solid rgba(61,144,172,.35); padding-top: 10px; font-size: 11px; color: #7c99aa; display: flex; justify-content: space-between; }
  .ph { display: grid; place-items: center; color: #6f8c9d; background: rgba(6,30,47,.72); }
</style>
</head>
<body>
<div class="page">
  <div class="head">
    <div><div class="kicker">AIMB · CLIMATE INTELLIGENCE</div><h1>ENSO 监测简报</h1></div>
    <div class="meta"><div>生成时间 ${esc(payload.generatedAt)}</div><div class="chip">当前数据 · ${esc(payload.modeText)}</div></div>
  </div>
  <div class="row">
    <div class="panel map"><span class="cap">${esc(payload.mapCaption)}</span>${imgTag(payload.mapImage)}</div>
    <div class="panel side">
      <span class="cap">关键指标</span>
      <div class="kpis">${payload.kpi.map(item => `<div class="kpi"><span>${esc(item.name)}</span><b style="color:${item.color || '#e3f5fb'}">${esc(item.value)}</b></div>`).join('')}</div>
      <div class="alert">${payload.alert ? `${esc(payload.alert.title)} · ${esc(payload.alert.value)}` : 'Niño3.4 未越过 ±0.5℃ 阈值 · 无告警'}</div>
      <div class="scope">${esc(payload.scopeLabel)}</div>
      <div class="meta-line">${esc(payload.regionLabel)}</div>
      <div class="meta-line">数据来源：${esc(payload.meta.source)}</div>
      <div class="meta-line">${esc(payload.meta.colorScale)} · ${esc(payload.meta.baseline)}</div>
      <div class="pins"><b>点位时序探针</b>${payload.pins.length ? payload.pins.map(pin => `<div>${esc(String(pin.index))} · ${esc(pin.coord)} — ${esc(pin.currentText)}</div>`).join('') : '<div>未落钉</div>'}</div>
    </div>
  </div>
  <div class="charts">
    <div class="row">
      <div class="panel" style="flex:0 0 660px"><span class="cap">Niño3.4 指数演变趋势（观测 / 预测）</span>${imgTag(payload.indexImage)}</div>
      <div class="panel" style="flex:1"><span class="cap">未来 ENSO 状态概率（%）</span>${imgTag(payload.probImage)}</div>
    </div>
    <div class="row"><div class="panel" style="flex:1"><span class="cap">${esc(payload.profileCaption)}</span>${imgTag(payload.profileImage)}</div></div>
  </div>
  <div class="footer">
    <div>数据来源：${esc(payload.meta.source)} · ${esc(payload.meta.colorScale)} · ${esc(payload.meta.baseline)}${payload.footerNote ? ' · ' + esc(payload.footerNote) : ''}</div>
    <div>AIMB 气候系统 · ENSO 预测分析与监测平台${payload.cursorText ? ' · ' + esc(payload.cursorText) : ''}</div>
  </div>
</div>
</body>
</html>`
}

export function buildSeriesCsv(rows) {
  const table = [['类别', '时间 / 时段', '数值', '备注'], ...(rows || [])]
  return '\ufeff' + table
    .map(row => row.map(cell => `"${String(cell == null ? '' : cell).replace(/"/g, '""')}"`).join(','))
    .join('\r\n')
}

export function buildDataJson(payload) {
  return JSON.stringify(payload, null, 2)
}
