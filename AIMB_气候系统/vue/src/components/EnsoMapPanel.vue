<template>
  <section class="enso-map-panel" :class="{ 'is-compact': compact, 'is-maximized': maximized }">
    <EnsoGlobe
      v-if="displayMode === '3d'"
      :wind-data="windData"
      :sst-data="sstData"
      :sst-mode="sstMode"
      :layer="currentLayer"
      :region-suppressed="hasHistoryField"
      layout="center"
      @unavailable="handleGlobeUnavailable"
    />
    <div v-show="displayMode === '2d'" ref="mapEl" class="map-2d"></div>
    <div class="map-vignette"></div>

    <!-- A11 · HUD 装饰层：四角取景框 + 缓慢下移的扫描光带 -->
    <div class="map-hud" aria-hidden="true">
      <span class="hud-corner hud-tl"></span>
      <span class="hud-corner hud-tr"></span>
      <span class="hud-corner hud-bl"></span>
      <span class="hud-corner hud-br"></span>
      <span class="hud-scanline"></span>
    </div>

    <!-- A1 · 十字探针：跟随鼠标的经纬十字线与实时格点读数 -->
    <div v-if="displayMode === '2d' && probe.visible" class="map-crosshair" aria-hidden="true">
      <span class="crosshair-line crosshair-x" :style="{ top: probe.y + 'px' }"></span>
      <span class="crosshair-line crosshair-y" :style="{ left: probe.x + 'px' }"></span>
    </div>
    <div
      v-if="displayMode === '2d' && probe.visible"
      class="map-probe"
      :class="{ flipped: probe.flip }"
      :style="{ left: probe.x + 'px', top: probe.y + 'px' }"
    >
      <div class="probe-head">
        <span class="probe-coord">{{ probe.coord }}</span>
        <span class="probe-mode">{{ sstMode === 'anomaly' ? '距平场' : '绝对场' }}</span>
      </div>
      <div class="probe-grid">
        <span>{{ sstMode === 'anomaly' ? 'SSTA' : 'SST' }}</span>
        <strong :class="probeValueClass">{{ probe.sstText }}</strong>
        <span>风速</span>
        <strong>{{ probe.windText }}</strong>
        <span>风向</span>
        <strong>{{ probe.dirText }}</strong>
      </div>
    </div>

    <!-- A2 · 点位时序探针：点击落钉，随回放累积该点曲线 -->
    <div v-if="displayMode === '2d' && pins.length" class="probe-pins" aria-label="点位时序探针">
      <div class="probe-pins-head">
        <span><i></i>点位时序探针</span>
        <button type="button" @click="clearPins">清除</button>
      </div>
      <ul>
        <li v-for="pin in pins" :key="pin.id">
          <span class="pin-index">{{ pin.index }}</span>
          <div class="pin-body">
            <div class="pin-meta">
              <b>{{ pin.coord }}</b>
              <em :class="pinValueClass(pin)">{{ pin.currentText }}</em>
            </div>
            <svg class="pin-spark" viewBox="0 0 120 30" preserveAspectRatio="none">
              <path class="pin-zero" d="M0 15 H120"></path>
              <path class="pin-line" :d="pin.path"></path>
            </svg>
            <small>{{ pin.series.length }} 帧 · {{ pin.range }}</small>
          </div>
        </li>
      </ul>
    </div>

    <!-- A14 · 内嵌回放控制条：与页面底部时间轴同源，播放控制上移到地图内 -->
    <div v-if="replayPeriods.length > 1" class="map-replay" role="group" aria-label="地图回放控制">
      <button type="button" aria-label="上一帧" @click="$emit('replay-step', -1)"><i class="el-icon-arrow-left"></i></button>
      <button type="button" class="replay-main" :aria-label="replayPlaying ? '暂停演变' : '播放演变'" @click="$emit('replay-toggle')"><i :class="replayPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i></button>
      <button type="button" aria-label="下一帧" @click="$emit('replay-step', 1)"><i class="el-icon-arrow-right"></i></button>
      <div class="replay-progress"><i :style="{ width: replayProgress + '%' }"></i></div>
      <span class="replay-readout">{{ replayPeriods[replayIndex] || '—' }} · {{ replayIndex + 1 }}/{{ replayPeriods.length }}</span>
      <button type="button" class="replay-speed" @click="cycleSpeed">{{ speedLabel }}</button>
    </div>

    <div class="map-toolbar map-toolbar-left" role="tablist" aria-label="地图显示模式">
      <button type="button" role="tab" :aria-selected="displayMode === '3d'" :class="{ active: displayMode === '3d' }" @click="switchDisplayMode('3d')">3D 地球</button>
      <button type="button" role="tab" :aria-selected="displayMode === '2d'" :class="{ active: displayMode === '2d' }" @click="switchDisplayMode('2d')">2D 地图</button>
    </div>

    <div class="map-toolbar map-toolbar-right" role="group" aria-label="地图图层">
      <button type="button" :class="{ active: currentLayer === 'wind' }" @click="switchLayer('wind')"><i class="el-icon-sort"></i>风场</button>
      <button type="button" :disabled="!hasSst" :class="{ active: currentLayer === 'sst' }" @click="switchLayer('sst')"><i class="el-icon-sunny"></i>海温</button>
      <button type="button" :disabled="!hasSst" :class="{ active: currentLayer === 'both' }" @click="switchLayer('both')"><i class="el-icon-copy-document"></i>叠加</button>
      <button type="button" class="maximize-button" :aria-label="maximized ? '还原地图大小' : '放大地图'" :title="maximized ? '还原地图大小' : '放大地图'" @click="$emit('toggle-maximize')"><i :class="maximized ? 'el-icon-close' : 'el-icon-full-screen'"></i><span>{{ maximized ? '还原' : '放大' }}</span></button>
    </div>

    <div v-if="(currentLayer === 'sst' || currentLayer === 'both') && hasSst" class="sst-legend" :aria-label="sstMode === 'anomaly' ? '海温距平图例' : '海表温度图例'">
      <div class="sst-legend-bar" :style="{ background: colorGradient }"></div><div class="sst-legend-labels"><span>{{ sstMode === 'anomaly' ? '−3' : '−2' }}</span><span>{{ sstMode === 'anomaly' ? '−1.5' : '7' }}</span><span>{{ sstMode === 'anomaly' ? '0' : '15' }}</span><span>{{ sstMode === 'anomaly' ? '+1.5' : '24' }}</span><span>{{ sstMode === 'anomaly' ? '+3 ℃' : '32°C' }}</span></div>
    </div>
    <div class="data-badge" :class="{ 'is-live': windStatus.source === 'real' }"><i></i>{{ windBadgeText }}</div>
  </section>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import EnsoGlobe from './EnsoGlobe.vue'
import presetSst from '../data/presetSst.json'
import { ensoFieldColor, SST_COLOR_GRADIENT } from '../data/ensoColorScale'
import blueMarbleUrl from '../assets/earth-blue-marble.jpg'
import waterMaskUrl from '../assets/earth-water.png'

if (!window.L) window.L = L
require('leaflet-velocity')

// A2 · 钉子位置跨地图实例保留：切换实时/历史模式时 EnsoMapPanel 会因 :key
// 变化而重建，这里用模块级缓存把钉子位置带回去（时序曲线重新累积）
let pinLocationCache = []

