<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/mjo_forecast'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/mjo_introduce">MJO介绍</el-menu-item>
        <el-menu-item index="/usermanage/mjo_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/mjo_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Prediction Methods -->
      <section class="content-section">
        <h2 class="content-title">预测方法</h2>
        <!-- A more semantic structure for the descriptive list -->
        <div class="description-list">
          <div class="description-item">
            <h3>基于统计学的方法：</h3>
            <p>这种方法依赖于历史观测数据和统计模型，通过分析过去的MJO事件与相关气象变量之间的关系，建立经验模型。这些模型通常使用回归分析、时间序列分析等统计工具，以识别并预测MJO的发生及其强度。这类方法的优点是相对简单，计算效率高，但其局限性在于对数据的依赖性，可能无法捕捉复杂的物理过程。</p>
          </div>
          <div class="description-item">
            <h3>基于动力学的方法：</h3>
            <p>这一方法则涉及气候动力学和数值天气预报模型，通过模拟大气和海洋的物理过程，捕捉MJO的形成和演变。这些模型考虑了多种气象因素的相互作用，如海表温度、风场和湿度等，以进行更加精确的预测。尽管这种方法通常计算复杂且需要大量的计算资源，但其能够提供更深入的物理理解和高分辨率的预报结果，适用于长时间尺度的气候研究。</p>
          </div>
        </div>
      </section>

      <!-- Section 2: MISM Model -->
      <section class="content-section">
        <h2 class="content-title">MISM：融合多模态数据与时空特征的MJO预测模型</h2>
        <p class="main-text">
          该模型构建了一个多模态信息融合的预测框架，通过结合气象因子与历史RMM指数，作为模型的输入，共同用于MJO的预测。采用了压缩激励网络、卷积神经网络和自注意力机制，有效提升了MJO的三维多元建模能力，能够捕捉气象因子之间的时空特征及历史RMM指数的时序特征。
          实验结果表明，所提出的MISM模型能够有效预测未来31天的RMM指数，且其预测性能优于当前最先进的动力学预测系统。
        </p>

        <!-- Two-column layout for charts -->
        <div class="two-column-layout">
          <div class="column text-center">
            <h3 class="sub-heading">RMM指数相关技巧情况</h3>
            <div class="content-image-wrapper">
              <img src="../../assets/MJO/BCOR.png" alt="相关系数" class="content-image" />
            </div>
          </div>
          <div class="column text-center">
            <h3 class="sub-heading">RMM指数位相图</h3>
            <div class="content-image-wrapper">
              <img src="../../assets/MJO/Pre_true.svg" alt="位相图" class="content-image" />
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
  name: "mjo_forecast"
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
  margin-bottom: 1em;
}
/* 描述列表的特定样式 */
.description-list .description-item {
  margin-bottom: 1.5em;
}
.description-list h3 {
  color: #94a3b8; /* A slightly more subdued color for these headings */
  font-size: 1em;
  margin-bottom: 0.5em;
}
.description-list p {
  font-size: 16px;
  color: #e0e7ff;
  margin: 0;
  padding-left: 1em;
  border-left: 2px solid #58a6ff;
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