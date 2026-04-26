<template>
  <el-card class="dark-theme-card">
    <div class="sub-nav-container">
      <el-menu :default-active="$route.path" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_pre">MEPM预测结果</el-menu-item>
        <el-menu-item index="/usermanage/mjo_pre">MISM预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_pre">STRA-Net预测结果</el-menu-item>
        <el-menu-item index="/usermanage/convlstm_pre">ConvLSTM预测结果</el-menu-item>
      </el-menu>
    </div>

    <div class="content-wrapper">
      <section class="content-section">
        <h2 class="content-title">ConvLSTM 实时预测与动态评估</h2>

        <div class="dashboard-layout">
          <div class="left-panel">
            <div class="control-box">
              <h3 class="panel-title">设定推演参数</h3>
              <div class="param-row">
                <span>预测时长:</span>
                <el-select v-model="leadTime" class="styled-select" style="width: 120px;">
                  <el-option label="3 个月" :value="3"></el-option>
                  <el-option label="6 个月" :value="6"></el-option>
                  <el-option label="12 个月" :value="12"></el-option>
                </el-select>
              </div>
              <el-button @click="startPrediction" class="predict-button start" :loading="isPredicting" style="width: 100%;">
                {{ isPredicting ? 'ConvLSTM预测与评估中...' : '启动实时预测' }}
              </el-button>
            </div>

            <div class="true-box">
              <h3 class="panel-title">Input: True SSTA (Ground Truth)</h3>
              <div class="image-display map-display">
                <img v-if="resultData && resultData.true_url" :src="'http://localhost:9090' + resultData.true_url" alt="True SSTA" />
                <div v-else class="placeholder"><span>等待预测...</span></div>
              </div>
            </div>
          </div>

          <div class="right-panel">
            <div class="output-header">
              <h3 class="panel-title" style="margin: 0; border: none;">Output: 模型预测与误差评估</h3>
              <el-button @click="resetPrediction" icon="el-icon-refresh" class="reset-button" circle size="mini"></el-button>
            </div>

            <div class="top-maps-row">
              <div class="map-container">
                <div class="mini-title">Predicted SSTA</div>
                <div class="image-display map-display">
                  <img v-if="resultData && resultData.pred_url" :src="'http://localhost:9090' + resultData.pred_url" alt="Predicted" />
                  <div v-else class="placeholder"><span>暂无数据</span></div>
                </div>
              </div>
              <div class="map-container">
                <div class="mini-title">Difference (Pred - True)</div>
                <div class="image-display map-display">
                  <img v-if="resultData && resultData.diff_url" :src="'http://localhost:9090' + resultData.diff_url" alt="Difference" />
                  <div v-else class="placeholder"><span>暂无数据</span></div>
                </div>
              </div>
            </div>

            <div class="bottom-chart-row">
              <div class="mini-title">Extracted Nino 3.4 Index Time Series</div>
              <div class="image-display chart-display">
                <img v-if="resultData && resultData.line_url" :src="'http://localhost:9090' + resultData.line_url" alt="Nino 3.4 Chart" />
                <div v-else class="placeholder"><span>暂无数据</span></div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </el-card>
</template>

<script>
import axios from 'axios';

export default {
  name: "ConvlstmPrediction",
  data() {
    return {
      leadTime: 6,
      isPredicting: false,
      resultData: null,
    };
  },
  methods: {
    async startPrediction() {
      this.isPredicting = true;
      this.resultData = null;

      try {
        // 确保这里的地址和你的实际文件地址对得上
        const defaultFilePath = "D:/1_ENSO_prediction/code/era5download/stdmamba_data/surface_data/2997adb93a53e5435d0933fecbde29dc.nc";
        const response = await axios.post(`http://localhost:9090/enso/predict?filePath=${defaultFilePath}&leadTime=${this.leadTime}`);

        if (response.data.code === "200") {
          this.resultData = response.data.data;
          this.$message.success("ConvLSTM预测与评估完成！");
        } else {
          this.$message.error("预测失败: " + response.data.msg);
        }
      } catch (error) {
        console.error(error);
        this.$message.error("ConvLSTM预测失败，请确保 Python 服务已启动。");
      } finally {
        this.isPredicting = false;
      }
    },
    resetPrediction() {
      this.resultData = null;
      this.leadTime = 6;
    }
  }
};
</script>

