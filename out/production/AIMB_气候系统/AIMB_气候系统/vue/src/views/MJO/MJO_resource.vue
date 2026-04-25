<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/mjo_resource'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/mjo_introduce">MJO介绍</el-menu-item>
        <el-menu-item index="/usermanage/mjo_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/mjo_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">MJO相关教程和监控</h2>
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
  name: "mjo_resource",
  data() {
    return {
      links: [
        {
          text: 'An All-season Real-time Multivariate MJO Index',
          url: 'http://www.bom.gov.au/climate/mjo/',
          description: '澳大利亚的MJO预测和监测图，利用历史数据并根据经验推导的 MJO 结构。'
        },
        {
          text: 'What is the MJO, and why do we care?',
          url: 'https://www.climate.gov/news-features/blogs/enso/what-mjo-and-why-do-we-care',
          description: '来自 Climate.gov ENSO 博客的有关 MJO 的简短教程。'
        },
        {
          text: 'Multi-Scale MJO Research Page',
          url: 'https://tad.ge-at.iastate.edu/multi-scale-mjo-research-page/',
          description: '爱荷华州热带大气动力学小组研究 MJO 以及它如何与波浪和大气/海洋的平均状态相互作用。'
        },
        {
          text: 'Adrian Matthews MJO page',
          url: 'http://envam1.env.uea.ac.uk/mjo.html',
          description: '一个非常完整的页面，其中包含 MJO 描述、当前研究主题、插图和动画，来自东英吉利大学的 Adrian Matthews。'
        },
        {
          text: 'MJO monitoring page',
          url: 'https://www.cpc.ncep.noaa.gov/products/precip/CWlink/MJO/mjo.shtml',
          description: '来自 NOAA/CPC。包括 30-60 天振荡当前状态的各种图，包括 OLR 异常、位势高度和其他动画和卫星照片。还包括一个非常好的常见问题解答，关于振荡机制和演化，以及它与其他气候过程的关系。他们还有一个 MJO 每日指数。'
        }
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
  text-align: left; /* Ensure long links wrap correctly */
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