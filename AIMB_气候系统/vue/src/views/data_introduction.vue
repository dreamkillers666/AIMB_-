<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/data_introduction'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/data_introduction">数据集介绍</el-menu-item>
        <el-menu-item index="/usermanage/data_download">数据集下载</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <h2 class="content-title main-page-title">数据集介绍</h2>

      <!-- Section 1: CMIP6 Dataset -->
      <section class="content-section">
        <h3 class="dataset-title">1. CMIP6数据集</h3>
        <p class="main-text">
          国际耦合模式比较计划（CMIP——Coupled Model Intercomparison Project)，其基础是大气模式比较计划（AMIP），由世界气候研究计划（WCRP）耦合模拟工第六次国际耦合模式比较计划。CMIP6是由各个国家、机构自己开发的（针对不同切入点的）模式共同参与组成的。
        </p>
        <p class="main-text">
          CMIP6的实验设计包括三个层次：DECK实验、历史模拟、以及23个子计划。
        </p>
        <p class="main-text">
          <strong>1. DECK实验:</strong> 这是最为核心的实验，是每个参与模式必做的实验，也被称为CMIP计划的“准入证”。它包含了4组基准实验：
        </p>
        <ul class="info-list">
          <li>全球大气环流模式 (AMIP)</li>
          <li>工业化前控制模拟 (piControl)</li>
          <li>CO2浓度每年增加1%模拟 (1pctCO2)</li>
          <li>CO2浓度突然增加4倍模拟 (abrupt4XCO2)</li>
        </ul>
        <p class="main-text"><strong>2. 历史模拟:</strong> 这是模式加入CMIP6的必做项目，即用CMIP6强迫进行历史模拟，时段为1850-2014年。</p>
        <p class="main-text"><strong>3. CMIP6的23个子计划:</strong> 这是各个模式选做的实验，主要为了解决“系统偏差、强迫响应、变率可预测性和未来情景”这三大科学主题而制定。</p>
        <div class="image-stack">
          <div class="content-image-wrapper">
            <img src="../assets/ENSO_data/CMIP1.png" alt="CMIP6 Structure Diagram 1" class="content-image" />
          </div>
          <div class="content-image-wrapper">
            <img src="../assets/ENSO_data/CMIP2.png" alt="CMIP6 Structure Diagram 2" class="content-image" />
          </div>
        </div>
      </section>

      <!-- Section 2: SODA Dataset -->
      <section class="content-section">
        <h3 class="dataset-title">2. SODA数据集</h3>
        <p class="main-text">
          简单海洋数据同化（SODA）项目的目标是重建海洋的历史物理历史。SODA 使用基于社区标准代码的简单架构，并选择匹配的分辨率。SODA 版本 3 代表了与之前的 SODA2 相比的重大变化。它现在使用更精细的 1/4°x1/4°x50lev 分辨率的 GFDL MOM5/SIS 数值，并包括一个活跃的海冰分量。
        </p>
        <div class="image-stack">
          <div class="content-image-wrapper">
            <img src="../assets/ENSO_data/SODA1.png" alt="SODA Data Example 1" class="content-image" />
          </div>
          <div class="content-image-wrapper">
            <img src="../assets/ENSO_data/SODA2.png" alt="SODA Data Example 2" class="content-image" />
          </div>
        </div>
      </section>

      <!-- Section 3: GODAS Dataset -->
      <section class="content-section">
        <h3 class="dataset-title">3. GODAS数据集</h3>
        <p class="main-text">
          美国国家环境预报中心（NCEP）使用全球资料同化系统（GDAS）将来自各种观测系统和仪器的数据内插到一个三维网格中，用来初始化NCEP的全球预报系统（GFS）模式。模式垂直层数从64个增加到127个，模式层顶扩展到中间层（约80km高度）。
        </p>
        <div class="content-image-wrapper">
          <img src="../assets/ENSO_data/GODAS1.png" alt="GODAS System Diagram" class="content-image" />
        </div>
        <p class="main-text">GDAS数据主要包含以下类型：</p>
        <ol class="info-list ordered">
          <li><strong>GDAS Snow, Ice, SST, Satellite, Ship, Aircraft:</strong> 以 GRIB 和 BUFR 格式提供，主要关注海洋、雪、冰、卫星和飞行器等数据来源。</li>
          <li><strong>GDAS - Daily Tar Files:</strong> 包含每日气象数据的 tar 格式压缩文件，包含多个层次和高度的气象变量。</li>
          <li><strong>GDAS - Hourly Tar Files:</strong> 包含每小时气象数据的 tar 格式压缩文件，提供更高的时间分辨率。</li>
          <li><strong>GDAS - Daily BUFR Files:</strong> 采用 BUFR 标准格式存储的每日气象分析数据，涵盖大气变量、温度、湿度、风速等。</li>
        </ol>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // Corrected component name
  name: "data_introduction"
}
</script>

