<template>
  <!-- 1. Apply our reusable dark theme card class -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/enso_introduce'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/enso_introduce">ENSO介绍</el-menu-item>
        <el-menu-item index="/usermanage/enso_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/enso_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/enso_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. Main content area with semantic classes instead of inline styles -->
    <div class="content-area">
      <h2 class="content-title">El Niño Southern Oscillation (ENSO)</h2>
      <span class="main-text">
        地球海洋和大气之间的联系直接影响我们所经历的天气和气候条件。厄尔尼诺和拉尼娜现象，统称为厄尔尼诺南方涛动 （ENSO），是赤道太平洋与预期海面温度 （SST） 的偶发性偏离。
        这些比正常海洋温度更暖或更冷的海洋温度会通过影响高压和低压系统、风和降水来影响世界各地的天气模式。
        ENSO 可能会给一个地区带来急需的水分，同时在其他地区造成极端的水分过多或过少。
      </span>

      <div class="content-image-wrapper">
        <img src="../assets/ENSO/elnino-banner.png" alt="ENSO Banner" class="content-image" />
      </div>

      <h2 class="content-title">厄尔尼诺指数</h2>
      <h3 class="sub-heading">不同类型的厄尔尼诺指数——1854年至今</h3>
      <span class="main-text">
        在近几十年逐年发生的厄尔尼诺事件中，太平洋中部海面变暖频繁发生。这与典型厄尔尼诺事件期间的东太平洋变暖模式截然不同。因此，中太平洋厄尔尼诺现象对全球生态系统、气候和飓风产生了明显的影响。新型厄尔尼诺发生频率的增加不仅对认识厄尔尼诺动态及其变化提出了挑战，也对预测厄尔尼诺及其对全球当前和未来气候的影响提出了挑战。
        <br/><br/>
        最近的一项研究（Sullivan 等人，2016 年）提出了代表不同类型厄尔尼诺现象的简单指数，可用于理解、预测和影响评估：
      </span>
      <p class="reference-text">
        沙利文，A.，J.-J。Luo*、AC Hirst、D. Bi、W. Cai 和 J. He，2016 年： 年代际异常对中太平洋厄尔尼诺现象的显着贡献 。科学报告，6，38540；doi: 10.1038/srep38540
      </p>

      <h3 class="sub-heading">东太平洋（EP）型、中太平洋（CP）型、混合型厄尔尼诺指数按以下1、2、3计算：</h3>
      <div class="main-text formula-list">
        <ol>
          <li>EP = Niño3_normalized – 0.5 * Niño4_normalized</li>
          <li>CP = Niño4_normalized – 0.5 * Niño3_normalized</li>
          <li>混合型=EP+CP≈Niño3.4_normalized</li>
        </ol>
        其中Niño3_normalized、Niño4_normalized和Niño3.4_normalized表示Niño3(5°S-5°N, 150°W-90°W), Niño4(5°S-5°N, 160°E-150°W）和Niño3.4（5°S-5°N，170°W-120°W）区域。
      </div>
    </div>
  </el-card>
</template>

<script>
// --- Script 部分保持不变 ---
export default {
  name: "enso_introduce"
}
</script>

<style scoped>
/* --- 统一的深色主题样式 --- */

/* 1. 主卡片容器 */
.dark-theme-card {
  /* “毛玻璃”效果 */
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(100, 116, 139, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 16px;
}
/* 覆盖 Element UI Card 的默认内边距 */
::v-deep .el-card__body {
  padding: 24px 32px;
}

/* 2. 子菜单容器 */
.sub-nav-container {
  border-bottom: 1px solid rgba(100, 116, 139, 0.3);
  margin-bottom: 24px;
}

/* 3. 内容区域 */
.content-area {
  font-size: 18px;
  line-height: 1.7;
  text-align: justify;
}

/* 4. 标题样式 */
.content-title {
  color: #58a6ff; /* 数据蓝 */
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}
.content-title:first-child {
  margin-top: 0;
}
.sub-heading {
  color: #58a6ff;
  font-size: 1.1em;
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}

/* 5. 文本样式 */
.main-text {
  display: block;
  color: #e0e7ff;
  margin-bottom: 1em;
}
.reference-text {
  display: block;
  background-color: rgba(0, 0, 0, 0.2);
  border-left: 3px solid #58a6ff;
  padding: 12px 16px;
  font-size: 0.9em;
  color: #94a3b8;
  border-radius: 4px;
  margin: 1.5em 0;
}
.formula-list ol {
  padding-left: 25px;
}

/* 6. 图片样式 */
.content-image-wrapper {
  margin: 24px 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
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
  background-color: rgba(88, 166, 255, 0.1) !important;
  color: #fff !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active {
  color: #67e8f9 !important; /* 高亮青色 */
  border-bottom: 2px solid #67e8f9 !important;
}
</style>