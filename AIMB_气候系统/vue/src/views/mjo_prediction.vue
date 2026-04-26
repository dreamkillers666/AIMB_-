<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/mjo_pre'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_pre">MEPM预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_pre">MISM预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_pre">STRA-Net预测结果</el-menu-item>
        <el-menu-item index="/usermanage/convlstm_pre">ConvLSTM预测结果</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Interactive Prediction Interface -->
      <section class="content-section">
        <h2 class="content-title">MISM模型预测结果</h2>

        <!-- Input Grid for meteorological factors -->
        <h3 class="panel-title">气象因子 (Meteorological Factors)</h3>
        <div class="factor-grid">
          <div v-for="(dropdown, index) in dropdowns" :key="index" class="factor-item">
            <select @change="changeImage(index, $event)" class="styled-select">
              <option value="one">OLR异常图</option>
              <option value="two">U200异常图</option>
              <option value="three">U850异常图</option>
            </select>
            <div class="image-display small">
              <img :src="selectedImages[index]" alt="Meteorological Factor" />
            </div>
          </div>
          <!-- Add Button -->
          <div class="add-button-container">
            <el-button @click="addDropdown" icon="el-icon-plus" class="add-button" circle></el-button>
          </div>
        </div>

        <!-- Prediction Core Interface -->
        <div class="prediction-interface">
          <!-- Input Panel (History) -->
          <div class="interface-panel input-panel">
            <h3 class="panel-title">历史RMM指数 (Historical RMM)</h3>
            <div class="image-display">
              <img :src="selectedIndex" alt="Historical RMM" />
            </div>
          </div>

          <!-- Action Panel -->
          <div class="interface-panel action-panel">
            <el-button v-if="buttonState === 'start'" @click="startPrediction" class="predict-button start">开始预测</el-button>
            <el-button v-if="buttonState === 'loading'" :loading="true" class="predict-button loading" disabled>加载中</el-button>
            <el-button v-if="buttonState === 'success'" type="success" icon="el-icon-check" class="predict-button success" disabled>预测成功</el-button>
          </div>

          <!-- Output Panel -->
          <div class="interface-panel output-panel">
            <h3 class="panel-title">未来RMM指数 (Future RMM)</h3>
            <div class="image-display">
              <img v-if="buttonState === 'success'" :src="predictionImageSrc" alt="Prediction Result" />
              <div v-else class="placeholder">
                <span class="placeholder-text">暂无预测结果</span>
              </div>
            </div>
            <el-button @click="resetPrediction" icon="el-icon-refresh" class="reset-button" circle></el-button>
          </div>
        </div>
      </section>

      <!-- Section 2: Results Table and Chart -->
      <section class="content-section">
        <div class="two-column-layout">
          <div class="column">
            <h2 class="content-title">不同模型相关技巧对比</h2>
            <el-table :data="tableData" :span-method="objectSpanMethod" class="dark-theme-table" border>
              <el-table-column prop="id" label="模型类型" width="150"></el-table-column>
              <el-table-column prop="name" label="模型名称"></el-table-column>
              <el-table-column prop="amount1" label="预测技巧（天）"></el-table-column>
            </el-table>
          </div>
          <div class="column">
            <h2 class="content-title">消融实验结果</h2>
            <div id="cor" class="chart-container"></div>
          </div>
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
// Your existing script is largely fine. We'll keep it.
// Ensure the ECharts `init` theme is set to 'dark'.
export default {
  name: "mjo_pre", // Corrected name
  data() {
    return {
      dropdowns: [{ value: "one" }],
      selectedImages: [require("../assets/MISM/OLR_anomalies.gif")],
      selectedIndex: require("../assets/MISM/input.svg"),
      buttonState: 'start',
      predictionImageSrc: '',
      tableData: [
        { id: '动力学系统', name: 'HWCR', amount1: '8' },
        { id: '动力学系统', name: 'BOM', amount1: '24' },
        { id: '动力学系统', name: 'CNRM', amount1: '25' },
        { id: '动力学系统', name: 'ECMWF', amount1: '30' },
        { id: '深度学习模型', name: 'FNN', amount1: '17' },
        { id: '深度学习模型', name: 'CNN', amount1: '22' },
        { id: '深度学习模型', name: 'AR-RNN', amount1: '25' },
        { id: '深度学习模型', name: 'MISM', amount1: '31' },
      ],
    };
  },
  methods: {
    addDropdown() {
      this.dropdowns.push({ value: "one" });
      this.selectedImages.push(require("../assets/MISM/OLR_anomalies.gif"));
    },
    changeImage(index, event) {
      const selectedValue = event.target.value;
      this.$set(this.dropdowns[index], 'value', selectedValue);
      if (selectedValue === "one") {
        this.$set(this.selectedImages, index, require("../assets/MISM/OLR_anomalies.gif"));
      } else if (selectedValue === "two") {
        this.$set(this.selectedImages, index, require("../assets/MISM/U200_anomalies.gif"));
      } else if (selectedValue === "three") {
        this.$set(this.selectedImages, index, require("../assets/MISM/U850_anomalies.gif"));
      }
    },
    startPrediction() {
      this.buttonState = 'loading';
      setTimeout(() => {
        this.buttonState = 'success';
        this.predictionImageSrc = require('@/assets/MISM/true_pred.svg');
      }, 5000);
    },
    resetPrediction() {
      this.buttonState = 'start';
      this.predictionImageSrc = '';
    },
    objectSpanMethod({ row, columnIndex }) {
      if (columnIndex === 0) {
        if (row.id === '动力学系统' && this.tableData.findIndex(item => item.id === '动力学系统') === this.tableData.indexOf(row)) {
          return { rowspan: 4, colspan: 1 };
        } else if (row.id === '深度学习模型' && this.tableData.findIndex(item => item.id === '深度学习模型') === this.tableData.indexOf(row)) {
          return { rowspan: 4, colspan: 1 };
        } else {
          return { rowspan: 0, colspan: 0 };
        }
      }
    },
    initChart() {
      let corDom = document.getElementById('cor');
      if (corDom) {
        let corChart = this.$echarts.init(corDom, 'dark');
        let option = {
          tooltip: { trigger: 'axis' },
          legend: { data: ['MISM', '气象因子', '历史指数'], top: 30 },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          toolbox: { feature: { saveAsImage: {} } },
          xAxis: { type: 'category', boundaryGap: false, data: Array.from({length: 45}, (_, i) => i + 1) },
          yAxis: { type: 'value' },
          series: [
            { name: 'MISM', type: 'line', data: [0.98,0.94,0.90,0.85,0.81,0.77,0.74,0.73,0.72,0.72,0.73,0.74,0.74,0.75,0.75,0.75,0.74,0.73,0.71,0.70,0.68,0.65,0.62,0.60,0.58,0.55,0.54,0.53,0.51,0.51,0.50,0.49,0.49,0.48,0.48,0.47,0.46,0.45,0.44,0.42,0.41,0.39,0.37,0.35,0.33] },
            { name: '气象因子', type: 'line', data: [0.72,0.74,0.75,0.76,0.77,0.77,0.77,0.78,0.77,0.76,0.75,0.73,0.72,0.71,0.70,0.68,0.66,0.65,0.63,0.62,0.61,0.60,0.59,0.57,0.55,0.53,0.52,0.50,0.48,0.46,0.44,0.42,0.40,0.38,0.36,0.34,0.32,0.31,0.30,0.28,0.27,0.26,0.24,0.23,0.22] },
            { name: '历史指数', type: 'line', data: [0.97,0.94,0.89,0.83,0.77,0.71,0.65,0.59,0.53,0.47,0.43,0.39,0.36,0.33,0.31,0.29,0.28,0.27,0.26,0.26,0.26,0.25,0.24,0.24,0.24,0.23,0.22,0.21,0.20,0.18,0.17,0.16,0.14,0.13,0.12,0.11,0.11,0.11,0.10,0.10,0.11,0.11,0.12,0.12,0.13] }
          ]
        };
        corChart.setOption(option);
      }
    }
  },
  mounted() {
    this.initChart();
  }
}
</script>