// Reuse the hero's smoothed raster overlay implementation: bilinear sampling,
// cached canvas rendering and low-cost panning between zoom events.
const SstOverlayLayer = L.Layer.extend({
  initialize(options) {
    L.setOptions(this, options);
    this._data = null;
    this._lut = null;
    this._offscreen = null;
    this._renderPanePos = null;
    this._renderZoom = null;
    this._zoomAnim = null;
    this._zoomRaf = null;
    this._redrawRaf = null;
    this._imgData = null;
    // 海温色块透明度：海温模式 0.65；叠加模式降为 0.42 作"海洋底色"
    this._opacity = 0.65;
    this._anomaly = false;
    // 海/陆掩膜（earth-water.png：白=海洋、黑=陆地），陆地处海温完全透明
    this._mask = null;
    this._maskPromise = this._loadMask().then((mask) => {
      // 掩膜晚于首帧到达时重绘一次，补上陆地裁切
      if (mask && this._map) this._reset();
      return mask;
    });
  },

  // 加载水体掩膜到灰度缓存；海岸线 40~140 灰度 smoothstep 柔边（与 3D 球一致）
  _loadMask() {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        const w = img.naturalWidth;
        const h = img.naturalHeight;
        const c = document.createElement('canvas');
        c.width = w;
        c.height = h;
        const ctx = c.getContext('2d');
        ctx.drawImage(img, 0, 0);
        const d = ctx.getImageData(0, 0, w, h).data;
        const gray = new Uint8Array(w * h);
        for (let i = 0; i < w * h; i++) gray[i] = d[i * 4];
        this._mask = { w, h, data: gray };
        resolve(this._mask);
      };
      img.onerror = () => {
        this._mask = null;
        resolve(null);
      };
      img.src = waterMaskUrl;
    });
  },

  onAdd(map) {
    this._map = map;
    const pane = map.getPane('overlayPane');
    const canvas = L.DomUtil.create('canvas', 'sst-overlay-canvas');
    canvas.style.position = 'absolute';
    canvas.style.top = '0';
    canvas.style.left = '0';
    canvas.style.pointerEvents = 'none';
    canvas.style.display = 'none';
    // 色块在粒子层下方（叠加模式时粒子在上层），z-index 低于 velocity canvas
    canvas.style.zIndex = '1';
    pane.appendChild(canvas);
    this._canvas = canvas;
    this._ctx = canvas.getContext('2d');
    // 拖拽中：只更新定位 + 平移离屏缓存（廉价，跟手）
    map.on('move', this._onMove, this);
    // 缩放动画中：逐帧按当前 zoom 重渲染（rAF 合并，避免旧缩放级别拉伸变糊）
    map.on('zoomanim', this._onZoomAnim, this);
    map.on('zoom', this._scheduleRedraw, this);
    // 结束后：重定位 + 重渲染离屏 + 重绘
    map.on('moveend zoomend resize', this._reset, this);
    this._reset();
  },

  onRemove(map) {
    map.off('move', this._onMove, this);
    map.off('zoomanim', this._onZoomAnim, this);
    map.off('zoom', this._scheduleRedraw, this);
    map.off('moveend zoomend resize', this._reset, this);
    if (this._zoomRaf) cancelAnimationFrame(this._zoomRaf);
    if (this._redrawRaf) cancelAnimationFrame(this._redrawRaf);
    this._zoomRaf = null;
    this._redrawRaf = null;
    this._zoomAnim = null;
    this._imgData = null;
    if (this._canvas && this._canvas.parentNode) {
      this._canvas.parentNode.removeChild(this._canvas);
    }
    this._canvas = null;
    this._ctx = null;
    this._offscreen = null;
    this._map = null;
  },

  setData(data) {
    this._data = data || null;
    this._lut = null;
    if (this._map) this._reset();
  },

  setAnomalyMode(value) {
    this._anomaly = !!value;
    this._lut = null;
    if (this._map) this._reset();
  },

  show() {
    if (this._canvas) this._canvas.style.display = '';
    if (this._map) this._reset();
  },

  hide() {
    if (this._canvas) this._canvas.style.display = 'none';
  },

  // 设置色块透明度（海温模式 0.65 / 叠加模式 0.42），可见时立即生效
  setOpacity(opacity) {
    this._opacity = opacity;
    if (this._map && this._canvas && this._canvas.style.display !== 'none') {
      this._blit();
    }
  },

  // 拖拽中：只更新定位 + 平移离屏缓存，不重绘 SST（廉价、跟手）
  _onMove() {
    const map = this._map;
    const canvas = this._canvas;
    if (!map || !canvas) return;
    if (canvas.style.display === 'none') return;
    if (!this._offscreen || !this._renderPanePos) return;
    // 缩放过程中离屏缓存尺度不匹配，交给 zoomanim/zoom 重渲染
    if (map.getZoom() !== this._renderZoom) return;
    L.DomUtil.setPosition(canvas, map.containerPointToLayerPoint([0, 0]));
    this._blit();
  },

  // 缩放动画开始：记录参数，启动 rAF 逐帧按当前有效 zoom 重渲染
  _onZoomAnim(e) {
    const map = this._map;
    const canvas = this._canvas;
    if (!map || !canvas) return;
    if (canvas.style.display === 'none') return;
    const toZoom = e && e.zoom;
    // 防御：目标 zoom 非有限值直接忽略
    if (!isFinite(toZoom)) return;
    this._zoomAnim = {
      fromZoom: map.getZoom(),
      toZoom,
      start: performance.now(),
    };
    if (this._zoomRaf) cancelAnimationFrame(this._zoomRaf);
    this._zoomRaf = requestAnimationFrame(() => this._zoomFrame());
  },

  // 缩放动画逐帧：按当前有效 zoom 重渲染离屏 + 重绘（粗采样保性能）
  _zoomFrame() {
    this._zoomRaf = null;
    const map = this._map;
    const canvas = this._canvas;
    if (!map || !canvas || !this._zoomAnim) return;
    if (canvas.style.display === 'none') { this._zoomAnim = null; return; }
    const now = performance.now();
    const p = Math.min(1, Math.max(0, (now - this._zoomAnim.start) / 250));
    const eased = this._cubicBezier(p);
    // 与 Leaflet 瓦片缩放动画同步：scale 从 1 缓动到 2^(toZoom-fromZoom)
    const scale = 1 + eased * (Math.pow(2, this._zoomAnim.toZoom - this._zoomAnim.fromZoom) - 1);
    let effZoom = this._zoomAnim.fromZoom + Math.log2(scale);
    // 防御：非有限值回退当前 zoom，并钳制到 min/max 缩放范围
    if (!isFinite(effZoom)) effZoom = map.getZoom();
    effZoom = Math.max(map.getMinZoom(), Math.min(map.getMaxZoom(), effZoom));
    this._renderOffscreen(effZoom, true);
    this._blit();
    if (p < 1) {
      this._zoomRaf = requestAnimationFrame(() => this._zoomFrame());
    } else {
      this._zoomAnim = null;
      // 动画结束：精细采样重绘，保证最终清晰
      this._reset();
    }
  },

  // rAF 合并的整帧重绘（pinch 缩放等 zoom 事件连续触发时用）
  _scheduleRedraw() {
    if (this._zoomRaf || this._zoomAnim) return; // 缩放动画循环中，交给 _zoomFrame
    if (this._redrawRaf) return;
    this._redrawRaf = requestAnimationFrame(() => {
      this._redrawRaf = null;
      if (!this._map || !this._canvas) return;
      if (this._canvas.style.display === 'none') return;
      this._reset();
    });
  },

  // cubic-bezier(0, 0, 0.25, 1) 缓动（Leaflet 缩放动画同款），二分求解
  _cubicBezier(p) {
    let t0 = 0, t1 = 1;
    for (let i = 0; i < 12; i++) {
      const t = (t0 + t1) / 2;
      const x = 3 * (1 - t) * t * t * 0.25 + t * t * t;
      if (x < p) t0 = t; else t1 = t;
    }
    const t = (t0 + t1) / 2;
    return 3 * (1 - t) * t * t + t * t * t;
  },

  // 结束：重定位 + 重渲染离屏 + 重绘
  _reset() {
    const map = this._map;
    const canvas = this._canvas;
    if (!map || !canvas) return;
    if (canvas.style.display === 'none') return;
    const size = map.getSize();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = Math.max(1, Math.round(size.x * dpr));
    canvas.height = Math.max(1, Math.round(size.y * dpr));
    canvas.style.width = size.x + 'px';
    canvas.style.height = size.y + 'px';
    L.DomUtil.setPosition(canvas, map.containerPointToLayerPoint([0, 0]));
    this._renderOffscreen();
    this._blit();
  },

  // 把离屏缓存的当前视图区域 blit 到可见 canvas（半透明 0.65，陆地保持透明）
  _blit() {
    const map = this._map;
    const ctx = this._ctx;
    if (!map || !ctx) return;
    const size = map.getSize();
    const dpr = window.devicePixelRatio || 1;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.clearRect(0, 0, size.x, size.y);
    if (!this._offscreen || !this._renderPanePos) return;
    // 当前视图左上角相对离屏原点（renderPanePos）的偏移
    const curPanePos = map.containerPointToLayerPoint([0, 0]);
    const srcX = curPanePos.x - this._renderPanePos.x;
    const srcY = curPanePos.y - this._renderPanePos.y;
    // 防御：偏移非有限值（NaN/Infinity）→ 跳过绘制
    if (!isFinite(srcX) || !isFinite(srcY)) return;
    // 防御：源矩形完全在离屏外 → 跳过绘制，避免 drawImage 抛 IndexSizeError
    const ow = this._offscreen.width;
    const oh = this._offscreen.height;
    if (srcX >= ow || srcY >= oh || srcX + size.x <= 0 || srcY + size.y <= 0) return;
    ctx.globalAlpha = this._opacity;
    ctx.imageSmoothingEnabled = true;
    ctx.drawImage(this._offscreen, srcX, srcY, size.x, size.y, 0, 0, size.x, size.y);
    ctx.globalAlpha = 1;
  },

  // 自适应采样：1° 网格像素越大步长越大；目标每帧采样点 ≤ ~30 万；动画中更粗
  _sampleStep(zoom, coarse) {
    const size = this._map.getSize();
    const margin = Math.round(Math.max(size.x, size.y) * 0.35);
    const ow = size.x + margin * 2;
    const oh = size.y + margin * 2;
    let step = Math.ceil(Math.sqrt((ow * oh) / 300000));
    // 防御：zoom 非有限值时跳过随 zoom 的步长调整（避免 NaN 传播）
    if (isFinite(zoom)) {
      // 高倍放大时 1° 网格在屏幕上很大，可加大步长（上限 8）
      // EPSG:4326 下全球宽 = 512 * 2^zoom
      const pxPerDeg = (512 * Math.pow(2, zoom)) / 360;
      step = Math.max(step, Math.min(8, Math.round(pxPerDeg / 6)));
    }
    // 缩放动画中更粗采样，保证单帧重绘够快
    if (coarse) step = Math.max(step, 5);
    return Math.max(2, Math.min(8, step));
  },

  // 离屏渲染：双线性插值平滑渲染当前视图（含边距，供拖拽平移）。
  // effZoom 用于缩放动画中按当前有效 zoom 渲染；coarse 表示动画中的粗采样。
  _renderOffscreen(effZoom, coarse) {
    const map = this._map;
    if (!map) { this._offscreen = null; return; }
    const zoom = effZoom !== undefined ? effZoom : map.getZoom();
    const size = map.getSize();
    const margin = Math.round(Math.max(size.x, size.y) * 0.35);
    const ow = size.x + margin * 2;
    const oh = size.y + margin * 2;
    if (!this._offscreen) this._offscreen = document.createElement('canvas');
    this._offscreen.width = Math.max(1, ow);
    this._offscreen.height = Math.max(1, oh);
    const octx = this._offscreen.getContext('2d');
    octx.setTransform(1, 0, 0, 1, 0, 0);
    octx.clearRect(0, 0, ow, oh);
    this._renderPanePos = map.containerPointToLayerPoint([0, 0]);
    this._renderZoom = zoom;

    if (!this._data) return;
    const header = this._data.header;
    const data = this._data.data;
    const nx = header.nx, ny = header.ny;
    const dx = header.dx, dy = header.dy;
    const lo1 = header.lo1, la1 = header.la1;
    if (!nx || !ny || !dx || !dy) return;
    if (!this._lut) this._buildLut();

    const step = this._sampleStep(zoom, coarse);
    // 防御：采样步长非有限值（<1 或 NaN）→ 跳过本次渲染
    if (!isFinite(step) || step < 1) return;
    // 投影基准：offscreen-local (x,y) = 容器点 (x,y)；投影点 = (x,y) + project(center, zoom) - size/2
    const centerProj = map.project(map.getCenter(), zoom);
    const offX = centerProj.x - size.x / 2;
    const offY = centerProj.y - size.y / 2;
    // 防御：投影基准非有限值（异常 center/zoom）→ 跳过本次渲染
    if (!isFinite(offX) || !isFinite(offY)) return;
    const rowLats = [];
    for (let y = 0; y <= oh; y += step) {
      rowLats[y] = map.unproject(L.point(offX, y + offY), zoom).lat;
    }
    const colLons = [];
    for (let x = 0; x <= ow; x += step) {
      colLons[x] = map.unproject(L.point(x + offX, offY), zoom).lng;
    }
    const hasMask = !!this._mask;
    // 掩膜内的经度 wrap 采样函数：视图跨 ±180° 时仍能取到正确海/陆
    const sampleMask = (longitude, latitude) => {
      const m = this._mask;
      let mx = Math.floor(((longitude + 180) / 360) * m.w);
      mx = ((mx % m.w) + m.w) % m.w;
      let my = Math.floor(((90 - latitude) / 180) * m.h);
      if (my < 0) my = 0;
      else if (my > m.h - 1) my = m.h - 1;
      const v = m.data[my * m.w + mx];
      if (v <= 40) return 0;
      if (v >= 140) return 1;
      const t = (v - 40) / 100;
      return t * t * (3 - 2 * t);
    };
    // 用 ImageData 直接写像素（比逐格 fillRect 快得多）；复用缓存避免动画逐帧 GC
    if (!this._imgData || this._imgData.width !== ow || this._imgData.height !== oh) {
      this._imgData = octx.createImageData(ow, oh);
    }
    const img = this._imgData;
    const px = img.data;
    // ImageData is reused between frames; clear transparent cells before sampling
    // so land and missing values cannot retain colors from the previous field.
    px.fill(0);
    for (let y = 0; y < oh; y += step) {
      const j0 = (la1 - rowLats[y]) / dy;
      const yEnd = Math.min(y + step, oh);
      // 行内纬度线性插值（EPSG:4326 屏幕 y 与纬度严格线性）
      const lat0 = rowLats[y];
      const lat1 = rowLats[yEnd] !== undefined ? rowLats[yEnd] : lat0;
      const fy = (yEnd - y) > 0 ? 1 / (yEnd - y) : 0;
      for (let x = 0; x < ow; x += step) {
        const i0 = (colLons[x] - lo1) / dx;
        const val = this._bilinear(data, nx, ny, i0, j0);
        if (val === null) continue; // 陆地/缺测 → 透明
        const t = this._anomaly ? Math.max(-3, Math.min(3, val)) : Math.max(-2, Math.min(32, val));
        const ci = this._anomaly ? Math.round(((t + 3) / 6) * 255) : Math.round(((t + 2) / 34) * 255);
        const c = this._lut[ci];
        const xEnd = Math.min(x + step, ow);
        // 掩膜逐像素采样（经度/纬度行内插值），海岸线柔边消除块状锯齿
        const lon0 = colLons[x];
        const lon1 = colLons[xEnd] !== undefined ? colLons[xEnd] : lon0;
        const fx = (xEnd - x) > 0 ? 1 / (xEnd - x) : 0;
        for (let yy = y; yy < yEnd; yy++) {
          const rowBase = yy * ow;
          const lat = hasMask ? lat0 + (lat1 - lat0) * (yy - y) * fy : 0;
          for (let xx = x; xx < xEnd; xx++) {
            const o = (rowBase + xx) * 4;
            px[o] = c[0];
            px[o + 1] = c[1];
            px[o + 2] = c[2];
            if (hasMask) {
              const lon = lon0 + (lon1 - lon0) * (xx - x) * fx;
              px[o + 3] = Math.round(255 * sampleMask(lon, lat));
            } else {
              px[o + 3] = 255;
            }
          }
        }
      }
    }
    octx.putImageData(img, 0, 0);
  },

  // 双线性插值：缺测邻居按权重剔除（加权插值），海温可平滑延伸到海岸线；
  // 全部邻居缺测（陆地内部）返回 null → 透明
  _bilinear(data, nx, ny, i0, j0) {
    // 防御：非有限网格坐标（NaN/Infinity）直接返回 null，避免 NaN 传播
    if (!isFinite(i0) || !isFinite(j0)) return null;
    const iw = ((i0 % nx) + nx) % nx;
    const i = Math.floor(iw);
    const fi = iw - i;
    const j = Math.floor(j0);
    const fj = j0 - j;
    if (j < 0 || j >= ny - 1) return null;
    const i1 = (i + 1) % nx;
    const j1 = j + 1;
    const base = j * nx;
    const base1 = j1 * nx;
    let weighted = 0;
    let weight = 0;
    const push = (v, w) => {
      if (this._isValid(v)) {
        weighted += v * w;
        weight += w;
      }
    };
    push(data[base + i], (1 - fi) * (1 - fj));
    push(data[base + i1], fi * (1 - fj));
    push(data[base1 + i], (1 - fi) * fj);
    push(data[base1 + i1], fi * fj);
    return weight > 0 ? weighted / weight : null;
  },

  _isValid(v) {
    return typeof v === 'number' && !isNaN(v) && Math.abs(v) < 100;
  },

  _buildLut() {
    const lut = [];
    for (let k = 0; k < 256; k++) {
      const value = this._anomaly ? -3 + (6 * k) / 255 : -2 + (34 * k) / 255;
      lut.push(ensoFieldColor(value, this._anomaly));
    }
    this._lut = lut;
  },
})

