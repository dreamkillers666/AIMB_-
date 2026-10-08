<template>
  <div class="page-container">

    <!-- HERO 实时主场景：Ventusky 式流动海洋地图 -->
        <section class="hero">
          <EnsoGlobe
            v-if="displayMode === '3d'"
            :wind-data="windData || []"
            :sst-data="sstData"
            :layer="currentLayer"
            @unavailable="handleGlobeUnavailable"
          />
          <!-- Leaflet 2D 地图保留为可切换视图 -->
          <div v-show="displayMode === '2d'" ref="mapEl" class="hero-map"></div>
          <div class="hero-map-vignette"></div>

          <!-- 收起状态标签：贴在 hero 最左边缘 -->
          <div v-show="panelCollapsed" class="enso-collapsed-tab" @click="panelCollapsed = false">
            <i class="el-icon-arrow-right"></i>
            <span>ENSO 状态</span>
          </div>

          <div class="hero-content hero-two-col">
            <!-- 左侧 ENSO 状态块（悬浮在地图上方，可收起） -->
            <div class="enso-status-panel ocean-panel" :class="{ collapsed: panelCollapsed }">
              <div class="enso-status-header">
                <span class="enso-status-title">ENSO 实时状态</span>
                <div class="enso-status-header-actions">
                  <el-tooltip placement="right" effect="dark">
                    <div slot="content" class="enso-tooltip-content">
                      <div><strong>厄尔尼诺</strong>：赤道中东太平洋海温较常年偏暖</div>
                      <div><strong>拉尼娜</strong>：赤道中东太平洋海温较常年偏冷</div>
                      <div><strong>中性</strong>：赤道中东太平洋海温接近常年</div>
                    </div>
                    <i class="el-icon-question enso-status-help"></i>
                  </el-tooltip>
                  <button class="enso-collapse-btn" title="收起" @click="panelCollapsed = true">
                    <i class="el-icon-arrow-left"></i>
                  </button>
                </div>
              </div>
              <div class="enso-status-main">
                <div class="enso-status-cn" :class="statusClass">{{ statusTextCN }}</div>
                <div class="enso-status-en">({{ statusText }})</div>
                <div class="enso-status-desc">{{ statusDesc }}</div>
                <!-- Niño3.4 指数（GFS 海温实测推导，与地图海温同源） -->
                <div class="enso-nino34" v-if="nino34Text">
                  <span class="enso-nino34-label">Niño3.4 指数（GFS 实测）</span>
                  <span class="enso-nino34-value" :class="statusClass">{{ nino34Text }}</span>
                </div>
              </div>
              <div class="enso-status-prob-title">{{ probTitle }}</div>
              <div class="prob-bars-cn">
                <div class="prob-bar-row-cn">
                  <div class="prob-bar-track-cn">
                    <div class="prob-bar-fill-cn heat" :style="{ width: ensoProbs.elNino + '%' }"></div>
                  </div>
                  <div class="prob-bar-info-cn">
                    <span class="prob-bar-percent heat">{{ Math.round(displayProbs.elNino) }}%</span>
                    <span class="prob-bar-label-cn">厄尔尼诺</span>
                  </div>
                </div>
                <div class="prob-bar-row-cn">
                  <div class="prob-bar-track-cn">
                    <div class="prob-bar-fill-cn neutral" :style="{ width: ensoProbs.neutral + '%' }"></div>
                  </div>
                  <div class="prob-bar-info-cn">
                    <span class="prob-bar-percent neutral">{{ Math.round(displayProbs.neutral) }}%</span>
                    <span class="prob-bar-label-cn">中性</span>
                  </div>
                </div>
                <div class="prob-bar-row-cn">
                  <div class="prob-bar-track-cn">
                    <div class="prob-bar-fill-cn cold" :style="{ width: ensoProbs.laNina + '%' }"></div>
                  </div>
                  <div class="prob-bar-info-cn">
                    <span class="prob-bar-percent cold">{{ Math.round(displayProbs.laNina) }}%</span>
                    <span class="prob-bar-label-cn">拉尼娜</span>
                  </div>
                </div>
              </div>

              <!-- 预测入口按钮组 -->
              <div class="hero-actions">
                <div class="action-btn" @click="navigateTo('/usermanage/enso_pre')">
                  <span class="action-btn-icon">🌊</span>
                  <div>
                    <div class="action-btn-title">ENSO 预测</div>
                    <div class="action-btn-sub">LSTA-Swin / ConvLSTM</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 实时数据流 ticker -->
          <div class="ticker-bar">
            <div class="ticker-label">实时数据流</div>
            <div class="ticker-track">
              <div class="ticker-content">
                <span v-for="(item, idx) in tickerItems" :key="idx" class="ticker-item">
                  {{ item.label }}：<strong :style="item.color ? { color: item.color } : {}">{{ item.value }}</strong>
                </span>
                <span v-for="(item, idx) in tickerItems" :key="'dup-' + idx" class="ticker-item">
                  {{ item.label }}：<strong :style="item.color ? { color: item.color } : {}">{{ item.value }}</strong>
                </span>
              </div>
            </div>
          </div>

          <div class="view-mode-switcher" role="tablist" aria-label="大屏显示模式">
            <button
              type="button"
              role="tab"
              :aria-selected="displayMode === '3d'"
              :class="{ active: displayMode === '3d' }"
              @click="switchDisplayMode('3d')"
            >3D</button>
            <button
              type="button"
              role="tab"
              :aria-selected="displayMode === '2d'"
              :class="{ active: displayMode === '2d' }"
              @click="switchDisplayMode('2d')"
            >2D</button>
          </div>

          <!-- 图层切换器：风场 / 海温 / 叠加（地图右上角） -->
          <div class="layer-switcher">
            <button
              class="layer-switcher-btn"
              :class="{ active: currentLayer === 'wind' }"
              @click="switchLayer('wind')"
            >风场</button>
            <button
              class="layer-switcher-btn"
              :class="{ active: currentLayer === 'sst', disabled: !hasSst }"
              :disabled="!hasSst"
              :title="hasSst ? '海温图层' : '海温数据暂不可用'"
              @click="switchLayer('sst')"
            >海温</button>
            <button
              class="layer-switcher-btn"
              :class="{ active: currentLayer === 'both', disabled: !hasSst }"
              :disabled="!hasSst"
              :title="hasSst ? '风场 + 海温叠加' : '海温数据暂不可用'"
              @click="switchLayer('both')"
            >叠加</button>
          </div>

          <!-- 海温色标图例（海温 / 叠加模式显示） -->
          <div v-if="(currentLayer === 'sst' || currentLayer === 'both') && hasSst" class="sst-legend">
            <div class="sst-legend-bar"></div>
            <div class="sst-legend-labels">
              <span>-2</span>
              <span>8</span>
              <span>16</span>
              <span>24</span>
              <span>32°C</span>
            </div>
          </div>

          <!-- 风场数据源与时效（地图角落小字） -->
          <div class="wind-data-badge" :class="{ mock: windStatus.source === 'mock' }">
            <span class="wind-badge-dot"></span>
            {{ windBadgeText }}
          </div>
        </section>

    <!-- 下块：系统简介（独立于 hero 地图区） -->
    <section class="intro-section">
      <h2 class="content-title">系统简介</h2>
      <span class="main-text">
        气候预测是指通过收集、分析和解释历史气象资料和其他相关数据，利用气象、海洋、大气、水文和地球物理等学科知识，运用数值模型和统计方法，预测和估计未来气候变化的科学活动。气候预测主要分为短期预测和长期预测两种类型。
        <br/><br/>
        短期预测通常指对未来数天到几周的气候变化进行预测。其中，ENSO预测是一种重要的短期预测。ENSO现象是指热带太平洋东部海域海温和大气压力变化的相互作用，具有周期性和不规则性。ENSO预测主要是通过分析海洋温度、海洋表层高度、大气压力、风速、降水等多个因素的变化，来确定ENSO现象的强度、发展趋势和可能的影响。ENSO预测可提供重要的气候信息，对于相关领域的决策和规划具有重要的参考价值。
        <br/><br/>
        长期气候预测主要是针对几个月到几年的时间尺度进行预测。长期预测依赖于全球气候模式，以及大气、海洋、陆面和冰雪等系统的相互作用，通过对这些因素的分析和模拟，预测未来气候的趋势和变化。长期气候预测对于应对气候变化和环境保护等方面具有重要的意义。
      </span>
    </section>

    <div class="content-section">
      <!-- 研究主题区 -->
      <h2 class="content-title">研究主题</h2>
      <div class="topics-container">
        <div class="theme-item" @click="navigateTo('/usermanage/enso_pre')">
          <h3>ENSO预测</h3>
          <div class="image-wrapper">
            <img src="../assets/ENSO/elnino-banner.png" alt="ENSO预测" class="theme-image" />
          </div>
        </div>

      </div>

      <!-- 核心监测图表区 -->
      <section class="monitor-section">
        <h2 class="content-title">
          核心监测
          <span class="section-subtitle">ENSO 概率演变 · 多模型技巧对比</span>
        </h2>
        <div class="charts-grid">
          <div class="chart-panel">
            <div ref="ensoChart" class="chart-box"></div>
          </div>
          <div class="chart-panel">
            <div ref="modelChart" class="chart-box"></div>
          </div>
        </div>
      </section>

      <!-- 预测图轮播 -->
      <section class="carousel-section">
        <h2 class="content-title">
          最新预测图
          <span class="section-subtitle">自动轮播 · 点击放大</span>
        </h2>
        <div class="carousel-wrapper" @mouseenter="pauseCarousel" @mouseleave="resumeCarousel">
          <button class="carousel-arrow" @click="prevSlide">‹</button>
          <div class="carousel-track">
            <div class="carousel-slides" :style="{ transform: `translateX(-${currentSlide * (100 / slidesPerView)}%)` }">
              <div
                v-for="(slide, index) in carouselSlides"
                :key="index"
                class="carousel-slide"
                :class="slide.gradient"
                @click="openLightbox(index)"
              >
                <div class="carousel-slide-label">{{ slide.label }}</div>
                <div class="carousel-slide-sub">{{ slide.sub }}</div>
              </div>
            </div>
          </div>
          <button class="carousel-arrow" @click="nextSlide">›</button>
        </div>
        <div class="carousel-indicators">
          <div
            v-for="index in maxSlideIndex + 1"
            :key="index"
            class="carousel-dot"
            :class="{ active: currentSlide === index - 1 }"
            @click="goToSlide(index - 1)"
          ></div>
        </div>
      </section>
    </div>

    <!-- 图片放大遮罩 -->
    <div v-if="lightboxVisible" class="lightbox-overlay" @click="closeLightboxOnBackdrop">
      <div class="lightbox-close" @click="closeLightbox">&times;</div>
      <div class="lightbox-main">
        <button class="carousel-arrow" @click.stop="lightboxPrev">‹</button>
        <div class="lightbox-box" :class="carouselSlides[lightboxIndex].gradient">
          <div class="lightbox-box-title">{{ carouselSlides[lightboxIndex].label }}</div>
          <div class="lightbox-box-sub">{{ carouselSlides[lightboxIndex].sub }}</div>
        </div>
        <button class="carousel-arrow" @click.stop="lightboxNext">›</button>
      </div>
      <div class="lightbox-label">
        {{ lightboxIndex + 1 }} / {{ carouselSlides.length }} · {{ carouselSlides[lightboxIndex].label }}
      </div>
    </div>
  </div>