<style scoped>
.dark-theme-card { background-color: rgba(20, 30, 50, 0.75); backdrop-filter: blur(12px); border: 1px solid rgba(100, 116, 139, 0.5); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); border-radius: 16px; }
::v-deep .el-card__body { padding: 24px 32px; }
.sub-nav-container { border-bottom: 1px solid rgba(100, 116, 139, 0.3); margin-bottom: 24px; }
.content-title { color: #58a6ff; margin-bottom: 24px; text-align: center; }

/* 核心网格布局 (左 1/3，右 2/3) */
.dashboard-layout { display: flex; gap: 24px; align-items: stretch; }
.left-panel { flex: 1; display: flex; flex-direction: column; gap: 20px; min-width: 250px; }
.right-panel { flex: 2.2; display: flex; flex-direction: column; gap: 16px; border: 1px solid rgba(100, 116, 139, 0.3); border-radius: 12px; padding: 20px; background-color: rgba(0, 0, 0, 0.1); }

/* 左侧控制区 */
.control-box { background-color: rgba(0, 0, 0, 0.2); border-radius: 10px; padding: 16px; border: 1px solid rgba(100, 116, 139, 0.2); }
.param-row { display: flex; justify-content: space-between; align-items: center; color: #cbd5e1; margin-bottom: 20px; font-size: 0.9em; }

.panel-title { color: #94a3b8; font-size: 1.05em; font-weight: bold; border-bottom: 1px solid rgba(100, 116, 139, 0.3); padding-bottom: 10px; margin-bottom: 15px; }
.mini-title { color: #e2e8f0; font-size: 0.9em; margin-bottom: 8px; font-weight: 500; text-align: center; }

.image-display { width: 100%; border-radius: 8px; border: 1px solid rgba(100, 116, 139, 0.2); overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: rgba(0, 0, 0, 0.3); }
.image-display img { width: 100%; height: 100%; display: block; }
.map-display { aspect-ratio: 2 / 1; }
.map-display img { object-fit: contain; }
.chart-display { height: 220px; }
.chart-display img { object-fit: fill; }

.output-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(100, 116, 139, 0.3); padding-bottom: 10px; }
.top-maps-row { display: flex; gap: 16px; }
.map-container { flex: 1; display: flex; flex-direction: column; }
.bottom-chart-row { margin-top: auto; }

.placeholder { color: rgba(148, 163, 184, 0.3); font-weight: bold; }
::v-deep .el-button.start { background-color: transparent !important; color: #58a6ff !important; border-color: #58a6ff !important; }
::v-deep .el-button.is-loading { background-color: rgba(255, 255, 255, 0.1) !important; color:#94a3b8 !important; border-color: #94a3b8 !important;}
::v-deep .el-button.reset-button { background-color: transparent !important; color: #e6a23c !important; border-color: #e6a23c !important; }
::v-deep .el-button.reset-button:hover { background-color: rgba(230, 162, 60, 0.1) !important; color: #ebb563 !important; border-color: #ebb563 !important; }
::v-deep .el-menu.el-menu-dark-theme { background-color: transparent !important; border-bottom: none !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item { color: #cbd5e1 !important; background-color: transparent !important; border-bottom-color: transparent !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active { color: #67e8f9 !important; border-bottom: 2px solid #67e8f9 !important; }
::v-deep .el-input__inner { background-color: rgba(0, 0, 0, 0.3) !important; color: #67e8f9 !important; border: 1px solid rgba(100, 116, 139, 0.5) !important; text-align: center;}
</style>