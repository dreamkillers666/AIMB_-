<template>
  <section class="enso-map-panel" :class="{ 'is-compact': compact, 'is-maximized': maximized }">
    <EnsoGlobe
      v-if="displayMode === '3d'"
      :wind-data="windData"
      :sst-data="sstData"
      :layer="currentLayer"
      layout="center"
      @unavailable="handleGlobeUnavailable"
    />
    <div v-show="displayMode === '2d'" ref="mapEl" class="map-2d"></div>
    <div class="map-vignette"></div>

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

    <div v-if="(currentLayer === 'sst' || currentLayer === 'both') && hasSst" class="sst-legend" aria-label="海表温度图例">
      <div class="sst-legend-bar"></div><div class="sst-legend-labels"><span>−2</span><span>8</span><span>16</span><span>24</span><span>32°C</span></div>
    </div>
    <div class="data-badge" :class="{ 'is-live': windStatus.source === 'real' }"><i></i>{{ windBadgeText }}</div>
  </section>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import EnsoGlobe from './EnsoGlobe.vue'
import presetSst from '../data/presetSst.json'

if (!window.L) window.L = L
require('leaflet-velocity')

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
      const pxPerDeg = (256 * Math.pow(2, zoom)) / 360;
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
    // 用 ImageData 直接写像素（比逐格 fillRect 快得多）；复用缓存避免动画逐帧 GC
    if (!this._imgData || this._imgData.width !== ow || this._imgData.height !== oh) {
      this._imgData = octx.createImageData(ow, oh);
    }
    const img = this._imgData;
    const px = img.data;
    for (let y = 0; y < oh; y += step) {
      const j0 = (la1 - rowLats[y]) / dy;
      const yEnd = Math.min(y + step, oh);
      for (let x = 0; x < ow; x += step) {
        const i0 = (colLons[x] - lo1) / dx;
        const val = this._bilinear(data, nx, ny, i0, j0);
        if (val === null) continue; // 陆地/缺测 → 透明
        const t = Math.max(-2, Math.min(32, val));
        const ci = Math.round(((t + 2) / 34) * 255);
        const c = this._lut[ci];
        const xEnd = Math.min(x + step, ow);
        for (let yy = y; yy < yEnd; yy++) {
          const rowBase = yy * ow;
          for (let xx = x; xx < xEnd; xx++) {
            const o = (rowBase + xx) * 4;
            px[o] = c[0];
            px[o + 1] = c[1];
            px[o + 2] = c[2];
            px[o + 3] = 255;
          }
        }
      }
    }
    octx.putImageData(img, 0, 0);
  },

  // 双线性插值：在网格点之间插值，返回插值后的海温；越界或缺测返回 null
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
    const v00 = data[base + i];
    const v10 = data[base + i1];
    const v01 = data[base1 + i];
    const v11 = data[base1 + i1];
    // 任一邻居缺测（陆地边缘）→ 不插值，保持透明
    if (!this._isValid(v00) || !this._isValid(v10) || !this._isValid(v01) || !this._isValid(v11)) return null;
    const v0 = v00 * (1 - fi) + v10 * fi;
    const v1 = v01 * (1 - fi) + v11 * fi;
    return v0 * (1 - fj) + v1 * fj;
  },

  _isValid(v) {
    return typeof v === 'number' && !isNaN(v) && Math.abs(v) < 100;
  },

  // 海温 -2~32°C 海洋风色带：蓝（冷）→ 青 → 绿 → 黄 → 红（暖），256 级查色表
  _buildLut() {
    const stops = [
      { t: -2, c: [15, 45, 120] },
      { t: 4, c: [30, 90, 170] },
      { t: 10, c: [40, 140, 190] },
      { t: 16, c: [45, 180, 170] },
      { t: 20, c: [110, 200, 120] },
      { t: 24, c: [220, 210, 90] },
      { t: 28, c: [235, 150, 60] },
      { t: 32, c: [210, 60, 40] },
    ];
    const lut = [];
    for (let k = 0; k < 256; k++) {
      const t = -2 + (34 * k) / 255;
      let c = stops[stops.length - 1].c;
      if (t <= stops[0].t) {
        c = stops[0].c;
      } else {
        for (let s = 1; s < stops.length; s++) {
          if (t <= stops[s].t) {
            const a = stops[s - 1];
            const b = stops[s];
            const f = (t - a.t) / (b.t - a.t);
            c = [
              Math.round(a.c[0] + (b.c[0] - a.c[0]) * f),
              Math.round(a.c[1] + (b.c[1] - a.c[1]) * f),
              Math.round(a.c[2] + (b.c[2] - a.c[2]) * f),
            ];
            break;
          }
        }
      }
      lut.push(c);
    }
    this._lut = lut;
  },
})

