<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="$route.path" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item :index="sectionRoute($route.path, 'introduction')">ENSO介绍</el-menu-item>
        <el-menu-item :index="sectionRoute($route.path, 'results')">预测结果</el-menu-item>
        <el-menu-item :index="sectionRoute($route.path, 'data')">数据</el-menu-item>
        <el-menu-item :index="sectionRoute($route.path, 'resources')">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">

      <!-- Section 1: Related Websites -->
      <section class="content-section">
        <h2 class="content-title">相关网站</h2>
        <ul class="resource-list">
          <li v-for="link in links" :key="link.text">
            <!-- We control the link type via CSS for theme consistency -->
            <el-link :href="link.url" target="_blank">{{ link.text }}</el-link>
          </li>
        </ul>
      </section>

      <!-- Section 2: ENSO Videos -->
      <section class="content-section">
        <h2 class="content-title">ENSO视频介绍</h2>
        <ul class="resource-list">
          <li v-for="link in videos" :key="link.text">
            <el-link :href="link.url" target="_blank">{{ link.text }}</el-link>
          </li>
        </ul>
      </section>

    </div>
  </el-card>
</template>

<script>
import ensoSectionRoute from '@/utils/ensoSectionRoutes'

export default {
  // A more accurate name for the component
  name: "enso_resource",
  methods: { sectionRoute: ensoSectionRoute },
  data() {
    return {
      links: [
        { text: 'NOAA Climate.gov ENSO Page', url: 'https://www.climate.gov/enso' },
        { text: 'NOAA Climate Prediction Center El Nino Page', url: 'https://www.cpc.ncep.noaa.gov/products/precip/CWlink/MJO/enso.shtml' },
        { text: 'NOAA Pacific Marine Environmental Laboratory El Nino Theme Page', url: 'https://www.pmel.noaa.gov/elnino/' },
        { text: 'Western Regional Climate Center ENSO Page', url: 'https://wrcc.dri.edu/Climate/enso.php' }
      ],
      videos: [
        { text: 'Understanding El Niño (NOAA climate.gov)', url: 'https://www.youtube.com/watch?v=_Tuou_QcgxI' },
        { text: 'Understanding La Niña (NOAA climate.gov)', url: 'https://www.youtube.com/watch?v=fAvk4RXrW_E' },
        { text: 'Observing El Niño (NOAA \'Ocean Today\')', url: 'http://oceantoday.noaa.gov/observingelnino/welcome.html' },
        { text: 'Potential Impacts of El Niño in Colorado (by NOAA/PSL and CIRES)', url: 'https://www.youtube.com/embed/8HmKFTEUoFk?rel=0' }
      ],
    };
  }
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

/* 3. 内容区域 */
.content-wrapper {
  line-height: 1.7;
}
.content-section {
  margin-top: 40px;
}
.content-section:first-child {
  margin-top: 0;
}
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
}

/* 4. [核心] 资源列表样式 */
.resource-list {
  list-style: none; /* 移除默认的圆点 */
  padding: 0;
  margin: 0;
}
.resource-list li {
  padding-left: 20px;
  position: relative;
  margin-bottom: 16px;
}
/* 创建一个自定义的“子弹头”图标 */
.resource-list li::before {
  content: '▶'; /* You can use other characters like '■' or SVG icons */
  position: absolute;
  left: 0;
  top: 0;
  color: #58a6ff; /* Use the same blue as titles */
  font-size: 0.8em;
}

/* --- 5. [核心] 覆盖 Element UI Link 的样式 --- */
.resource-list ::v-deep .el-link {
  font-size: 1.05em; /* Make links slightly larger */
}
/* Target the inner span to change the color */
.resource-list ::v-deep .el-link .el-link--inner {
  color: #67e8f9 !important; /* High-contrast cyan */
}
/* Hover effect */
.resource-list ::v-deep .el-link:hover .el-link--inner {
  color: #fff !important;
  text-decoration: underline;
}

/* --- 6. [核心] 覆盖子菜单样式 --- */
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
