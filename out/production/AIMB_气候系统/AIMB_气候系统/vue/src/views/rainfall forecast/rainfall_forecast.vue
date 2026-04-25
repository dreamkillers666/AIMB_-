<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/rainfall_forecast'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/seasonal_introduce">次季节降水介绍</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_data">数据</el-menu-item>
        <!-- The "More Resources" link was commented out, so it's omitted here as well -->
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Prediction Method Intro -->
      <section class="content-section">
        <h2 class="content-title">预测方法</h2>
        <p class="main-text">
          研究时段为1983～2022 年夏季。首先分别针对中国夏季月际尺度（6、7、8 月）降水距平百分率，
          在选取具有明确物理机制预测因子的基础上，基于年际增量的场信息耦合型降尺度预测方法，
          构建中国夏季不同月份降水单因子预测模型，并进行误差订正，提高单因子模型预测准确率。之后，采用多因子择优集合，进而构建动力和统计结合预测模型并检验。
        </p>
      </section>

      <!-- Section 2: Interannual Increment Method -->
      <section class="content-section">
        <h2 class="content-title">年际增量预测方法</h2>
        <p class="main-text">
          所建立模型的预测量为降水距平百分率的年际增量，其定义为当年的距平百分率与前一年值之差。
          基于降水量的年际增量建立的中国夏季降水预测模型在中国汛期业务预测显示较好预测效能（Dai and Fan, 2021），
          但考虑到通过降水量的年际增量预测值回算降水距平百分率会产生误差，而降水距平百分率的年际增量与降水量的年际增量具有一致的年际变化，
          因此，采用降水距平百分率的年际增量作为预测量建立预测模型。其中， Δ(yPA)i表示第i年降水距平百分率的年际增量， Δyi表示第i年降水量的年际增量，y¯表示降水量多年平均值。
        </p>
      </section>

      <!-- Section 3: Downscaling Method -->
      <section class="content-section">
        <h2 class="content-title">场信息耦合型降尺度预测方法</h2>
        <p class="main-text">
          场信息耦合型降尺度预测方法主要分为以下三个步骤：（1）利用经验正交函数（EOF）分解保留预测因子和预测量前90%解释方差的模态进行场重建以达到滤波的目的；
          （2）利用奇异值分解（SVD）方法提取预测因子和预测量空间场的最优耦合变化型；（3）针对提取的最优耦合变化型所对应的时间系数，利用线性回归方法建立降尺度预测模型（Chu et al., 2008; 刘颖等, 2013）。
          这样的降尺度预测方法可以获得大尺度环流系统与局地气象要素间稳定的统计关系，使建立的降尺度预测模型更稳定。
        </p>

        <!-- Two-column layout for charts -->
        <div class="two-column-layout">
          <div class="column text-center">
            <h3 class="sub-heading">基于单个因子建立的降尺度预测模型</h3>
            <div class="content-image-wrapper">
              <img src="../../assets/降水预测/rainfall4.png" alt="降尺度预测模型" class="content-image" />
            </div>
          </div>
          <div class="column text-center">
            <h3 class="sub-heading">夏季平均的中国降水距平百分率的空间分布</h3>
            <div class="content-image-wrapper">
              <img src="../../assets/降水预测/rainfall5.png" alt="降水距平百分率空间分布" class="content-image" />
            </div>
          </div>
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // Corrected component name
  name: "rainfall_forecast"
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
.text-center {
  text-align: center;
}

/* 4. 标题样式 */
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
  line-height: 1.4;
}
.sub-heading {
  color: #cbd5e1;
  font-size: 0.9em;
  font-weight: normal;
  margin-bottom: 16px;
  line-height: 1.4;
}

/* 5. 文本样式 */
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 1.5em;
}

/* 6. 两列布局 */
.two-column-layout {
  display: flex;
  justify-content: space-between;
  gap: 32px;
  align-items: flex-start;
  margin-top: 32px;
}
.column {
  width: 48%;
}

/* 7. 图片样式 */
.content-image-wrapper {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  background-color: rgba(255, 255, 255, 0.95);
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