<template>
  <div ref="stage" class="enso-globe-stage" @contextmenu.prevent>
    <div class="globe-controls">
      <button
        class="globe-control"
        type="button"
        title="定位 Niño3.4 海区"
        aria-label="定位 Niño3.4 海区"
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
      <strong>Niño3.4 指数参考区</strong>
      <span>赤道中东太平洋</span>
      <small>5°N–5°S · 170°W–120°W</small>
    </div>
    <div v-if="rendererError" class="globe-error" role="status">
      3D 场景不可用，已切换到二维地图
    </div>
  </div>
</template>

<script>
import * as THREE from 'three';
import earthMapUrl from '../assets/earth.jpg';

const RADIUS = 2;
const PARTICLE_COUNT = 5200;
const MOBILE_PARTICLE_COUNT = 2800;
const TRAIL_LENGTH = 18;
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
    layer: { type: String, default: 'wind' }
  },
  data() {
    return { rendererError: '', regionVisible: true };
  },
  watch: {
    windData() {
      this.refreshWind();
    },
    sstData() {
      this.refreshSurfaceTextures();
    },
    layer() {
      this.applyLayer();
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
        renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.75));
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
        const camera = new THREE.PerspectiveCamera(36, width / height, 0.1, 80);
        camera.position.set(0, 0, 6.1);

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

        const earthGeometry = new THREE.SphereGeometry(RADIUS, 256, 192);
        const earthMaterial = new THREE.MeshStandardMaterial({
          color: 0xffffff,
          roughness: 0.92,
          metalness: 0.02
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

        const ninoArea = this.createNino34Area();
        group.add(ninoArea);
        const grid = this.createGraticule();
        group.add(grid);
        const ninoBox = this.createNino34Outline();
        group.add(ninoBox);
        const ninoCenter = toSpherePoint(215, 0, RADIUS + 0.055);
        const ninoMarker = new THREE.Mesh(
          new THREE.SphereGeometry(0.035, 20, 14),
          new THREE.MeshBasicMaterial({ color: 0xffe08a, depthTest: true })
        );
        ninoMarker.position.copy(ninoCenter);
        group.add(ninoMarker);
        const ninoLabelAnchor = toSpherePoint(242, 0, RADIUS + 0.035);

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
          ninoLabelAnchor,
          labelWorldPosition: new THREE.Vector3(),
          globeWorldPosition: new THREE.Vector3(),
          labelNormal: new THREE.Vector3(),
          cameraDirection: new THREE.Vector3(),
          labelScreenPosition: new THREE.Vector3(),
          atmosphere,
          frame: 0,
          lastFrame: 0,
          resizeObserver: null,
          drag: null,
          particles: null,
          streaks: null,
          particleState: [],
          windField: null,
          windSample: { u: 0, v: 0 },
          windColor: new THREE.Color(),
          earthTexture: null,
          sstTexture: null,
          zoom: 6.1,
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

    createNino34Outline() {
      const points = [];
      const west = 190;
      const east = 240;
      const south = -5;
      const north = 5;
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

    createNino34Area() {
      const west = 190;
      const east = 240;
      const south = -5;
      const north = 5;
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
      new THREE.TextureLoader().load(earthMapUrl, (texture) => {
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

      const width = Math.max(720, Math.min(1440, sourceWidth * 4));
      const height = width / 2;
      const sstCanvas = document.createElement('canvas');
      sstCanvas.width = width;
      sstCanvas.height = height;
      const sstContext = sstCanvas.getContext('2d');
      const sstImage = sstContext.createImageData(width, height);

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
          const color = this.sstColor(weighted / totalWeight);
          sstImage.data[offset] = color[0];
          sstImage.data[offset + 1] = color[1];
          sstImage.data[offset + 2] = color[2];
          sstImage.data[offset + 3] = Math.round(238 * totalWeight);
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
      const stops = [
        { value: -2, rgb: [29, 66, 132] },
        { value: 0, rgb: [38, 137, 184] },
        { value: 8, rgb: [74, 190, 193] },
        { value: 16, rgb: [120, 194, 117] },
        { value: 22, rgb: [220, 207, 96] },
        { value: 28, rgb: [235, 135, 66] },
        { value: 34, rgb: [194, 55, 52] }
      ];
      const bounded = Math.max(stops[0].value, Math.min(stops[stops.length - 1].value, value));
      let upperIndex = stops.findIndex((stop) => stop.value >= bounded);
      if (upperIndex <= 0) return stops[0].rgb;
      const lower = stops[upperIndex - 1];
      const upper = stops[upperIndex];
      const ratio = (bounded - lower.value) / (upper.value - lower.value);
      return lower.rgb.map((channel, index) => Math.round(channel + (upper.rgb[index] - channel) * ratio));
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

      const particleCount = window.innerWidth < 768 ? MOBILE_PARTICLE_COUNT : PARTICLE_COUNT;
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
        opacity: 0.68,
        depthWrite: false
      }));
      state.group.add(streaks);
      state.streaks = streaks;
      this.applyLayer();
    },

    windColor(speed, target) {
      const color = target || new THREE.Color();
      const strength = Math.max(0, Math.min(1, speed / 22));
      color.setHSL(0.64 - strength * 0.32, 0.78, 0.56);
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
      const state = this._three;
      if (!state) return;
      [state.ninoArea, state.ninoBox, state.ninoMarker].forEach((object) => {
        object.visible = this.regionVisible;
      });
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
      state.pointerUp = () => {
        state.drag = null;
        canvas.style.cursor = 'grab';
      };
      state.wheel = (event) => {
        event.preventDefault();
      state.zoom = Math.max(4.25, Math.min(8.5, state.zoom + Math.sign(event.deltaY) * 0.32));
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
      state.group.position.x = width > 900 ? 0.78 : 0;
    },

    renderFrame(time) {
      const state = this._three;
      if (!state) return;
      state.frame = requestAnimationFrame((nextTime) => this.renderFrame(nextTime));
      const delta = state.lastFrame ? Math.min((time - state.lastFrame) / 1000, 0.04) : 0;
      state.lastFrame = time;
      if (!state.drag) state.group.rotation.y += delta * 0.035;
      this.updateParticles(delta);
      this.updateRegionLabel();
      state.renderer.render(state.scene, state.camera);
    },

    updateRegionLabel() {
      const state = this._three;
      const label = this.$refs.ninoLabel;
      if (!state || !label) return;
      if (!this.regionVisible) {
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
      const placeLeft = x > stage.clientWidth - 255;
      label.classList.toggle('is-left', placeLeft);
      label.style.transform = placeLeft
        ? 'translate(calc(-100% - 20px), -50%)'
        : 'translate(20px, -50%)';
      label.style.visibility = 'visible';
      label.style.opacity = '1';
    },

    updateParticles(delta) {
      const state = this._three;
      if (!state || !state.particles || !state.windField || delta <= 0) return;
      const pointPositions = state.particles.geometry.attributes.position.array;
      const streakPositions = state.streaks.geometry.attributes.position.array;
      const colorAttribute = state.particles.geometry.attributes.color.array;
      const streakColorAttribute = state.streaks.geometry.attributes.color.array;
      const sphereRadius = RADIUS + 0.026;
      const windScale = 0.42;
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
      state.zoom = 6.1;
      state.camera.position.set(0, 0, state.zoom * state.mobileZoom);
    },

    focusRegion() {
      const state = this._three;
      if (!state) return;
      state.group.rotation.set(0, Math.PI / 2 - (215 * Math.PI / 180), 0);
      state.zoom = 5.05;
      state.camera.position.set(0, 0, state.zoom * state.mobileZoom);
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
  min-width: 216px;
  max-width: 246px;
  flex-direction: column;
  gap: 3px;
  padding: 11px 14px 12px;
  border: 1px solid rgba(255, 214, 130, 0.82);
  border-left: 4px solid #ffd36a;
  background: rgba(6, 20, 29, 0.94);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.48);
  color: #f2f6f2;
  pointer-events: none;
  visibility: hidden;
  opacity: 0;
  transition: opacity 140ms ease;
}

.globe-region-label::before {
  position: absolute;
  top: 50%;
  left: -17px;
  width: 14px;
  border-top: 1px solid rgba(255, 214, 130, 0.9);
  content: '';
}

.globe-region-label.is-left {
  border-right: 4px solid #ffd36a;
  border-left: 1px solid rgba(255, 214, 130, 0.82);
}

.globe-region-label.is-left::before {
  right: -17px;
  left: auto;
}

.globe-region-kicker {
  color: #ffd36a;
  font-size: 11px;
  line-height: 1.2;
}

.globe-region-label strong {
  color: #fff4d1;
  font-size: 17px;
  line-height: 1.25;
}

.globe-region-label > span:not(.globe-region-kicker) {
  color: #d1e0e1;
  font-size: 13px;
}

.globe-region-label small {
  color: #afc5cb;
  font-size: 11px;
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
    min-width: 194px;
    max-width: min(226px, calc(100vw - 32px));
  }
}
</style>
