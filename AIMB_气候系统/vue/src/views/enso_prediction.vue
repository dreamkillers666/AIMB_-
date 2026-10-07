<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div v-if="!embedded" class="sub-nav-container">
      <el-menu :default-active="'/usermanage/enso_pre'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_pre">LSTA-Swin预测结果</el-menu-item>
        <el-menu-item index="/usermanage/convlstm_pre">ConvLSTM预测结果</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Interactive Prediction Interface -->
      <section class="content-section">
        <h2 class="content-title">LSTA-Swin模型预测结果</h2>
        <div class="prediction-interface">
          <!-- Input Panel -->
          <div class="interface-panel input-panel">
            <h3 class="panel-title">Input [2022-06 : 2023-03]</h3>
            <div class="image-display">
              <img :src="inputImage" alt="Input Data" />
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
            <h3 class="panel-title">Output [2023-04 : 2024-11]</h3>
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

      <!-- Section 2: ECharts Visualizations -->
      <section class="content-section">
        <h2 class="content-title">预测值与真实值对比</h2>
        <div id="fur" class="chart-container"></div>

        <h2 class="content-title">相关技巧对比</h2>
        <div id="pre" class="chart-container"></div>
      </section>

      <!-- Section 3: Image Gallery -->
      <section class="content-section">
        <h2 class="content-title">2024海温预测图</h2>
        <div class="gallery-controls">
          <label for="imageSelector">Date:</label>
          <select id="imageSelector" @change="changeImage" class="styled-select">
            <option value="one">2023-04 - 2023-07</option>
            <option value="two">2023-08 - 2023-11</option>
          </select>
        </div>
        <div class="image-grid">
          <div v-for="(image, index) in selectedImage" :key="index" class="grid-item">
            <img :src="image" alt="Forecast map" />
          </div>
        </div>
        <img src="../assets/zhang/colorbar.png" alt="Color Bar" class="color-bar">
      </section>

    </div>
  </el-card>
</template>

