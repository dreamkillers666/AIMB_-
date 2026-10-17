<template>
  <div ref="stage" class="enso-globe-stage" @contextmenu.prevent>
    <div class="globe-controls">
      <button
        class="globe-control"
        type="button"
        :title="`定位 ${regionInfo.label} 海区`"
        :aria-label="`定位 ${regionInfo.label} 海区`"
        @click.stop="focusRegion"
      >
        <i class="el-icon-location-outline"></i>
      </button>
      <button
        class="globe-control"
        :class="{ active: regionVisible }"
        type="button"
        :title="regionVisible ? '隐藏 Niño3.4 指数参考区' : '显示 Niño3.4 指数参考区'"
        :aria-label="regionVisible ? '隐藏 Niño3.4 指数参考区' : '显示 Niño3.4 指数参考区'"
        :aria-pressed="String(regionVisible)"
        @click.stop="toggleReferenceArea"
      >
        <i class="el-icon-view"></i>
      </button>
      <button
        class="globe-control"
        type="button"
        title="重置地球视角"
        aria-label="重置地球视角"
        @click.stop="resetView"
      >
        <i class="el-icon-refresh"></i>
      </button>
    </div>
    <div ref="ninoLabel" class="globe-region-label" aria-live="polite">
      <span class="globe-region-kicker">ENSO 指数定义区域</span>
      <strong>{{ regionInfo.label }} 指数参考区</strong>
      <span>{{ regionInfo.desc }}</span>
      <small>{{ regionInfo.range }}</small>
    </div>
    <!-- 2D 地图落定的点位探针 + 冷暖异常热点：投影到球面同步显示 -->
    <div ref="pinLayer" class="globe-pin-layer" aria-hidden="true"></div>
    <div v-if="rendererError" class="globe-error" role="status">
      3D 场景不可用，已切换到二维地图
    </div>
  </div>
</template>

<script>
import * as THREE from 'three';
import earthMapUrl from '../assets/earth-blue-marble.jpg';
import waterMaskUrl from '../assets/earth-water.png';
import { ensoFieldColor } from '../data/ensoColorScale';
import { marchingSquares } from '../data/ensoAnalysis';

const RADIUS = 2;
const PARTICLE_COUNT = 3600;
const MOBILE_PARTICLE_COUNT = 1900;
const TRAIL_LENGTH = 15;
const TRAIL_INTERVAL = 0.08;

const toSpherePoint = (longitude, latitude, radius) => {
  const lon = longitude * Math.PI / 180;
  const lat = latitude * Math.PI / 180;
  const cosLat = Math.cos(lat);
  return new THREE.Vector3(
    -radius * cosLat * Math.cos(lon),
    radius * Math.sin(lat),
    radius * cosLat * Math.sin(lon)
  );
};

const writeSpherePoint = (positions, offset, longitude, latitude, radius) => {
  const lon = longitude * Math.PI / 180;
  const lat = latitude * Math.PI / 180;
  const cosLat = Math.cos(lat);
  positions[offset] = -radius * cosLat * Math.cos(lon);
  positions[offset + 1] = radius * Math.sin(lat);
  positions[offset + 2] = radius * cosLat * Math.sin(lon);
};

