<template>
  <!-- 1. The main container is a single, styled "dark theme" card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu, consistently styled -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/mjo_introduce'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/mjo_introduce">MJO介绍</el-menu-item>
        <el-menu-item index="/usermanage/mjo_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/mjo_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: MJO Introduction -->
      <section class="content-section text-center">
        <h2 class="content-title">MJO</h2>
        <p class="main-text">
          马登朱利安震荡(Madden-Julian Oscillation，MJO)是一种重要的气候现象，主要影响热带地区的天气模式和全球气候。
          它由两个主要部分组成：大尺度的环流和区域性的降水变化，通常表现为热带地区的热带波动。
          这一现象的周期大约为30到60天，主要通过影响降水、风场和温度的变化而对气候系统产生深远影响。
        </p>
        <div class="content-image-wrapper">
          <img src="../assets/MJO/mjo.png" alt="MJO Banner" class="content-image" />
        </div>
      </section>

      <!-- Section 2: Meteorological Factors -->
      <section class="content-section">
        <h2 class="content-title text-center">气象因子</h2>
        <div class="two-column-layout">
          <!-- Left Column: OLR -->
          <div class="column">
            <h3 class="sub-heading">OLR</h3>
            <p class="main-text">
              OLR（出射长波辐射）是指地球表面向外辐射的长波辐射能量，反映了地球的热量损失。在MJO预测中，OLR是重要的气象因子，通过卫星遥感技术获取。OLR数据能够揭示热带地区的云层分布和辐射特征，帮助研究者识别MJO的发生与发展。
            </p>
            <div class="content-image-wrapper">
              <img src="../assets/MJO/olr.png" alt="OLR图" class="content-image" />
            </div>
          </div>
          <!-- Right Column: U-wind -->
          <div class="column">
            <h3 class="sub-heading">U-wind(200&850)</h3>
            <p class="main-text">
              U-wind代表的是纬向风速，在850 hPa和200 hPa的气压层次上分别描述了近地面和高层大气的风场特征。这些数据通常通过气象探空和卫星观测获得，对MJO的动力学特征有重要意义。分析这两个层次的U-wind变化，可以更准确地捕捉MJO的活动及其对全球天气和气候的影响。
            </p>
            <div class="image-pair">
              <div class="content-image-wrapper">
                <img src="../assets/MJO/u200.png" alt="U-wind 200 hPa" class="content-image" />
              </div>
              <div class="content-image-wrapper">
                <img src="../assets/MJO/u850.png" alt="U-wind 850 hPa" class="content-image" />
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Section 3: MJO Indices -->
      <section class="content-section">
        <h2 class="content-title text-center">MJO指数</h2>
        <div class="two-column-layout">
          <!-- Left Column: OMI -->
          <div class="column">
            <h3 class="sub-heading">OMI (OLR MJO Index)</h3>
            <p class="main-text">
              LH00使用了全球热带区域的出射长波辐射（OLR）或流函数的经验正交函数（EOFs）来定义MJO的空间模式。
              OMI指数来自于EOFs的时间系数,通过对带通滤波数据进行EOF分析长期以来一直可以识别MJO。
            </p>
            <div class="content-image-wrapper">
              <img src="../assets/MJO/OMI_phase.svg" alt="OMI Phase" class="content-image" />
            </div>
          </div>
          <!-- Right Column: RMM -->
          <div class="column">
            <h3 class="sub-heading">RMM (Real-Time Multivariate MJO Index)</h3>
            <p class="main-text">
              RMM指数利用近赤道平均850 hPa纬向风、200 hPa纬向风和卫星观测到的长波辐射（OLR）数据。通过将这些日观测数据投影到多变量EOFs上，并去除年际变化和年内变化的组件，得到主要在MJO的季节内时间尺度上变化的主成分（PC）时间序列。
            </p>
            <div class="content-image-wrapper">
              <img src="../assets/MJO/RMM_phase.svg" alt="RMM Phase" class="content-image" />
            </div>
          </div>
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // A more appropriate name for the component
  name: "mjo_introduce"
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

/* 2. 子菜单容器 */
.sub-nav-container {
  border-bottom: 1px solid rgba(100, 116, 139, 0.3);
  margin-bottom: 24px;
}

/* 3. 内容区域和章节 */
.content-wrapper {
  line-height: 1.7;
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
.text-center {
  text-align: center;
}

/* 4. 标题样式 */
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
}
.sub-heading {
  color: #58a6ff;
  font-size: 1.1em;
  margin-bottom: 16px;
}

/* 5. 文本样式 */
.main-text {
  display: block;
  font-size: 16px; /* Slightly smaller text for better density */
  color: #e0e7ff;
  margin-bottom: 1em;
}

/* 6. 两列布局 */
.two-column-layout {
  display: flex;
  justify-content: space-between;
  gap: 32px; /* Modern way to create space */
  align-items: flex-start;
}
.column {
  width: 48%;
}

/* 7. 图片样式 */
.image-pair {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}
.content-image-wrapper {
  margin-top: 24px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  background-color: rgba(255, 255, 255, 0.9); /* Add a light background for images that need it */
}
.content-image {
  width: 100%;
  height: auto;
  display: block;
}

/* --- 8. [核心] 覆盖子菜单样式 --- */
::v-deep .el-menu.el-menu-dark-theme {
  background-color: transparent !important;
  border-bottom: none !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item {
  color: #cbd5e1 !important;
  background-color: transparent !important;
  border-bottom-color: transparent !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item:hover {
  background-color: rgba(88, 116, 255, 0.1) !important;
  color: #fff !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active {
  color: #67e8f9 !important;
  border-bottom: 2px solid #67e8f9 !important;
}
</style>