export default {
  name: 'EnsoMapPanel',
  components: { EnsoGlobe },
  props: {
    compact: { type: Boolean, default: false },
    maximized: { type: Boolean, default: false }
  },
  data() {
    return {
      displayMode: '3d',
      currentLayer: 'both',
      windData: [],
      sstData: null,
      hasSst: false,
      windStatus: { source: 'local', refTime: null },
      map: null,
      velocityLayer: null,
      sstLayer: null,
      pollTimer: null,
      destroyed: false,
      fetchController: null
    }
  },
  computed: {
    windBadgeText() {
      if (this.windStatus.source !== 'real') return '全球风场 · 海温监测'
      return 'GFS 分析场 · ' + (this.windStatus.refTime || '最新时次')
    }
  },
  watch: {
    maximized() {
      this.$nextTick(() => this.resizeMap())
    }
  },
  mounted() {
    this.applyLocalData()
    this.loadRealWind()
    this.pollTimer = window.setInterval(this.refreshRealWind, 30 * 60 * 1000)
  },
  beforeDestroy() {
    this.destroyed = true
    if (this.pollTimer) window.clearInterval(this.pollTimer)
    if (this.fetchController) this.fetchController.abort()
    if (this.map) {
      this.map.off()
      this.map.remove()
      this.map = null
    }
    this.velocityLayer = null
    this.sstLayer = null
  },
  methods: {
    applyLocalData() {
      this.windData = this.generateWindData()
      this.sstData = this.normalizeGridField(presetSst)
      this.hasSst = !!this.sstData
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
      this.windData = wind
      if (sst) this.sstData = sst
      this.hasSst = !!this.sstData
      this.windStatus = { source: 'real', refTime: this.formatTime(wind[0].header.refTime) }
      if (this.velocityLayer) this.velocityLayer.setData(wind)
      if (this.sstLayer) this.sstLayer.setData(this.sstData)
      if (this.displayMode === '3d' && this.velocityLayer && this.velocityLayer.pause) this.velocityLayer.pause()
      if (!this.hasSst && this.currentLayer !== 'wind') this.switchLayer('wind')
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
          this.applyLayerVisibility()
        })
      } else if (this.velocityLayer && this.velocityLayer.pause) {
        this.velocityLayer.pause()
      }
    },
    handleGlobeUnavailable() { this.switchDisplayMode('2d') },
    initMap() {
      const el = this.$refs.mapEl
      if (!el || this.map) return
      this.map = L.map(el, {
        zoomControl: false, attributionControl: true, minZoom: 2, maxZoom: 8,
        center: [8, 170], zoom: 2.5, zoomSnap: 0.5, worldCopyJump: true,
        fadeAnimation: true, bounceAtZoomLimits: false, dragging: true, tap: true,
        touchZoom: true, scrollWheelZoom: true, inertia: true
      })
      this.map.setMaxBounds(L.latLngBounds([-85, -180], [85, 180]))
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 8, attribution: '&copy; OpenStreetMap contributors', noWrap: false
      }).addTo(this.map)
      this.velocityLayer = L.velocityLayer({
        displayValues: false, displayOptions: { velocityType: '', emptyString: '' },
        maxVelocity: 22, minVelocity: 0, velocityScale: 0.02, particleAge: 70,
        lineWidth: 1.2, particleMultiplier: 1 / 1800, frameRate: 20,
        colorScale: ['rgb(8, 34, 80)', 'rgb(12, 58, 122)', 'rgb(18, 88, 168)', 'rgb(26, 122, 200)', 'rgb(40, 156, 214)', 'rgb(66, 188, 224)', 'rgb(110, 214, 232)', 'rgb(150, 232, 238)'],
        data: this.windData
      }).addTo(this.map)
      this.velocityLayer.pause = () => { if (this.velocityLayer && this.velocityLayer._windy) this.velocityLayer._windy.stop() }
      this.velocityLayer.resume = () => { if (this.velocityLayer) this.velocityLayer._clearAndRestart() }
      this.sstLayer = new SstOverlayLayer().addTo(this.map)
      this.sstLayer.setData(this.sstData)
      if (this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas) this.velocityLayer._canvasLayer._canvas.style.zIndex = '2'
      this.map.on('dragstart', () => this.velocityLayer && this.velocityLayer.pause())
      this.map.on('dragend', () => this.velocityLayer && this.velocityLayer.resume())
      this.map.on('mousedown', e => {
        if (e.originalEvent && e.originalEvent.button === 1) {
          e.originalEvent.preventDefault()
          this.map.setView(this.map.mouseEventToLatLng(e.originalEvent), this.map.getZoom() + 1, { animate: true })
        }
      })
      this.applyLayerVisibility()
      this.resizeMap()
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
        if (this.velocityLayer.setOptions) this.velocityLayer.setOptions({ particleMultiplier: 1 / 2100 })
        this._showVelocityLayer()
      } else {
        if (this.velocityLayer.setOptions) this.velocityLayer.setOptions({ particleMultiplier: 1 / 1800 })
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
    }
  }
}
</script>

