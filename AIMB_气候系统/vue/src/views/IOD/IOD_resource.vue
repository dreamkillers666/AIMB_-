<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/iod_resource'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/iod_introduce">IOD介绍</el-menu-item>
        <el-menu-item index="/usermanage/iod_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/iod_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">IOD相关资料</h2>
        <ul class="resource-list-detailed">
          <li v-for="link in links" :key="link.text">
            <el-link :href="link.url" target="_blank">{{ link.text }}</el-link>
            <p class="description">{{ link.description }}</p>
          </li>
        </ul>
      </section>
    </div>
  </el-card>
</template>

<script>
// --- Script 部分保持不变 ---
export default {
  // A more appropriate name for the component
  name: "iod_resource",
  data() {
    return {
      links: [
        {
          text: 'Ocean Surface Topography from Space',
          url: 'https://sealevel.jpl.nasa.gov/overlay-iod/',
          description: 'NASA对IOD的监测。'
        },
        {
          text: 'Meet ENSO’s neighbor, the Indian Ocean Dipole',
          url: 'https://www.climate.gov/news-features/blogs/enso/meet-enso%E2%80%99s-neighbor-indian-ocean-dipole',
          description: '一篇主要关于监测和预测IOD及其区域和全球影响的博客。'
        },
        {
          text: 'Indian Ocean Dipole: What is it and why is it linked to floods and bushfires',
          url: 'https://www.bbc.com/news/science-environment-50602971',
          description: '印度洋偶极子与洪水和丛林大火的关系。'
        },
        {
          text: 'SINTEX-F:Indian Ocean Dipole',
          url: 'https://www.jamstec.go.jp/aplinfo/sintexf/e/iod/about_iod.html',
          description: 'SINTEX-F对印度洋偶极子的预测。'
        },
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
  margin-top: 0;
}

.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
}

/* 4. [核心] 带描述的资源列表样式 */
.resource-list-detailed {
  list-style: none;
  padding: 0;
  margin: 0;
}

.resource-list-detailed li {
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(100, 116, 139, 0.3);
}

.resource-list-detailed li:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

/* 5. 描述文本样式 */
.description {
  color: #94a3b8; /* Subdued color for description text */
  font-size: 15px;
  margin-top: 8px;
  line-height: 1.6;
}

/* --- 6. [核心] 覆盖 Element UI Link 的样式 --- */
.resource-list-detailed ::v-deep .el-link {
  font-size: 1.1em;
  font-weight: bold;
}

.resource-list-detailed ::v-deep .el-link .el-link--inner {
  color: #67e8f9 !important; /* High-contrast cyan */
  text-align: left;
  line-height: 1.4;
}

.resource-list-detailed ::v-deep .el-link:hover .el-link--inner {
  color: #fff !important;
  text-decoration: underline;
}

/* --- 7. [核心] 覆盖子菜单样式 --- */
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