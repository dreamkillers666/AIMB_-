<template>
  <div id="app">
    <router-view/>
    <!-- 全局 ENSO AI 助手：悬浮于所有页面之上，可拖动 -->
    <enso-ai-assistant />
  </div>
</template>

<script>
import EnsoAiAssistant from '@/components/EnsoAiAssistant.vue'

export default {
  name: 'App',
  components: { EnsoAiAssistant }
}
</script>

<style>
/* --- 全局深色主题样式 --- */

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;

  /* 核心：从深海军蓝到近乎黑色的渐变，营造深邃感 */
  background: linear-gradient(135deg, #0f172a, #0c0c1e);
  background-attachment: fixed; /* 固定背景，滚动时不移动 */
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;

  /* 核心：设定全局默认字体颜色为柔和的浅灰色，确保可读性 */
  color: #cbd5e1;
}
</style>