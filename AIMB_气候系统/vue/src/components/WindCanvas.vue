<template>
  <canvas ref="canvas" class="wind-canvas"></canvas>
</template>

<script>
/**
 * WindCanvas — 极光波纹光带背景
 *
 * 多层正弦波光带在深色背景上缓慢流动，
 * 模拟极光 / 大气层流动的柔和效果。
 * 纯 canvas 2D，零依赖，自适应分辨率。
 */
export default {
  name: "WindCanvas",
  props: {
    // 光带数量
    bandCount: { type: Number, default: 6 },
    // 流动速度
    speed: { type: Number, default: 0.000012 },
  },
  data() {
    return {
      rafId: null,
      width: 0,
      height: 0,
      dpr: 1,
      time: 0,
      bands: [],
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.resize();
      this.initBands();
      this.animate();
      window.addEventListener("resize", this.handleResize);
    });
  },
  beforeDestroy() {
    if (this.rafId) cancelAnimationFrame(this.rafId);
    window.removeEventListener("resize", this.handleResize);
  },
  methods: {
    resize() {
      const canvas = this.$refs.canvas;
      if (!canvas) return;
      this.dpr = window.devicePixelRatio || 1;
      this.width = window.innerWidth;
      this.height = window.innerHeight;
      canvas.width = this.width * this.dpr;
      canvas.height = this.height * this.dpr;
      canvas.style.width = this.width + "px";
      canvas.style.height = this.height + "px";
      const ctx = canvas.getContext("2d");
      ctx.scale(this.dpr, this.dpr);
    },
    handleResize() {
      if (this.rafId) cancelAnimationFrame(this.rafId);
      this.resize();
      this.initBands();
      this.animate();
    },
    initBands() {
      // 极光配色：蓝 → 青蓝 → 青绿 → 浅青 → 紫蓝
      const colors = [
        { r: 88, g: 166, b: 255 },    // 蓝
        { r: 103, g: 232, b: 249 },   // 青
        { r: 94, g: 234, b: 212 },    // 青绿
        { r: 125, g: 211, b: 252 },   // 浅蓝
        { r: 147, g: 197, b: 253 },   // 蓝紫
        { r: 110, g: 214, b: 232 },   // 浅青
      ];
      this.bands = [];
      for (let i = 0; i < this.bandCount; i++) {
        const color = colors[i % colors.length];
        this.bands.push({
          // 基线 y 位置（均匀分布）
          baseY: this.height * (0.15 + 0.7 * (i / Math.max(1, this.bandCount - 1))),
          // 振幅
          amplitude: 40 + Math.random() * 60,
          // 频率（波纹密度）
          frequency: 0.002 + Math.random() * 0.003,
          // 相位偏移
          phase: Math.random() * Math.PI * 2,
          // 相位移动速度
          phaseSpeed: 0.3 + Math.random() * 0.7,
          // 光带厚度
          thickness: 80 + Math.random() * 80,
          // 透明度
          opacity: 0.06 + Math.random() * 0.08,
          // 颜色
          color: color,
          // 竖直方向缓慢漂移
          driftSpeed: (Math.random() - 0.5) * 0.15,
          driftPhase: Math.random() * Math.PI * 2,
        });
      }
    },
    animate() {
      const canvas = this.$refs.canvas;
      if (!canvas) return;
      const ctx = canvas.getContext("2d");

      // 深色底
      ctx.fillStyle = "#0c0c1e";
      ctx.fillRect(0, 0, this.width, this.height);

      this.time += this.speed * 1000;

      for (let i = 0; i < this.bands.length; i++) {
        const band = this.bands[i];
        const localTime = this.time * band.phaseSpeed;

        // 当前基线 y（缓慢漂移）
        const driftY = Math.sin(this.time * band.driftSpeed + band.driftPhase) * 25;
        const baseY = band.baseY + driftY;

        // 画一条加粗的渐变光带：用多个偏移的 sine 叠加产生柔和波形
        ctx.beginPath();
        const step = 4;
        for (let x = 0; x <= this.width; x += step) {
          // 两层 sine 叠加，让波形更自然
          const wave1 = Math.sin(x * band.frequency + localTime + band.phase) * band.amplitude;
          const wave2 = Math.sin(x * band.frequency * 0.5 + localTime * 0.7 + band.phase * 1.3) * band.amplitude * 0.4;
          const y = baseY + wave1 + wave2;
          if (x === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        }
        // 闭合到下方形成带状
        ctx.lineTo(this.width, this.height);
        ctx.lineTo(0, this.height);
        ctx.closePath();

        // 沿光带方向的线性渐变（上下淡出）
        const grad = ctx.createLinearGradient(0, baseY - band.thickness, 0, baseY + band.thickness);
        const c = band.color;
        const op = band.opacity;
        grad.addColorStop(0, `rgba(${c.r},${c.g},${c.b},0)`);
        grad.addColorStop(0.5, `rgba(${c.r},${c.g},${c.b},${op})`);
        grad.addColorStop(1, `rgba(${c.r},${c.g},${c.b},0)`);
        ctx.fillStyle = grad;
        ctx.fill();
      }

      // 叠加一层极淡的顶部辉光
      const topGlow = ctx.createRadialGradient(
        this.width * 0.3, this.height * 0.1, 0,
        this.width * 0.3, this.height * 0.1, this.width * 0.6
      );
      topGlow.addColorStop(0, "rgba(88, 166, 255, 0.04)");
      topGlow.addColorStop(1, "rgba(88, 166, 255, 0)");
      ctx.fillStyle = topGlow;
      ctx.fillRect(0, 0, this.width, this.height);

      const bottomGlow = ctx.createRadialGradient(
        this.width * 0.7, this.height * 0.9, 0,
        this.width * 0.7, this.height * 0.9, this.width * 0.5
      );
      bottomGlow.addColorStop(0, "rgba(103, 232, 249, 0.03)");
      bottomGlow.addColorStop(1, "rgba(103, 232, 249, 0)");
      ctx.fillStyle = bottomGlow;
      ctx.fillRect(0, 0, this.width, this.height);

      this.rafId = requestAnimationFrame(this.animate);
    },
  },
};
</script>

<style scoped>
.wind-canvas {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
