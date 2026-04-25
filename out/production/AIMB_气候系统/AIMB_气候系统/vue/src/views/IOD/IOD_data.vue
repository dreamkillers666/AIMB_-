<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/iod_data'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/iod_introduce">IOD介绍</el-menu-item>
        <el-menu-item index="/usermanage/iod_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/iod_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">IOD指数</h2>
        <p class="main-text">
          IOD指数，全称为印度洋偶极子指数（Indian Ocean Dipole Index），是衡量印度洋偶极子强度的一个指标。它定义为热带西印度洋（50°~70°E, 10°S~10°N）与热带东南印度洋（90°~110°E, 10°S~0°）区域平均的海表面温度（SST）异常之差。正IOD事件（或正位相IOD）指的是热带西印度洋异常暖和热带东南印度洋异常冷的偶极型模态，而相反的模态称为负IOD事件（或负位相IOD）。
        </p>

        <h3 class="sub-heading">自 1993 年来的IOD 指数</h3>
        <div class="content-image-wrapper">
          <img src="../../assets/IOD/IOD指数统计图.png" alt="IOD指数统计图" class="content-image" />
        </div>
      </section>
    </div>
  </el-card>

</template>

<script>
// --- Script 部分保持不变 ---
export default {
  // A more accurate name for the component
  name: "iod_data",
  data() {
    return {
      // No data needed for this static page
    };
  }
};
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

/* 3. 内容区域 */
.content-wrapper {
  line-height: 1.7;
}
.content-section {
  text-align: justify;
}
.content-title {
  color: #58a6ff;
  margin-bottom: 16px;
}
.sub-heading {
  color: #94a3b8;
  font-size: 1.1em;
  margin-bottom: 16px;
  margin-top: 32px;
  text-align: center;
}
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 24px;
}

/* 4. 图片样式 */
.content-image-wrapper {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  /* Add a light background to ensure the chart is always visible */
  background-color: rgba(255, 255, 255, 0.95);
}
.content-image {
  width: 100%;
  height: auto;
  display: block;
}


/* --- 5. [核心] 覆盖子菜单样式 --- */
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