export default {
  name: 'EnsoGlobe',
  props: {
    windData: { type: Array, default: () => [] },
    sstData: { type: Object, default: null },
    sstMode: { type: String, default: 'absolute' },
    layer: { type: String, default: 'wind' },
    layout: { type: String, default: 'showcase' },
    // 回放/历史模式下由父组件置 true：自动隐藏 Niño3.4 参考区（卡片 + 球面标注）
    regionSuppressed: { type: Boolean, default: false },
    // 2D 地图落定的点位探针：[{ id, lat, lon, label }]
    pins: { type: Array, default: () => [] },
    // 当前 ENSO 参考区（A6 分区切换器）：{ key, label, desc, range, glowLon, glowLat, glowScale }
    region: { type: Object, default: null },
    // A4 · 冷暖异常热点（3D 球面标记）：[{ id, lat, lon, text, tone }]
    hotspots: { type: Array, default: () => [] },
    // A5 · 距平等值线（3D 球面 LineSegments）
    contourOn: { type: Boolean, default: false },
    // A8 · 纬向剖面切割线（3D 球面纬线）
    profileOn: { type: Boolean, default: false },
    profileLat: { type: Number, default: 0 }
  },
  data() {
    return { rendererError: '', regionVisible: true };
  },
  computed: {
    regionShown() {
      return this.regionVisible && !this.regionSuppressed;
    },
    regionInfo() {
      return this.region || {
        label: 'Niño3.4',
        desc: '赤道中东部太平洋',
        range: '5°N–5°S · 170°W–120°W',
        glowLon: 215,
        glowLat: 0,
        glowScale: 1
      };
    }
  },
  watch: {
    windData() {
      this.refreshWind();
    },
    sstData() {
      this.refreshSurfaceTextures();
      if (this.contourOn) this.rebuildContourLines();
    },
    sstMode() {
      this.refreshSurfaceTextures();
      if (this.contourOn) this.rebuildContourLines();
    },
    layer() {
      this.applyLayer();
    },
    layout() {
      this.resizeScene();
    },
    regionSuppressed() {
      this.applyRegionVisibility();
    },
    region() {
      this.rebuildRegionBounds();
      if (!this.regionSuppressed) this.focusRegion();
    },
    pins: {
      deep: false,
      handler() {
        this.rebuildPinMarkers();
      }
    },
    hotspots: {
      deep: false,
      handler() {
        this.rebuildHotspotMarkers();
      }
    },
    contourOn() {
      this.rebuildContourLines();
    },
    profileOn() {
      this.rebuildProfileLine();
    },
    profileLat() {
      this.rebuildProfileLine();
    }
  },
  mounted() {
    this.initScene();
  },
  beforeDestroy() {
    this.disposeScene();
  },
  methods: {
    initScene() {
      const stage = this.$refs.stage;
      if (!stage) return;

      try {
        const rect = stage.getBoundingClientRect();
        const width = Math.max(1, rect.width);
        const height = Math.max(1, rect.height);
        const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false, powerPreference: 'high-performance' });
        renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.5));
        renderer.setSize(width, height, false);
        renderer.setClearColor(0x071923, 1);
        renderer.outputColorSpace = THREE.SRGBColorSpace;
        renderer.domElement.className = 'enso-globe-canvas';
        renderer.domElement.style.position = 'absolute';
        renderer.domElement.style.top = '0';
        renderer.domElement.style.left = '0';
        renderer.domElement.style.display = 'block';
        renderer.domElement.style.width = '100%';
        renderer.domElement.style.height = '100%';
        renderer.domElement.style.touchAction = 'none';
        renderer.domElement.style.cursor = 'grab';
        stage.insertBefore(renderer.domElement, stage.firstChild);

        const scene = new THREE.Scene();
        scene.background = new THREE.Color(0x071923);
        const starfield = this.createStarfield();
        scene.add(starfield);
        const camera = new THREE.PerspectiveCamera(36, width / height, 0.1, 80);
        camera.position.set(0, 0, 5.3);

        const group = new THREE.Group();
        group.rotation.set(0.08, -Math.PI * 2 / 3, 0);
        scene.add(group);
        scene.add(new THREE.HemisphereLight(0xa5e5ef, 0x10202b, 1.35));
        const keyLight = new THREE.DirectionalLight(0xd6f5ff, 2.15);
        keyLight.position.set(-3.5, 2.2, 5.5);
        scene.add(keyLight);
        const fillLight = new THREE.DirectionalLight(0x3b91a2, 0.75);
        fillLight.position.set(3, -1, -3);
        scene.add(fillLight);

        const orbit = this.createOrbitRing(0x47e1e1, 0.13, 0.36, 1.15);
        const orbitAccent = this.createOrbitRing(0xffc56a, 0.34, 0.23, 0.42);
        group.add(orbit);
        group.add(orbitAccent);

        const earthGeometry = new THREE.SphereGeometry(RADIUS, 256, 192);
        const earthMaterial = new THREE.MeshPhongMaterial({
          color: 0xffffff,
          shininess: 16,
          specular: new THREE.Color(0x2a4a58)
        });
        const earth = new THREE.Mesh(earthGeometry, earthMaterial);
        group.add(earth);

        const sstGeometry = new THREE.SphereGeometry(RADIUS + 0.006, 256, 192);
        const sstMaterial = new THREE.MeshBasicMaterial({
          transparent: true,
          opacity: 0.94,
          depthWrite: false,
          side: THREE.FrontSide
        });
        const sstMesh = new THREE.Mesh(sstGeometry, sstMaterial);
        sstMesh.visible = false;
        group.add(sstMesh);

        // A6 · 参考区按当前选中分区构建，切换分区时由 rebuildRegionBounds 重建
        const regionBounds = this.regionBounds();
        const ninoArea = this.createRegionArea(regionBounds.west, regionBounds.east, regionBounds.south, regionBounds.north);
        group.add(ninoArea);
        const grid = this.createGraticule();
        group.add(grid);
        const ninoBox = this.createRegionOutline(regionBounds.west, regionBounds.east, regionBounds.south, regionBounds.north);
        group.add(ninoBox);
        const ninoCenter = toSpherePoint(regionBounds.midLon, regionBounds.midLat, RADIUS + 0.055);
        const ninoMarker = new THREE.Mesh(
          new THREE.SphereGeometry(0.035, 20, 14),
          new THREE.MeshBasicMaterial({ color: 0xffe08a, depthTest: true })
        );
        ninoMarker.position.copy(ninoCenter);
        group.add(ninoMarker);
        const ninoGlow = new THREE.Mesh(
          new THREE.SphereGeometry(0.105, 18, 12),
          new THREE.MeshBasicMaterial({ color: 0xffae50, transparent: true, opacity: 0.23, depthWrite: false })
        );
        ninoGlow.position.copy(toSpherePoint(this.regionInfo.glowLon, this.regionInfo.glowLat, RADIUS + 0.052));
        group.add(ninoGlow);
        const ninoHalo = new THREE.Mesh(
          new THREE.TorusGeometry(0.082, 0.006, 5, 42),
          new THREE.MeshBasicMaterial({ color: 0xffe29a, transparent: true, opacity: 0.9, depthWrite: false })
        );
        ninoHalo.position.copy(toSpherePoint(this.regionInfo.glowLon, this.regionInfo.glowLat, RADIUS + 0.063));
        ninoHalo.lookAt(ninoHalo.position.clone().multiplyScalar(2));
        group.add(ninoHalo);
        const ninoLabelAnchor = toSpherePoint(this.regionInfo.glowLon + 20, this.regionInfo.glowLat, RADIUS + 0.035);

        const atmosphere = new THREE.Mesh(
          new THREE.SphereGeometry(RADIUS + 0.075, 72, 48),
          new THREE.ShaderMaterial({
            vertexShader: [
              'varying vec3 vNormal;',
              'varying vec3 vViewPosition;',
              'void main() {',
              '  vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);',
              '  vNormal = normalize(normalMatrix * normal);',
              '  vViewPosition = -mvPosition.xyz;',
              '  gl_Position = projectionMatrix * mvPosition;',
              '}'
            ].join('\n'),
            fragmentShader: [
              'varying vec3 vNormal;',
              'varying vec3 vViewPosition;',
              'void main() {',
              '  float rim = pow(1.0 - max(dot(normalize(vNormal), normalize(vViewPosition)), 0.0), 3.0);',
              '  gl_FragColor = vec4(0.15, 0.62, 0.72, rim * 0.42);',
              '}'
            ].join('\n'),
            side: THREE.BackSide,
            transparent: true,
            blending: THREE.AdditiveBlending,
            depthWrite: false
          })
        );
        group.add(atmosphere);

        this._three = {
          renderer,
          scene,
          camera,
          group,
          earth,
          earthMaterial,
          sstMesh,
          sstMaterial,
          grid,
          ninoArea,
          ninoBox,
          ninoCenter,
          ninoMarker,
          ninoGlow,
          ninoHalo,
          ninoLabelAnchor,
          labelWorldPosition: new THREE.Vector3(),
          globeWorldPosition: new THREE.Vector3(),
          labelNormal: new THREE.Vector3(),
          cameraDirection: new THREE.Vector3(),
          labelScreenPosition: new THREE.Vector3(),
          atmosphere,
          starfield,
          orbit,
          orbitAccent,
          reducedMotion: !!(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches),
          firstWindFrame: true,
          frame: 0,
          lastFrame: 0,
          resizeObserver: null,
          drag: null,
          fly: null,
          particles: null,
          streaks: null,
          particleState: [],
          windField: null,
          windSample: { u: 0, v: 0 },
          windColor: new THREE.Color(),
          earthTexture: null,
          sstTexture: null,
          zoom: 5.3,
          mobileZoom: 1,
          pointerDown: null,
          pointerMove: null,
          pointerUp: null,
          wheel: null
        };

        this.bindInteraction();
        this.observeSize();
        this.loadEarthTexture();
        this.refreshSurfaceTextures();
        this.refreshWind();
        this.applyLayer();
        this.applyRegionVisibility();
        if (this.region) this.applyRegionFocus();
        this.rebuildPinMarkers();
        this.rebuildHotspotMarkers();
        this.rebuildContourLines();
        this.rebuildProfileLine();
        this.renderFrame(performance.now());
      } catch (error) {
        this.rendererError = error && error.message ? error.message : 'WebGL unavailable';
        this.$emit('unavailable', this.rendererError);
      }
    },

    createGraticule() {
      const points = [];
      [-60, -45, -30, -15, 0, 15, 30, 45, 60].forEach((latitude) => {
        for (let longitude = 0; longitude < 360; longitude += 4) {
          points.push(toSpherePoint(longitude, latitude, RADIUS + 0.009));
          points.push(toSpherePoint(longitude + 4, latitude, RADIUS + 0.009));
        }
      });
      for (let longitude = 0; longitude < 360; longitude += 15) {
        for (let latitude = -88; latitude < 88; latitude += 4) {
          points.push(toSpherePoint(longitude, latitude, RADIUS + 0.009));
          points.push(toSpherePoint(longitude, latitude + 4, RADIUS + 0.009));
        }
      }
      const geometry = new THREE.BufferGeometry().setFromPoints(points);
      const material = new THREE.LineBasicMaterial({
        color: 0x74b6be,
        transparent: true,
        opacity: 0.2,
        depthWrite: false
      });
      return new THREE.LineSegments(geometry, material);
    },

    createStarfield() {
      const count = 440;
      const positions = new Float32Array(count * 3);
      for (let index = 0; index < count; index += 1) {
        const direction = new THREE.Vector3(
          Math.random() * 2 - 1,
          Math.random() * 2 - 1,
          Math.random() * 2 - 1
        ).normalize();
        const radius = 23 + Math.random() * 27;
        positions[index * 3] = direction.x * radius;
        positions[index * 3 + 1] = direction.y * radius;
        positions[index * 3 + 2] = direction.z * radius;
      }
      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
      return new THREE.Points(geometry, new THREE.PointsMaterial({
        color: 0x8dd9ec,
        size: 1.15,
        sizeAttenuation: false,
        transparent: true,
        opacity: 0.42,
        depthWrite: false
      }));
    },

    createOrbitRing(color, rotationX, rotationY, opacity, arc) {
      const geometry = new THREE.TorusGeometry(2.19, 0.006, 4, 180, arc || Math.PI * 2);
      const material = new THREE.MeshBasicMaterial({
        color,
        transparent: true,
        opacity,
        blending: THREE.AdditiveBlending,
        depthWrite: false
      });
      const ring = new THREE.Mesh(geometry, material);
      ring.rotation.set(rotationX, rotationY, 0.16);
      return ring;
    },

    createRegionOutline(west, east, south, north) {
      const points = [];
      for (let lon = west; lon <= east; lon += 2) points.push(toSpherePoint(lon, south, RADIUS + 0.02));
      for (let lat = south; lat <= north; lat += 1) points.push(toSpherePoint(east, lat, RADIUS + 0.02));
      for (let lon = east; lon >= west; lon -= 2) points.push(toSpherePoint(lon, north, RADIUS + 0.02));
      for (let lat = north; lat >= south; lat -= 1) points.push(toSpherePoint(west, lat, RADIUS + 0.02));
      const geometry = new THREE.BufferGeometry().setFromPoints(points);
      const material = new THREE.LineBasicMaterial({
        color: 0xffd36a,
        transparent: true,
        opacity: 0.95,
        depthWrite: false
      });
      return new THREE.LineLoop(geometry, material);
    },

    createRegionArea(west, east, south, north) {
      const longitudeSteps = 50;
      const latitudeSteps = 10;
      const positions = [];
      const indices = [];

      for (let latitudeIndex = 0; latitudeIndex <= latitudeSteps; latitudeIndex += 1) {
        const latitude = south + ((north - south) * latitudeIndex) / latitudeSteps;
        for (let longitudeIndex = 0; longitudeIndex <= longitudeSteps; longitudeIndex += 1) {
          const longitude = west + ((east - west) * longitudeIndex) / longitudeSteps;
          const point = toSpherePoint(longitude, latitude, RADIUS + 0.016);
          positions.push(point.x, point.y, point.z);
        }
      }

      for (let latitudeIndex = 0; latitudeIndex < latitudeSteps; latitudeIndex += 1) {
        for (let longitudeIndex = 0; longitudeIndex < longitudeSteps; longitudeIndex += 1) {
          const topLeft = latitudeIndex * (longitudeSteps + 1) + longitudeIndex;
          const topRight = topLeft + 1;
          const bottomLeft = topLeft + longitudeSteps + 1;
          const bottomRight = bottomLeft + 1;
          indices.push(topLeft, topRight, bottomLeft, topRight, bottomRight, bottomLeft);
        }
      }

      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3));
      geometry.setIndex(indices);
      geometry.computeVertexNormals();
      return new THREE.Mesh(geometry, new THREE.MeshBasicMaterial({
        color: 0xffc85a,
        transparent: true,
        opacity: 0.24,
        side: THREE.DoubleSide,
        depthWrite: false
      }));
    },

    loadEarthTexture() {
      const state = this._three;
      if (!state) return;
      const loader = new THREE.TextureLoader();
      loader.load(earthMapUrl, (texture) => {
        if (this._three !== state) {
          texture.dispose();
          return;
        }
        texture.colorSpace = THREE.SRGBColorSpace;
        texture.wrapS = THREE.RepeatWrapping;
        texture.offset.x = 0.5;
        texture.anisotropy = state.renderer.capabilities.getMaxAnisotropy();
        state.earthTexture = texture;
        state.earthMaterial.map = texture;
        state.earthMaterial.needsUpdate = true;
      });
      // 海洋高光贴图（白=海洋，黑=陆地）：让海面出现真实的高光反射
      loader.load(waterMaskUrl, (maskTexture) => {
        if (this._three !== state) {
          maskTexture.dispose();
          return;
        }
        maskTexture.wrapS = THREE.RepeatWrapping;
        maskTexture.offset.x = 0.5;
        state.earthMaterial.specularMap = maskTexture;
        state.earthMaterial.needsUpdate = true;
      });
      // 预加载水体掩膜的原始像素，用于给海温叠加做陆地裁切
      this.loadWaterMaskImage().then((image) => {
        if (image && this._three === state) this.refreshSurfaceTextures();
      });
    },

    loadWaterMaskImage() {
      if (this._waterMaskImage) return Promise.resolve(this._waterMaskImage);
      if (!this._waterMaskPromise) {
        this._waterMaskPromise = new Promise((resolve) => {
          const image = new Image();
          image.onload = () => {
            this._waterMaskImage = image;
            // 一次性解码成灰度缓存：掩膜 4096×2048，逐帧 getImageData 代价过高
            try {
              const c = document.createElement('canvas');
              c.width = image.width;
              c.height = image.height;
              const ctx = c.getContext('2d');
              ctx.drawImage(image, 0, 0);
              const d = ctx.getImageData(0, 0, image.width, image.height).data;
              const gray = new Uint8Array(image.width * image.height);
              for (let i = 0; i < gray.length; i++) gray[i] = d[i * 4];
              this._waterMaskGray = { w: image.width, h: image.height, data: gray };
            } catch (e) {
              this._waterMaskGray = null;
            }
            resolve(image);
          };
          image.onerror = () => resolve(null);
          image.src = waterMaskUrl;
        });
      }
      return this._waterMaskPromise;
    },

    makeTextures() {
      const component = this.sstData;
      const header = component && component.header;
      const values = component && component.data;
      const sourceWidth = header && Number(header.nx);
      const sourceHeight = header && Number(header.ny);
      const data = values && values.length === sourceWidth * sourceHeight ? values : [];
      const dx = header && Number(header.dx);
      const dy = header && Number(header.dy);
      const lo1 = header && Number(header.lo1);
      const la1 = header && Number(header.la1);
      if (!data.length || !sourceWidth || !sourceHeight || !dx || !dy || !Number.isFinite(lo1) || !Number.isFinite(la1)) {
        return new THREE.CanvasTexture(document.createElement('canvas'));
      }

      const isSmallScreen = window.innerWidth < 768;
      // 纹理宽度随数据网格分辨率自适应：1° 场用 2048，粗网格（如 5° 演示场）降到 1024，
      // 时间轴逐帧重建纹理时明显更快，视觉无损
      const width = Math.min(isSmallScreen ? 1024 : 2048, Math.max(512, sourceWidth * 14));
      const height = width / 2;
      const sstCanvas = document.createElement('canvas');
      sstCanvas.width = width;
      sstCanvas.height = height;
      const sstContext = sstCanvas.getContext('2d');
      const sstImage = sstContext.createImageData(width, height);

      // 陆地掩膜：白=海洋、黑=陆地。海温只画在海上，与陆地边界清晰。
      // 优先用加载时解码好的灰度缓存，避免每次重建纹理都全量 getImageData
      let maskData = null;
      let maskWidth = 0;
      let maskHeight = 0;
      const cachedMask = this._waterMaskGray;
      if (cachedMask && cachedMask.w) {
        maskWidth = cachedMask.w;
        maskHeight = cachedMask.h;
        maskData = cachedMask.data;
      } else {
        const maskImage = this._waterMaskImage;
        if (maskImage && maskImage.width) {
          maskWidth = maskImage.width;
          maskHeight = maskImage.height;
          const maskCanvas = document.createElement('canvas');
          maskCanvas.width = maskWidth;
          maskCanvas.height = maskHeight;
          const maskContext = maskCanvas.getContext('2d');
          maskContext.drawImage(maskImage, 0, 0);
          maskData = maskContext.getImageData(0, 0, maskWidth, maskHeight).data;
        }
      }
      // 掩膜采样取 R 通道灰度：灰度缓存直接索引，Image 版本需 *4 取 R
      const maskStride = cachedMask && cachedMask.w ? 1 : 4;
      // 掩膜灰度 -> 海洋系数：40~140 之间平滑过渡，海岸线柔和不生硬
      const oceanFactor = (gray) => {
        const t = Math.max(0, Math.min(1, (gray - 40) / (140 - 40)));
        return t * t * (3 - 2 * t);
      };

      const wrapsLongitude = sourceWidth * Math.abs(dx) >= 359.5;
      const sample = (x, y) => {
        let sampleX = x;
        if (wrapsLongitude) sampleX = ((sampleX % sourceWidth) + sourceWidth) % sourceWidth;
        else if (sampleX < 0 || sampleX >= sourceWidth) return null;
        if (y < 0 || y >= sourceHeight) return null;
        const value = data[y * sourceWidth + sampleX];
        const numeric = Number(value);
        return value !== null && value !== undefined && Number.isFinite(numeric) && Math.abs(numeric) < 1000
          ? numeric
          : null;
      };

      for (let y = 0; y < height; y += 1) {
        const latitude = 90 - ((y + 0.5) / height) * 180;
        const sourceGridY = (la1 - latitude) / dy;
        if (sourceGridY < -0.5 || sourceGridY > sourceHeight - 0.5) continue;
        const gridY = Math.max(0, Math.min(sourceHeight - 1, sourceGridY));
        const y0 = Math.floor(gridY);
        const y1 = Math.min(sourceHeight - 1, y0 + 1);
        const fy = gridY - y0;
        for (let x = 0; x < width; x += 1) {
          const longitude = ((x + 0.5) / width) * 360;
          const direction = Math.sign(dx);
          const longitudeDelta = ((direction * (longitude - lo1)) % 360 + 360) % 360;
          const gridX = longitudeDelta / Math.abs(dx);
          const x0 = Math.floor(gridX);
          const x1 = x0 + 1;
          const fx = gridX - x0;
          const offset = (y * width + x) * 4;

          const corners = [
            [sample(x0, y0), (1 - fx) * (1 - fy)],
            [sample(x1, y0), fx * (1 - fy)],
            [sample(x0, y1), (1 - fx) * fy],
            [sample(x1, y1), fx * fy]
          ];
          let weighted = 0;
          let totalWeight = 0;
          corners.forEach(([value, weight]) => {
            if (value !== null) {
              weighted += value * weight;
              totalWeight += weight;
            }
          });
          if (totalWeight <= 0.01) continue;
          let maskFactor = 1;
          if (maskData) {
            const maskX = Math.min(maskWidth - 1, Math.floor((((longitude / 360) + 0.5) % 1) * maskWidth));
            const maskY = Math.min(maskHeight - 1, Math.floor(((90 - latitude) / 180) * maskHeight));
            maskFactor = oceanFactor(maskData[(maskY * maskWidth + maskX) * maskStride]);
            if (maskFactor <= 0.01) continue;
          }
          const color = this.sstColor(weighted / totalWeight);
          sstImage.data[offset] = color[0];
          sstImage.data[offset + 1] = color[1];
          sstImage.data[offset + 2] = color[2];
          sstImage.data[offset + 3] = Math.round(238 * totalWeight * maskFactor);
        }
      }
      sstContext.putImageData(sstImage, 0, 0);

      const sstTexture = new THREE.CanvasTexture(sstCanvas);
      sstTexture.colorSpace = THREE.SRGBColorSpace;
      sstTexture.wrapS = THREE.RepeatWrapping;
      sstTexture.wrapT = THREE.ClampToEdgeWrapping;
      sstTexture.anisotropy = this._three.renderer.capabilities.getMaxAnisotropy();
      return sstTexture;
    },

    sstColor(value) {
      return ensoFieldColor(value, this.sstMode === 'anomaly');
    },

    refreshSurfaceTextures() {
      const state = this._three;
      if (!state) return;
      const texture = this.makeTextures();
      if (state.sstTexture) state.sstTexture.dispose();
      state.sstTexture = texture;
      state.sstMaterial.map = texture;
      state.sstMaterial.needsUpdate = true;
    },

    readWindField() {
      if (!Array.isArray(this.windData) || this.windData.length < 2) return null;
      const u = this.windData.find((field) => field && field.header && field.header.parameterNumber === 2) || this.windData[0];
      const v = this.windData.find((field) => field && field.header && field.header.parameterNumber === 3) || this.windData[1];
      if (!u || !v || !u.header || !v.header || !Array.isArray(u.data) || !Array.isArray(v.data)) return null;
      return { header: u.header, u: u.data, v: v.data };
    },

    refreshWind() {
      const state = this._three;
      if (!state) return;
      state.windField = this.readWindField();
      if (state.particles) {
        state.group.remove(state.particles);
        state.particles.geometry.dispose();
        state.particles.material.dispose();
        state.particles = null;
      }
      if (state.streaks) {
        state.group.remove(state.streaks);
        state.streaks.geometry.dispose();
        state.streaks.material.dispose();
        state.streaks = null;
      }
      state.particleState = [];
      if (!state.windField) return;

      const particleCount = state.reducedMotion
        ? 1
        : (window.innerWidth < 768 ? MOBILE_PARTICLE_COUNT : PARTICLE_COUNT);
      const pointPositions = new Float32Array(particleCount * 3);
      const pointColors = new Float32Array(particleCount * 3);
      const streakPositions = new Float32Array(particleCount * (TRAIL_LENGTH - 1) * 2 * 3);
      const streakColors = new Float32Array(streakPositions.length);
      const color = new THREE.Color();
      const initialColor = this.windColor(8, color);
      for (let index = 0; index < particleCount; index += 1) {
        const latitude = Math.asin(Math.random() * 2 - 1) * 180 / Math.PI;
        const longitude = Math.random() * 360;
        const trail = Array.from({ length: TRAIL_LENGTH }, () => ({ longitude, latitude }));
        state.particleState.push({ longitude, latitude, trail, trailElapsed: 0, age: Math.random() * 3.5 });
        pointColors.set([initialColor.r, initialColor.g, initialColor.b], index * 3);
        const trailStart = index * (TRAIL_LENGTH - 1) * 2 * 3;
        for (let vertex = 0; vertex < (TRAIL_LENGTH - 1) * 2; vertex += 1) {
          streakColors.set([initialColor.r, initialColor.g, initialColor.b], trailStart + vertex * 3);
        }
      }

      const pointGeometry = new THREE.BufferGeometry();
      pointGeometry.setAttribute('position', new THREE.BufferAttribute(pointPositions, 3).setUsage(THREE.DynamicDrawUsage));
      pointGeometry.setAttribute('color', new THREE.BufferAttribute(pointColors, 3));
      const points = new THREE.Points(pointGeometry, new THREE.PointsMaterial({
        size: 1,
        sizeAttenuation: false,
        vertexColors: true,
        transparent: true,
        opacity: 0,
        depthWrite: false
      }));
      state.group.add(points);
      state.particles = points;

      const streakGeometry = new THREE.BufferGeometry();
      streakGeometry.setAttribute('position', new THREE.BufferAttribute(streakPositions, 3).setUsage(THREE.DynamicDrawUsage));
      streakGeometry.setAttribute('color', new THREE.BufferAttribute(streakColors, 3));
      const streaks = new THREE.LineSegments(streakGeometry, new THREE.LineBasicMaterial({
        vertexColors: true,
        transparent: true,
        opacity: 0.86,
        depthWrite: false
      }));
      state.group.add(streaks);
      state.streaks = streaks;
      this.applyLayer();
    },

    windColor(speed, target) {
      const color = target || new THREE.Color();
      const strength = Math.max(0, Math.min(1, speed / 22));
      color.setHSL(0.64 - strength * 0.36, 0.86, 0.58);
      return color;
    },

    sampleWind(longitude, latitude, target) {
      const result = target || { u: 0, v: 0 };
      const field = this._three && this._three.windField;
      if (!field) {
        result.u = 0;
        result.v = 0;
        return result;
      }
      const header = field.header;
      const nx = header.nx;
      const ny = header.ny;
      const dx = Math.abs(header.dx) || 1;
      const dy = Math.abs(header.dy) || 1;
      const wrapsLongitude = nx * dx >= 359.5;
      const gridX = ((((longitude - (header.lo1 || 0)) % 360) + 360) % 360) / dx;
      const gridY = Math.max(0, Math.min(ny - 1, ((header.la1 || 90) - latitude) / dy));
      const x0 = Math.floor(gridX);
      const x1 = x0 + 1;
      const y0 = Math.max(0, Math.min(ny - 1, Math.floor(gridY)));
      const y1 = Math.max(0, Math.min(ny - 1, y0 + 1));
      const fx = gridX - x0;
      const fy = Math.max(0, Math.min(1, gridY - Math.floor(gridY)));
      const sample = (data, x, y) => {
        let sampleX = x;
        if (wrapsLongitude) sampleX = ((sampleX % nx) + nx) % nx;
        else sampleX = Math.max(0, Math.min(nx - 1, sampleX));
        const value = Number(data[y * nx + sampleX]);
        return Number.isFinite(value) ? value : 0;
      };
      const interpolate = (data) => {
        const north = sample(data, x0, y0) * (1 - fx) + sample(data, x1, y0) * fx;
        const south = sample(data, x0, y1) * (1 - fx) + sample(data, x1, y1) * fx;
        return north * (1 - fy) + south * fy;
      };
      result.u = interpolate(field.u);
      result.v = interpolate(field.v);
      return result;
    },

    toggleReferenceArea() {
      this.regionVisible = !this.regionVisible;
      this.applyRegionVisibility();
    },

    // 同步 Niño3.4 参考区的可见性（手动开关 && 回放抑制 共同决定）
    applyRegionVisibility() {
      const state = this._three;
      const shown = this.regionShown;
      if (state) {
        [state.ninoArea, state.ninoBox, state.ninoMarker].forEach((object) => {
          if (object) object.visible = shown;
        });
      }
      this.updateRegionLabel();
    },

    applyLayer() {
      const state = this._three;
      if (!state) return;
      state.sstMesh.visible = this.layer === 'sst' || this.layer === 'both';
      if (state.particles) state.particles.visible = this.layer !== 'sst';
      if (state.streaks) state.streaks.visible = this.layer !== 'sst';
      state.sstMaterial.opacity = this.layer === 'both' ? 0.72 : 0.94;
    },

    bindInteraction() {
      const state = this._three;
      const canvas = state && state.renderer.domElement;
      if (!canvas) return;
      state.pointerDown = (event) => {
        if (event.button !== undefined && event.button !== 0) return;
        state.drag = { x: event.clientX, y: event.clientY };
        state.downAt = { x: event.clientX, y: event.clientY };
        state.fly = null;
        canvas.style.cursor = 'grabbing';
        if (canvas.setPointerCapture && event.pointerId !== undefined) canvas.setPointerCapture(event.pointerId);
      };
      state.pointerMove = (event) => {
        if (!state.drag) return;
        const dx = event.clientX - state.drag.x;
        const dy = event.clientY - state.drag.y;
        state.drag = { x: event.clientX, y: event.clientY };
        state.group.rotation.y += dx * 0.005;
        state.group.rotation.x = Math.max(-0.68, Math.min(0.68, state.group.rotation.x + dy * 0.004));
        event.preventDefault();
      };
      state.pointerUp = (event) => {
        const origin = state.downAt;
        state.downAt = null;
        state.drag = null;
        canvas.style.cursor = 'grab';
        // A2 · 3D 点击落钉：按下/弹起位移极小视为点击，射线求交地球反解经纬度
        if (!origin) return;
        const moved = Math.abs(event.clientX - origin.x) + Math.abs(event.clientY - origin.y);
        if (moved > 5) return;
        const rect = canvas.getBoundingClientRect();
        if (!rect.width || !rect.height) return;
        const raycaster = new THREE.Raycaster();
        raycaster.setFromCamera(new THREE.Vector2(
          ((event.clientX - rect.left) / rect.width) * 2 - 1,
          -((event.clientY - rect.top) / rect.height) * 2 + 1
        ), state.camera);
        const hit = raycaster.intersectObject(state.earth, false)[0];
        if (!hit) return;
        const local = state.group.worldToLocal(hit.point.clone());
        const radius = local.length();
        if (!radius) return;
        const lat = Math.asin(Math.max(-1, Math.min(1, local.y / radius))) * 180 / Math.PI;
        const lon = Math.atan2(local.z, -local.x) * 180 / Math.PI;
        this.$emit('globe-pick', { lon, lat });
      };
      state.wheel = (event) => {
        event.preventDefault();
        state.fly = null;
      state.zoom = Math.max(3.6, Math.min(7.4, state.zoom + Math.sign(event.deltaY) * 0.32));
        state.camera.position.z = state.zoom * state.mobileZoom;
      };
      canvas.addEventListener('pointerdown', state.pointerDown);
      canvas.addEventListener('pointermove', state.pointerMove);
      canvas.addEventListener('pointerup', state.pointerUp);
      canvas.addEventListener('pointercancel', state.pointerUp);
      canvas.addEventListener('wheel', state.wheel, { passive: false });
    },

    observeSize() {
      const state = this._three;
      if (!state) return;
      const resize = () => this.resizeScene();
      if (typeof ResizeObserver !== 'undefined') {
        state.resizeObserver = new ResizeObserver(resize);
        state.resizeObserver.observe(this.$refs.stage);
      } else {
        window.addEventListener('resize', resize);
        state.resizeObserver = { disconnect: () => window.removeEventListener('resize', resize) };
      }
      this.resizeScene();
    },

    resizeScene() {
      const state = this._three;
      const stage = this.$refs.stage;
      if (!state || !stage) return;
      const width = Math.max(1, stage.clientWidth);
      const height = Math.max(1, stage.clientHeight);
      state.renderer.setSize(width, height, false);
      state.camera.aspect = width / height;
      state.camera.fov = width < 768 ? 48 : 36;
      state.mobileZoom = width < 768 ? 1.16 : 1;
      state.camera.position.x = 0;
      state.camera.position.y = 0;
      state.camera.position.z = state.zoom * state.mobileZoom;
      state.camera.updateProjectionMatrix();
      state.group.position.x = this.layout === 'center' ? 0 : (width > 900 ? 0.78 : 0);
    },

    renderFrame(time) {
      const state = this._three;
      if (!state) return;
      state.frame = requestAnimationFrame((nextTime) => this.renderFrame(nextTime));
      const delta = state.lastFrame ? Math.min((time - state.lastFrame) / 1000, 0.04) : 0;
      state.lastFrame = time;
      const motionEnabled = !state.reducedMotion;
      // 有点位探针时暂停自转，避免钉子转到背面"消失"
      if (motionEnabled && !state.drag && !this.pins.length) state.group.rotation.y += delta * 0.022;
      // A6 · 分区定位飞行：缓动覆盖自转，拖动/滚轮会中断
      if (state.fly) {
        const fly = state.fly;
        const progress = Math.min(1, (time - fly.start) / fly.duration);
        const eased = 1 - Math.pow(1 - progress, 3);
        state.group.rotation.y = fly.fromY + (fly.toY - fly.fromY) * eased;
        state.group.rotation.x = fly.fromX + (fly.toX - fly.fromX) * eased;
        state.zoom = fly.fromZoom + (fly.toZoom - fly.fromZoom) * eased;
        state.camera.position.z = state.zoom * state.mobileZoom;
        if (progress >= 1) state.fly = null;
      }
      if (motionEnabled || state.firstWindFrame) this.updateParticles(state.firstWindFrame ? Math.max(delta, 0.016) : delta);
      state.firstWindFrame = false;
      if (motionEnabled) {
        const pulse = (Math.sin(time * 0.0018) + 1) * 0.5;
        state.ninoGlow.material.opacity = 0.14 + pulse * 0.15;
        state.ninoGlow.scale.setScalar(0.92 + pulse * 0.17);
        state.ninoHalo.material.opacity = 0.54 + pulse * 0.4;
        state.ninoHalo.scale.setScalar(0.92 + pulse * 0.12);
        state.orbit.rotation.y += delta * 0.018;
        state.orbitAccent.rotation.y -= delta * 0.012;
      }
      this.updateRegionLabel();
      this.updateOverlays();
      state.renderer.render(state.scene, state.camera);
    },

    updateRegionLabel() {
      const state = this._three;
      const label = this.$refs.ninoLabel;
      if (!state || !label) return;
      if (!this.regionShown) {
        label.style.visibility = 'hidden';
        label.style.opacity = '0';
        return;
      }
      state.group.updateMatrixWorld(true);
      state.labelWorldPosition.copy(state.ninoLabelAnchor).applyMatrix4(state.group.matrixWorld);
      state.globeWorldPosition.setFromMatrixPosition(state.group.matrixWorld);
      state.labelNormal.subVectors(state.labelWorldPosition, state.globeWorldPosition);
      state.cameraDirection.subVectors(state.camera.position, state.globeWorldPosition);
      const onVisibleHemisphere = state.labelNormal.dot(state.cameraDirection) > 0;
      state.labelScreenPosition.copy(state.labelWorldPosition).project(state.camera);
      const withinView = Math.abs(state.labelScreenPosition.x) <= 1 && Math.abs(state.labelScreenPosition.y) <= 1;
      if (!onVisibleHemisphere || !withinView) {
        label.style.visibility = 'hidden';
        label.style.opacity = '0';
        return;
      }

      const stage = this.$refs.stage;
      const x = ((state.labelScreenPosition.x + 1) / 2) * stage.clientWidth;
      const rawY = ((1 - state.labelScreenPosition.y) / 2) * stage.clientHeight;
      const y = Math.max(label.offsetHeight / 2 + 10, Math.min(stage.clientHeight - label.offsetHeight / 2 - 10, rawY));
      label.style.left = `${Math.max(10, Math.min(stage.clientWidth - 10, x))}px`;
      label.style.top = `${y}px`;
      const placeLeft = x > stage.clientWidth - 195;
      label.classList.toggle('is-left', placeLeft);
      label.style.transform = placeLeft
        ? 'translate(calc(-100% - 20px), -50%)'
        : 'translate(20px, -50%)';
      label.style.visibility = 'visible';
      label.style.opacity = '1';
    },

    // A6 · 分区边界：经度统一换算到 0–360°，跨日界线（如 Niño4）时 east 加 360
    regionBounds() {
      const region = this.regionInfo || {};
      const westBase = Number.isFinite(region.west) ? region.west : -170;
      const eastBase = Number.isFinite(region.east) ? region.east : -120;
      const south = Number.isFinite(region.south) ? region.south : -5;
      const north = Number.isFinite(region.north) ? region.north : 5;
      const west = ((westBase % 360) + 360) % 360;
      let east = ((eastBase % 360) + 360) % 360;
      if (east <= west) east += 360;
      return { west, east, south, north, midLon: (west + east) / 2, midLat: (south + north) / 2 };
    },

    // 分区切换：按新边界重建球面参考框 / 填充面 / 中心点，并同步光斑与标签锚点
    rebuildRegionBounds() {
      const state = this._three;
      if (!state || !state.ninoBox) return;
      const bounds = this.regionBounds();
      const visible = this.regionShown;
      [state.ninoArea, state.ninoBox].forEach((object) => {
        if (!object) return;
        state.group.remove(object);
        object.geometry.dispose();
        object.material.dispose();
      });
      state.ninoArea = this.createRegionArea(bounds.west, bounds.east, bounds.south, bounds.north);
      state.ninoBox = this.createRegionOutline(bounds.west, bounds.east, bounds.south, bounds.north);
      state.ninoArea.visible = visible;
      state.ninoBox.visible = visible;
      state.group.add(state.ninoArea);
      state.group.add(state.ninoBox);
      if (state.ninoMarker) {
        state.ninoMarker.position.copy(toSpherePoint(bounds.midLon, bounds.midLat, RADIUS + 0.055));
        state.ninoMarker.visible = visible;
      }
      this.applyRegionFocus();
    },

    // A6 · 分区切换：移动球面参考区光斑、光环与标签锚点
    applyRegionFocus() {
      const state = this._three;
      if (!state) return;
      const info = this.regionInfo;
      const glow = toSpherePoint(info.glowLon, info.glowLat, RADIUS + 0.052);
      const halo = toSpherePoint(info.glowLon, info.glowLat, RADIUS + 0.063);
      if (state.ninoGlow) state.ninoGlow.position.copy(glow);
      if (state.ninoHalo) {
        state.ninoHalo.position.copy(halo);
        state.ninoHalo.lookAt(halo.clone().multiplyScalar(2));
      }
      if (state.ninoGlow && info.glowScale) state.ninoGlow.scale.setScalar(info.glowScale);
      if (state.ninoLabelAnchor) state.ninoLabelAnchor.copy(toSpherePoint(info.glowLon + 20, info.glowLat, RADIUS + 0.035));
    },

    // 2D 钉子 → 3D 球面标记：原生 DOM 管理，避免逐帧触发 Vue 重渲染
    rebuildPinMarkers() {
      const layer = this.$refs.pinLayer;
      if (!layer) return;
      layer.innerHTML = '';
      this._pinEls = [];
      const state = this._three;
      this.pins.forEach((pin) => {
        const el = document.createElement('div');
        el.className = 'globe-pin-marker';
        el.innerHTML = `<span class="pin-dot"></span><span class="pin-text">${pin.label}</span>`;
        layer.appendChild(el);
        this._pinEls.push({
          el,
          anchor: toSpherePoint(((pin.lon % 360) + 540) % 360 - 180, Math.max(-89, Math.min(89, pin.lat)), RADIUS + 0.012)
        });
      });
      // 有钉子时把球转到最新一颗钉子正面，避免"打了钉却看不到"
      if (state && this.pins.length) {
        const last = this.pins[this.pins.length - 1];
        const lon = ((last.lon % 360) + 540) % 360 - 180;
        state.group.rotation.set(0, Math.PI / 2 - (lon * Math.PI / 180), 0);
      }
      if (state) {
        state.pinWorld = state.pinWorld || new THREE.Vector3();
        state.pinScreen = state.pinScreen || new THREE.Vector3();
        state.pinNormal = state.pinNormal || new THREE.Vector3();
        state.pinCameraDir = state.pinCameraDir || new THREE.Vector3();
        state.pinGlobeCenter = state.pinGlobeCenter || new THREE.Vector3();
      }
      this.updateOverlays();
    },

    // A4 · 冷暖异常热点 → 3D 球面发光标记
    rebuildHotspotMarkers() {
      const layer = this.$refs.pinLayer;
      if (!layer) return;
      if (this._hotspotEls) this._hotspotEls.forEach(item => item.el.remove());
      this._hotspotEls = [];
      this.hotspots.forEach((spot) => {
        const el = document.createElement('div');
        el.className = `globe-hotspot-marker ${spot.tone === 'cool' ? 'cool' : 'warm'}`;
        el.innerHTML = `<span class="pin-dot"></span><span class="pin-text">${spot.text}</span>`;
        layer.appendChild(el);
        this._hotspotEls.push({
          el,
          anchor: toSpherePoint(((spot.lon % 360) + 540) % 360 - 180, Math.max(-89, Math.min(89, spot.lat)), RADIUS + 0.012)
        });
      });
      this.updateOverlays();
    },

    // 每帧把钉子/热点投影到屏幕坐标，转到背面时隐藏
    updateOverlays() {
      const state = this._three;
      const groups = [this._pinEls, this._hotspotEls];
      if (!state || groups.every(list => !list || !list.length)) return;
      const stage = this.$refs.stage;
      if (!stage) return;
      state.group.updateMatrixWorld(true);
      state.pinGlobeCenter.setFromMatrixPosition(state.group.matrixWorld);
      state.pinCameraDir.subVectors(state.camera.position, state.pinGlobeCenter);
      groups.forEach((els) => {
        if (!els) return;
        els.forEach((item) => {
          state.pinWorld.copy(item.anchor).applyMatrix4(state.group.matrixWorld);
          state.pinNormal.subVectors(state.pinWorld, state.pinGlobeCenter);
          const visible = state.pinNormal.dot(state.pinCameraDir) > 0;
          if (!visible) {
            item.el.style.display = 'none';
            return;
          }
          state.pinScreen.copy(state.pinWorld).project(state.camera);
          const x = ((state.pinScreen.x + 1) / 2) * stage.clientWidth;
          const y = ((1 - state.pinScreen.y) / 2) * stage.clientHeight;
          item.el.style.display = '';
          item.el.style.left = `${x}px`;
          item.el.style.top = `${y}px`;
        });
      });
    },

    // A5 · 距平等值线 → 球面 LineSegments（分暖/冷/零线三组）
    rebuildContourLines() {
      const state = this._three;
      if (!state) return;
      if (state.contourGroup) {
        state.group.remove(state.contourGroup);
        state.contourGroup = null;
      }
      if (!this.contourOn || !this.sstData) return;
      const { header, data } = this.sstData;
      const nx = Number(header.nx);
      const ny = Number(header.ny);
      const dy = -Math.abs(header.dy);
      const anomaly = this.sstMode === 'anomaly';
      const levels = anomaly ? [0, 0.5, 1, 1.5, -0.5, -1, -1.5] : [26, 27, 28, 29];
      const groups = { warm: [], cool: [], zero: [] };
      levels.forEach((iso) => {
        const segments = marchingSquares(data, nx, ny, iso);
        segments.forEach(seg => seg.forEach((pt) => {
          const lon = header.lo1 + pt[0] * header.dx;
          const lat = header.la1 + pt[1] * dy;
          const key = anomaly ? (iso === 0 ? 'zero' : iso > 0 ? 'warm' : 'cool') : 'warm';
          groups[key].push(toSpherePoint(lon, lat, RADIUS + 0.008));
        }));
      });
      const contourGroup = new THREE.Group();
      const material = key => new THREE.LineBasicMaterial({
        color: key === 'zero' ? 0xd5e9ef : key === 'warm' ? 0xffab61 : 0x6fb7f5,
        transparent: true,
        opacity: 0.85
      });
      Object.keys(groups).forEach((key) => {
        if (!groups[key].length) return;
        const geometry = new THREE.BufferGeometry().setFromPoints(groups[key]);
        contourGroup.add(new THREE.LineSegments(geometry, material(key)));
      });
      state.group.add(contourGroup);
      state.contourGroup = contourGroup;
    },

    // A8 · 纬向剖面切割线 → 球面纬线圆
    rebuildProfileLine() {
      const state = this._three;
      if (!state) return;
      if (state.profileLine) {
        state.group.remove(state.profileLine);
        state.profileLine.geometry.dispose();
        state.profileLine.material.dispose();
        state.profileLine = null;
      }
      if (!this.profileOn) return;
      const lat = Math.max(-89, Math.min(89, this.profileLat));
      const points = [];
      for (let lon = 0; lon <= 360; lon += 5) {
        points.push(toSpherePoint(lon, lat, RADIUS + 0.01));
      }
      const geometry = new THREE.BufferGeometry().setFromPoints(points);
      const line = new THREE.Line(geometry, new THREE.LineBasicMaterial({
        color: 0xff8f6b, transparent: true, opacity: 0.95
      }));
      state.group.add(line);
      state.profileLine = line;
    },

    updateParticles(delta) {
      const state = this._three;
      if (!state || !state.particles || !state.windField || delta <= 0) return;
      const pointPositions = state.particles.geometry.attributes.position.array;
      const streakPositions = state.streaks.geometry.attributes.position.array;
      const colorAttribute = state.particles.geometry.attributes.color.array;
      const streakColorAttribute = state.streaks.geometry.attributes.color.array;
      const sphereRadius = RADIUS + 0.026;
      const windScale = 0.84;
      const color = state.windColor;

      for (let index = 0; index < state.particleState.length; index += 1) {
        const particle = state.particleState[index];
        const flow = this.sampleWind(particle.longitude, particle.latitude, state.windSample);
        const cosine = Math.max(0.18, Math.cos(particle.latitude * Math.PI / 180));
        particle.longitude = (particle.longitude + flow.u * windScale * delta / cosine + 360) % 360;
        particle.latitude = Math.max(-84, Math.min(84, particle.latitude + flow.v * windScale * delta));
        particle.age += delta;
        particle.trailElapsed += delta;
        if (particle.age > 7.5 || Math.abs(particle.latitude) > 83.5) {
          particle.longitude = Math.random() * 360;
          particle.latitude = Math.asin(Math.random() * 2 - 1) * 180 / Math.PI;
          particle.age = 0;
          for (let trailIndex = 0; trailIndex < particle.trail.length; trailIndex += 1) {
            const point = particle.trail[trailIndex];
            point.longitude = particle.longitude;
            point.latitude = particle.latitude;
          }
        }
        if (particle.trailElapsed >= TRAIL_INTERVAL) {
          for (let trailIndex = TRAIL_LENGTH - 1; trailIndex > 0; trailIndex -= 1) {
            particle.trail[trailIndex].longitude = particle.trail[trailIndex - 1].longitude;
            particle.trail[trailIndex].latitude = particle.trail[trailIndex - 1].latitude;
          }
          particle.trail[0].longitude = particle.longitude;
          particle.trail[0].latitude = particle.latitude;
          particle.trailElapsed = 0;
        }

        writeSpherePoint(pointPositions, index * 3, particle.longitude, particle.latitude, sphereRadius);
        const speed = Math.sqrt(flow.u * flow.u + flow.v * flow.v);
        this.windColor(speed, color);
        colorAttribute[index * 3] = color.r;
        colorAttribute[index * 3 + 1] = color.g;
        colorAttribute[index * 3 + 2] = color.b;

        const trailOffset = index * (TRAIL_LENGTH - 1) * 2 * 3;
        for (let trailIndex = 0; trailIndex < TRAIL_LENGTH - 1; trailIndex += 1) {
          const offset = trailOffset + trailIndex * 6;
          writeSpherePoint(streakPositions, offset, particle.trail[trailIndex].longitude, particle.trail[trailIndex].latitude, sphereRadius);
          writeSpherePoint(streakPositions, offset + 3, particle.trail[trailIndex + 1].longitude, particle.trail[trailIndex + 1].latitude, sphereRadius);
          const tailBrightness = 1 - (trailIndex / (TRAIL_LENGTH - 1)) * 0.8;
          for (let vertex = 0; vertex < 2; vertex += 1) {
            const colorOffset = trailOffset + (trailIndex * 2 + vertex) * 3;
            streakColorAttribute[colorOffset] = color.r * tailBrightness;
            streakColorAttribute[colorOffset + 1] = color.g * tailBrightness;
            streakColorAttribute[colorOffset + 2] = color.b * tailBrightness;
          }
        }
      }

      state.particles.geometry.attributes.position.needsUpdate = true;
      state.particles.geometry.attributes.color.needsUpdate = true;
      state.streaks.geometry.attributes.position.needsUpdate = true;
      state.streaks.geometry.attributes.color.needsUpdate = true;
    },

    resetView() {
      const state = this._three;
      if (!state) return;
      state.group.rotation.set(0.08, -Math.PI * 2 / 3, 0);
      state.zoom = 5.3;
      state.camera.position.set(0, 0, state.zoom * state.mobileZoom);
    },

    focusRegion() {
      const state = this._three;
      if (!state) return;
      // A6 · 定位到当前选中的参考区，而不是固定的 Niño3.4
      const targetY = Math.PI / 2 - (this.regionInfo.glowLon * Math.PI) / 180;
      const targetZoom = 4.55;
      if (state.reducedMotion) {
        state.group.rotation.set(0, targetY, 0);
        state.zoom = targetZoom;
        state.camera.position.set(0, 0, state.zoom * state.mobileZoom);
        state.fly = null;
        return;
      }
      // 平滑飞行：经度取最短路径，缓动过渡到目标视角
      const fromY = state.group.rotation.y;
      let deltaY = (targetY - fromY) % (Math.PI * 2);
      if (deltaY > Math.PI) deltaY -= Math.PI * 2;
      if (deltaY < -Math.PI) deltaY += Math.PI * 2;
      state.fly = {
        fromY,
        toY: fromY + deltaY,
        fromX: state.group.rotation.x,
        toX: 0,
        fromZoom: state.zoom,
        toZoom: targetZoom,
        start: performance.now(),
        duration: 750
      };
    },

    disposeScene() {
      const state = this._three;
      if (!state) return;
      if (state.frame) cancelAnimationFrame(state.frame);
      if (state.resizeObserver) state.resizeObserver.disconnect();
      const canvas = state.renderer.domElement;
      canvas.removeEventListener('pointerdown', state.pointerDown);
      canvas.removeEventListener('pointermove', state.pointerMove);
      canvas.removeEventListener('pointerup', state.pointerUp);
      canvas.removeEventListener('pointercancel', state.pointerUp);
      canvas.removeEventListener('wheel', state.wheel);
      state.scene.traverse((object) => {
        if (object.geometry) object.geometry.dispose();
        if (object.material) {
          const materials = Array.isArray(object.material) ? object.material : [object.material];
          materials.forEach((material) => {
            if (material.map) material.map.dispose();
            material.dispose();
          });
        }
      });
      if (state.earthTexture) state.earthTexture.dispose();
      if (state.sstTexture) state.sstTexture.dispose();
      state.renderer.dispose();
      if (canvas.parentNode) canvas.parentNode.removeChild(canvas);
      this._three = null;
    }
  }
};
</script>

