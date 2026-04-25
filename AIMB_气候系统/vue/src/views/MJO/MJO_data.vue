<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/mjo_data'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/mjo_introduce">MJO介绍</el-menu-item>
        <el-menu-item index="/usermanage/mjo_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/mjo_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">MJO指数</h2>
        <p class="main-text">
          常用一组时间序列，用于量化当前和历史 MJO 活动。
          <br/><br/>
          <strong class="highlight-text">注意：</strong>ROMI 现在几乎实时提供。OMI 已更新至 2024 年 5 月 20 日。这些 PC 振幅将与以前的版本略有不同，因为用于 20-96 天过滤器的样品更长 （1979-2021），导致用于计算 OMI 的过滤 OLR 值略有不同。在某些情况下，这也可能导致与以前的版本相差（一个相位）。使用的 EOF 仍然是 1979 年至 2012 年的（见下文）。
          <br/><br/>
          <strong class="highlight-text">注意：</strong>当将 OMI 直接与 RMM 进行比较时，为了获得正确的相位，OMI PC1 的符号和 PC 顺序应该颠倒，以便 OMI（PC2） 类似于 RMM（PC1），-OMI（PC1） 类似于 RMM （PC2）。
        </p>

        <h3 class="sub-heading">自 1979 年来的每日 MJO 指数</h3>
        <!-- 4. The Element UI Table with our dark theme class -->
        <el-table :data="tableData" class="dark-theme-table">
          <el-table-column prop="index" label="名称" width="220"></el-table-column>
          <el-table-column prop="description" label="描述"></el-table-column>
          <el-table-column label="获取连接" width="180">
            <template slot-scope="scope">
              <a :href="scope.row.link" target="_blank" class="table-link">{{ scope.row.timeseries }}</a>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </el-card>

</template>

<script>
// --- Script 部分保持不变 ---
export default {
  // A more accurate name for the component
  name: "mjo_data",
  data() {
    return {
      tableData: [
        { index: 'ERA5 OLR MJO 指数', link: 'https://www.psl.noaa.gov/mjo/mjoindex/omi.era5.1x.webpage.4023.txt', description: '20-96天滤波的ERA5 OLR的投影，包括所有东西向波数到30-96天向东滤波OLR的日空间EOF模式上，这些EOF是从ERA5数据集中计算得出的。EOF使用从1940年至今的数据计算。', timeseries: 'ERA5 OMI 值' },
        { index: 'OLR MJO 指数', link: 'https://www.psl.noaa.gov/mjo/mjoindex/omi.1x.txt', description: '20-96天滤波的OLR的投影，包括所有东西向波数到30-96天向东滤波OLR的日空间EOF模式上。', timeseries: 'OMI 值' },
        { index: '实时OLR MJO 指数', link: 'https://www.psl.noa.gov/mjo/mjoindex/romi.cpcolr.1x.txt', description: '9天运行平均OLR异常投影到30-96天向东滤波OLR的日空间EOF模式上。OLR异常是通过首先减去前40天的平均OLR来计算的。随着目标日期的临近，运行平均值会逐渐减小。', timeseries: 'ROMI 值' },
        { index: '滤波OLR MJO 指数', link: 'https://www.psl.noaa.gov/mjo/mjoindex/fmo.1x.txt', description: '15S-15N范围内，按经度平均的20-96天滤波OLR的单变量EOF。整个年度使用相同的空间EOF模式。', timeseries: 'FMO 值' }
      ]
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
}
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 24px;
}
.highlight-text {
  color: #67e8f9;
}

/* 4. 表格内的链接样式 */
.table-link {
  color: #67e8f9;
  text-decoration: none;
  font-weight: bold;
}
.table-link:hover {
  text-decoration: underline;
}

/* --- 5. [核心] 覆盖 Element UI Table 的样式 --- */
::v-deep .el-table.dark-theme-table,
::v-deep .el-table.dark-theme-table th,
::v-deep .el-table.dark-theme-table tr {
  background-color: transparent !important;
}
::v-deep .dark-theme-table thead {
  color: #94a3b8;
}
::v-deep .dark-theme-table th.el-table__cell {
  background-color: rgba(0, 0, 0, 0.2) !important;
}
::v-deep .dark-theme-table .el-table__row {
  color: #cbd5e1;
}
::v-deep .dark-theme-table .el-table__cell {
  border-bottom: 1px solid rgba(100, 116, 139, 0.3) !important;
}
::v-deep .dark-theme-table .el-table__body tr:hover > td.el-table__cell {
  background-color: rgba(88, 116, 255, 0.1) !important;
}
::v-deep .dark-theme-table::before {
  height: 0px;
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