export default {
  name: 'EnsoMapPanel',
  components: { EnsoGlobe },
  props: {
    compact: { type: Boolean, default: false },
    maximized: { type: Boolean, default: false },
    sstField: { type: Object, default: null },
    sstMode: { type: String, default: 'absolute' },
    initialLayer: { type: String, default: 'both' },
    // A14 · 内嵌回放条的数据源（页面级时间轴），播放状态由父组件驱动
    replayPeriods: { type: Array, default: () => [] },
    replayIndex: { type: Number, default: 0 },
    replayPlaying: { type: Boolean, default: false },
    // 回放身份（事件 id / 起报月份）：变化时点位曲线归零，避免跨事件混叠
    replayKey: { type: String, default: '' }
  },
  data() {
    return {
      displayMode: '3d',
      currentLayer: this.initialLayer,
      windData: [],
      sstData: null,
      hasSst: false,
      windStatus: { source: 'local', refTime: null },
      map: null,
      velocityLayer: null,
      sstLayer: null,
      pollTimer: null,
      destroyed: false,
      fetchController: null,
      externalSstActive: false,
      // A1 · 探针读数
      probe: { visible: false, x: 0, y: 0, flip: false, coord: '—', sstText: '—', windText: '—', dirText: '—' },
      // A2 · 点位时序探针
      pins: [],
      pinSeq: 0,
      pinDownAt: null,
      // A14 · 倍速循环
      speeds: [900, 450, 1600],
      speedIndex: 0
    }
  },
  computed: {
    colorGradient() {
      return SST_COLOR_GRADIENT
    },
    // 回放/历史模式：隐藏 Niño3.4 指数参考区（2D 卡片与 3D 卡片行为一致）
    hasHistoryField() {
      return !!this.sstField
    },
    windBadgeText() {
      if (this.windStatus.source === 'replay') return '历史风场 · 随月份变化'
      if (this.windStatus.source !== 'real') return '全球风场 · 海温监测'
      return 'GFS 分析场 · ' + (this.windStatus.refTime || '最新时次')
    },
    probeValueClass() {
      const anomaly = this.sstMode === 'anomaly'
      return { 'is-warm': !anomaly, 'warm-text': anomaly && this.probe.sstText.indexOf('+') === 0, 'cool-text': anomaly && this.probe.sstText.indexOf('−') === 0 }
    },
    // A14
    replayProgress() {
      const total = this.replayPeriods.length
      if (total < 2) return 0
      return (Math.min(Math.max(this.replayIndex, 0), total - 1) / (total - 1)) * 100
    },
    speedLabel() {
      const ms = this.speeds[this.speedIndex]
      if (ms <= 500) return '2×'
      if (ms >= 1600) return '0.5×'
      return '1×'
    }
  },
  watch: {
    sstField: {
      immediate: true,
      handler() {
        if (this.sstField) this.applyExternalSst()
        else this.clearExternalSst()
        this.updateNinoRegionVisibility()
        this.refreshPinSeries()
      }
    },
    sstMode() {
      if (this.sstLayer) this.sstLayer.setAnomalyMode(this.sstMode === 'anomaly')
      if (this.sstField && this.sstMode === 'anomaly') this.applyReplayWind()
      if (!this.sstField) this.clearExternalSst()
      this.pins.forEach((pin) => { pin.series = [] })
    },
    maximized() {
      this.$nextTick(() => this.resizeMap())
    },
    // 切换历史事件 / 起报月份：点位时序从新事件重新累积
    replayKey() {
      this.pins.forEach((pin) => {
        pin.series = []
        pin.path = ''
        pin.range = '—'
        pin.currentText = '—'
      })
      this.refreshPinSeries()
    }
  },
  mounted() {
    this.applyLocalData()
    this.applyExternalSst()
    this.loadRealWind()
    this.pollTimer = window.setInterval(this.refreshRealWind, 30 * 60 * 1000)
  },
  beforeDestroy() {
    this.destroyed = true
    pinLocationCache = this.pins.map(pin => ({ lat: pin.lat, lon: pin.lon }))
    if (this.pollTimer) window.clearInterval(this.pollTimer)
    if (this.fetchController) this.fetchController.abort()
    if (this.map) {
      this.map.off()
      this.map.remove()
      this.map = null
    }
    this.velocityLayer = null
    this.sstLayer = null
    this.ninoRegionGroup = null
    this.basemapLayers = null
  },
  methods: {
    applyLocalData() {
      this.windData = this.generateWindData()
      this.sstData = this.normalizeGridField(presetSst)
      this.hasSst = !!this.sstData
    },
    applyExternalSst() {
      if (!this.sstField) return
      const normalized = this.normalizeGridField(this.sstField)
      if (!normalized) return
      this.sstData = normalized
      this.hasSst = true
      this.externalSstActive = true
      this.applyReplayWind()
      if (this.sstLayer) {
        this.sstLayer.setAnomalyMode(this.sstMode === 'anomaly')
        this.sstLayer.setData(this.sstData)
      }
    },
    clearExternalSst() {
      // Leaving history mode must remove the last replay frame from both renderers.
      if (this.destroyed || this.sstField || !this.externalSstActive) return
      this.externalSstActive = false
      this.applyLocalData()
      this.windStatus = { source: 'local', refTime: null }
      if (this.velocityLayer && this.velocityLayer.setData) this.velocityLayer.setData(this.windData)
      if (this.sstLayer) {
        this.sstLayer.setAnomalyMode(false)
        this.sstLayer.setData(this.sstData)
      }
      this.loadRealWind()
    },
    applyReplayWind() {
      if (!this.sstField || this.sstMode !== 'anomaly') return
      const wind = this.generateReplayWind(this.sstField)
      if (!wind) return
      this.windData = wind
      this.windStatus = { source: 'replay', refTime: this.sstField.header && this.sstField.header.forecastTime != null ? `第 ${Number(this.sstField.header.forecastTime) + 1} 帧` : '当前月份' }
      if (this.velocityLayer && this.velocityLayer.setData) this.velocityLayer.setData(wind)
    },
    generateWindData() {
      const nx = 180, ny = 90, lo1 = 0, la1 = 90, dx = 2, dy = 2
      const u = new Array(nx * ny), v = new Array(nx * ny)
      const vortices = [
        { longitude: 300, latitude: 44, strength: 12, radiusX: 27, radiusY: 19 },
        { longitude: 350, latitude: 58, strength: -10, radiusX: 24, radiusY: 17 },
        { longitude: 235, latitude: 28, strength: -11, radiusX: 23, radiusY: 17 },
        { longitude: 75, latitude: 46, strength: 10, radiusX: 26, radiusY: 18 },
        { longitude: 140, latitude: -38, strength: 12, radiusX: 26, radiusY: 19 },
        { longitude: 20, latitude: -52, strength: -11, radiusX: 24, radiusY: 18 },
        { longitude: 250, latitude: -22, strength: 9, radiusX: 22, radiusY: 16 }
      ]
      for (let j = 0; j < ny; j++) {
        const lat = la1 - j * dy, latRad = lat * Math.PI / 180
        for (let i = 0; i < nx; i++) {
          const lon = lo1 + i * dx, lonRad = lon * Math.PI / 180, idx = j * nx + i
          const trade = -Math.exp(-Math.pow(lat / 24, 2)) * 4
          const westerly = Math.exp(-Math.pow((lat - 46) / 16, 2)) * 5 + Math.exp(-Math.pow((lat + 46) / 16, 2)) * 5
          const polar = -Math.exp(-Math.pow((lat - 78) / 12, 2)) * 2.5 - Math.exp(-Math.pow((lat + 78) / 12, 2)) * 2.5
          const meander = Math.sin(lonRad * 2 + latRad * 1.7) * 5 + Math.cos(lonRad * 3 - latRad * 2.4) * 2.5
          let vortexU = 0, vortexV = 0
          vortices.forEach((vortex) => {
            const deltaLongitude = ((lon - vortex.longitude + 540) % 360) - 180
            const x = deltaLongitude * Math.cos(vortex.latitude * Math.PI / 180), y = lat - vortex.latitude
            const xScaled = x / vortex.radiusX, yScaled = y / vortex.radiusY
            const envelope = Math.exp(-0.5 * (xScaled * xScaled + yScaled * yScaled))
            vortexU -= vortex.strength * yScaled * envelope
            vortexV += vortex.strength * xScaled * envelope
          })
          u[idx] = trade + westerly + polar + meander + vortexU
          v[idx] = Math.sin(lonRad * 2 + latRad * 2.5) * 5 + Math.cos(lonRad * 3 - latRad * 1.8) * 3 + Math.sin(latRad * 4 + lonRad) * 2.5 + vortexV
        }
      }
      const header = { lo1, la1, dx, dy, nx, ny, refTime: new Date().toISOString(), forecastTime: 0, gridDefinitionTemplate: 0 }
      return [
        { header: { ...header, parameterCategory: 2, parameterNumber: 2 }, data: u },
        { header: { ...header, parameterCategory: 2, parameterNumber: 3 }, data: v }
      ]
    },
    generateReplayWind(field) {
      if (!field || !field.header || !Array.isArray(field.data)) return null
      const base = this.generateWindData()
      const source = field.header
      const sourceWidth = Number(source.nx)
      const sourceHeight = Number(source.ny)
      const dx = Number(source.dx)
      const dy = Number(source.dy)
      const lo1 = Number(source.lo1)
      const la1 = Number(source.la1)
      if (![sourceWidth, sourceHeight, dx, dy, lo1, la1].every(Number.isFinite)) return base
      const values = field.data
      const frame = Number(source.forecastTime) || 0
      const sample = (longitude, latitude) => {
        const x = ((longitude - lo1) / dx % sourceWidth + sourceWidth) % sourceWidth
        const y = Math.max(0, Math.min(sourceHeight - 1, (la1 - latitude) / Math.abs(dy)))
        const x0 = Math.floor(x)
        const x1 = (x0 + 1) % sourceWidth
        const y0 = Math.floor(y)
        const y1 = Math.min(sourceHeight - 1, y0 + 1)
        const fx = x - x0
        const fy = y - y0
        const corners = [
          [values[y0 * sourceWidth + x0], (1 - fx) * (1 - fy)],
          [values[y0 * sourceWidth + x1], fx * (1 - fy)],
          [values[y1 * sourceWidth + x0], (1 - fx) * fy],
          [values[y1 * sourceWidth + x1], fx * fy]
        ]
        let weighted = 0
        let weight = 0
        corners.forEach(([value, factor]) => {
          if (Number.isFinite(value)) {
            weighted += value * factor
            weight += factor
          }
        })
        return weight > 0 ? weighted / weight : 0
      }
      const distance = (longitude, center) => {
        const delta = Math.abs(((longitude - center + 540) % 360) - 180)
        return delta
      }
      const waveCenter = 158 + ((frame * 13) % 88)
      for (let j = 0; j < 90; j += 1) {
        const latitude = 90 - j * 2
        const latEnvelope = Math.exp(-Math.pow(latitude / 13, 2))
        const tradeEnvelope = Math.exp(-Math.pow(latitude / 20, 2))
        for (let i = 0; i < 180; i += 1) {
          const longitude = i * 2
          const anomaly = Math.max(-3, Math.min(3, sample(longitude, latitude)))
          const core = latEnvelope * Math.exp(-Math.pow(distance(longitude, 215) / 62, 2))
          const movingWave = latEnvelope * Math.exp(-Math.pow(distance(longitude, waveCenter) / 38, 2))
          const basinPulse = tradeEnvelope * Math.sin(frame * 0.72 + longitude * Math.PI / 31)
          const offset = j * 180 + i
          // Warm anomalies weaken the equatorial trades and add westerly flow;
          // cold anomalies strengthen easterlies. The moving wave keeps changes
          // visible even when the index remains near a local plateau.
          base[0].data[offset] += anomaly * core * 6.8 + Math.sin(frame * 0.8) * movingWave * 4.5 + basinPulse * 2.8
          base[1].data[offset] += anomaly * core * Math.sin((longitude - 215) * Math.PI / 42) * 4.8 + Math.cos(frame * 0.65) * movingWave * 3.6 + Math.cos(frame * 0.54 + longitude * Math.PI / 27) * tradeEnvelope * 1.8
        }
      }
      const replayHeader = { refTime: source.refTime || 'historical-demo', forecastTime: frame }
      base.forEach(fieldItem => { fieldItem.header = { ...fieldItem.header, ...replayHeader } })
      return base
    },
    normalizeGridField(comp, parameterCategory, parameterNumber) {
      if (!comp || !comp.header || !Array.isArray(comp.data)) return null
      const header = { ...comp.header }
      if (parameterCategory !== undefined) header.parameterCategory = parameterCategory
      if (parameterNumber !== undefined) header.parameterNumber = parameterNumber
      if (header.forecastTime === undefined) header.forecastTime = 0
      if (header.gridDefinitionTemplate === undefined) header.gridDefinitionTemplate = 0
      if ([header.lo1, header.la1, header.dx, header.dy, header.nx, header.ny].some(v => v == null)) return null
      let data = comp.data
      if (Array.isArray(data[0])) data = data.reduce((flat, row) => flat.concat(row), [])
      if (data.length !== header.nx * header.ny) return null
      if (header.la2 !== undefined && header.la1 < header.la2) {
        const flipped = new Array(data.length)
        for (let j = 0; j < header.ny; j++) {
          const srcRow = header.ny - 1 - j
          for (let i = 0; i < header.nx; i++) flipped[j * header.nx + i] = data[srcRow * header.nx + i]
        }
        data = flipped
        header.la1 = header.la2
        header.dy = Math.abs(header.dy)
      }
      return { header, data }
    },
    adaptWindData(raw) {
      if (!raw || !raw.u || !raw.v) return null
      const u = this.normalizeGridField(raw.u, 2, 2), v = this.normalizeGridField(raw.v, 2, 3)
      return u && v ? [u, v] : null
    },
    fetchRealWind() {
      if (this.fetchController) this.fetchController.abort()
      const controller = new AbortController()
      this.fetchController = controller
      const timeout = window.setTimeout(() => controller.abort(), 5000)
      return fetch('http://localhost:8000/wind', { signal: controller.signal })
        .then(res => { if (!res.ok) throw new Error('HTTP ' + res.status); return res.json() })
        .then(raw => {
          const wind = this.adaptWindData(raw)
          if (!wind) throw new Error('Invalid wind data')
          return { wind, sst: this.normalizeGridField(raw.sst) }
        })
        .finally(() => window.clearTimeout(timeout))
    },
    loadRealWind() {
      this.fetchRealWind().then(({ wind, sst }) => this.applyWindData(wind, sst)).catch(() => {})
    },
    refreshRealWind() {
      this.fetchRealWind().then(({ wind, sst }) => {
        if (this.destroyed) return
        const refTime = wind[0].header.refTime || null
        if (refTime !== this.windStatus.refTime) this.applyWindData(wind, sst)
      }).catch(() => {})
    },
    applyWindData(wind, sst) {
      if (this.destroyed) return
      const replayWind = this.sstField && this.sstMode === 'anomaly' ? this.generateReplayWind(this.sstField) : null
      this.windData = replayWind || wind
      if (sst && !this.sstField) this.sstData = sst
      this.hasSst = !!this.sstData
      this.windStatus = replayWind
        ? { source: 'replay', refTime: this.sstField.header && this.sstField.header.forecastTime != null ? `第 ${Number(this.sstField.header.forecastTime) + 1} 帧` : '当前月份' }
        : { source: 'real', refTime: this.formatTime(wind[0].header.refTime) }
      if (this.velocityLayer) this.velocityLayer.setData(this.windData)
      if (this.sstLayer) this.sstLayer.setData(this.sstData)
      if (this.sstLayer) this.sstLayer.setAnomalyMode(this.sstMode === 'anomaly')
      if (this.displayMode === '3d' && this.velocityLayer && this.velocityLayer.pause) this.velocityLayer.pause()
      if (!this.hasSst && this.currentLayer !== 'wind') this.switchLayer('wind')
      this.refreshPinSeries()
    },
    formatTime(value) {
      if (!value) return '最新时次'
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false })
    },
    switchDisplayMode(mode) {
      if (mode === this.displayMode) return
      this.displayMode = mode
      if (mode === '2d') {
        this.$nextTick(() => {
          if (!this.map) this.initMap()
          else this.resizeMap()
          this.updateNinoRegionVisibility()
          this.applyLayerVisibility()
        })
      } else if (this.velocityLayer && this.velocityLayer.pause) {
        this.velocityLayer.pause()
      }
    },
    // 2D Niño3.4 参考区：实时模式显示，进入历史/回放（sstField 非空）自动隐藏
    updateNinoRegionVisibility() {
      if (!this.map || !this.ninoRegionGroup) return
      const want = !this.hasHistoryField
      if (want && !this.map.hasLayer(this.ninoRegionGroup)) this.ninoRegionGroup.addTo(this.map)
      else if (!want && this.map.hasLayer(this.ninoRegionGroup)) this.map.removeLayer(this.ninoRegionGroup)
    },
    handleGlobeUnavailable() { this.switchDisplayMode('2d') },
    initMap() {
      const el = this.$refs.mapEl
      if (!el || this.map) return
      // EPSG:4326 等经纬度投影，与 NASA Blue Marble 底图（等经纬度贴图）严格对齐。
      // 保留 CRS 默认经度环绕（wrapLng），配合 5 份底图世界副本：可以一直向左/向右
      // 拖动不卡住、不出现黑块；拖出 ±180 后 Leaflet 会无缝把视图折回主世界
      this.map = L.map(el, {
        zoomControl: false, attributionControl: false, minZoom: 0, maxZoom: 4.5,
        center: [8, 170], zoom: 0.75, zoomSnap: 0.25, worldCopyJump: false,
        crs: L.CRS.EPSG4326, maxBoundsViscosity: 1.0,
        fadeAnimation: true, bounceAtZoomLimits: false, dragging: true, tap: true,
        touchZoom: true, scrollWheelZoom: true, inertia: true
      })
      // 只限制纬度（经度交给环绕机制），避免把拖动卡死在单一世界内
      this.map.setMaxBounds(L.latLngBounds([-89, -3600], [89, 3600]))
      // 本地高清底图（4096×2048）× 5 份世界副本，覆盖 ±720° 经度，拖动永远有底图
      this.basemapLayers = [-720, -360, 0, 360, 720].map((offset) => {
        return L.imageOverlay(blueMarbleUrl, L.latLngBounds([[-90, -180 + offset], [90, 180 + offset]]), {
          interactive: false, className: 'enso-basemap-img'
        }).addTo(this.map)
      })
      // Niño3.4 指数参考区（5°N–5°S · 170°W–120°W）：黄色虚线框 + 紧凑卡片，跨世界副本
      this.map.createPane('ninoPane')
      const ninoPane = this.map.getPane('ninoPane')
      ninoPane.style.zIndex = '450'
      ninoPane.style.pointerEvents = 'none'
      const ninoLayers = []
      ;[-720, -360, 0, 360, 720].forEach((offset) => {
        ninoLayers.push(L.rectangle([[-5, -170 + offset], [5, -120 + offset]], {
          pane: 'ninoPane', interactive: false, fill: false,
          color: '#ffd36a', weight: 1.2, dashArray: '5 4', opacity: 0.85
        }))
        ninoLayers.push(L.marker([0, -145 + offset], {
          pane: 'ninoPane', interactive: false, keyboard: false,
          icon: L.divIcon({ className: 'nino34-icon', html: '<div class="nino34-card"><span class="nino34-title">Niño3.4 指数参考区</span><small>5°N–5°S · 170°W–120°W</small></div>', iconSize: [0, 0] })
        }))
      })
      this.ninoRegionGroup = L.layerGroup(ninoLayers)
      this.updateNinoRegionVisibility()
      this.velocityLayer = L.velocityLayer({
        displayValues: false, displayOptions: { velocityType: '', emptyString: '' },
        maxVelocity: 26, minVelocity: 0, velocityScale: 0.03, particleAge: 90,
        lineWidth: 1.45, particleMultiplier: 1 / 1250, frameRate: 20,
        colorScale: ['rgb(8, 34, 80)', 'rgb(12, 58, 122)', 'rgb(18, 88, 168)', 'rgb(26, 122, 200)', 'rgb(40, 156, 214)', 'rgb(66, 188, 224)', 'rgb(110, 214, 232)', 'rgb(150, 232, 238)'],
        data: this.windData
      }).addTo(this.map)
      this.velocityLayer.pause = () => { if (this.velocityLayer && this.velocityLayer._windy) this.velocityLayer._windy.stop() }
      this.velocityLayer.resume = () => { if (this.velocityLayer) this.velocityLayer._clearAndRestart() }
      this.sstLayer = new SstOverlayLayer().addTo(this.map)
      this.sstLayer.setData(this.sstData)
      this.sstLayer.setAnomalyMode(this.sstMode === 'anomaly')
      if (this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas) this.velocityLayer._canvasLayer._canvas.style.zIndex = '2'
      this.map.on('dragstart', () => this.velocityLayer && this.velocityLayer.pause())
      this.map.on('dragend', () => this.velocityLayer && this.velocityLayer.resume())
      // A1 · 探针：拖拽中持续刷新读数，离开地图即隐藏
      this.map.on('mousemove', this.handleProbeMove, this)
      this.map.on('mouseout', this.handleProbeLeave, this)
      // A2 · 落钉：用按下/弹起位移区分"点击"与"拖动"，避免拖地图误打钉
      this.map.on('mousedown', (event) => {
        this.pinDownAt = event.originalEvent ? { x: event.originalEvent.clientX, y: event.originalEvent.clientY } : null
      })
      this.map.on('mouseup', (event) => {
        const origin = this.pinDownAt
        this.pinDownAt = null
        if (!origin || !event.originalEvent || this.displayMode !== '2d') return
        const moved = Math.abs(event.originalEvent.clientX - origin.x) + Math.abs(event.originalEvent.clientY - origin.y)
        if (moved > 5) return
        const latLng = this.map.mouseEventToLatLng(event.originalEvent)
        this.togglePin(latLng.lat, latLng.lng)
      })
      this.map.on('mousedown', e => {
        if (e.originalEvent && e.originalEvent.button === 1) {
          e.originalEvent.preventDefault()
          this.map.setView(this.map.mouseEventToLatLng(e.originalEvent), this.map.getZoom() + 1, { animate: true })
        }
      })
      this.applyLayerVisibility()
      this.resizeMap()
      this.restoreCachedPins()
    },
    resizeMap() {
      if (this.map && this.displayMode === '2d') {
        this.map.invalidateSize({ pan: false })
        if (this.sstLayer && (this.currentLayer === 'sst' || this.currentLayer === 'both')) this.sstLayer._reset()
      }
    },
    switchLayer(layer) {
      if (layer === this.currentLayer || (layer !== 'wind' && !this.hasSst)) return
      this.currentLayer = layer
      this.applyLayerVisibility()
    },
    applyLayerVisibility() {
      if (!this.velocityLayer || !this.sstLayer) return
      if (this.currentLayer === 'sst') {
        this._hideVelocityLayer()
        this.sstLayer.setOpacity(0.65)
        this.sstLayer.show()
      } else if (this.currentLayer === 'both') {
        this.sstLayer.setOpacity(0.42)
        this.sstLayer.show()
        if (this.velocityLayer.setOptions) this.velocityLayer.setOptions({ particleMultiplier: 1 / 1450 })
        this._showVelocityLayer()
      } else {
        if (this.velocityLayer.setOptions) this.velocityLayer.setOptions({ particleMultiplier: 1 / 1250 })
        this._showVelocityLayer()
        this.sstLayer.hide()
      }
    },
    _hideVelocityLayer() {
      const canvas = this.velocityLayer && this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas
      if (canvas) canvas.style.display = 'none'
      if (this.velocityLayer && this.velocityLayer.pause) this.velocityLayer.pause()
    },
    _showVelocityLayer() {
      const canvas = this.velocityLayer && this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas
      if (canvas) canvas.style.display = ''
      if (this.velocityLayer && this.velocityLayer.resume) this.velocityLayer.resume()
    },

    // ---- A1 · 十字探针 ----
    // 与 SstOverlayLayer 的双线性采样保持一致：经度环绕、纬度截断、缺测邻居加权
    sampleGrid(grid, longitude, latitude) {
      if (!grid || !grid.header || !Array.isArray(grid.data)) return null
      const header = grid.header
      const nx = Number(header.nx)
      const ny = Number(header.ny)
      const dx = Number(header.dx)
      const dy = Number(header.dy)
      const lo1 = Number(header.lo1)
      const la1 = Number(header.la1)
      if (![nx, ny, dx, dy, lo1, la1].every(Number.isFinite) || nx < 2 || ny < 2) return null
      const stepY = Math.abs(dy) || 1
      let x = (((longitude - lo1) / dx) % nx + nx) % nx
      const y = Math.max(0, Math.min(ny - 1.001, (la1 - latitude) / stepY))
      const x0 = Math.floor(x)
      const y0 = Math.floor(y)
      const x1 = (x0 + 1) % nx
      const y1 = Math.min(ny - 1, y0 + 1)
      const fx = x - x0
      const fy = y - y0
      const pick = (i, j) => {
        const value = grid.data[j * nx + i]
        return Number.isFinite(value) ? value : null
      }
      const corners = [[pick(x0, y0), (1 - fx) * (1 - fy)], [pick(x1, y0), fx * (1 - fy)], [pick(x0, y1), (1 - fx) * fy], [pick(x1, y1), fx * fy]]
      let weighted = 0
      let weight = 0
      corners.forEach(([value, factor]) => {
        if (value !== null) {
          weighted += value * factor
          weight += factor
        }
      })
      return weight > 0 ? weighted / weight : null
    },
    windLabel(degree) {
      const directions = ['北', '东北', '东', '东南', '南', '西南', '西', '西北']
      return directions[Math.round(((degree % 360) + 360) % 360 / 45) % 8] + '风'
    },
    sampleWindAt(longitude, latitude) {
      if (!Array.isArray(this.windData) || this.windData.length < 2) return null
      const u = this.sampleGrid(this.windData[0], longitude, latitude)
      const v = this.sampleGrid(this.windData[1], longitude, latitude)
      if (u === null || v === null) return null
      const speed = Math.sqrt(u * u + v * v)
      // 气象风向指风的来向：由 (u, v) 求指向源点的方位角
      const degree = ((Math.atan2(-u, -v) * 180 / Math.PI) + 360) % 360
      return { speed, degree, label: this.windLabel(degree) }
    },
    handleProbeMove(event) {
      if (!this.map || this.displayMode !== '2d') return
      const container = event.containerPoint
      const latLng = event.latlng
      if (!container || !latLng) return
      const longitude = (((latLng.lng + 180) % 360) + 360) % 360 - 180
      const latitude = Math.max(-89.5, Math.min(89.5, latLng.lat))
      const sst = this.sampleGrid(this.sstData, longitude, latitude)
      const wind = this.sampleWindAt(longitude, latitude)
      const anomaly = this.sstMode === 'anomaly'
      this.probe.x = container.x
      this.probe.y = container.y
      this.probe.flip = container.x > (this.$refs.mapEl ? this.$refs.mapEl.clientWidth * 0.6 : 480)
      this.probe.coord = `${Math.abs(latitude).toFixed(1)}°${latitude >= 0 ? 'N' : 'S'} · ${Math.abs(longitude).toFixed(1)}°${longitude >= 0 ? 'E' : 'W'}`
      this.probe.sstText = sst === null
        ? '缺测'
        : anomaly
          ? `${sst > 0 ? '+' : sst < 0 ? '−' : ''}${Math.abs(sst).toFixed(2)}℃`
          : `${sst.toFixed(1)}℃`
      this.probe.windText = wind ? `${wind.speed.toFixed(1)} m/s` : '—'
      this.probe.dirText = wind ? `${wind.label} ${wind.degree.toFixed(0)}°` : '—'
      this.probe.visible = true
    },
    handleProbeLeave() {
      this.probe.visible = false
    },

    // ---- A2 · 点位时序探针 ----
    pinValueClass(pin) {
      if (this.sstMode !== 'anomaly') return {}
      return { 'warm-text': pin.currentText.indexOf('+') === 0, 'cool-text': pin.currentText.indexOf('−') === 0 }
    },
    togglePin(latitude, longitude) {
      const lon = (((longitude + 180) % 360) + 360) % 360 - 180
      const lat = Math.max(-89, Math.min(89, latitude))
      const existing = this.pins.find(pin => Math.abs(pin.lat - lat) < 1.2 && Math.abs(pin.lon - lon) < 1.2)
      if (existing) {
        this.removePin(existing)
        return
      }
      this.createPin(lat, lon)
    },
    createPin(latitude, longitude) {
      if (this.pins.length >= 4) this.removePin(this.pins[0])
      this.pinSeq += 1
      const offsets = [-720, -360, 0, 360, 720]
      const markers = offsets.map(offset => L.circleMarker([latitude, longitude + offset], {
        radius: 5, weight: 1.6, color: '#ffd694', fillColor: 'rgba(255, 214, 148, .85)', fillOpacity: .95, interactive: false
      }))
      const pin = {
        id: `pin-${this.pinSeq}`,
        index: this.pinSeq,
        lat: latitude,
        lon: longitude,
        markers,
        coord: `${Math.abs(latitude).toFixed(1)}°${latitude >= 0 ? 'N' : 'S'} · ${Math.abs(longitude).toFixed(1)}°${longitude >= 0 ? 'E' : 'W'}`,
        series: [],
        currentText: '—',
        path: '',
        range: '—'
      }
      this.pins.push(pin)
      if (this.map) markers.forEach(marker => marker.addTo(this.map))
      this.refreshPinSeries()
      return pin
    },
    // 地图重建后按缓存位置恢复钉子（曲线从当前事件重新累积）
    restoreCachedPins() {
      if (!pinLocationCache.length || this.pins.length) return
      const cached = pinLocationCache.slice()
      pinLocationCache = []
      cached.forEach(item => this.createPin(item.lat, item.lon))
    },
    removePin(pin) {
      if (this.map) pin.markers.forEach(marker => this.map.removeLayer(marker))
      this.pins = this.pins.filter(item => item !== pin)
    },
    clearPins() {
      this.pins.slice().forEach(pin => this.removePin(pin))
    },
    refreshPinSeries() {
      if (!this.pins.length) return
      const anomaly = this.sstMode === 'anomaly'
      const header = this.sstField && this.sstField.header ? this.sstField.header : null
      const refTime = header && header.refTime ? String(header.refTime) : '当前场'
      // 帧号唯一：refTime 在回放里是固定串（historical-demo），必须叠加帧号
      const frame = header && header.forecastTime != null ? Number(header.forecastTime) : this.replayIndex
      const label = `${refTime}·#${frame}`
      this.pins.forEach((pin) => {
        const value = this.sampleGrid(this.sstData, pin.lon, pin.lat)
        if (value === null) {
          pin.currentText = '缺测'
          return
        }
        pin.currentText = anomaly
          ? `${value > 0 ? '+' : value < 0 ? '−' : ''}${Math.abs(value).toFixed(2)}℃`
          : `${value.toFixed(1)}℃`
        const hit = pin.series.findIndex(item => item.label === label)
        if (hit >= 0) pin.series[hit].value = value
        else {
          pin.series.push({ label, value })
          if (pin.series.length > 40) pin.series.shift()
        }
        pin.path = this.sparkPath(pin.series)
        const values = pin.series.map(item => item.value)
        pin.range = `${Math.min(...values).toFixed(1)} ~ ${Math.max(...values).toFixed(1)}℃`
      })
    },
    sparkPath(series) {
      if (!series || series.length < 2) return ''
      const width = 120
      const height = 30
      const pad = 3
      const values = series.map(item => item.value)
      const min = Math.min(...values)
      const max = Math.max(...values)
      const span = (max - min) || 1
      return series.map((item, index) => {
        const x = pad + (index / (series.length - 1)) * (width - pad * 2)
        const y = height - pad - ((item.value - min) / span) * (height - pad * 2)
        return `${index === 0 ? 'M' : 'L'}${x.toFixed(1)} ${y.toFixed(1)}`
      }).join(' ')
    },

    // ---- A14 · 内嵌回放 ----
    cycleSpeed() {
      this.speedIndex = (this.speedIndex + 1) % this.speeds.length
      this.$emit('replay-speed', this.speeds[this.speedIndex])
    }
  }
}
</script>