<style scoped>
.enso-map-panel{position:relative;isolation:isolate;display:block;width:100%;height:100%;min-height:320px;overflow:hidden;border:1px solid rgba(55,143,171,.34);border-radius:7px;background:radial-gradient(ellipse at 50% 48%,rgba(16,91,117,.44),transparent 62%),#061a2a;color:#dff8ff}
.map-2d,.map-vignette{position:absolute;inset:0}.map-2d{z-index:0;background:#061a2a}.map-vignette{z-index:1;pointer-events:none;background:radial-gradient(ellipse at center,transparent 48%,rgba(2,12,24,.46) 100%),linear-gradient(180deg,rgba(3,16,28,.14),transparent 24%,transparent 77%,rgba(3,16,28,.35))}
.map-toolbar{position:absolute;z-index:5;top:12px;display:flex;gap:4px;padding:4px;border:1px solid rgba(64,164,192,.3);border-radius:7px;background:rgba(3,20,34,.76);box-shadow:0 8px 22px rgba(0,0,0,.2);backdrop-filter:blur(9px)}.map-toolbar-left{left:12px}.map-toolbar-right{right:12px}.map-toolbar button{display:inline-flex;align-items:center;gap:5px;min-height:29px;padding:0 9px;border:1px solid transparent;border-radius:4px;color:#96b6c4;background:transparent;font:600 10px/1 inherit;cursor:pointer;transition:color .2s,border-color .2s,background .2s}.map-toolbar button:hover,.map-toolbar button.active{border-color:rgba(64,211,220,.34);color:#c7fbf6;background:rgba(17,110,128,.32)}.map-toolbar button:disabled{opacity:.36;cursor:not-allowed}.map-toolbar .maximize-button{margin-left:3px;padding:0 10px;border-left:1px solid rgba(105,162,182,.23);border-radius:0;color:#dcf8ff}.map-toolbar .maximize-button:hover{color:#fff;background:rgba(25,117,147,.34)}
.sst-legend{position:absolute;z-index:4;right:15px;bottom:37px;width:min(250px,38%);padding:8px 10px;border:1px solid rgba(91,170,191,.28);border-radius:5px;background:rgba(3,20,34,.76);backdrop-filter:blur(8px)}.sst-legend-bar{height:7px;border-radius:5px;background:linear-gradient(90deg,#0f2d78,#1e5aaa,#288cbe,#2db4aa,#6ec878,#dcd25a,#eb963c,#d23c28)}.sst-legend-labels{display:flex;justify-content:space-between;margin-top:5px;color:#a8c2cd;font-size:9px;font-variant-numeric:tabular-nums}.data-badge{position:absolute;z-index:4;left:14px;bottom:12px;display:flex;align-items:center;gap:7px;padding:6px 9px;border:1px solid rgba(67,151,176,.25);border-radius:4px;color:#8faebb;background:rgba(3,20,34,.74);font-size:9px;backdrop-filter:blur(8px)}.data-badge i{width:6px;height:6px;border-radius:50%;background:#5adcc8;box-shadow:0 0 8px #5adcc8}.data-badge.is-live i{background:#67f3b5;box-shadow:0 0 8px #67f3b5}
.enso-map-panel>>>.enso-globe-stage{background:radial-gradient(ellipse at center,rgba(14,70,95,.16),transparent 60%),#061a2a}.enso-map-panel>>>.globe-controls{right:15px;bottom:17px}.enso-map-panel>>>.globe-control{width:32px;height:32px;border-radius:5px;background:rgba(4,22,37,.83)}.enso-map-panel>>>.globe-region-label{z-index:3}.enso-map-panel>>>.leaflet-container{font-family:Inter,"PingFang SC","Microsoft YaHei",sans-serif;background:#071a2a}.enso-map-panel>>>.leaflet-tile{filter:saturate(.66) brightness(.72) hue-rotate(8deg)}.enso-map-panel>>>.leaflet-control-attribution{display:none}
.is-compact .map-toolbar{top:8px;gap:2px;padding:3px}.is-compact .map-toolbar-left{left:8px}.is-compact .map-toolbar-right{right:8px}.is-compact .map-toolbar button{min-height:25px;padding:0 6px;font-size:9px}.is-compact .map-toolbar button i{display:none}.is-compact .map-toolbar .maximize-button i{display:inline}.is-compact .sst-legend{right:9px;bottom:31px;padding:6px 8px}.is-compact .data-badge{left:8px;bottom:8px;padding:5px 7px;font-size:8px}
.is-maximized{border-color:rgba(61,190,205,.45);border-radius:0}.is-maximized .map-toolbar{top:20px}.is-maximized .map-toolbar-left{left:22px}.is-maximized .map-toolbar-right{right:22px}.is-maximized .sst-legend{right:24px;bottom:45px}.is-maximized .data-badge{left:22px;bottom:20px}
@media(max-width:700px){.enso-map-panel{min-height:280px}.map-toolbar{top:7px}.map-toolbar button{min-height:26px;padding:0 6px;font-size:9px}.map-toolbar-left{left:7px}.map-toolbar-right{right:7px}.map-toolbar .maximize-button span{display:none}.map-toolbar .maximize-button{padding:0 7px}.is-compact .map-toolbar-left{left:7px}.is-compact .map-toolbar-right{right:7px}.sst-legend{width:34%;right:7px;bottom:30px}}
</style>
