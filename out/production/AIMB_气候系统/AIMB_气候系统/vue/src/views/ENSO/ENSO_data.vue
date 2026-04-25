<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/enso_data'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_introduce">ENSO介绍</el-menu-item>
        <el-menu-item index="/usermanage/enso_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/enso_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/enso_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper -->
    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">与 ENSO相关的气候指数</h2>
        <p class="main-text">
          虽然 ENSO 是一个在空间和时间上都变化的过程，但在监控和分析中使用单个时间序列来表示可能很方便。
          其中一些时间序列是热带太平洋特定区域的 SST 平均值，而另一些则使用超过 1 个变量来尝试捕捉 ENSO 中发生的更多动态过程。
          一些指数衡量事件之间的差异。
        </p>

        <!-- 4. The Element UI Table -->
        <el-table :data="tableData" class="dark-theme-table">
          <el-table-column label="Name">
            <template slot-scope="scope">
              <!-- Links are styled via CSS for better visibility -->
              <a :href="scope.row.link" target="_blank" class="table-link">{{ scope.row.name }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="region" label="地区" width="250"></el-table-column>
          <el-table-column prop="dateRange" label="时间范围" width="180"></el-table-column>
        </el-table>
      </section>
    </div>
  </el-card>
</template>

<script>
// --- Script 部分保持不变 ---
export default {
  // A more accurate name for the component
  name: "enso_data",
  data() {
    return {
      tableData: [
        { name: 'Oceanic Niño Index (ONI)', link: 'https://www.psl.noaa.gov/data/correlation/oni.data', region: 'SST 5N-5S, 170W-12W', dateRange: '1950-present' },
        { name: 'Niño 1.2', link: 'https://www.psl.noaa.gov/gcos_wgsp/Timeseries/Data/nino12.long.data', region: 'SST 0-10S, 90W-80W', dateRange: '1870-present' },
        { name: 'Niño 3', link: 'https://www.psl.noaa.gov/gcos_wgsp/Timeseries/Data/nino3.long.data', region: 'SST 5N-5S, 150W-90W', dateRange: '1870-present' },
        { name: 'Niño 3.4', link: 'https://www.psl.noaa.gov/gcos_wgsp/Timeseries/Data/nino34.long.data', region: 'SST 5N-5S, 170W-12W', dateRange: '1870-present' },
        { name: 'Niño 4', link: 'https://www.psl.noaa.gov/gcos_wgsp/Timeseries/Data/nino4.long.anom.data', region: 'SST 5N-5S, 160E-150W', dateRange: '1870-present' },
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
  margin-bottom: 24px;
}
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 24px;
}

/* 4. 表格内的链接样式 */
.table-link {
  color: #67e8f9; /* 高亮青色 */
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

/* 表头样式 */
::v-deep .dark-theme-table thead {
  color: #94a3b8; /* 表头文字颜色 */
}
::v-deep .dark-theme-table th.el-table__cell {
  background-color: rgba(0, 0, 0, 0.2) !important; /* 表头背景色 */
}

/* 表格行和单元格样式 */
::v-deep .dark-theme-table .el-table__row {
  color: #cbd5e1; /* 表格行文字颜色 */
}
::v-deep .dark-theme-table .el-table__cell {
  border-bottom: 1px solid rgba(100, 116, 139, 0.3) !important; /* 单元格下边框 */
}

/* 鼠标悬浮行样式 */
::v-deep .dark-theme-table .el-table__body tr:hover > td.el-table__cell {
  background-color: rgba(88, 116, 255, 0.1) !important;
}

/* 移除表格底部多余的边框 */
::v-deep .dark-theme-table::before {
  height: 0px;
}
</style>