<style scoped>
.enso-map-panel{position:relative;isolation:isolate;display:block;width:100%;height:100%;min-height:320px;overflow:hidden;border:1px solid rgba(55,143,171,.34);border-radius:7px;background:radial-gradient(ellipse at 50% 48%,rgba(16,91,117,.44),transparent 62%),#061a2a;color:#dff8ff}
.map-2d,.map-vignette{position:absolute;inset:0}.map-2d{z-index:0;background:#061a2a}.map-vignette{z-index:1;pointer-events:none;background:radial-gradient(ellipse at center,transparent 48%,rgba(2,12,24,.46) 100%),linear-gradient(180deg,rgba(3,16,28,.14),transparent 24%,transparent 77%,rgba(3,16,28,.35))}
.map-toolbar{position:absolute;z-index:5;top:12px;display:flex;gap:4px;padding:4px;border:1px solid rgba(64,164,192,.3);border-radius:7px;background:rgba(3,20,34,.76);box-shadow:0 8px 22px rgba(0,0,0,.2);backdrop-filter:blur(9px)}.map-toolbar-left{left:12px}.map-toolbar-right{right:12px}.map-toolbar button{display:inline-flex;align-items:center;gap:5px;min-height:29px;padding:0 9px;border:1px solid transparent;border-radius:4px;color:#96b6c4;background:transparent;font:600 10px/1 inherit;cursor:pointer;transition:color .2s,border-color .2s,background .2s}.map-toolbar button:hover,.map-toolbar button.active{border-color:rgba(64,211,220,.34);color:#c7fbf6;background:rgba(17,110,128,.32)}.map-toolbar button:disabled{opacity:.36;cursor:not-allowed}.map-toolbar .maximize-button{margin-left:3px;padding:0 10px;border-left:1px solid rgba(105,162,182,.23);border-radius:0;color:#dcf8ff}.map-toolbar .maximize-button:hover{color:#fff;background:rgba(25,117,147,.34)}
.sst-legend{position:absolute;z-index:4;right:15px;bottom:37px;width:min(250px,38%);padding:8px 10px;border:1px solid rgba(91,170,191,.28);border-radius:5px;background:rgba(3,20,34,.76);backdrop-filter:blur(8px)}.sst-legend-bar{height:7px;border-radius:5px}.sst-legend-labels{display:flex;justify-content:space-between;margin-top:5px;color:#a8c2cd;font-size:9px;font-variant-numeric:tabular-nums}.data-badge{position:absolute;z-index:4;left:14px;bottom:12px;display:flex;align-items:center;gap:7px;padding:6px 9px;border:1px solid rgba(67,151,176,.25);border-radius:4px;color:#8faebb;background:rgba(3,20,34,.74);font-size:9px;backdrop-filter:blur(8px)}.data-badge i{width:6px;height:6px;border-radius:50%;background:#5adcc8;box-shadow:0 0 8px #5adcc8}.data-badge.is-live i{background:#67f3b5;box-shadow:0 0 8px #67f3b5}
.enso-map-panel>>>.enso-globe-stage{background:radial-gradient(ellipse at center,rgba(14,70,95,.16),transparent 60%),#061a2a}.enso-map-panel>>>.globe-controls{right:15px;bottom:17px}.enso-map-panel>>>.globe-control{width:32px;height:32px;border-radius:5px;background:rgba(4,22,37,.83)}.enso-map-panel>>>.globe-region-label{z-index:3}.enso-map-panel>>>.leaflet-container{font-family:Inter,"PingFang SC","Microsoft YaHei",sans-serif;background:#071a2a}.enso-map-panel>>>.enso-basemap-img{filter:saturate(.8) brightness(.85) contrast(1.05)}.enso-map-panel>>>.leaflet-control-attribution{display:none}
.enso-map-panel>>>.nino34-icon{width:0;height:0}.enso-map-panel>>>.nino34-card{position:absolute;left:10px;top:0;transform:translateY(-50%);display:flex;flex-direction:column;gap:1px;padding:5px 8px 6px;border:1px solid rgba(255,214,130,.82);border-left:3px solid #ffd36a;background:rgba(6,20,29,.92);box-shadow:0 6px 18px rgba(0,0,0,.44);white-space:nowrap}.enso-map-panel>>>.nino34-title{color:#fff4d1;font-size:11px;line-height:1.25;font-weight:600}.enso-map-panel>>>.nino34-card small{color:#afc5cb;font-size:9px;line-height:1.3}
.is-compact .map-toolbar{top:8px;gap:2px;padding:3px}.is-compact .map-toolbar-left{left:8px}.is-compact .map-toolbar-right{right:8px}.is-compact .map-toolbar button{min-height:25px;padding:0 6px;font-size:9px}.is-compact .map-toolbar button i{display:none}.is-compact .map-toolbar .maximize-button i{display:inline}.is-compact .sst-legend{right:9px;bottom:31px;padding:6px 8px}.is-compact .data-badge{left:8px;bottom:8px;padding:5px 7px;font-size:8px}
.is-maximized{border-color:rgba(61,190,205,.45);border-radius:0}.is-maximized .map-toolbar{top:20px}.is-maximized .map-toolbar-left{left:22px}.is-maximized .map-toolbar-right{right:22px}.is-maximized .sst-legend{right:24px;bottom:45px}.is-maximized .data-badge{left:22px;bottom:20px}
@media(max-width:700px){.enso-map-panel{min-height:280px}.map-toolbar{top:7px}.map-toolbar button{min-height:26px;padding:0 6px;font-size:9px}.map-toolbar-left{left:7px}.map-toolbar-right{right:7px}.map-toolbar .maximize-button span{display:none}.map-toolbar .maximize-button{padding:0 7px}.is-compact .map-toolbar-left{left:7px}.is-compact .map-toolbar-right{right:7px}.sst-legend{width:34%;right:7px;bottom:30px}}

/* ---- A11 · HUD 取景框 / 扫描光带 ---- */
.map-hud{position:absolute;inset:0;z-index:3;pointer-events:none}
.hud-corner{position:absolute;width:20px;height:20px;border:1px solid rgba(94,224,226,.42)}
.hud-tl{top:8px;left:8px;border-right:0;border-bottom:0}
.hud-tr{top:8px;right:8px;border-bottom:0;border-left:0}
.hud-bl{bottom:8px;left:8px;border-top:0;border-right:0}
.hud-br{bottom:8px;right:8px;border-top:0;border-left:0}
.hud-scanline{position:absolute;left:0;right:0;height:22%;background:linear-gradient(180deg,transparent,rgba(86,226,230,.06),transparent);animation:hudScan 7s linear infinite}
@keyframes hudScan{0%{top:-24%}100%{top:102%}}

/* ---- A1 · 十字探针 ---- */
.map-crosshair{position:absolute;inset:0;z-index:4;pointer-events:none}
.crosshair-line{position:absolute;background:rgba(126,240,240,.26)}
.crosshair-x{left:0;right:0;height:1px}
.crosshair-y{top:0;bottom:0;width:1px}
.map-probe{position:absolute;z-index:6;min-width:152px;margin:15px 0 0 15px;padding:7px 9px;border:1px solid rgba(96,214,226,.4);border-radius:5px;background:rgba(4,22,36,.93);box-shadow:0 8px 22px rgba(0,0,0,.42);backdrop-filter:blur(8px);pointer-events:none}
.map-probe.flipped{margin-left:0;margin-right:15px;transform:translateX(-100%)}
.probe-head{display:flex;justify-content:space-between;gap:10px;padding-bottom:5px;border-bottom:1px solid rgba(84,168,190,.22);color:#8fd7e0;font-size:9px;font-variant-numeric:tabular-nums}
.probe-mode{color:#6f96a6;font-size:8px}
.probe-grid{display:grid;grid-template-columns:auto auto;gap:4px 12px;margin-top:6px;font-size:9px}
.probe-grid span{color:#7e9dad}
.probe-grid strong{color:#e6fbff;font-weight:500;font-variant-numeric:tabular-nums}
.probe-grid .warm-text{color:#ffb875}
.probe-grid .cool-text{color:#79c8f5}

/* ---- A2 · 点位时序探针 ---- */
.probe-pins{position:absolute;z-index:6;left:14px;bottom:38px;width:236px;padding:8px 9px;border:1px solid rgba(96,214,226,.32);border-radius:6px;background:rgba(4,22,36,.93);box-shadow:0 10px 26px rgba(0,0,0,.42);backdrop-filter:blur(8px)}
.probe-pins-head{display:flex;justify-content:space-between;align-items:center;color:#8fd7e0;font-size:9px}
.probe-pins-head span{display:flex;align-items:center;gap:6px}
.probe-pins-head i{width:5px;height:5px;border-radius:50%;background:#ffd694;box-shadow:0 0 7px #ffd694}
.probe-pins-head button{border:0;background:none;color:#7e9dad;font-family:inherit;font-size:8px;cursor:pointer}
.probe-pins-head button:hover{color:#ffd694}
.probe-pins ul{display:flex;max-height:152px;flex-direction:column;gap:7px;margin:7px 0 0;padding:0;overflow:auto;list-style:none}
.probe-pins li{display:flex;align-items:flex-start;gap:7px}
.pin-index{display:grid;width:16px;height:16px;flex:0 0 16px;place-items:center;border:1px solid rgba(255,206,120,.5);border-radius:50%;color:#ffd694;font-size:8px}
.pin-body{flex:1;min-width:0}
.pin-meta{display:flex;justify-content:space-between;gap:8px;font-size:8px}
.pin-meta b{color:#cfeaf1;font-weight:500}
.pin-meta em{color:#ffcf8a;font-style:normal;font-variant-numeric:tabular-nums}
.pin-meta em.warm-text{color:#ffb875}
.pin-meta em.cool-text{color:#79c8f5}
.pin-spark{width:100%;height:22px;margin-top:3px}
.pin-zero{stroke:rgba(120,160,180,.32);stroke-width:1}
.pin-line{fill:none;stroke:#5fe3d6;stroke-width:1.4}
.pin-body small{display:block;margin-top:2px;color:#6f8fa0;font-size:7px}
.is-compact .probe-pins{width:200px;bottom:32px}
.is-maximized .probe-pins{left:22px;bottom:46px}
@media(max-width:700px){.probe-pins{display:none}}

/* ---- A14 · 内嵌回放控制条 ---- */
.map-replay{position:absolute;z-index:6;top:52px;left:50%;display:flex;align-items:center;gap:7px;padding:5px 9px;border:1px solid rgba(96,214,226,.32);border-radius:20px;background:rgba(4,22,36,.9);box-shadow:0 8px 22px rgba(0,0,0,.36);backdrop-filter:blur(8px);transform:translateX(-50%)}
.map-replay button{border:0;background:none;color:#8fb6c4;font-size:12px;cursor:pointer;transition:color .18s}
.map-replay button:hover{color:#e6feff}
.map-replay .replay-main{display:grid;width:24px;height:24px;place-items:center;border:1px solid rgba(96,224,226,.45);border-radius:50%;color:#8ef0e6;background:rgba(20,96,112,.32);font-size:11px}
.replay-progress{position:relative;width:118px;height:3px;overflow:hidden;border-radius:3px;background:rgba(28,70,93,.9)}
.replay-progress i{position:absolute;inset:0 auto 0 0;background:linear-gradient(90deg,#4fd8d0,#ffc477)}
.replay-readout{color:#a9ccd8;font-size:9px;font-variant-numeric:tabular-nums;white-space:nowrap}
.replay-speed{padding:3px 6px;border:1px solid rgba(96,214,226,.28);border-radius:3px;font-size:8px}
.is-maximized .map-replay{top:60px}
@media(max-width:700px){.map-replay{gap:5px;padding:4px 7px}.replay-progress{width:70px}.replay-readout{display:none}}
</style>