<style scoped>
/* --- 统一的深色主题样式 --- */
.dark-theme-card {
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(100, 116, 139, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 16px;
}
::v-deep .el-card__body { padding: 24px 32px; }
.sub-nav-container { border-bottom: 1px solid rgba(100, 116, 139, 0.3); margin-bottom: 24px; }
.content-wrapper { line-height: 1.7; }
.content-section { margin-top: 40px; padding-top: 40px; border-top: 1px solid rgba(100, 116, 139, 0.3); }
.content-section:first-child { margin-top: 0; padding-top: 0; border-top: none; }
.content-title { color: #58a6ff; margin-bottom: 24px; text-align: center; }
.panel-title { color: #94a3b8; font-size: 1em; font-weight: bold; text-align: left; margin-bottom: 12px; }

/* 气象因子输入网格 */
.factor-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: flex-start;
  padding: 24px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  background-color: rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}
.factor-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: calc(33.33% - 16px);
}
.add-button-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  min-height: 150px;
}
::v-deep .el-button.add-button { font-size: 1.5em; background-color: transparent !important; color: #58a6ff !important; border-color: #58a6ff !important; }

/* 预测核心界面 */
.prediction-interface { display: flex; gap: 24px; align-items: center; justify-content: center; padding: 24px; border: 1px solid rgba(100, 116, 139, 0.3); border-radius: 12px; background-color: rgba(0, 0, 0, 0.1); }
.interface-panel { display: flex; flex-direction: column; gap: 12px; }
.input-panel, .output-panel { flex: 1; max-width: 400px; }
.output-panel { position: relative; }
.image-display { width: 100%; border-radius: 8px; border: 1px solid rgba(100, 116, 139, 0.2); overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: rgba(0, 0, 0, 0.2); }
.image-display.small { aspect-ratio: 4 / 3; }
.image-display img { width: 100%; height: 100%; object-fit: cover; }
.placeholder { width: 100%; height: 100%; min-height: 200px; display: flex; align-items: center; justify-content: center; }
.placeholder-text { font-size: 2em; color: rgba(148, 163, 184, 0.2); font-weight: bold; }

/* 按钮和下拉框样式 */
.predict-button { width: 120px; font-weight: bold; }
.reset-button { position: absolute; top: 0; right: 0; }
.styled-select { background-color: rgba(0, 0, 0, 0.2); color: #e0e7ff; border: 1px solid rgba(100, 116, 139, 0.5); padding: 8px 12px; border-radius: 6px; font-size: 1em; width: 100%; }
::v-deep .el-button.start { background-color: transparent !important; color: #58a6ff !important; border-color: #58a6ff !important; }
::v-deep .el-button.success { border-color: #67c23a !important; }
::v-deep .el-button.loading { background-color: rgba(255, 255, 255, 0.1) !important; border-color: #94a3b8 !important; }
::v-deep .el-button.reset-button { background-color: transparent !important; color: #e6a23c !important; border-color: #e6a23c !important; font-size: 1.2em; }

/* 表格和图表 */
.two-column-layout { display: flex; gap: 24px; align-items: flex-start; }
.column { flex: 1; }
.chart-container { width: 100%; height: 400px; border: 1px solid rgba(100, 116, 139, 0.3); border-radius: 12px; padding: 16px; background-color: rgba(0, 0, 0, 0.1); }
::v-deep .el-table.dark-theme-table, ::v-deep .el-table.dark-theme-table th, ::v-deep .el-table.dark-theme-table tr { background-color: transparent !important; }
::v-deep .dark-theme-table thead { color: #94a3b8; }
::v-deep .dark-theme-table th.el-table__cell { background-color: rgba(0, 0, 0, 0.2) !important; }
::v-deep .dark-theme-table .el-table__row { color: #cbd5e1; }
::v-deep .dark-theme-table .el-table__cell { border-bottom: 1px solid rgba(100, 116, 139, 0.3) !important; }
::v-deep .dark-theme-table .el-table__body tr:hover > td.el-table__cell { background-color: rgba(88, 116, 255, 0.1) !important; }
::v-deep .dark-theme-table::before { height: 0px; }

/* 子菜单样式 */
::v-deep .el-menu.el-menu-dark-theme, ::v-deep .el-menu.el-menu-dark-theme .el-menu-item, ::v-deep .el-menu.el-menu-dark-theme .el-submenu__title { background-color: transparent !important; border-bottom-color: transparent !important; color: #cbd5e1 !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item:hover, ::v-deep .el-menu.el-menu-dark-theme .el-submenu__title:hover { background-color: rgba(88, 116, 255, 0.1) !important; color: #fff !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active { color: #67e8f9 !important; border-bottom: 2px solid #67e8f9 !important; }
</style>