<style scoped>
.enso-globe-stage {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  background: #071923;
  cursor: grab;
}

.enso-globe-stage:active {
  cursor: grabbing;
}

.enso-globe-canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.globe-controls {
  position: absolute;
  right: 20px;
  bottom: 82px;
  z-index: 1;
  display: flex;
  gap: 7px;
}

.globe-control {
  width: 38px;
  height: 38px;
  padding: 0;
  border: 1px solid rgba(148, 180, 220, 0.3);
  background: rgba(8, 22, 46, 0.78);
  color: #d4e7eb;
  cursor: pointer;
  font-size: 16px;
}

.globe-control:hover {
  border-color: #69c7cd;
  color: #fff;
}

.globe-control.active {
  border-color: rgba(255, 214, 130, 0.82);
  color: #ffe08a;
}

.globe-region-label {
  position: absolute;
  z-index: 2;
  display: flex;
  min-width: 132px;
  max-width: 168px;
  flex-direction: column;
  gap: 1px;
  padding: 6px 9px 7px;
  border: 1px solid rgba(255, 214, 130, 0.82);
  border-left: 3px solid #ffd36a;
  background: rgba(6, 20, 29, 0.92);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.44);
  color: #f2f6f2;
  pointer-events: none;
  visibility: hidden;
  opacity: 0;
  transition: opacity 140ms ease;
}

