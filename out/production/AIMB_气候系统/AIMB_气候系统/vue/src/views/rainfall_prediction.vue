<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">
    <!-- Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/rainfall_pre'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_pre">MEPM预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_pre">MISM预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_pre">STRA-Net预测结果</el-menu-item>
      </el-menu>
    </div>

    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">STRA-Net模型预测结果</h2>

        <!-- Main container with vertical layout -->
        <div class="prediction-container-vertical">

          <!-- 1. Top Input Area (Horizontal) -->
          <div class="input-area-horizontal">
            <h3 class="area-title">输入气象因子</h3>
            <div class="prediction-grid-horizontal">
              <!-- Dynamically generated factor cards (smaller) -->
              <div v-for="(factor, index) in factors" :key="index" class="prediction-card small">
                <select v-model="factor.month" @change="changeImage(index)" class="styled-select">
                  <option value="one">6月气象因子</option>
                  <option value="two">7月气象因子</option>
                  <option value="three">8月气象因子</option>
                </select>
                <div class="image-display">
                  <img :src="factor.imageSrc" alt="Meteorological Factor" />
                </div>
              </div>
              <!-- Add button card (smaller) -->
              <div class="add-card small" @click="addFactor">
                <i class="el-icon-plus"></i>
                <span>添加因子</span>
              </div>
            </div>
          </div>

          <!-- 2. Bottom Output and Action Area -->
          <div class="output-area-full-width">
            <h3 class="area-title">综合预测结果</h3>
            <!-- Global prediction button -->
            <el-button
                class="predict-button global"
                :class="predictionState"
                :loading="predictionState === 'loading'"
                @click="startGlobalPrediction"
                round
            >
              {{ getButtonText() }}
            </el-button>
            <div class="result-display large">
              <img v-if="predictionState === 'success'" :src="predictionImageSrc" alt="Prediction Result" />
              <div v-else class="placeholder">
                <span class="placeholder-text">暂无预测结果</span>
              </div>
            </div>
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
              <el-table-column prop="amount1" label="MASE"></el-table-column>
              <el-table-column prop="amount2" label="MAE"></el-table-column>
            </el-table>
          </div>
          <div class="column">
            <h2 class="content-title">预测值与真实值对比结果</h2>
            <div id="cor" class="chart-container"></div>
          </div>
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  name: "rainfall_pre",
  data() {
    return {
      // Manages all input factor cards
      factors: [
        { month: 'one', imageSrc: require('../assets/STRA-Net/june.png') },
      ],
      // Global state for the single prediction result
      predictionState: 'start', // Can be 'start', 'loading', 'success'
      predictionImageSrc: '',
      // Table data remains the same
      tableData: [
        { id: '传统方法', name: 'SVM', amount1: '0.646', amount2: '44.008' },
        { id: '传统方法', name: 'RF', amount1: '0.548', amount2: '51.581' },
        { id: '传统方法', name: 'MLP', amount1: '0.495', amount2: '33.715' },
        { id: '深度学习模型', name: 'CNVLSTM', amount1: '0.363', amount2: '32.738' },
        { id: '深度学习模型', name: 'SimVP', amount1: '0.359', amount2: '32.408' },
        { id: '深度学习模型', name: 'STRA-Net', amount1: '0.303', amount2: '27.373' },
      ],
    };
  },
  methods: {
    // Adds a new factor card to the input area
    addFactor() {
      if (this.factors.length >= 3) {
        this.$message.info('最多只能添加三个气象因子。');
        return;
      }
      this.factors.push({
        month: 'one',
        imageSrc: require('../assets/STRA-Net/june.png')
      });
    },

    // Updates the image in a specific card when its dropdown changes
    changeImage(index) {
      const selectedMonth = this.factors[index].month;
      let newImageSrc = '';
      if (selectedMonth === "one") {
        newImageSrc = require("../assets/STRA-Net/june.png");
      } else if (selectedMonth === "two") {
        newImageSrc = require("../assets/STRA-Net/july.png");
      } else if (selectedMonth === "three") {
        newImageSrc = require("../assets/STRA-Net/august.png");
      }
      this.factors[index].imageSrc = newImageSrc;
    },

    // Triggers the global prediction
    startGlobalPrediction() {
      if (this.predictionState === 'loading') return;
      this.predictionState = 'loading';

      // Simulate prediction process
      setTimeout(() => {
        // Set the result to your specified image path
        this.predictionImageSrc = require("../assets/STRA-Net/result.png");
        this.predictionState = 'success';
      }, 2000); // Simulate a 2-second prediction time
    },

    // Gets the text for the global button based on the current state
    getButtonText() {
      switch (this.predictionState) {
        case 'loading': return '预测中...';
        case 'success': return '预测完成';
        default: return '开始综合预测';
      }
    },

    // Table and chart methods remain the same
    objectSpanMethod({ row, columnIndex }) {
      const data = this.tableData;
      if (columnIndex === 0) {
        const id = row.id;
        const startIndex = data.findIndex(item => item.id === id);
        const count = data.filter(item => item.id === id).length;
        if (data.indexOf(row) === startIndex) {
          return { rowspan: count, colspan: 1 };
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
          legend: { data: ['STRA-Net', '真实值'], top: 30 },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          toolbox: { feature: { saveAsImage: {} } },
          xAxis: { type: 'category', boundaryGap: false, data: ['2020-06', '2020-07', '2020-08', '2021-06', '2021-07', '2021-08', '2022-06', '2022-07', '2022-08', '2023-06', '2023-07', '2023-08', '2024-06', '2024-07', '2024-08'] },
          yAxis: { type: 'value' },
          series: [
            { name: 'STRA-Net', type: 'line', data: [60, 160, 300, 85, 240, 238, 100, 130, 240, 100, 160, 221, 180, 107, 320] },
            { name: '真实值', type: 'line', data: [50, 140, 331, 81, 230, 215, 110, 117, 260, 120, 140, 219, 184, 120, 319] },
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
/* --- 统一的深色主题样式 (大部分保持不变) --- */
.dark-theme-card { background-color: rgba(20, 30, 50, 0.75); backdrop-filter: blur(12px); border: 1px solid rgba(100, 116, 139, 0.5); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); border-radius: 16px; }
::v-deep .el-card__body { padding: 24px 32px; }
.sub-nav-container { border-bottom: 1px solid rgba(100, 116, 139, 0.3); margin-bottom: 24px; }
.content-wrapper { line-height: 1.7; }
.content-section { margin-top: 40px; padding-top: 40px; border-top: 1px solid rgba(100, 116, 139, 0.3); }
.content-section:first-child { margin-top: 0; padding-top: 0; border-top: none; }
.content-title { color: #58a6ff; margin-bottom: 24px; text-align: center; }
.area-title { color: #94a3b8; font-size: 1.1em; margin-bottom: 16px; text-align: center; font-weight: bold; }

/* --- 核心修改：新的垂直主布局 --- */
.prediction-container-vertical {
  display: flex;
  flex-direction: column; /* 整体垂直排列 */
  gap: 32px;
}

/* 1. 顶部输入区 */
.input-area-horizontal {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  background-color: rgba(0, 0, 0, 0.1);
}
.prediction-grid-horizontal {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  gap: 24px;
  justify-content: center; /* 居中排列卡片 */
}

/* 缩小后的因子卡片 */
.prediction-card.small {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 250px; /* 核心：减小卡片宽度以缩小图片 */
  padding: 12px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  background-color: rgba(0, 0, 0, 0.2);
}
.add-card.small {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 250px; /* 与因子卡片宽度一致 */
  min-height: 250px; /* 保持一个合适的高度 */
  border: 2px dashed rgba(100, 116, 139, 0.5);
  border-radius: 12px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}
.add-card.small:hover { background-color: rgba(88, 116, 255, 0.1); border-color: #58a6ff; color: #fff; }
.add-card.small .el-icon-plus { font-size: 2.5em; }


/* 2. 底部输出区 */
.output-area-full-width {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  background-color: rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

/* 放大后的结果展示区 */
.result-display.large {
  width: 100%;
  max-width: 800px; /* 核心：给结果图片一个较大的最大宽度 */
  border-radius: 8px;
  border: 1px solid rgba(100, 116, 139, 0.2);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.2);
  min-height: 400px;
}
.result-display.large img {
  width: 100%;
  height: auto;
  display: block;
}

/* 全局预测按钮 */
.predict-button.global {
  width: 50%;
  max-width: 300px;
  font-weight: bold;
}

/* 其他通用样式 (大部分保持不变) */
.styled-select { background-color: rgba(0, 0, 0, 0.2); color: #e0e7ff; border: 1px solid rgba(100, 116, 139, 0.5); padding: 8px 12px; border-radius: 6px; font-size: 1em; width: 100%; }
.image-display { width: 100%; border-radius: 8px; border: 1px solid rgba(100, 116, 139, 0.2); overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: rgba(0, 0, 0, 0.2); }
.image-display img { width: 100%; height: auto; display: block; }
.placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.placeholder-text { font-size: 1.2em; color: rgba(148, 163, 184, 0.2); font-weight: bold; }
::v-deep .el-button.start { background-color: transparent !important; color: #58a6ff !important; border-color: #58a6ff !important; }
::v-deep .el-button.loading { background-color: rgba(255, 255, 255, 0.1) !important; border-color: #94a3b8 !important; }
::v-deep .el-button.success { background-color: #28a745 !important; border-color: #28a745 !important; color: white !important; }
/* 表格和图表 (保持不变) */
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

/* 子菜单样式 (保持不变) */
::v-deep .el-menu.el-menu-dark-theme, ::v-deep .el-menu.el-menu-dark-theme .el-menu-item, ::v-deep .el-menu.el-menu-dark-theme .el-submenu__title { background-color: transparent !important; border-bottom-color: transparent !important; color: #cbd5e1 !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item:hover, ::v-deep .el-menu.el-menu-dark-theme .el-submenu__title:hover { background-color: rgba(88, 116, 255, 0.1) !important; color: #fff !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active { color: #67e8f9 !important; border-bottom: 2px solid #67e8f9 !important; }
</style>