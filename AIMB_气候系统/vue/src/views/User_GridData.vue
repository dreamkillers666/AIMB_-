<template>
  <div class="grid-data-container">
    <!-- =========== Card 1: 文件管理 =========== -->
    <el-card v-if="!visualizationOnly" class="file-management-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">网格数据文件管理</span>
        </div>
      </template>

      <!-- 搜索和操作按钮 -->
      <div class="toolbar">
        <el-input
            v-model="searchQuery.name"
            placeholder="请输入名称"
            suffix-icon="el-icon-search"
            style="width: 200px"
            clearable
            @clear="resetSearch"
            @keyup.enter="load"
        ></el-input>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>

        <div class="toolbar-actions">
          <el-upload
              :action="uploadActionForTable"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeTableUpload"
              :on-success="handleTableUploadSuccess"
              :on-error="handleTableUploadError"
              accept=".nc,.nc4,.cdf,.netcdf"
          >
            <el-button type="primary">
              <i class="el-icon-upload"></i> 上传文件到数据库
            </el-button>
          </el-upload>

          <el-popconfirm
              title="您确定批量删除这些数据吗？"
              :disabled="multipleSelection.length === 0"
              @confirm="delBatch"
          >
            <template #reference>
              <el-button
                  type="danger"
                  :disabled="multipleSelection.length === 0"
              >
                <i class="el-icon-delete"></i> 批量删除 ({{ multipleSelection.length }})
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <!-- 文件数据表格 -->
      <el-table
          :data="tableData"
          border
          stripe
          v-loading="tableLoading"
          :header-cell-class-name="'headerBg'"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="数据名称" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="数据类型" width="120" align="center"></el-table-column>
        <el-table-column prop="size" label="数据大小" width="120" align="center">
          <template slot-scope="scope">
            {{ formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" align="center">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="下载" width="100" align="center">
          <template slot-scope="scope">
            <el-button
                type="primary"
                size="small"
                @click="downloadFile(scope.row)"
                :loading="scope.row.downloading"
            >
              <i class="el-icon-download"></i>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="100" align="center">
          <template slot-scope="scope">
            <el-switch
                v-model="scope.row.enable"
                active-color="#13ce66"
                inactive-color="#dcdfe6"
                @change="changeEnable(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button
                type="primary"
                size="small"
                @click="previewFile(scope.row)"
                v-if="isNetCDFFile(scope.row)"
            >
              预览
            </el-button>
            <el-popconfirm
                title="确定删除这个文件吗？"
                @confirm="del(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small">
                  <i class="el-icon-delete"></i>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- =========== Card 2: NC文件可视化工具 =========== -->
    <el-card class="visualization-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">NC文件可视化工具</span>
          <el-tag type="info" size="small">支持 .nc, .nc4, .cdf 格式</el-tag>
        </div>
      </template>

      <div class="visualization-process">
        <!-- 步骤1: 文件上传 -->
        <div class="process-step">
          <div class="step-header">
            <el-tag type="primary" effect="dark">步骤 1</el-tag>
            <span class="step-title">上传NC文件</span>
          </div>
          <el-upload
              :action="uploadActionForViz"
              :headers="uploadHeaders"
              :show-file-list="true"
              :before-upload="beforeVizUpload"
              :on-success="handleUploadAndGetVarsSuccess"
              :on-error="handleVizUploadError"
              :on-remove="handleVizFileRemove"
              :file-list="vizFileList"
              accept=".nc,.nc4,.cdf,.netcdf"
              :limit="1"
              class="viz-upload"
          >
            <el-button type="primary" size="medium">
              <i class="el-icon-upload"></i> 选择NC文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">请上传NetCDF格式文件，大小不超过50MB</div>
            </template>
          </el-upload>
        </div>

        <!-- 步骤2: 变量选择 -->
        <div
            class="process-step"
            v-if="uiState === 'vars-loaded' || uiState === 'plotting' || uiState === 'plot-success'"
        >
          <div class="step-header">
            <el-tag type="success" effect="dark">步骤 2</el-tag>
            <span class="step-title">选择可视化变量</span>
          </div>
          <div class="variable-selection">
            <el-select
                v-model="selectedVariable"
                placeholder="请选择要可视化的变量"
                style="width: 300px;"
                filterable
                clearable
                @change="loadVariableInfo"
            >
              <el-option
                  v-for="item in availableVariables"
                  :key="item"
                  :label="item"
                  :value="item"
              >
                <span style="float: left">{{ item }}</span>
              </el-option>
            </el-select>

            <el-button
                type="success"
                @click="generatePlot"
                :loading="uiState === 'plotting'"
                :disabled="!selectedVariable"
                style="margin-left: 15px;"
            >
              {{ uiState === 'plotting' ? '生成中...' : '开始可视化' }}
              <i class="el-icon-picture" v-if="uiState !== 'plotting'"></i>
            </el-button>
          </div>
          <div v-if="variableInfo && variableInfo.timeDimensionSize > 1" class="time-selection">
            <label>时间片</label>
            <el-input-number
                v-model="timeIndex"
                :min="0"
                :max="variableInfo.maxTimeIndex"
                :step="1"
                :disabled="uiState === 'plotting'"
                controls-position="right"
                @change="onTimeIndexChange"
            />
            <span>共 {{ variableInfo.timeDimensionSize }} 个</span>
          </div>
        </div>

        <!-- 步骤3: 可视化结果 -->
        <div class="process-step" v-if="uiState === 'plot-success'">
          <div class="step-header">
            <el-tag type="warning" effect="dark">步骤 3</el-tag>
            <span class="step-title">可视化结果</span>
            <el-button
                type="text"
                @click="downloadImage"
                style="margin-left: auto;"
            >
              <i class="el-icon-download"></i> 下载图片
            </el-button>
          </div>
          <div class="visualization-result">
            <div class="image-container">
              <img
                  :src="predictionImageSrc"
                  alt="可视化结果"
                  class="result-image"
                  @load="onImageLoad"
                  @error="onImageError"
              />
              <div v-if="imageLoading" class="image-loading">
                <i class="el-icon-loading"></i>
                <span>图片加载中...</span>
              </div>
            </div>

            <div class="image-info" v-if="imageInfo">
              <el-descriptions title="图像信息" :column="1" border size="small">
                <el-descriptions-item label="变量名称">{{ imageInfo.variableName }}</el-descriptions-item>
                <el-descriptions-item label="文件名称">{{ imageInfo.fileName }}</el-descriptions-item>
                <el-descriptions-item label="生成时间">{{ imageInfo.generateTime }}</el-descriptions-item>
                <el-descriptions-item label="时间片">{{ imageInfo.timeIndex }}</el-descriptions-item>
                <el-descriptions-item label="数据范围">{{ imageInfo.dataRange }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div
          v-if="uiState === 'start'"
          class="empty-state"
      >
        <i class="el-icon-picture-outline"></i>
        <p>请上传NC文件开始可视化</p>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "GridData",
  props: {
    visualizationOnly: { type: Boolean, default: false }
  },
  data() {
    return {
      // 文件管理数据
      tableData: [],
      searchQuery: {
        name: '',
        type: ''
      },
      multipleSelection: [],
      tableLoading: false,
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },

      // 可视化功能数据
      uiState: 'start', // 'start', 'vars-loaded', 'plotting', 'plot-success', 'error'
      availableVariables: [],
      selectedVariable: '',
      tempFileId: '',
      variableInfo: null,
      timeIndex: 0,
      predictionImageSrc: '',
      vizFileList: [],
      imageLoading: false,
      imageInfo: null,

      // 上传配置
      uploadHeaders: {},
      uploadStatus: 'idle', // 'idle', 'uploading', 'success', 'error'
      debugMode: true,
      gridDataApiBase: (process.env.VUE_APP_GRID_DATA_API_BASE_URL || 'http://localhost:9090').replace(/\/+$/, '')
    }
  },
  computed: {
    uploadActionForTable() { return `${this.gridDataApiBase}/griddata/upload` },
    uploadActionForViz() { return `${this.gridDataApiBase}/griddata/get-variables` }
  },
  created() {
    if (!this.visualizationOnly) this.load();
    this.initUploadHeaders();
  },
  beforeDestroy() {
    this.releaseTempFile(this.tempFileId);
  },
  methods: {
    // ==================== 文件管理方法 ====================
    async load() {
      this.tableLoading = true;
      try {
        const res = await this.request.get("/griddata/page", {
          params: {
            pageNum: this.pagination.pageNum,
            pageSize: this.pagination.pageSize,
            ...this.searchQuery
          }
        });
        if (res.code === '200') {
          this.tableData = res.data.records;
          this.pagination.total = res.data.total;
        }
      } catch (error) {
        this.$message.error("加载数据失败");
        console.error(error);
      } finally {
        this.tableLoading = false;
      }
    },

    changeEnable(row) {
      this.request.post("/griddata/update", row).then(res => {
        if (res.code === '200') {
          this.$message.success(row.enable ? "已启用" : "已禁用");
        }
      });
    },

    async del(id) {
      try {
        await this.request.delete("/griddata/" + id);
        this.$message.success("删除成功");
        this.load();
      } catch (error) {
        this.$message.error("删除失败");
      }
    },

    async delBatch() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning("请选择要删除的文件");
        return;
      }

      try {
        const ids = this.multipleSelection.map(v => v.id);
        await this.request.post("/griddata/del/batch", ids);
        this.$message.success(`成功删除 ${ids.length} 个文件`);
        this.load();
        this.multipleSelection = [];
      } catch (error) {
        this.$message.error("批量删除失败");
      }
    },

    handleSelectionChange(selection) {
      this.multipleSelection = selection;
    },

    resetSearch() {
      this.searchQuery = { name: '', type: '' };
      this.pagination.pageNum = 1;
      this.load();
    },

    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.pagination.pageNum = 1;
      this.load();
    },

    handleCurrentChange(page) {
      this.pagination.pageNum = page;
      this.load();
    },

    // ==================== 文件上传相关方法 ====================
    initUploadHeaders() {
      const user = localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {};
      if (user && user.token) {
        this.uploadHeaders = { Authorization: "Bearer " + user.token };
      }
    },

    beforeTableUpload(file) {
      const isNetCDF = /\.(nc|nc4|cdf|netcdf)$/i.test(file.name);
      const isLt50M = file.size / 1024 / 1024 < 50;

      if (!isNetCDF) {
        this.$message.error("只能上传NetCDF格式文件!");
        return false;
      }
      if (!isLt50M) {
        this.$message.error("文件大小不能超过50MB!");
        return false;
      }
      return true;
    },

    handleTableUploadSuccess(response) {
      console.log("文件管理上传成功:", response);
      if (response && response.success !== false) {
        this.$message.success("文件上传成功");
        this.load();
      } else {
        this.$message.error(response.message || "上传失败");
      }
    },

    handleTableUploadError(error) {
      this.$message.error("文件上传失败");
      console.error("Upload error:", error);
    },

    // ==================== 可视化功能方法 ====================
    beforeVizUpload(file) {
      return this.beforeTableUpload(file); // 复用相同的验证逻辑
    },

    handleUploadAndGetVarsSuccess(response, file, fileList) {
      console.log("=== 上传成功回调开始 ===");
      console.log("完整响应:", response);
      console.log("响应类型:", typeof response);
      console.log("响应keys:", Object.keys(response || {}));
      console.log("文件信息:", file);
      console.log("文件列表:", fileList);

      // 确保文件列表更新
      this.vizFileList = fileList;

      // 处理不同的响应格式
      let variables, tempFileId, message;

      // 情况1: 直接包含 variables 和 tempFileId
      if (response && response.variables !== undefined) {
        console.log("情况1: 直接格式");
        variables = response.variables;
        tempFileId = response.tempFileId;
        message = response.message;
      }
      // 情况2: 包含在 data 字段中
      else if (response && response.data) {
        console.log("情况2: data格式");
        variables = response.data.variables;
        tempFileId = response.data.tempFileId;
        message = response.data.message;
      }
      // 情况3: 其他格式
      else {
        console.log("情况3: 其他格式", response);
        // 尝试直接使用响应
        variables = response?.variables || response?.data?.variables;
        tempFileId = response?.tempFileId || response?.data?.tempFileId;
        message = response?.message || response?.data?.message;
      }

      console.log("解析出的变量:", variables);
      console.log("解析出的tempFileId:", tempFileId);

      if (variables && tempFileId) {
        this.availableVariables = Array.isArray(variables) ? variables : [variables];
        this.tempFileId = tempFileId;
        this.variableInfo = null;
        this.timeIndex = 0;
        this.uiState = 'vars-loaded';
        this.selectedVariable = '';
        this.predictionImageSrc = '';

        console.log("UI状态已更新为 vars-loaded");
        console.log("可用变量:", this.availableVariables);
        console.log("临时文件ID:", this.tempFileId);

        this.$message.success(`文件解析成功，找到 ${this.availableVariables.length} 个变量`);
      } else {
        console.error("无法解析变量列表或tempFileId");
        console.error("variables:", variables);
        console.error("tempFileId:", tempFileId);

        this.$message.error("无法解析返回的变量列表");
        this.uiState = 'error';

        // 从文件列表中移除失败的文件
        this.vizFileList = this.vizFileList.filter(f => f.uid !== file.uid);
      }

      console.log("=== 上传成功回调结束 ===");
    },

    handleVizUploadError(error, file, fileList) {
      console.error("上传错误:", error);
      this.$message.error("文件上传失败，请检查网络连接或重新登录");
      this.uiState = 'error';

      // 从文件列表中移除失败的文件
      this.vizFileList = this.vizFileList.filter(f => f.uid !== file.uid);
    },

    handleVizFileRemove() {
      this.resetVisualization();
    },

    async loadVariableInfo(variableName) {
      this.variableInfo = null;
      this.timeIndex = 0;
      this.predictionImageSrc = '';
      this.imageInfo = null;
      if (this.uiState !== 'start' && this.uiState !== 'error') this.uiState = 'vars-loaded';
      if (!variableName || !this.tempFileId) return;

      try {
        const info = await this.request.post(`${this.gridDataApiBase}/griddata/get-variable-info`, {
          tempFileId: this.tempFileId,
          variableName
        });
        if (this.selectedVariable === variableName) this.variableInfo = info;
      } catch (error) {
        this.$message.error(`读取变量维度失败: ${error.message || '请检查后端服务'}`);
      }
    },

    onTimeIndexChange() {
      this.predictionImageSrc = '';
      this.imageInfo = null;
      if (this.uiState === 'plot-success') this.uiState = 'vars-loaded';
    },

    async generatePlot() {
      if (!this.selectedVariable) {
        this.$message.warning("请先选择要可视化的变量");
        return;
      }

      this.uiState = 'plotting';
      this.imageLoading = true;

      try {
        const res = await this.request.post(`${this.gridDataApiBase}/griddata/visualize`, {
          tempFileId: this.tempFileId,
          variableName: this.selectedVariable,
          timeIndex: this.variableInfo && this.variableInfo.timeDimensionSize > 1 ? this.timeIndex : 0
        });

        if (res && res.imageUrl) {
          this.predictionImageSrc = this.resolveGridDataUrl(res.imageUrl);
          this.uiState = 'plot-success';

          // 设置图像信息
          this.imageInfo = {
            variableName: this.selectedVariable,
            fileName: this.vizFileList[0]?.name || '未知文件',
            generateTime: new Date().toLocaleString(),
            timeIndex: res.timeIndex == null ? 0 : res.timeIndex,
            dataRange: res.dataRange || '—'
          };
        } else {
          throw new Error(res?.message || "未能获取图片URL");
        }
      } catch (error) {
        this.$message.error("绘图失败: " + error.message);
        this.uiState = 'vars-loaded';
      } finally {
        this.imageLoading = false;
      }
    },

    onImageLoad() {
      this.imageLoading = false;
    },

    onImageError() {
      this.imageLoading = false;
      this.$message.error("图片加载失败");
    },

    downloadImage() {
      if (this.predictionImageSrc) {
        const link = document.createElement('a');
        link.href = this.predictionImageSrc;
        link.download = `visualization_${this.selectedVariable}_${Date.now()}.png`;
        link.click();
      }
    },

    resetVisualization() {
      const tempFileId = this.tempFileId;
      this.uiState = 'start';
      this.availableVariables = [];
      this.selectedVariable = '';
      this.tempFileId = '';
      this.variableInfo = null;
      this.timeIndex = 0;
      this.predictionImageSrc = '';
      this.vizFileList = [];
      this.imageInfo = null;
      this.releaseTempFile(tempFileId);
    },

    releaseTempFile(tempFileId) {
      if (tempFileId) this.request.post(`${this.gridDataApiBase}/griddata/cleanup`, { tempFileId }).catch(() => {});
    },

    resolveGridDataUrl(path) {
      if (/^https?:\/\//i.test(path)) return path;
      return `${this.gridDataApiBase}${path.startsWith('/') ? '' : '/'}${path}`;
    },

    // ==================== 工具方法 ====================
    formatFileSize(bytes) {
      if (!bytes || bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    formatTime(timestamp) {
      if (!timestamp) return '-';
      return new Date(timestamp).toLocaleString();
    },

    isNetCDFFile(file) {
      return file && file.name && /\.(nc|nc4|cdf|netcdf)$/i.test(file.name);
    },

    previewFile(file) {
      if (this.isNetCDFFile(file)) {
        // 触发可视化流程
        this.vizFileList = [{ name: file.name, url: file.url }];
        // 这里可以调用后端接口获取变量列表
        this.$message.info("预览功能开发中...");
      }
    },

    downloadFile(file) {
      if (file.url) {
        file.downloading = true;
        window.open(file.url);
        // 模拟下载完成
        setTimeout(() => {
          file.downloading = false;
          this.$forceUpdate();
        }, 1000);
      }
    }
  }
}
</script>

<style scoped>
.grid-data-container {
  padding: 20px;
}

.file-management-card,
.visualization-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.visualization-process {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.process-step {
  border-left: 3px solid #409EFF;
  padding-left: 16px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.step-title {
  font-weight: bold;
  color: #303133;
}

.variable-selection {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-selection {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  color: #606266;
  font-size: 13px;
}

.visualization-result {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.image-container {
  position: relative;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}

.result-image {
  width: 70%;
  height: auto;
  display: block;
}

.image-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #909399;
}

.image-info {
  background: white;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e6e6e6;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
}

.viz-upload {
  width: 100%;
}

</style>