.globe-region-label::before {
  position: absolute;
  top: 50%;
  left: -13px;
  width: 10px;
  border-top: 1px solid rgba(255, 214, 130, 0.9);
  content: '';
}

.globe-region-label.is-left {
  border-right: 3px solid #ffd36a;
  border-left: 1px solid rgba(255, 214, 130, 0.82);
}

.globe-region-label.is-left::before {
  right: -13px;
  left: auto;
}

.globe-region-kicker {
  color: #ffd36a;
  font-size: 8.5px;
  letter-spacing: 0.04em;
  line-height: 1.2;
}

.globe-region-label strong {
  color: #fff4d1;
  font-size: 12.5px;
  line-height: 1.25;
}

.globe-region-label > span:not(.globe-region-kicker) {
  color: #d1e0e1;
  font-size: 10px;
}

.globe-region-label small {
  color: #afc5cb;
  font-size: 9px;
  line-height: 1.3;
}

.globe-error {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: #d8e7e8;
  font-size: 13px;
}

@media (max-width: 767px) {
  .globe-controls {
    right: 12px;
    bottom: 74px;
  }

  .globe-region-label {
    min-width: 118px;
    max-width: min(152px, calc(100vw - 32px));
  }
}

</style>

<style>
/* ---- 2D 点位探针/热点的球面投影标记 ----
   注意：标记由 createElement 动态创建，没有 scoped 属性，样式必须放非 scoped 块 */
