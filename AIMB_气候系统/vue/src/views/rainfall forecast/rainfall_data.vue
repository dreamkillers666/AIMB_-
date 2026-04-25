<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/rainfall_data'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/seasonal_introduce">次季节降水介绍</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_data">数据</el-menu-item>
        <!-- The "More Resources" link was commented out, so it's omitted here as well -->
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">降水数据</h2>
        <p class="main-text">
          所使用的中国夏季（6、7、8 月）降水观测资料来源于中国气象局国家气候中心公开并能实时更新的160 站月平均降水数据集。
          月平均海表面温度数据来源于美国国家海洋大气局（NOAA）气候诊断中心提供的海温扩展重建资料，水平分辨率为2.0°×2.0°（Huang et al., 2017）。
          海冰密集度（SIC）数据来自NOAA 发布的最优内插海表海冰数据集，其分辨率为1.0°×1.0°。
          CFSv2 预测的月平均海表面温度资料分辨率为1.0°×1.0°的经纬度格点。CFSv2 月平均数据自1982 年更新至今，其中1982～2010 年为回报试验结果，2011 年之后的数据为实时预测结果。考 虑 每 年3 月 初 需 要 提 交6～8 月实时汛期预测结果，
          选取CFSv2 2 月起报的预测结果和2 月及之前的观测数据。
        </p>

        <!-- 4. The Element UI Table with our dark theme class -->
        <el-table :data="tableData" class="dark-theme-table">
          <el-table-column label="Name">
            <template slot-scope="scope">
              <a :href="scope.row.link" target="_blank" class="table-link">{{ scope.row.name }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="dateRange" label="时间范围" width="200"></el-table-column>
        </el-table>
      </section>
    </div>
  </el-card>

</template>

<script>
export default {
  // Corrected component name
  name: "rainfall_data",
  data() {
    return {
      tableData: [
        { name: 'South Sierras 6-station Precipitation Index', link: 'https://psl.noaa.gov/gcos_wgsp/Timeseries/Data/precip.6station.data', dateRange: '1923 to 2017' },
        { name: 'Puget Sound Lowland Precipitation', link: 'http://research.jisao.washington.edu/data_sets/climate_division/puget_sound.html#data', dateRange: '1931-2009' },
        { name: 'Piura Perú (5S) rainfall', link: 'http://research.jisao.washington.edu/data_sets/piura/', dateRange: '1932-1997' },
        { name: 'Gulf of Guinea standardized rainfall index', link: 'https://www.psl.noaa.gov/gcos_wgsp/Timeseries/Data/nino4.long.anom.data', dateRange: '1899-2001' }
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
  line-height: 1.8;
}
.content-section {
  text-align: justify;
}
.content-title {
  color: #58a6ff;
  margin-bottom: 16px;
}
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 24px;
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