<style scoped>
/* --- 统一的深色主题样式 --- */

/* 1. 主卡片容器 */
.dark-theme-card { background-color: rgba(20, 30, 50, 0.75); backdrop-filter: blur(12px); border: 1px solid rgba(100, 116, 139, 0.5); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); border-radius: 16px; }
::v-deep .el-card__body { padding: 24px 32px; }

/* 2. 子菜单容器 */
.sub-nav-container { border-bottom: 1px solid rgba(100, 116, 139, 0.3); margin-bottom: 24px; }

/* 3. 内容区域和章节 */
.content-wrapper { line-height: 1.8; text-align: justify; }
.content-section { margin-top: 40px; padding-top: 40px; border-top: 1px solid rgba(100, 116, 139, 0.3); }
.content-section:first-child { margin-top: 0; padding-top: 0; border-top: none; }

/* 4. 标题样式 */
.content-title { color: #58a6ff; margin-bottom: 24px; }
.main-page-title { text-align: center; }
.dataset-title { color: #94a3b8; font-size: 1.4em; margin-bottom: 1em; padding-bottom: 0.5em; border-bottom: 1px solid rgba(100, 116, 139, 0.3); }

/* 5. 文本和列表样式 */
.main-text { display: block; font-size: 16px; color: #e0e7ff; margin-bottom: 1.5em; }
.main-text strong { color: #94a3b8; }
.info-list { list-style: none; padding: 0 0 0 25px; margin: 1.5em 0; font-size: 16px; color: #e0e7ff; }
.info-list li { position: relative; margin-bottom: 12px; }
.info-list li::before { content: '■'; position: absolute; left: -25px; top: 2px; color: #58a6ff; font-size: 0.8em; }
.info-list.ordered { counter-reset: item; }
.info-list.ordered li::before { content: counter(item); counter-increment: item; background-color: #58a6ff; color: #0f172a; border-radius: 50%; width: 20px; height: 20px; line-height: 20px; text-align: center; font-size: 0.7em; font-weight: bold; top: 4px; left: -30px; }

/* 6. 图片样式 */
.image-stack { display: flex; flex-direction: column; align-items: center; gap: 16px; margin: 24px 0; }
.content-image-wrapper { width: 100%; max-width: 70%; border-radius: 12px; overflow: hidden; border: 1px solid rgba(100, 116, 139, 0.2); background-color: rgba(255, 255, 255, 0.95); }
.content-image { width: 100%; height: auto; display: block; }

/* --- 7. [核心] 覆盖子菜单样式 --- */
::v-deep .el-menu.el-menu-dark-theme, ::v-deep .el-menu.el-menu-dark-theme .el-menu-item, ::v-deep .el-menu.el-menu-dark-theme .el-submenu__title { background-color: transparent !important; border-bottom-color: transparent !important; color: #cbd5e1 !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item:hover, ::v-deep .el-menu.el-menu-dark-theme .el-submenu__title:hover { background-color: rgba(88, 116, 255, 0.1) !important; color: #fff !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active { color: #67e8f9 !important; border-bottom: 2px solid #67e8f9 !important; }
</style>