.enso-globe-stage .globe-pin-layer {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
}

.enso-globe-stage .globe-pin-marker,
.enso-globe-stage .globe-hotspot-marker {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 5px;
  transform: translate(-50%, -50%);
  left: -9999px;
  top: -9999px;
}

.enso-globe-stage .globe-pin-marker .pin-dot {
  width: 9px;
  height: 9px;
  flex: 0 0 9px;
  border: 1.5px solid #fff3d8;
  border-radius: 50%;
  background: #ffce7a;
  box-shadow: 0 0 10px rgba(255, 200, 120, .85);
}

.enso-globe-stage .globe-pin-marker .pin-text {
  padding: 2px 6px;
  border: 1px solid rgba(255, 206, 122, .5);
  border-radius: 3px;
  background: rgba(6, 22, 30, .82);
  color: #ffd694;
  font-size: 9px;
  line-height: 1.3;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.enso-globe-stage .globe-hotspot-marker .pin-dot {
  width: 10px;
  height: 10px;
  flex: 0 0 10px;
  border: 1.5px solid rgba(255, 255, 255, .85);
  border-radius: 50%;
}

.enso-globe-stage .globe-hotspot-marker.warm .pin-dot {
  background: #ff8b55;
  box-shadow: 0 0 12px rgba(255, 130, 70, .95);
}

.enso-globe-stage .globe-hotspot-marker.cool .pin-dot {
  background: #5aa8f0;
  box-shadow: 0 0 12px rgba(90, 168, 240, .95);
}

.enso-globe-stage .globe-hotspot-marker .pin-text {
  padding: 3px 7px;
  border: 1px solid rgba(255, 178, 110, .45);
  border-left-width: 3px;
  border-radius: 3px;
  background: rgba(6, 20, 29, .92);
  color: #ffd7ad;
  font-size: 10px;
  line-height: 1.3;
  white-space: nowrap;
}

.enso-globe-stage .globe-hotspot-marker.cool .pin-text {
  border-color: rgba(120, 180, 240, .45);
  color: #bfe0ff;
}
</style>