</template>

<script>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import EnsoGlobe from '../components/EnsoGlobe.vue';
import presetSst from '../data/presetSst.json';
// leaflet-velocity 依赖全局 L，先挂到 window 再加载
if (!window.L) window.L = L;
require('leaflet-velocity');

// ===================== 海温 canvas 覆盖层 =====================
// 平滑渲染：离屏缓存 + 双线性插值（在网格点之间插值，消除 1° 块状感）。
// 拖拽跟随：canvas 挂在 overlayPane；move 时廉价定位 + 平移离屏缓存（不重绘 SST），
// moveend/zoomend/resize 时重渲染离屏缓存。陆地缺测值（NaN/999999）保持透明。
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
});

export default {
  name: "introducePage",
  components: { EnsoGlobe },
  data() {
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      announcement: {
        title: "",
        content: "",
        releaseTime: "",
        author: "",
      },
      usertotal: 0,
      ensoProbs: { laNina: 15, neutral: 25, elNino: 60 },
      displayProbs: { laNina: 15, neutral: 25, elNino: 60 },
      hasRealEnso: false,
      panelCollapsed: false,
      iriPublishedAt: "--",
      cpcFetchedAt: "--",
      ensoIriData: null,
      modelData: null,
      ensoSeason: null,
      nino34: null,
      charts: {},
      map: null,
      velocityLayer: null,
      tileLayer: null,        // CARTO voyager 亮色底图
      intervals: [],
      carouselTimer: null,
      currentSlide: 0,
      slidesPerView: 3,
      destroyed: false,
      lightboxVisible: false,
      lightboxIndex: 0,
      windStatus: { source: "mock", refTime: null },
      windRefTime: null,
      currentLayer: "wind",
      displayMode: "3d",
      hasSst: false,
      windData: null,
      sstData: null,
      sstLayer: null,
      carouselSlides: [
        { label: "SST 预测图", sub: "未来 3 个月海表温度", gradient: "gradient-1" },
        { label: "距平图", sub: "Nino3.4 区域距平", gradient: "gradient-2" },
        { label: "时序图", sub: "逐月演变曲线", gradient: "gradient-3" },
        { label: "相关系数图", sub: "模型技巧评分", gradient: "gradient-4" },
        { label: "概率分布图", sub: "ENSO 三态概率", gradient: "gradient-5" },
        { label: "空间分布图", sub: "全球异常分布", gradient: "gradient-6" },
      ],
    };
  },

  computed: {
    statusText() {
      // 优先级：nino34.status（GFS 海温实测）→ 概率主导 → 模拟
      if (this.nino34 && this.nino34.status) return this.nino34.status;
      const { laNina, neutral, elNino } = this.ensoProbs;
      if (elNino >= laNina && elNino >= neutral) return "El Niño";
      if (laNina >= elNino && laNina >= neutral) return "La Niña";
      return "Neutral";
    },
    probTitle() {
      const base = "未来一季 ENSO 状态概率";
      return this.ensoSeason ? `未来一季（${this.ensoSeason}）ENSO 状态概率` : base;
    },
    nino34Text() {
      if (!this.nino34) return null;
      // 有距平：带正负号、保留 2 位小数
      if (this.nino34.anomaly_available && typeof this.nino34.anomaly === "number") {
        const sign = this.nino34.anomaly >= 0 ? "+" : "";
        return sign + this.nino34.anomaly.toFixed(2) + "°C";
      }
      // 无距平：显示绝对海温（1 位小数），再没有则隐藏
      if (typeof this.nino34.sst === "number") {
        return this.nino34.sst.toFixed(1) + "°C";
      }
      return null;
    },
    statusTextCN() {
      const map = { "El Niño": "厄尔尼诺", "La Niña": "拉尼娜", "Neutral": "中性" };
      return map[this.statusText] || "中性";
    },
    statusClass() {
      const map = { "El Niño": "heat", "La Niña": "cold", "Neutral": "neutral" };
      return map[this.statusText] || "neutral";
    },
    statusDesc() {
      const map = {
        "El Niño": "赤道中东太平洋海温较常年偏暖",
        "La Niña": "赤道中东太平洋海温较常年偏冷",
        "Neutral": "赤道中东太平洋海温接近常年",
      };
      return map[this.statusText] || "赤道中东太平洋海温接近常年";
    },
    dominantSignal() {
      const map = { "El Niño": "El Niño 偏强", "La Niña": "La Niña 偏强", "Neutral": "Neutral 主导" };
      return map[this.statusText] || "--";
    },
    tickerItems() {
      return [
        { label: "IRI 发布时间", value: "2026/8/15 08:00:00" },
        { label: "CPC 更新时间", value: "2026/8/15 08:00:00" },
        { label: "主导信号", value: this.dominantSignal, color: this.getStatusColor() },
        { label: "模型状态", value: "运行正常", color: "#5aa7ff" },
      ];
    },
    maxSlideIndex() {
      return Math.max(0, this.carouselSlides.length - this.slidesPerView);
    },
    windBadgeText() {
      const labels = { wind: "GFS 风场", sst: "GFS 海温", both: "GFS 风场+海温" };
      const label = labels[this.currentLayer] || "GFS 风场";
      if (this.windStatus.source !== "real") {
        return { wind: "风场", sst: "海温", both: "风场+海温" }[this.currentLayer] || "风场";
      }
      return label + " · 起报时间 " + (this.windStatus.refTime || "--");
    },
  },

  watch: {
    ensoProbs: {
      deep: true,
      handler(newVal, oldVal) {
        this.animateProbs(oldVal, newVal);
      },
    },
  },

  created() {
    this.loadData();
  },

  mounted() {
    this.$nextTick(() => {
      this.updateSlidesPerView();
      this.applyMockWindData();
      if (this.displayMode === "2d") this.initMap();
      this.initCharts();
      this.setupResizeListener();
      this.startPolling();
    });
  },

  beforeDestroy() {
    this.destroyed = true;
    this.clearIntervals();
    this.disposeCharts();
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
    window.removeEventListener("resize", this.handleResize);
  },

  methods: {
    // ===================== 数据加载 =====================
    loadData() {
      this.announcement = {
        title: "ENSO 预制案例",
        content: "当前页面展示本地归档案例，不连接实时数据服务。",
        releaseTime: "2025-08-15",
        author: "AIMB 演示系统"
      };
      this.usertotal = 128;
      this.iriPublishedAt = "2026/8/15 08:00:00";
      this.cpcFetchedAt = "2026/8/15 08:00:00";
      this.ensoProbs = { laNina: 15, neutral: 25, elNino: 60 };
      this.ensoIriData = this.generateMockEnsoIri();
      this.modelData = this.generateMockModelData();
    },

    loadAnnouncement() {
      this.request.get("/announcement/maxId")
        .then((res) => {
          const d = this.extractData(res);
          if (d) {
            this.announcement = {
              title: d.title || "",
              content: d.content || "",
              releaseTime: d.releaseTime || "",
              author: d.author || "",
            };
          }
        })
        .catch((err) => console.warn("公告加载失败", err));
    },

    loadUserTotal() {
      this.request.get("/user/total")
        .then((res) => {
          const data = this.extractData(res);
          if (data !== undefined && data !== null) {
            this.usertotal = data;
          }
        })
        .catch((err) => console.warn("用户总数加载失败", err));
    },

    loadEnsoLatest() {
      this.request.get("/enso/latest")
        .then((res) => {
          const d = this.extractData(res);
          if (d) {
            this.iriPublishedAt = "2026/8/15 08:00:00";
            this.cpcFetchedAt = "2026/8/15 08:00:00";
            let probs = null;
            // 优先级 1：probabilities（新字段，CPC 实测，0-100 整数）
            if (d.probabilities && typeof d.probabilities === "object") {
              const p = d.probabilities;
              if (p.laNina !== undefined && p.neutral !== undefined && p.elNino !== undefined) {
                probs = this.normalizeProbs({
                  laNina: p.laNina,
                  neutral: p.neutral,
                  elNino: p.elNino,
                });
                this.ensoSeason = p.season || null;
              }
            }
            // 优先级 2：iri[0]（历史 IRI 数据）
            if (!probs && Array.isArray(d.iri) && d.iri.length > 0) {
              const latest = d.iri[0];
              probs = this.normalizeProbs({
                laNina: latest.laNina,
                neutral: latest.neutral,
                elNino: latest.elNino,
              });
              this.ensoSeason = null; // 季节标注仅来自 probabilities.season
            }
            if (probs) {
              this.ensoProbs = probs;
              this.hasRealEnso = true;
            }
          }
        })
        .catch((err) => {
          console.warn("ENSO 最新状态加载失败，使用模拟数据", err);
          this.hasRealEnso = false;
        });
    },

    loadEnsoIri() {
      this.request.get("/enso/iri")
        .then((res) => {
          const data = this.extractData(res);
          if (Array.isArray(data) && data.length > 0) {
            this.ensoIriData = data.map((item) => ({
              season: item.season,
              laNina: this.toPercent(item.laNina),
              neutral: this.toPercent(item.neutral),
              elNino: this.toPercent(item.elNino),
            }));
          } else {
            console.warn("ENSO IRI 数据为空，使用模拟数据");
            this.ensoIriData = this.generateMockEnsoIri();
          }
          this.renderEnsoChart();
        })
        .catch((err) => {
          console.warn("ENSO IRI 数据加载失败，使用模拟数据", err);
          this.ensoIriData = this.generateMockEnsoIri();
          this.renderEnsoChart();
        });
    },

    loadModelComparison() {
      const modelNames = ["SINTEX_F", "CNN", "Our_model", "Transformer", "GRU", "STANet"];
      Promise.all(
        modelNames.map((name) =>
          this.request.get(`/echarts/${name}`).then((res) => {
            const data = this.extractData(res);
            return Array.isArray(data) ? data : null;
          }).catch(() => null)
        )
      )
        .then((results) => {
          const hasReal = results.every((r) => r && r.length === 20);
          if (hasReal) {
            this.modelData = {};
            modelNames.forEach((name, idx) => {
              this.modelData[name] = results[idx];
            });
          } else {
            this.modelData = this.generateMockModelData();
          }
          this.renderModelChart();
        })
        .catch((err) => {
          console.warn("模型对比数据加载失败，使用模拟数据", err);
          this.modelData = this.generateMockModelData();
          this.renderModelChart();
        });
    },

    // ===================== 工具方法 =====================
    extractData(res) {
      if (!res) return null;
      return res.data !== undefined ? res.data : res;
    },

    toPercent(value) {
      if (value === undefined || value === null) return 0;
      const num = Number(value);
      if (isNaN(num)) return 0;
      return num <= 1 ? Math.round(num * 100) : Math.round(num);
    },

    normalizeProbs(probs) {
      const la = this.toPercent(probs.laNina);
      const ne = this.toPercent(probs.neutral);
      const el = this.toPercent(probs.elNino);
      const sum = la + ne + el;
      if (sum === 0) return { laNina: 15, neutral: 25, elNino: 60 };
      return {
        laNina: Math.round((la / sum) * 100),
        neutral: Math.round((ne / sum) * 100),
        elNino: 100 - Math.round((la / sum) * 100) - Math.round((ne / sum) * 100),
      };
    },

    formatTime(v) {
      if (!v || v === "--") return "--";
      try {
        const d = new Date(v);
        if (isNaN(d.getTime())) return v;
        return d.toLocaleString("zh-CN");
      } catch (e) {
        return v;
      }
    },

    getStatusColor() {
      const map = { heat: "#ff8a5c", cold: "#5aa7ff", neutral: "#9aa8b8" };
      return map[this.statusClass] || "#9aa8b8";
    },

    // ===================== 模拟数据生成 =====================
    generateMockEnsoIri() {
      const seasons = ["2024 JJA", "2024 JAS", "2024 ASO", "2024 SON", "2024 OND", "2024 NDJ", "2024 DJF", "2024 JFM"];
      const laNina = [15, 18, 22, 25, 28, 30, 32, 30];
      const neutral = [35, 32, 28, 24, 20, 18, 16, 17];
      const elNino = [50, 50, 50, 51, 52, 52, 52, 53];
      return seasons.map((season, idx) => ({
        season,
        laNina: laNina[idx],
        neutral: neutral[idx],
        elNino: elNino[idx],
      }));
    },

    generateMockModelData() {
      return {
        SINTEX_F: [0.89,0.87,0.83,0.80,0.75,0.72,0.70,0.65,0.63,0.60,0.55,0.51,0.48,0.47,0.46,0.45,0.40,0.35,0.32,0.31],
        CNN: [0.93,0.91,0.88,0.83,0.80,0.75,0.71,0.71,0.70,0.69,0.65,0.64,0.63,0.60,0.58,0.53,0.51,0.45,0.41,0.38],
        Our_model: [0.94,0.90,0.86,0.84,0.83,0.79,0.77,0.75,0.75,0.71,0.66,0.65,0.63,0.64,0.63,0.62,0.60,0.52,0.48,0.49],
        Transformer: [0.98,0.94,0.89,0.85,0.81,0.76,0.75,0.74,0.72,0.70,0.68,0.66,0.65,0.62,0.59,0.55,0.53,0.49,0.44,0.41],
        GRU: [0.93,0.91,0.89,0.86,0.81,0.78,0.73,0.68,0.65,0.61,0.57,0.51,0.50,0.48,0.44,0.41,0.40,0.38,0.37,0.32],
        STANet: [0.94,0.91,0.90,0.88,0.85,0.80,0.78,0.72,0.69,0.64,0.61,0.57,0.53,0.51,0.49,0.48,0.45,0.43,0.41,0.40]
      };
    },

    // ===================== 轮询与模拟波动 =====================
    startPolling() {
      this.startCarouselTimer();
    },

    clearIntervals() {
      this.intervals.forEach((id) => clearInterval(id));
      this.intervals = [];
      this.clearCarouselTimer();
    },

    startCarouselTimer() {
      this.clearCarouselTimer();
      this.carouselTimer = setInterval(() => {
        this.nextSlide();
      }, 4000);
    },

    clearCarouselTimer() {
      if (this.carouselTimer) {
        clearInterval(this.carouselTimer);
        this.carouselTimer = null;
      }
    },

    pauseCarousel() {
      this.clearCarouselTimer();
    },

    resumeCarousel() {
      this.startCarouselTimer();
    },

    // ===================== Leaflet 流动风场地图 =====================
    initMap() {
      const el = this.$refs.mapEl;
      if (!el || this.map) return;

      this.map = L.map(el, {
        zoomControl: false,
        attributionControl: false,
        // minZoom 3：世界地图高度 256×2^3=2048px，保证最小缩放时能填满 hero 高度（视口高-60px）
        minZoom: 3,
        maxZoom: 8,
        center: [18, 0],
        zoom: 3,
        zoomSnap: 0.5,
        worldCopyJump: true,
        fadeAnimation: true,
        // 拖到世界边界时直接停住，不回弹，避免露出空白
        bounceAtZoomLimits: false,
        // 显式开启交互，确保拖拽 / 缩放 / 惯性都可用
        dragging: true,
        tap: true,
        touchZoom: true,
        scrollWheelZoom: true,
        inertia: true,
      });

      // 限制拖拽边界为世界范围，任意方向都拖不出地图边缘（不会露出空白）
      this.map.setMaxBounds(L.latLngBounds([-85, -180], [85, 180]));

      // OpenStreetMap 公开底图，不依赖第三方 API Key
      this.tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 8,
        attribution: '&copy; OpenStreetMap contributors',
        noWrap: false,
      }).addTo(this.map);

      // 程序化生成全球风场并叠加流动粒子层
      const wind = this.generateWindData();
      this.velocityLayer = L.velocityLayer({
        displayValues: false,
        displayOptions: { velocityType: "", emptyString: "" },
        maxVelocity: 22,
        minVelocity: 0,
        velocityScale: 0.02,
        particleAge: 70,
        lineWidth: 1.2,
        // 粒子数量减半，减轻拖拽时动画开销
        particleMultiplier: 1 / 1800,
        frameRate: 20,
        // 亮底上使用深蓝→青→浅青的深色粒子，清晰可见
        colorScale: [
          "rgb(8, 34, 80)",
          "rgb(12, 58, 122)",
          "rgb(18, 88, 168)",
          "rgb(26, 122, 200)",
          "rgb(40, 156, 214)",
          "rgb(66, 188, 224)",
          "rgb(110, 214, 232)",
          "rgb(150, 232, 238)",
        ],
        data: wind,
      }).addTo(this.map);

      // 手动为 velocity 层补充 pause / resume（插件未暴露，底层 windy 支持 stop / _clearAndRestart）
      this.velocityLayer.pause = () => {
        if (this.velocityLayer && this.velocityLayer._windy) this.velocityLayer._windy.stop();
      };
      this.velocityLayer.resume = () => {
        if (this.velocityLayer) this.velocityLayer._clearAndRestart();
      };

      // 海温 canvas 覆盖层（默认隐藏，切到海温/叠加时显示）
      this.sstLayer = new SstOverlayLayer().addTo(this.map);

      // 叠加模式下粒子在色块上层：velocity canvas z-index 高于 sst canvas
      if (this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas) {
        this.velocityLayer._canvasLayer._canvas.style.zIndex = '2';
      }

      // 拖拽时暂停粒子动画，拖拽结束恢复 —— 让拖拽更流畅
      this.map.on('dragstart', () => this.velocityLayer.pause());
      this.map.on('dragend', () => this.velocityLayer.resume());

      // 中键单击 = 以光标为中心放大一级（并阻止中键默认自动滚动）
      this.map.on('mousedown', (e) => {
        if (e.originalEvent && e.originalEvent.button === 1) {
          e.originalEvent.preventDefault();
          const center = this.map.mouseEventToLatLng(e.originalEvent);
          this.map.setView(center, this.map.getZoom() + 1, { animate: true });
        }
      });
    },

    // 程序化生成有组织的全球风场（U/V 分量网格）
    generateWindData() {
      const nx = 180; // 2° 经度
      const ny = 90;  // 2° 纬度
      const lo1 = 0;  // 左上角经度
      const la1 = 90; // 左上角纬度（北纬 90）
      const dx = 2;
      const dy = 2;

      const u = new Array(nx * ny);
      const v = new Array(nx * ny);
      const vortices = [
        { longitude: 300, latitude: 44, strength: 12, radiusX: 27, radiusY: 19 },
        { longitude: 350, latitude: 58, strength: -10, radiusX: 24, radiusY: 17 },
        { longitude: 235, latitude: 28, strength: -11, radiusX: 23, radiusY: 17 },
        { longitude: 75, latitude: 46, strength: 10, radiusX: 26, radiusY: 18 },
        { longitude: 140, latitude: -38, strength: 12, radiusX: 26, radiusY: 19 },
        { longitude: 20, latitude: -52, strength: -11, radiusX: 24, radiusY: 18 },
        { longitude: 250, latitude: -22, strength: 9, radiusX: 22, radiusY: 16 },
      ];

      for (let j = 0; j < ny; j++) {
        const lat = la1 - j * dy;
        for (let i = 0; i < nx; i++) {
          const lon = lo1 + i * dx;
          const idx = j * nx + i;
          const latRad = (lat * Math.PI) / 180;

          const lonRad = (lon * Math.PI) / 180;
          // 保留行星风带的大尺度结构，同时加入经向波动，避免粒子收敛成规则纬向线。
          const trade = -Math.exp(-Math.pow(lat / 24, 2)) * 4;
          const westerly =
            Math.exp(-Math.pow((lat - 46) / 16, 2)) * 5 +
            Math.exp(-Math.pow((lat + 46) / 16, 2)) * 5;
          const polar =
            -Math.exp(-Math.pow((lat - 78) / 12, 2)) * 2.5 -
            Math.exp(-Math.pow((lat + 78) / 12, 2)) * 2.5;
          const meander =
            Math.sin(lonRad * 2 + latRad * 1.7) * 5 +
            Math.cos(lonRad * 3 - latRad * 2.4) * 2.5;
          let vortexU = 0;
          let vortexV = 0;
          vortices.forEach((vortex) => {
            const deltaLongitude = ((lon - vortex.longitude + 540) % 360) - 180;
            const x = deltaLongitude * Math.cos((vortex.latitude * Math.PI) / 180);
            const y = lat - vortex.latitude;
            const xScaled = x / vortex.radiusX;
            const yScaled = y / vortex.radiusY;
            const envelope = Math.exp(-0.5 * (xScaled * xScaled + yScaled * yScaled));
            vortexU -= vortex.strength * yScaled * envelope;
            vortexV += vortex.strength * xScaled * envelope;
          });

          u[idx] = trade + westerly + polar + meander + vortexU;

          // 南北向波动让流线具有经向交换，不只沿纬线平移。
          v[idx] =
            Math.sin(lonRad * 2 + latRad * 2.5) * 5 +
            Math.cos(lonRad * 3 - latRad * 1.8) * 3 +
            Math.sin(latRad * 4 + lonRad) * 2.5 +
            vortexV;
        }
      }

      const header = {
        lo1,
        la1,
        dx,
        dy,
        nx,
        ny,
        refTime: new Date().toISOString(),
        forecastTime: 0,
        gridDefinitionTemplate: 0,
      };

      // leaflet-velocity 要求：U/V 两条记录，parameterCategory/Number 区分
      return [
        { header: { ...header, parameterCategory: 2, parameterNumber: 2 }, data: u },
        { header: { ...header, parameterCategory: 2, parameterNumber: 3 }, data: v },
      ];
    },

    // ===================== 真实 GFS 风场 / 海温 / Niño3.4 =====================
    // 拉取后端 /wind 接口（{ u, v, sst, nino34 }，各含 header+data），5 秒超时
    fetchRealWind() {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), 5000);
      return fetch("http://localhost:8000/wind", { signal: controller.signal })
        .then((res) => {
          clearTimeout(timer);
          if (!res.ok) throw new Error("HTTP " + res.status);
          return res.json();
        })
        .then((raw) => {
          const wind = this.adaptWindData(raw);
          if (!wind) throw new Error("风场数据格式无效");
          const sst = this.adaptSstData(raw); // 后端可能尚未返回 sst → null
          const nino34 = this.adaptNino34(raw); // 后端可能尚未返回 nino34 → null
          return { wind, sst, nino34 };
        })
        .catch((err) => {
          clearTimeout(timer);
          throw err;
        });
    },

    // 页面加载：先尝试真实数据，失败回退模拟数据（不报错不白屏）
    loadRealWind() {
      this.fetchRealWind()
        .then(({ wind, sst, nino34 }) => this.applyWindData(wind, sst, nino34))
        .catch((err) => {
          console.warn("[wind] 真实 GFS 数据获取失败，回退到模拟数据", err);
          this.applyMockWindData();
        });
    },

    // 30 分钟轮询：刷新成功且起报时间变化时才更新图层，失败保留当前数据
    refreshRealWind() {
      this.fetchRealWind()
        .then(({ wind, sst, nino34 }) => {
          const newRef = wind[0] && wind[0].header ? wind[0].header.refTime : null;
          if (newRef && newRef !== this.windRefTime) {
            this.applyWindData(wind, sst, nino34);
          } else if (nino34) {
            // 起报时间未变时也同步 nino34（与海温同源，可能单独更新）
            this.nino34 = nino34;
          }
        })
        .catch((err) => {
          console.warn("[wind] 轮询刷新失败，保留当前风场", err);
        });
    },

    startWindPolling() {
      const windPoll = setInterval(() => {
        this.refreshRealWind();
      }, 30 * 60 * 1000);
      this.intervals.push(windPoll);
    },

    // 用真实数据更新 velocity 层 / sst 层 / nino34（setData 只换数据，不重建地图，保持用户视角）
    applyWindData(wind, sst, nino34) {
      if (this.destroyed) return;
      this.windData = wind;
      this.sstData = sst || null;
      // 海温模式需要 sst 数据（无 sst 时自动切回风场）
      this.hasSst = !!sst;
      this.nino34 = nino34 || null;
      if (this.velocityLayer) this.velocityLayer.setData(wind);
      if (this.sstLayer) this.sstLayer.setData(this.sstData);
      const refTime = wind[0] && wind[0].header ? wind[0].header.refTime : null;
      this.windRefTime = refTime;
      this.windStatus = {
        source: "real",
        refTime: refTime ? this.formatTime(refTime) : "--",
      };
      // 若当前在海温/叠加图层但 sst 不可用，自动切回风场
      if (this.currentLayer !== "wind" && !this.hasSst) {
        this.currentLayer = "wind";
        this.applyLayerVisibility();
      }
    },

    // 固定风场案例配套本地海温场，确保图层不依赖实时接口。
    applyMockWindData() {
      if (this.destroyed) return;
      this.windData = this.generateWindData();
      this.sstData = this.adaptSstData({ sst: presetSst });
      this.hasSst = !!this.sstData;
      this.nino34 = null;
      if (this.velocityLayer) this.velocityLayer.setData(this.windData);
      if (this.sstLayer) this.sstLayer.setData(this.sstData);
      this.windStatus = { source: "mock", refTime: null };
      if (this.currentLayer !== "wind" && !this.hasSst) {
        this.currentLayer = "wind";
        this.applyLayerVisibility();
      }
    },

    // ===================== 图层切换 =====================
    switchDisplayMode(mode) {
      if (mode !== "2d" && mode !== "3d") return;
      if (mode === this.displayMode) return;
      this.displayMode = mode;
      if (mode === "2d") {
        this.$nextTick(() => {
          if (!this.map) {
            this.initMap();
            this.applyMockWindData();
          } else {
            this.map.invalidateSize();
          }
          this.applyLayerVisibility();
        });
      }
    },

    handleGlobeUnavailable() {
      if (this.displayMode === "3d") this.switchDisplayMode("2d");
    },

    switchLayer(layer) {
      if (layer === this.currentLayer) return;
      if (layer !== "wind" && !this.hasSst) return; // 海温/叠加模式需要 sst 数据
      this.currentLayer = layer;
      this.applyLayerVisibility();
    },

    applyLayerVisibility() {
      if (!this.velocityLayer || !this.sstLayer) return;
      if (this.currentLayer === "sst") {
        this._hideVelocityLayer();
        // 海温模式：半透明海温（opacity=0.65），亮色底图上陆地透明露出国家边界
        this.sstLayer.setOpacity(0.65);
        this.sstLayer.show();
      } else if (this.currentLayer === "both") {
        // 叠加模式：半透明海温（0.42）+ 粒子流动
        this.sstLayer.setOpacity(0.42);
        this.sstLayer.show();
        this._applyParticleStyle(true);
        this._showVelocityLayer();
      } else {
        this._applyParticleStyle(false);
        this._showVelocityLayer();
        this.sstLayer.hide();
      }
    },

    // 叠加模式粒子微调：密度略降（1/2100），避免与 SST 底色叠加后杂乱；风场模式恢复默认
    _applyParticleStyle(both) {
      if (!this.velocityLayer || !this.velocityLayer.setOptions) return;
      const target = both ? 1 / 2100 : 1 / 1800;
      if (this.velocityLayer.options && this.velocityLayer.options.particleMultiplier === target) return;
      this.velocityLayer.setOptions({ particleMultiplier: target });
    },

    _hideVelocityLayer() {
      const canvas =
        this.velocityLayer && this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas;
      if (canvas) canvas.style.display = "none";
      if (this.velocityLayer.pause) this.velocityLayer.pause();
    },

    _showVelocityLayer() {
      const canvas =
        this.velocityLayer && this.velocityLayer._canvasLayer && this.velocityLayer._canvasLayer._canvas;
      if (canvas) canvas.style.display = "";
      if (this.velocityLayer.resume) this.velocityLayer.resume();
    },

    // ===================== 数据格式适配 =====================
    // 通用网格字段归一化：校验必需字段、二维展平、网格方向归一化（第 0 行在北纬）
    normalizeGridField(comp, parameterCategory, parameterNumber) {
      const header = Object.assign({}, comp.header);
      if (parameterCategory !== undefined) header.parameterCategory = parameterCategory;
      if (parameterNumber !== undefined) header.parameterNumber = parameterNumber;
      if (header.forecastTime === undefined) header.forecastTime = 0;
      if (header.gridDefinitionTemplate === undefined) header.gridDefinitionTemplate = 0;
      if (
        [header.lo1, header.la1, header.dx, header.dy, header.nx, header.ny].some(
          (v) => v === undefined || v === null
        )
      ) {
        return null;
      }
      let data = comp.data;
      if (!Array.isArray(data)) return null;
      // 兼容二维数组（行×列）→ 展平为 nx*ny 一维
      if (Array.isArray(data[0])) {
        const flat = [];
        for (let j = 0; j < data.length; j++) {
          for (let i = 0; i < data[j].length; i++) flat.push(data[j][i]);
        }
        data = flat;
      }
      if (data.length !== header.nx * header.ny) return null;
      // 网格方向归一化：leaflet-velocity 要求第 0 行位于北纬 la1、纬度随行号递减（dy>0）
      if (header.la2 !== undefined && header.la1 < header.la2) {
        const nx = header.nx;
        const flipped = new Array(data.length);
        for (let j = 0; j < header.ny; j++) {
          const srcRow = header.ny - 1 - j;
          for (let i = 0; i < nx; i++) {
            flipped[j * nx + i] = data[srcRow * nx + i];
          }
        }
        data = flipped;
        header.la1 = header.la2;
        header.dy = Math.abs(header.dy);
      }
      return { header, data };
    },

    // 将后端 { u, v } 结构适配为 leaflet-velocity 的数组格式
    adaptWindData(raw) {
      if (!raw || !raw.u || !raw.v) return null;
      const u = this.normalizeGridField(raw.u, 2, 2);
      const v = this.normalizeGridField(raw.v, 2, 3);
      if (!u || !v) return null;
      return [u, v];
    },

    // 海温场：与 u/v 同网格，供 canvas 覆盖层渲染；缺失返回 null
    adaptSstData(raw) {
      if (!raw || !raw.sst || !raw.sst.header || !raw.sst.data) return null;
      return this.normalizeGridField(raw.sst);
    },

    // Niño3.4 指数（GFS 海温实测推导）；缺失或字段无效返回 null
    adaptNino34(raw) {
      if (!raw || !raw.nino34 || typeof raw.nino34 !== "object") return null;
      const n = raw.nino34;
      return {
        sst: typeof n.sst === "number" && !isNaN(n.sst) ? n.sst : null,
        anomaly: typeof n.anomaly === "number" && !isNaN(n.anomaly) ? n.anomaly : null,
        status: ["El Niño", "La Niña", "Neutral"].indexOf(n.status) >= 0 ? n.status : null,
        climatology_month: n.climatology_month != null ? n.climatology_month : null,
        anomaly_available: !!n.anomaly_available,
      };
    },

    // ===================== ECharts =====================
    initCharts() {
      this.charts.enso = this.$echarts.init(this.$refs.ensoChart);
      this.charts.model = this.$echarts.init(this.$refs.modelChart);
      this.renderEnsoChart();
      this.renderModelChart();
    },

    renderEnsoChart() {
      if (!this.charts.enso) return;
      const data = this.ensoIriData || this.generateMockEnsoIri();
      const seasons = data.map((i) => i.season);
      const laNina = data.map((i) => i.laNina);
      const neutral = data.map((i) => i.neutral);
      const elNino = data.map((i) => i.elNino);

      this.charts.enso.setOption({
        backgroundColor: "transparent",
        title: { text: "ENSO 概率演变", left: 0, top: 0, textStyle: { color: "#dcebf8", fontSize: 16, fontWeight: 600 } },
        tooltip: { trigger: "axis", axisPointer: { type: "cross" } },
        legend: { data: ["La Niña", "Neutral", "El Niño"], top: 0, right: 0, textStyle: { color: "#a9c6e4" } },
        grid: { left: 40, right: 20, top: 50, bottom: 70 },
        dataZoom: [
          { type: "inside", start: 0, end: 100 },
          { type: "slider", start: 0, end: 100, bottom: 10, height: 20, borderColor: "rgba(90,167,255,0.2)", textStyle: { color: "#a9c6e4" } },
        ],
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: seasons,
          axisLine: { lineStyle: { color: "#7fa6cf" } },
          axisLabel: { color: "#a9c6e4", fontSize: 11 },
        },
        yAxis: {
          type: "value",
          min: 0,
          max: 100,
          axisLine: { show: false },
          splitLine: { lineStyle: { color: "rgba(255,255,255,0.07)" } },
          axisLabel: { color: "#a9c6e4", formatter: "{value}%" },
        },
        series: [
          { name: "La Niña", type: "line", smooth: true, stack: "Total", areaStyle: { color: "rgba(90, 167, 255, 0.3)" }, lineStyle: { color: "#5aa7ff", width: 2 }, itemStyle: { color: "#5aa7ff" }, data: laNina },
          { name: "Neutral", type: "line", smooth: true, stack: "Total", areaStyle: { color: "rgba(154, 168, 184, 0.28)" }, lineStyle: { color: "#9aa8b8", width: 2 }, itemStyle: { color: "#9aa8b8" }, data: neutral },
          { name: "El Niño", type: "line", smooth: true, stack: "Total", areaStyle: { color: "rgba(255, 138, 92, 0.3)" }, lineStyle: { color: "#ff8a5c", width: 2 }, itemStyle: { color: "#ff8a5c" }, data: elNino },
        ],
      });
    },

    renderModelChart() {
      if (!this.charts.model) return;
      const data = this.modelData || this.generateMockModelData();
      const modelNames = Object.keys(data);
      const colors = ["#5aa7ff", "#4fc3c9", "#ffb36b", "#7aa7d9", "#6fb8a8", "#9a8ad9"];
      const leadMonths = Array.from({ length: 20 }, (_, i) => i + 1);

      this.charts.model.setOption({
        backgroundColor: "transparent",
        title: { text: "多模型技巧对比", left: 0, top: 0, textStyle: { color: "#dcebf8", fontSize: 16, fontWeight: 600 } },
        tooltip: { trigger: "axis", axisPointer: { type: "cross", crossStyle: { color: "rgba(255,255,255,0.3)" } } },
        legend: {
          data: modelNames,
          top: 0,
          left: "center",
          orient: "horizontal",
          type: "scroll",
          textStyle: { color: "#a9c6e4", fontSize: 11 },
          itemWidth: 16,
          itemHeight: 10,
        },
        grid: { left: 45, right: 20, top: 55, bottom: 55 },
        dataZoom: [
          { type: "inside", start: 0, end: 100 },
          { type: "slider", start: 0, end: 100, bottom: 10, height: 18, borderColor: "rgba(90,167,255,0.2)", textStyle: { color: "#a9c6e4" } },
        ],
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: leadMonths,
          axisLine: { lineStyle: { color: "#7fa6cf" } },
          axisLabel: { color: "#a9c6e4", formatter: "{value}月" },
        },
        yAxis: {
          type: "value",
          min: 0.2,
          max: 1.0,
          axisLine: { show: false },
          splitLine: { lineStyle: { color: "rgba(255,255,255,0.07)" } },
          axisLabel: { color: "#a9c6e4" },
        },
        series: modelNames.map((name, index) => ({
          name,
          type: "line",
          smooth: true,
          symbol: "circle",
          symbolSize: 3,
          showSymbol: false,
          lineStyle: { color: colors[index], width: 2 },
          itemStyle: { color: colors[index] },
          data: data[name],
        })),
      });
    },

    // ===================== 概率滚动动画 =====================
    animateProbs(start, target) {
      if (!start || !target) return;
      const duration = 800;
      const startTime = performance.now();
      const from = { ...start };
      const to = { ...target };

      const step = (now) => {
        if (this.destroyed) return;
        const progress = Math.min((now - startTime) / duration, 1);
        const ease = 1 - Math.pow(1 - progress, 3);
        this.displayProbs = {
          laNina: from.laNina + (to.laNina - from.laNina) * ease,
          neutral: from.neutral + (to.neutral - from.neutral) * ease,
          elNino: from.elNino + (to.elNino - from.elNino) * ease,
        };
        if (progress < 1) requestAnimationFrame(step);
      };
      requestAnimationFrame(step);
    },

    resizeCharts() {
      Object.keys(this.charts).forEach((key) => {
        if (this.charts[key]) this.charts[key].resize();
      });
      if (this.map && this.displayMode === "2d") this.map.invalidateSize();
    },

    disposeCharts() {
      Object.keys(this.charts).forEach((key) => {
        if (this.charts[key]) {
          this.charts[key].dispose();
          this.charts[key] = null;
        }
      });
    },

    setupResizeListener() {
      this.handleResize = () => {
        this.resizeCharts();
        this.updateSlidesPerView();
      };
      window.addEventListener("resize", this.handleResize);
    },

    // ===================== 轮播图 =====================
    updateSlidesPerView() {
      const w = window.innerWidth;
      if (w < 768) {
        this.slidesPerView = 1;
      } else if (w < 1200) {
        this.slidesPerView = 2;
      } else {
        this.slidesPerView = 3;
      }
      if (this.currentSlide > this.maxSlideIndex) {
        this.currentSlide = this.maxSlideIndex;
      }
    },

    nextSlide() {
      this.currentSlide = this.currentSlide >= this.maxSlideIndex ? 0 : this.currentSlide + 1;
    },

    prevSlide() {
      this.currentSlide = this.currentSlide <= 0 ? this.maxSlideIndex : this.currentSlide - 1;
    },

    goToSlide(index) {
      this.currentSlide = index;
    },

    // ===================== Lightbox =====================
    openLightbox(index) {
      this.lightboxIndex = index;
      this.lightboxVisible = true;
    },

    closeLightbox() {
      this.lightboxVisible = false;
    },

    closeLightboxOnBackdrop(e) {
      if (e.target === e.currentTarget) this.closeLightbox();
    },

    lightboxNext() {
      this.lightboxIndex = (this.lightboxIndex + 1) % this.carouselSlides.length;
    },

    lightboxPrev() {
      this.lightboxIndex = (this.lightboxIndex - 1 + this.carouselSlides.length) % this.carouselSlides.length;
    },

    // ===================== 交互 =====================
    navigateTo(path) {
      if (this.$route.path !== path) {
        this.$router.push(path);
      }
    },
  },
};
</script>

