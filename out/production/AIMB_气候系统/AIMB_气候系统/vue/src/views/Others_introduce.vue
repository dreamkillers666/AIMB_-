<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Marine Heatwaves -->
      <section class="content-section">
        <h2 class="content-title">海洋热浪</h2>
        <p class="main-text">
          海洋热浪是指海洋中发生的极端高温事件，这种现象对海洋环境、生态系统和经济生活具有重要影响。海洋热浪定义为海表温度至少连续五天超出气候平均态90百分位阈值的事件。它们在时间上可持续数天至数月，面积可达几平方公里至数千平方公里，对海洋生态系统构成严重威胁由于全球变暖，海洋热浪的频次和强度居高不下，对珊瑚礁群落构成新的危机。温室效应导致超过90%的热量进入海洋，可能导致海洋热浪继续增加。
        </p>
        <div class="content-image-wrapper">
          <img src="../assets/ENSO/MHW_maps_demo_trend_us.png" alt="Marine Heatwave Trend" class="content-image" />
        </div>
      </section>

      <!-- Section 2: PDO -->
      <section class="content-section">
        <h2 class="content-title">PDO</h2>
        <p class="main-text">
          太平洋十年涛动（Pacific Decadal Oscillation, PDO），是一种气候模式，以10年周期尺度变化的以中纬度太平洋盆地为中心的强、周期性的海洋大气气候变化模式。PDO对全球气候有显著影响，包括影响风向和海洋环流，进而影响全球许多地区的气候条件。
        </p>
        <div class="content-image-wrapper">
          <img src="../assets/ENSO/PDO.jpg" alt="Pacific Decadal Oscillation" class="content-image" />
        </div>
      </section>

      <!-- Section 3: AMO -->
      <section class="content-section">
        <h2 class="content-title">AMO</h2>
        <p class="main-text">
          大西洋多年代际振荡（Atlantic Multidecadal Oscillation, AMO）发生在北大西洋区域具有海盆尺度的、多十年变率的SST异常变化。是一种自然变率，具有65~80 a的周期，振幅为0.4℃。AMO对全球气候有影响，包括对太平洋和印度洋气候的影响。
        </p>
        <div class="content-image-wrapper">
          <img src="../assets/ENSO/AMO.jpg" alt="Atlantic Multidecadal Oscillation" class="content-image" />
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // A more appropriate name for the component
  name: "others_introduce"
}
</script>

<style scoped>
/* --- 统一的深色主题样式 --- */

/* 1. 主卡片容器 */
.dark-theme-card {
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(100, 116, 139, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 16px;
}
::v-deep .el-card__body {
  padding: 24px 32px;
}

/* 2. 内容区域和章节 */
.content-wrapper {
  line-height: 1.8;
  text-align: justify;
}
.content-section {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid rgba(100, 116, 139, 0.3);
}
.content-section:first-child {
  margin-top: 0;
  padding-top: 0;
  border-top: none;
}

/* 3. 标题样式 */
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
  text-align: center;
}

/* 4. 文本样式 */
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 1.5em;
}

/* 5. 图片样式 */
.content-image-wrapper {
  margin: 24px auto; /* Center the image block */
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  background-color: rgba(255, 255, 255, 0.95);
  max-width: 90%; /* Ensure large images don't touch the edges */
}
.content-image {
  width: 100%;
  height: auto;
  display: block;
}
</style>