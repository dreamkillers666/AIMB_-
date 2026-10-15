<template>
  <span class="animated-number">{{ display }}</span>
</template>

<script>
// B3 · KPI 数字翻牌：数值变化时按 easeOutCubic 补间滚动，保持项目里的
// 正负号书写习惯（'+0.80' / '−0.40'，负号为 U+2212）。
export default {
  name: 'AnimatedNumber',
  props: {
    value: { type: [String, Number], default: '—' },
    decimals: { type: Number, default: 2 },
    duration: { type: Number, default: 720 },
    pulseKey: { type: String, default: '' }
  },
  data() {
    return {
      display: '—',
      frame: null
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(next, previous) {
        const target = this.parse(next)
        const from = previous === undefined ? target : this.parse(previous)
        this.animate(from, target)
      }
    }
  },
  beforeDestroy() {
    if (this.frame) cancelAnimationFrame(this.frame)
    this.frame = null
  },
  methods: {
    parse(raw) {
      if (raw === null || raw === undefined || raw === '' || raw === '—') return null
      const text = String(raw).replace('−', '-').replace('＋', '+').replace(/[^\d.+-]/g, '')
      const number = Number(text)
      return Number.isFinite(number) ? number : null
    },
    animate(from, to) {
      if (this.frame) {
        cancelAnimationFrame(this.frame)
        this.frame = null
      }
      const keepPlus = to !== null && to > 0 && String(this.value).indexOf('+') === 0
      if (from === null || to === null || from === to || typeof requestAnimationFrame !== 'function') {
        this.display = this.format(to, keepPlus)
        if (this.pulseKey) this.$emit('change', this.pulseKey)
        return
      }
      const startedAt = performance.now()
      const step = (now) => {
        const progress = Math.min(1, (now - startedAt) / this.duration)
        const eased = 1 - Math.pow(1 - progress, 3)
        this.display = this.format(from + (to - from) * eased, keepPlus)
        if (progress < 1) {
          this.frame = requestAnimationFrame(step)
        } else {
          this.frame = null
          this.display = this.format(to, keepPlus)
          if (this.pulseKey) this.$emit('change', this.pulseKey)
        }
      }
      this.frame = requestAnimationFrame(step)
    },
    format(number, keepPlus) {
      if (number === null) return '—'
      const rounded = Math.abs(number).toFixed(this.decimals)
      if (number < 0) return `−${rounded}`
      return keepPlus ? `+${rounded}` : rounded
    }
  }
}
</script>

<style scoped>
.animated-number{font-variant-numeric:tabular-nums}
</style>