<style scoped>
.page-container {
  width: 100%;
  margin-top: -24px;
  padding-bottom: 24px;
  background: linear-gradient(180deg, #0a1e3c 0%, #0d2c4f 100%);
}

.content-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.content-title {
  color: #7fb0e0;
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}

.content-title:first-child {
  margin-top: 0;
}

.main-text {
  display: block;
  font-size: 18px;
  color: #dcebf8;
  margin-bottom: 1em;
  line-height: 1.7;
  text-align: justify;
}

/* 下块：系统简介（与 hero 地图区分隔的独立板块） */
.intro-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 44px 24px 8px;
  border-top: 1px solid rgba(148, 180, 220, 0.25);
  background: linear-gradient(180deg, rgba(10, 30, 60, 0.5) 0%, rgba(10, 30, 60, 0) 100%);
}

.intro-section .content-title {
  margin-top: 0;
}

/* HERO：海洋渐变兜底（瓦片加载失败时地图仍可用） */
.hero {
  position: relative;
  left: 50%;
  transform: translateX(-50%);
  width: 100vw;
  min-height: calc(100vh - 60px);
  /* 确保 min-height 包含 padding，hero 恰好一屏高（顶部导航栏 + 全屏地图） */
  box-sizing: border-box;
  overflow: hidden;
  background: linear-gradient(180deg, #0a1e3c 0%, #123a5c 55%, #0a1e3c 100%);
}

/* Leaflet 地图铺满 hero，作为主体背景 */
.hero-map {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: transparent;
}

/* 3D 球面由独立 Three.js 组件渲染，和 2D 地图共用同一大屏区域。 */
.view-mode-switcher {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 4;
  display: flex;
  padding: 3px;
  border: 1px solid rgba(148, 180, 220, 0.3);
  background: rgba(8, 22, 46, 0.82);
  box-shadow: 0 8px 24px rgba(4, 14, 32, 0.35);
  pointer-events: auto;
}

.view-mode-switcher button {
  min-width: 48px;
  height: 32px;
  padding: 0 12px;
  border: 0;
  background: transparent;
  color: #a9c6e4;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
}

.view-mode-switcher button:hover,
.view-mode-switcher button.active {
  background: rgba(72, 168, 177, 0.28);
  color: #fff;
}

/* 轻量暗角：让面板区域更沉、地图边缘自然过渡，不遮挡地图主体 */
.hero-map-vignette {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background:
    radial-gradient(circle at 50% 45%, transparent 45%, rgba(8, 24, 48, 0.35) 100%),
    linear-gradient(180deg, rgba(8, 24, 48, 0.25) 0%, transparent 30%, transparent 70%, rgba(8, 24, 48, 0.5) 100%);
}

.hero-content {
  position: relative;
  z-index: 2;
  /* 固定高度：hero 永远恰好一屏高，标签 50% 即地图区域垂直中间 */
  height: calc(100vh - 60px);
  /* 确保 height 包含 padding，不超出 hero 高度 */
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  /* 面板超高时不被裁剪，靠面板内部滚动兜底 */
  align-items: flex-start;
  gap: 48px;
  padding: 80px 48px 120px;
  /* 让整个容器不拦截鼠标，事件穿透到下方地图；仅面板自身可交互 */
  pointer-events: none;
}

.hero-two-col {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

/* ===================== 海洋风玻璃面板 ===================== */
.ocean-panel {
  position: relative;
  /* 亮色地图上加深底色，保证文字清晰可读 */
  background: rgba(8, 24, 48, 0.72);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(148, 180, 220, 0.3);
  border-radius: 10px;
  box-shadow: 0 16px 40px rgba(4, 14, 32, 0.4);
  padding: 28px 30px;
  /* 面板自身恢复鼠标交互（按钮、tooltip 等） */
  pointer-events: auto;
  /* 收起/展开过渡动画 */
  transition: width 0.3s ease, padding 0.3s ease, opacity 0.3s ease,
    flex-basis 0.3s ease, min-width 0.3s ease, border-color 0.3s ease;
  /* 面板超高时内部滚动兜底，不撑高 hero；横向仍裁剪以配合收起动画 */
  max-height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  /* 垂直居中；超高时自动占满容器并内部滚动 */
  margin: auto 0;
}

/* 面板内部细滚动条（海洋风） */
.ocean-panel::-webkit-scrollbar {
  width: 6px;
}
.ocean-panel::-webkit-scrollbar-thumb {
  background: rgba(148, 180, 220, 0.35);
  border-radius: 3px;
}
.ocean-panel::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 180, 220, 0.55);
}
.ocean-panel::-webkit-scrollbar-track {
  background: transparent;
}

