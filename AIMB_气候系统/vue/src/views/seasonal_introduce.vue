<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/seasonal_introduce'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/seasonal_introduce">次季节降水介绍</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_data">数据</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: Introduction -->
      <section class="content-section">
        <h2 class="content-title">次季节–季节模式预测(Subseasonal-to-Seasonal Precipitation Prediction)</h2>
        <p class="main-text">
          随着极端天气气候事件造成的气象灾害日益严重，对其科学预测是国际气候预测研究面临的挑战。近些年，我国出现大幅度的月际间气候异常，如冬季不同月份之间极端冷暖事件交替和转折、夏季不同月份或不同阶段极端干旱和洪涝交替和转折等，造成严重气象灾害，同时也影响季节尺度气候预测准确率。由于月际气候异常往往被季节平均气候掩盖，因此亟需考虑月际尺度气候异常，提升月际—季节尺度气候预测。
        </p>
        <p class="main-text">
          中国第二代月动力延伸预测模式系统（DERF2.0）对中国月际气温的预测效能总体高于月际降水，但仍有待改进（吴统文等, 2013;何慧根等, 2014）。美国国家环境预报中心（NCEP）的第二代气候预测系统（CFSv2）对中国夏季月际尺度降水的预测效能存在显著月际和区域差异。如图1 所示，1983～2022 年CFSv2 预测系统2 月起报的中国6 月降水距平百分率具有显著预测效能的区域主要集中在长江以南部分地区，CFSv2 预测的6 月降水距平百分率与观测之间的时间相关系数（TCC）仅有6.3%（11/160）的站点通过95%置信水平的显著性检验；对7 月和8 月降水百分率预测TCC 通过95%置信水平的显著性检验的站点占比分别为4.4%（7/160）和2.5%（4/160）。但相对于降水距平百分率的预测，CFSv2 对中国夏季6、7、8 月降水年际增量（当年降水量减去前一年降水量）预测准确率略高，6～8 月逐月TCC 通过95%置信水平的站点占比分别为8.1%（13/160）、7.5%（12/160）和5.0%（8/160）。
        </p>
        <div class="content-image-wrapper">
          <img src="../assets/降水预测/rainfall1.png" alt="CFSv2 Prediction Skill" class="content-image" />
        </div>
      </section>

      <!-- Section 2: Prediction Factors -->
      <section class="content-section">
        <h2 class="content-title">预测因子</h2>

        <h3 class="sub-heading">12月南太平洋中高纬关键海温区</h3>
        <p class="main-text">
          南半球大气环流包括南极涛动、澳大利亚高压、马斯克林高压、越赤道气流等是亚洲季风系统的重要成员，对东亚气候和气候预测有重要影响。
          下图为夏季不同月份降水距平百分率年际增量EOF，分析前90%解释方差的模态对应的时间系数与12 月海温年际增量之间TCC 的累积解释方差（通过90%置信水平显著性检验）。结果显示，12 月南太平洋中高纬关键区海温分别与中国6、7、8 月的降水均存在显著关系。其中，影响6月和7月中国降水的海温关键区为（30°S～70°S，120°E～60°W），影响8月降水的海温关键区为（20°S～70°S，150°W～60°W；图2）。当12月（30°S～70°S，120°E～60°W）海温年际增量呈南北“偶极子”型分布时，中国6 月降水华南、西南地区较前一年偏少，北方地区较前一年偏多（图a、b），且偶极型的海温年际增量分布与6月南极涛动相关（图c）。
        </p>
        <div class="content-image-wrapper large-image">
          <img src="../assets/降水预测/rainfall2.png" alt="SST Anomaly" class="content-image" />
        </div>

        <h3 class="sub-heading">1 月北极关键海冰区</h3>
        <p class="main-text">
          近些年北极海冰快速减少，北极海冰年际变率强度显著增强（Fan et al., 2018），北极海冰对东亚气候的影响也越来越显著，北极海冰是东亚气候预测的重要预测因子之一。
          已有研究表明，前期冬春季巴伦支海海冰负（正）异常可激发类欧亚EU 型遥相关波列，造成夏季我国长江流域及其以北大部分地区降水偏少（多），南方地区降水偏多（少）。此外，2020 年春末夏初西伯利亚沿岸海冰偏少也是6～7 月的“暴力超级梅雨”发生的原因之一（Chen et al., 2021）。该地区海冰偏少引起局地对流层增温，使得西伯利亚地区经向温度梯度、纬向风和涡动动量通量减弱，有利于东西伯利亚地区阻高的维持和发展，进而有利于高纬冷อากาศ南下，使得梅雨锋维持并产生极强的降水。前期秋季喀拉海—拉普捷夫海海冰与东亚夏季降水的关系在90 年代之后也呈现年代际增强。该地区海冰异常通过影响欧亚地区的土壤湿度和雪深，使海冰异常信号从前期秋季持续到春季，并影响夏季中国东北地区降水（Liu et al., 2020）。而春季波弗特海海冰异常通过引起夏季北极偶极子异常模态，影响东亚夏季西风急流和欧亚遥相关型，进而影响长江中下游极端降水频次。
        </p>

        <h3 class="sub-heading">CFSv2 预测的夏季逐月关键海温区</h3>
        <p class="main-text">
          根据中国夏季不同月份降水距平百分率年际增量EOF 分析前90%解释方差的模态对应的时间系数与观测、CFSv2 预测（2 月起报）同期海温年际增量之间TCC 的累积解释方差（通过90%置信水平显著性检验，图5），选取了CFSv2 预测（2 月起报）的6 月北太平洋中高纬地区（40°～60°N，150°E～150°W），7 月赤道中东太平洋地区（5°S～5°N，180°～80°W）和8 月热带西太平洋地区（10°S～30°N，80°～160°E）的海温分别作为中国夏季不同月份降水的同期预测因子（P3-SST-CFSv2）。此外，评估了CFSv2 对夏季逐月海温的预测效能（图6）。结果表明，CFSv2 预测系统2 月起报的夏季逐月海温与观测之间的TCC，在所选择的北太平洋中高纬、赤道中东太平洋以及热带西太平洋等区域，均通过了90%的置信水平。因此，选取CFSv2 预测系统2月起报的6～8 月关键区海温作为中国夏季月际尺度降水的预测因子具有较好的预测效能。
        </p>
        <div class="content-image-wrapper large-image">
          <img src="../assets/降水预测/rainfall3.png" alt="CFSv2 SST Prediction" class="content-image" />
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  name: "seasonal_introduce"
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
  line-height: 1.8; /* Increased line-height for long text blocks */
  text-align: justify;
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

/* 4. 标题样式 */
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
  line-height: 1.4; /* Ensure long titles wrap nicely */
}
.sub-heading {
  color: #94a3b8; /* Subdued color for sub-headings */
  font-size: 1.1em;
  margin-top: 2em;
  margin-bottom: 1em;
  padding-left: 1em;
  border-left: 3px solid #58a6ff;
}

/* 5. 文本样式 */
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 1.5em;
}

/* 6. 图片样式 */
.content-image-wrapper {
  margin: 24px 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  background-color: rgba(255, 255, 255, 0.95);
}
.content-image-wrapper.large-image {
  max-width: 80%; /* Allow larger images to take more space */
  margin-left: auto;
  margin-right: auto;
}
.content-image {
  width: 100%;
  height: auto;
  display: block;
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