<script>
// Your existing script is largely fine. We'll keep it as is.
// NOTE: Make sure the ECharts `init` theme is set to 'dark'.
export default {
  name: "predictionPage",
  props: {
    embedded: { type: Boolean, default: false }
  },
  data() {
    return {
      selectedImage: [
        require("../assets/zhang/2023-04.png"),
        require("../assets/zhang/2023-05.png"),
        require("../assets/zhang/2023-06.png"),
        require("../assets/zhang/2023-07.png"),
      ],
      inputImage: require("../assets/Model_pre/input_10_res.gif"),
      buttonState: 'start',
      predictionImageSrc: '',
    }
  },
  methods: {
    changeImage(event) {
      const selectedValue = event.target.value;
      if (selectedValue === "one") {
        this.selectedImage = [
          require("../assets/zhang/2023-04.png"),
          require("../assets/zhang/2023-05.png"),
          require("../assets/zhang/2023-06.png"),
          require("../assets/zhang/2023-07.png"),
        ];
      } else if (selectedValue === "two") {
        this.selectedImage = [
          require("../assets/zhang/2023-08.png"),
          require("../assets/zhang/2023-09.png"),
          require("../assets/zhang/2023-10.png"),
          require("../assets/zhang/2023-11.png"),
        ];
      }
    },
    startPrediction() {
      this.buttonState = 'loading';
      setTimeout(() => {
        this.buttonState = 'success';
        this.predictionImageSrc = require('@/assets/Model_pre/input_20_res.gif');
      }, 5000);
    },
    resetPrediction() {
      this.buttonState = 'start';
      this.predictionImageSrc = '';
    },
    initCharts() {
      // --- ECharts for 'pre' ---
      let preChartDom = document.getElementById('pre');
      let preChart = this.$echarts.init(preChartDom, 'dark'); // Using 'dark' theme
      let preOption = {
        // ... (Your ECharts option for 'pre')
        title: { left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['SINTEX_F', 'CNN', 'ours', 'Transformer', 'GRU','STANet','CanCM4','CCSM3','GFDLaer04'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        toolbox: { feature: { saveAsImage: {} } },
        xAxis: { type: 'category', boundaryGap: false, data: ['1', '2', '3', '4', '5', '6', '7','8', '9', '10','11', '12', '13', '14', '15', '16', '17','18', '19', '20'] },
        yAxis: { type: 'value' },
        series: [
          { name: 'SINTEX_F', type: 'line', data: [] },
          { name: 'CNN', type: 'line', data: [] },
          { name: 'ours', type: 'line', data: [] },
          { name: 'Transformer', type: 'line', data: [] },
          { name: 'GRU', type: 'line', data: [] },
          { name: 'STANet', type: 'line', data: [] },
          { name: 'CanCM4', type: 'line', data: [0.92,0.91,0.88,0.83,0.80,0.75,0.70,0.67,0.65,0.63,0.61] },
          { name: 'CCSM3', type: 'line', data: [0.88,0.85,0.80,0.75,0.70,0.64,0.60,0.57,0.55,0.48,0.40] },
          { name: 'GFDLaer04', type: 'line', data: [0.90,0.89,0.84,0.80,0.75,0.70,0.68,0.65,0.60,0.52,0.48] }
        ]
      };
      const presetCurves = {
        SINTEX_F: [0.89,0.87,0.83,0.80,0.75,0.72,0.70,0.65,0.63,0.60,0.55,0.51,0.48,0.47,0.46,0.45,0.40,0.35,0.32,0.31],
        CNN: [0.93,0.91,0.88,0.83,0.80,0.75,0.71,0.71,0.70,0.69,0.65,0.64,0.63,0.60,0.58,0.53,0.51,0.45,0.41,0.38],
        Our_model: [0.94,0.90,0.86,0.84,0.83,0.79,0.77,0.75,0.75,0.71,0.66,0.65,0.63,0.64,0.63,0.62,0.60,0.52,0.48,0.49],
        Transformer: [0.98,0.94,0.89,0.85,0.81,0.76,0.75,0.74,0.72,0.70,0.68,0.66,0.65,0.62,0.59,0.55,0.53,0.49,0.44,0.41],
        GRU: [0.93,0.91,0.89,0.86,0.81,0.78,0.73,0.68,0.65,0.61,0.57,0.51,0.50,0.48,0.44,0.41,0.40,0.38,0.37,0.32],
        STANet: [0.94,0.91,0.90,0.88,0.85,0.80,0.78,0.72,0.69,0.64,0.61,0.57,0.53,0.51,0.49,0.48,0.45,0.43,0.41,0.40]
      };
      Object.keys(presetCurves).forEach((name, index) => {
        preOption.series[index].data = presetCurves[name];
      });
      preChart.setOption(preOption);


      // --- ECharts for 'fur' ---
      let furChartDom = document.getElementById('fur');
      let furChart = this.$echarts.init(furChartDom, 'dark'); // Using 'dark' theme
      let furOption = {
        // ... (Your ECharts option for 'fur')
        tooltip: { trigger: 'axis' },
        legend: { data: ['True', 'Pre'] },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        toolbox: { feature: { saveAsImage: {} } },
        xAxis: { type: 'category', boundaryGap: false, data: ['2023-04', '2023-05', '2023-06', '2023-07', '2023-08', '2023-09', '2023-10','2023-11', '2023-12', '2024-01', '2024-02', '2024-03', '2024-04', '2024-05', '2024-06', '2024-07', '2024-08','2024-09', '2024-10', '2024-11'] },
        yAxis: { type: 'value' },
        series: [
          { name: 'True', type: 'line', data: [ 0.008, 0.458, 0.833, 0.980, 1.414, 1.445, 1.464, 1.662, 1.864, 1.858] },
          { name: 'Pre', type: 'line', data: [-0.136, 0.163, 0.458, 0.872, 1.192, 1.317, 1.577, 1.496, 1.565, 1.318, 1.138, 0.987, 0.721, 0.489, 0.327, 0.205, 0.093, 0.119, 0.301, 0.280] }
        ]
      };
      furChart.setOption(furOption);
    }
  },
  mounted() {
    this.initCharts();
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

/* 3. 内容区域和章节 */
.content-wrapper {
  line-height: 1.7;
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
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
  text-align: center;
}

/* 4. 预测交互界面 */
.prediction-interface {
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: center;
  padding: 24px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  background-color: rgba(0, 0, 0, 0.1);
}
.interface-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.input-panel, .output-panel {
  flex: 1;
  max-width: 400px; /* Control max width of panels */
}
.output-panel {
  position: relative;
}
.panel-title {
  color: #94a3b8;
  font-size: 1em;
  font-weight: bold;
  text-align: left;
}
.image-display {
  width: 100%;
  aspect-ratio: 1 / 1; /* Make it square */
  border-radius: 8px;
  border: 1px solid rgba(100, 116, 139, 0.2);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.2);
}
.image-display img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-text {
  font-size: 2em;
  color: rgba(148, 163, 184, 0.2);
  font-weight: bold;
}

/* 按钮样式 */
.predict-button {
  width: 120px;
  font-weight: bold;
}
.reset-button {
  position: absolute;
  top: 0;
  right: 0;
}
::v-deep .el-button.start {
  background-color: transparent !important;
  color: #58a6ff !important;
  border-color: #58a6ff !important;
}
::v-deep .el-button.success {
  border-color: #67c23a !important;
}
::v-deep .el-button.loading {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-color: #94a3b8 !important;
}
::v-deep .el-button.reset-button {
  background-color: transparent !important;
  color: #e6a23c !important;
  border-color: #e6a23c !important;
  font-size: 1.2em;
}

/* ECharts 容器 */
.chart-container {
  width: 100%;
  height: 400px; /* Define a fixed height */
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  padding: 16px;
  background-color: rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

/* 图片画廊 */
.gallery-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #94a3b8;
}
.styled-select {
  background-color: rgba(0, 0, 0, 0.2);
  color: #e0e7ff;
  border: 1px solid rgba(100, 116, 139, 0.5);
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 1em;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  padding: 16px;
  background-color: rgba(0, 0, 0, 0.1);
}
.grid-item {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
}
.grid-item img {
  width: 100%;
  display: block;
}
.color-bar {
  width: 100%;
  max-width: 100%;
  height: auto;
  margin-top: 16px;
}

/* --- [核心] 覆盖子菜单样式 --- */
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