/* 收起状态：宽度收为 0，内容被 overflow 裁剪，地图完全露出 */
.enso-status-panel.collapsed {
  flex: 0 0 0;
  min-width: 0;
  width: 0;
  padding: 0;
  opacity: 0;
  border-color: transparent;
  box-shadow: none;
  pointer-events: none;
}

/* 左侧 ENSO 状态块 */
.enso-status-panel {
  flex: 0 0 38%;
  max-width: 460px;
  min-width: 320px;
}

.enso-status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.enso-status-header::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, rgba(148, 180, 220, 0.5), rgba(148, 180, 220, 0.1), transparent);
}

/* 头部右侧操作区（帮助 + 收起按钮） */
.enso-status-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 收起按钮：海洋风朴素样式 */
.enso-collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: rgba(148, 180, 220, 0.12);
  border: 1px solid rgba(148, 180, 220, 0.3);
  color: #cfe3f7;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s, border-color 0.3s, color 0.3s;
}

.enso-collapse-btn:hover {
  background: rgba(148, 180, 220, 0.22);
  border-color: rgba(148, 180, 220, 0.6);
  color: #ffffff;
}

/* 收起状态标签：贴在 hero 最左边缘 */
.enso-collapsed-tab {
  /* 相对 .hero 定位（hero 有 position: relative），位于地图区域左侧中间；
     滚动到下方内容区时随 hero 一起滚走，不悬浮 */
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 9px;
  background: rgba(8, 24, 48, 0.72);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(148, 180, 220, 0.3);
  border-left: none;
  border-radius: 0 10px 10px 0;
  color: #cfe3f7;
  cursor: pointer;
  pointer-events: auto;
  transition: background 0.3s, border-color 0.3s, color 0.3s;
}

