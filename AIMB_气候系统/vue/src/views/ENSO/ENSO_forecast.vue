<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/enso_forecast'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_introduce">ENSO介绍</el-menu-item>
        <el-menu-item index="/usermanage/enso_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/enso_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/enso_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Prediction Methods -->
      <section class="content-section">
        <h2 class="content-title">预测方法</h2>
        <p class="main-text">
          预测厄尔尼诺和拉尼娜事件的能力极为重要，因为这些事件往往与热带地区持续的气候变化有关，甚至会影响世界各地的大气。
          有两种类型的预报：从各种海洋-大气模型获得的预报和从统计模型获得的预报。
          这些模型的技能各不相同，有时甚至在 ENSO 的某些阶段比其他模型做得更好。
          预测员在进行预测时会尝试考虑所有这些因素。
        </p>
      </section>

      <!-- Section 2: MEPM Model -->
      <section class="content-section">
        <h2 class="content-title">MEPM：一种新型多变量ENSO预测模型</h2>
        <p class="main-text">
          本系统使用一种利用多气候要素的ENSO预测模型（Multivariate ENSO Prediction Model，MEPM），
          包括多变量信息提取模块（Multivariate Information Extraction Module，MIEM）和时空融合模块（Spatial-Temporal Fusion Module，STFM），
          以捕获不同气候要素在时空上的相互依赖性。
          本文选取了经度方向风应力异常、纬度方向风应力异常、海表温度异常和海表下150m温度异常四个变量的距平值并进行了大量的实验。
          实验结果表明，MEPM在提前11个月的Niño3.4指数相关技巧上分别比动力模型CanCM4、CCSM3和GFDLaer04高14%、31%和24%。
          此外，MEPM在短中期的Niño3.4指数相关技巧上显著优于其他深度学习模型，并可提供长达17个月的有效预测。
        </p>

        <!-- Two-column layout for charts -->
        <div class="two-column-layout">
          <div class="column text-center">
            <h3 class="sub-heading">Niño3.4指数相关技巧情况</h3>
            <div class="content-image-wrapper">
              <img src="../../assets/ENSO/相关系数.png" alt="相关系数" class="content-image" />
            </div>
          </div>
          <div class="column text-center">
            <h3 class="sub-heading">MEPM在不同时效下的Niño3.4指数3个月滑动平均值情况</h3>
            <div class="content-image-wrapper">
              <img src="../../assets/ENSO/ENSO指数.png" alt="Niño3.4指数" class="content-image" />
            </div>
          </div>
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // It's good practice to name the component after its function
  name: "enso_forecast"
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
  color: #cbd5e1; /* Using a lighter color for subheadings under charts */
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
  margin-bottom: 1em;
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
  /* Add a light background to ensure charts are always visible */
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