.enso-collapsed-tab:hover {
  background: rgba(14, 36, 68, 0.85);
  border-color: rgba(148, 180, 220, 0.6);
  color: #ffffff;
}

.enso-collapsed-tab i {
  font-size: 14px;
}

.enso-collapsed-tab span {
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  font-size: 13px;
  font-weight: 600;
}

.enso-status-title {
  font-size: 15px;
  color: #cfe3f7;
  letter-spacing: 2px;
  font-weight: 600;
}

.enso-status-help {
  font-size: 16px;
  color: #9fc2e6;
  cursor: pointer;
  transition: color 0.3s;
}

.enso-status-help:hover {
  color: #ffffff;
}

.enso-tooltip-content {
  line-height: 1.8;
  font-size: 13px;
}

.enso-status-main {
  margin-bottom: 28px;
}

.enso-status-cn {
  font-size: 48px;
  font-weight: 800;
  letter-spacing: 2px;
  transition: color 0.8s ease;
  margin-bottom: 4px;
}

.enso-status-cn.heat { color: #ff8a5c; }
.enso-status-cn.cold { color: #5aa7ff; }
.enso-status-cn.neutral { color: #b8c6d6; }

.enso-status-en {
  font-size: 18px;
  color: #a9c6e4;
  margin-bottom: 10px;
}

.enso-status-desc {
  font-size: 14px;
  color: #dcebf8;
  line-height: 1.6;
}

/* Niño3.4 指数（GFS 实测） */
.enso-nino34 {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(148, 180, 220, 0.15);
}

.enso-nino34-label {
  font-size: 12px;
  color: #a9c6e4;
  letter-spacing: 1px;
}

.enso-nino34-value {
  font-size: 18px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  transition: color 0.5s ease;
}

.enso-nino34-value.heat { color: #ff8a5c; }
.enso-nino34-value.cold { color: #5aa7ff; }
.enso-nino34-value.neutral { color: #b8c6d6; }

.enso-status-prob-title {
  font-size: 13px;
  color: #a9c6e4;
  margin-bottom: 14px;
}

.prob-bars-cn {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.prob-bar-row-cn {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.prob-bar-track-cn {
  height: 12px;
  background: rgba(4, 16, 38, 0.5);
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(148, 180, 220, 0.18);
}

.prob-bar-fill-cn {
  position: relative;
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}

.prob-bar-fill-cn.cold {
  background: linear-gradient(90deg, #3f86cf 0%, #5aa7ff 100%);
}

.prob-bar-fill-cn.neutral {
  background: linear-gradient(90deg, #7f8ea0 0%, #9aa8b8 100%);
}

.prob-bar-fill-cn.heat {
  background: linear-gradient(90deg, #e07a4e 0%, #ff8a5c 100%);
}

.prob-bar-info-cn {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.prob-bar-percent {
  font-size: 18px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  min-width: 44px;
  transition: color 0.5s ease;
}

.prob-bar-percent.heat { color: #ff8a5c; }
.prob-bar-percent.cold { color: #5aa7ff; }
.prob-bar-percent.neutral { color: #9aa8b8; }

.prob-bar-label-cn {
  color: #dcebf8;
}

/* 预测入口按钮：朴素描边 */
.hero-actions {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: rgba(12, 34, 66, 0.4);
  border: 1px solid rgba(148, 180, 220, 0.3);
  border-radius: 8px;
  color: #eaf3fc;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  border-color: rgba(148, 180, 220, 0.7);
  background: rgba(20, 46, 84, 0.55);
  transform: translateY(-2px);
}

.action-btn-icon {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: rgba(148, 180, 220, 0.12);
  border: 1px solid rgba(148, 180, 220, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.action-btn-title {
  font-size: 13px;
  font-weight: 600;
}

.action-btn-sub {
  font-size: 10px;
  color: #9fb6d0;
}

/* Ticker */
.ticker-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 3;
  height: 40px;
  background: rgba(8, 22, 46, 0.82);
  border-top: 1px solid rgba(148, 180, 220, 0.2);
  backdrop-filter: blur(4px);
  overflow: hidden;
  display: flex;
  align-items: center;
}

.ticker-label {
  padding: 0 16px;
  font-size: 12px;
  color: #cfe3f7;
  font-weight: 600;
  white-space: nowrap;
  border-right: 1px solid rgba(148, 180, 220, 0.2);
  background: rgba(148, 180, 220, 0.08);
  height: 100%;
  display: flex;
  align-items: center;
}

.ticker-track {
  flex: 1;
  overflow: hidden;
  position: relative;
  height: 100%;
}

.ticker-content {
  position: absolute;
  white-space: nowrap;
  display: flex;
  gap: 48px;
  align-items: center;
  height: 100%;
  padding-left: 24px;
  animation: tickerScroll 24s linear infinite;
}

.ticker-content:hover {
  animation-play-state: paused;
}

@keyframes tickerScroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.ticker-item {
  font-size: 13px;
  color: #a9c6e4;
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.ticker-item::after {
  content: '';
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #7fa6cf;
  margin-left: 24px;
}

.ticker-item:last-child::after {
  display: none;
}

.ticker-item strong {
  color: #eaf3fc;
  font-weight: 600;
}

/* 风场数据源与时效小字（地图右下角，ticker 上方） */
.wind-data-badge {
  position: absolute;
  right: 16px;
  bottom: 52px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  font-size: 12px;
  color: #cfe3f7;
  background: rgba(8, 22, 46, 0.72);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border: 1px solid rgba(148, 180, 220, 0.25);
  border-radius: 999px;
  box-shadow: 0 6px 18px rgba(4, 14, 32, 0.35);
  pointer-events: none;
  white-space: nowrap;
}

.wind-badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4fc3c9;
  box-shadow: 0 0 8px rgba(79, 195, 201, 0.8);
  flex-shrink: 0;
}

.wind-data-badge.mock .wind-badge-dot {
  background: #b8a04a;
  box-shadow: 0 0 8px rgba(184, 160, 74, 0.6);
}

/* 图层切换器：风场 / 海温（地图右上角，海洋风玻璃胶囊） */
.layer-switcher {
  position: absolute;
  top: 62px;
  right: 16px;
  z-index: 3;
  display: flex;
  padding: 3px;
  background: rgba(8, 22, 46, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(148, 180, 220, 0.3);
  border-radius: 999px;
  box-shadow: 0 8px 24px rgba(4, 14, 32, 0.35);
  pointer-events: auto;
}

.layer-switcher-btn {
  padding: 6px 18px;
  font-size: 13px;
  color: #a9c6e4;
  background: transparent;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.3s ease, color 0.3s ease, box-shadow 0.3s ease;
}

.layer-switcher-btn:hover {
  color: #eaf3fc;
}

.layer-switcher-btn.active {
  background: linear-gradient(180deg, rgba(90, 167, 255, 0.35), rgba(90, 167, 255, 0.18));
  color: #ffffff;
  box-shadow: inset 0 0 0 1px rgba(148, 180, 220, 0.35);
}

.layer-switcher-btn.disabled {
  color: rgba(169, 198, 228, 0.35);
  cursor: not-allowed;
}

.layer-switcher-btn.disabled:hover {
  color: rgba(169, 198, 228, 0.35);
}

/* 海温色标图例（切换器下方） */
.sst-legend {
  position: absolute;
  top: 110px;
  right: 16px;
  z-index: 3;
  padding: 10px 12px;
  background: rgba(8, 22, 46, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(148, 180, 220, 0.3);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(4, 14, 32, 0.35);
  pointer-events: none;
}

.sst-legend-bar {
  width: 160px;
  height: 10px;
  border-radius: 5px;
  border: 1px solid rgba(148, 180, 220, 0.25);
  background: linear-gradient(90deg,
    rgb(15, 45, 120) 0%,
    rgb(30, 90, 170) 18%,
    rgb(40, 140, 190) 35%,
    rgb(45, 180, 170) 53%,
    rgb(110, 200, 120) 65%,
    rgb(220, 210, 90) 76%,
    rgb(235, 150, 60) 88%,
    rgb(210, 60, 40) 100%);
}

.sst-legend-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 10px;
  color: #a9c6e4;
}

/* 研究主题 */
.topics-container {
  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  margin-top: 20px;
  margin-bottom: 24px;
  gap: 20px;
}

.theme-item {
  text-align: center;
  width: 33.33%;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.theme-item:hover {
  transform: translateY(-6px);
}

.theme-item h3 {
  color: #7fb0e0;
  margin-bottom: 12px;
  transition: color 0.3s;
}

.theme-item:hover h3 {
  color: #a9c6e4;
}

.image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(148, 180, 220, 0.25);
  transition: box-shadow 0.3s, border-color 0.3s;
}

.theme-item:hover .image-wrapper {
  border-color: rgba(148, 180, 220, 0.5);
  box-shadow: 0 8px 24px rgba(4, 14, 32, 0.4);
}

.theme-image {
  width: 100%;
  height: auto;
  display: block;
}

/* 核心监测 */
.monitor-section {
  margin-bottom: 24px;
}

.monitor-section .content-title,
.carousel-section .content-title {
  display: flex;
  align-items: center;
}

.section-subtitle {
  font-size: 13px;
  color: #9fb6d0;
  margin-left: auto;
  font-weight: normal;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.chart-panel {
  background: rgba(10, 30, 60, 0.4);
  border: 1px solid rgba(148, 180, 220, 0.22);
  border-radius: 12px;
  padding: 20px;
  transition: border-color 0.3s;
}

.chart-panel:hover {
  border-color: rgba(148, 180, 220, 0.4);
}

.chart-box {
  width: 100%;
  height: 400px;
}

/* 轮播图 */
.carousel-section {
  margin-bottom: 16px;
}

.carousel-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.carousel-track {
  flex: 1;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid rgba(148, 180, 220, 0.22);
}

.carousel-slides {
  display: flex;
  transition: transform 0.5s ease;
}

.carousel-slide {
  min-width: calc(100% / 3);
  aspect-ratio: 16 / 9;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.carousel-slide:hover {
  transform: scale(0.98);
}

.carousel-slide-label {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.carousel-slide-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6px;
}

.carousel-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(10, 30, 60, 0.7);
  border: 1px solid rgba(148, 180, 220, 0.22);
  color: #ffffff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;
}

.carousel-arrow:hover {
  border-color: rgba(148, 180, 220, 0.6);
}

.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.carousel-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s;
}

.carousel-dot.active {
  background: #7fb0e0;
  width: 24px;
  border-radius: 4px;
}

/* Lightbox */
.lightbox-overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 14, 32, 0.92);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 24px;
  backdrop-filter: blur(8px);
}

.lightbox-main {
  display: flex;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 1000px;
}

.lightbox-box {
  flex: 1;
  aspect-ratio: 16 / 9;
  border-radius: 12px;
  border: 1px solid rgba(148, 180, 220, 0.22);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.6);
}

.lightbox-box-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
}

.lightbox-box-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
}

.lightbox-label {
  margin-top: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.lightbox-close {
  position: absolute;
  top: 24px;
  right: 32px;
  font-size: 32px;
  color: #a9c6e4;
  cursor: pointer;
  transition: color 0.2s;
}

.lightbox-close:hover {
  color: #ffffff;
}

/* 渐变 */
.gradient-1 { background: linear-gradient(135deg, #0c4a6e 0%, #075985 100%); }
.gradient-2 { background: linear-gradient(135deg, #1e3a5f 0%, #2a4d7a 100%); }
.gradient-3 { background: linear-gradient(135deg, #312e81 0%, #4338ca 100%); }
.gradient-4 { background: linear-gradient(135deg, #064e3b 0%, #065f46 100%); }
.gradient-5 { background: linear-gradient(135deg, #7c2d12 0%, #9a3412 100%); }
.gradient-6 { background: linear-gradient(135deg, #4c1d95 0%, #5b21b6 100%); }

/* 响应式 */
@media (max-width: 1199px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .carousel-slide {
    min-width: 50%;
  }
  .enso-status-panel {
    flex: 0 0 42%;
    min-width: 300px;
    padding: 24px;
  }
}

@media (max-width: 767px) {
  .content-section {
    padding: 0 16px;
  }
  .hero-content {
    flex-direction: column;
    padding: 40px 24px 80px;
    gap: 32px;
    justify-content: flex-start;
  }
  .view-mode-switcher {
    top: 12px;
    right: 12px;
  }
  .layer-switcher {
    top: 56px;
    right: 12px;
  }
  .sst-legend {
    top: 102px;
    right: 12px;
  }
  .hero-two-col {
    max-width: 100%;
  }
  .enso-status-panel {
    flex: 0 0 auto;
    width: 100%;
    padding: 24px;
  }
  .enso-status-cn { font-size: 36px; }
  .enso-status-en { font-size: 15px; }
  .prob-bar-row-cn { gap: 4px; }
  .prob-bar-percent { font-size: 16px; }
  .hero-actions {
    grid-template-columns: 1fr 1fr;
    margin-top: 20px;
  }
  .topics-container {
    flex-direction: column;
    align-items: center;
  }
  .theme-item {
    width: 100%;
    max-width: 320px;
  }
  .carousel-slide { min-width: 100%; }
  .chart-box { height: 320px; }
}
</style>
