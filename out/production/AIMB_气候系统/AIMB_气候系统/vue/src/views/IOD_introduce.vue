<template>
  <!-- 1. The main container is a single, styled "dark theme" card -->
  <el-card class="dark-theme-card">

    <!-- 2. Sub-navigation menu, consistently styled -->
    <div class="sub-nav-container">
      <el-menu :default-active="'/usermanage/iod_introduce'" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item index="/usermanage/iod_introduce">IOD介绍</el-menu-item>
        <el-menu-item index="/usermanage/iod_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/iod_resource">更多资源</el-menu-item>
      </el-menu>
    </div>

    <!-- 3. A single content wrapper for all sections -->
    <div class="content-wrapper">

      <!-- Section 1: IOD Introduction -->
      <section class="content-section text-center">
        <h2 class="content-title">IOD</h2>
        <p class="main-text">
          印度洋偶极子（Indian Ocean Dipole, IOD）是一种重要的气候现象，它描述了印度洋东西两侧海表面温度（Sea Surface Temperature, SST）的异常变化。
          IOD通过影响印度洋及其周边地区的气候系统，对全球气候产生显著影响。
        </p>
        <div class="content-image-wrapper large-image">
          <img src="../assets/IOD/Sstanom_199711_krig.jpg" alt="IOD Banner" class="content-image" />
        </div>
      </section>

      <!-- Section 2: Positive and Negative Events -->
      <section class="content-section">
        <h2 class="content-title text-center">IOD的正负事件</h2>
        <p class="main-text text-center">
          IOD的形成与印度洋的东西两侧海表面温度差异有关。在正常情况下，印度洋西部的海表面温度较高，而东部的海表面温度较低。
          然而，在某些年份，这种温度差异会显著增强，导致IOD事件的发生。
          当西部的海表面温度较东部显著降低，而东部的海表面温度相对较高时，称为正IOD事件；相反的情况称为负IOD事件。
        </p>
        <div class="two-column-layout">
          <div class="column text-center">
            <h3 class="sub-heading">正IOD事件</h3>
            <div class="content-image-wrapper">
              <img src="../assets/IOD/IOD正事件.png" alt="IOD正事件图" class="content-image" />
            </div>
          </div>
          <div class="column text-center">
            <h3 class="sub-heading">负IOD事件</h3>
            <div class="content-image-wrapper">
              <img src="../assets/IOD/IOD负事件.png" alt="IOD负事件" class="content-image" />
            </div>
          </div>
        </div>
      </section>

      <!-- Section 3: IOD Impacts -->
      <section class="content-section">
        <h2 class="content-title text-center">IOD的影响</h2>
        <p class="main-text">IOD对气候的影响主要表现在以下几个方面：</p>
        <ul class="impact-list">
          <li><strong>降水变化：</strong>IOD事件会影响印度洋周边地区的降水模式。正IOD事件通常与印度洋西部地区的降水减少和东部地区的降水增加相关，而负IOD事件则相反。</li>
          <li><strong>季风系统：</strong>IOD事件会影响季风系统，从而影响南亚和东南亚地区的季风降水。</li>
          <li><strong>全球气候：</strong>IOD与全球其他气候现象如厄尔尼诺-南方涛动（El Niño-Southern Oscillation, ENSO）存在相互作用，进而影响全球气候。</li>
          <li><strong>极端天气事件：</strong>IOD事件可能会增加极端天气事件的发生频率，如干旱、洪水和热浪。</li>
        </ul>
        <div class="two-column-layout">
          <div class="column text-center">
            <h3 class="sub-heading">正IOD与厄尔尼诺对澳大利亚的影响</h3>
            <div class="content-image-wrapper">
              <img src="../assets/IOD/1982年-正IOD与厄尔尼诺对澳大利亚的影响.png" alt="正IOD与厄尔尼诺对澳大利亚的影响" class="content-image" />
            </div>
          </div>
          <div class="column text-center">
            <h3 class="sub-heading">负IOD与拉尼娜对澳大利亚的影响</h3>
            <div class="content-image-wrapper">
              <img src="../assets/IOD/2010年-负IOD与拉尼娜对澳大利亚的影响.png" alt="负IOD与拉尼娜对澳大利亚的影响" class="content-image" />
            </div>
          </div>
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // A more appropriate name for the component
  name: "iod_introduce"
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
.text-center {
  text-align: center;
}

/* 4. 标题样式 */
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
}
.sub-heading {
  color: #cbd5e1;
  font-size: 0.9em;
  font-weight: normal;
  margin-bottom: 16px;
  line-height: 1.4;
}

/* 5. 文本和列表样式 */
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin: 0 auto 1em auto;
  max-width: 80%; /* Center-aligned text shouldn't be too wide */
}
.impact-list {
  list-style: none;
  padding: 0;
  margin: 0 0 32px 0;
  font-size: 16px;
  color: #e0e7ff;
}
.impact-list li {
  padding-left: 25px;
  position: relative;
  margin-bottom: 12px;
}
.impact-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  top: 2px;
  color: #67e8f9; /* Cyan checkmark */
  font-weight: bold;
}
.impact-list li strong {
  color: #94a3b8;
}

/* 6. 两列布局 */
.two-column-layout {
  display: flex;
  justify-content: space-between;
  gap: 32px;
  align-items: flex-start;
  margin-top: 32px;
}
.column {
  width: 48%;
}

/* 7. 图片样式 */
.content-image-wrapper {
  margin-top: 24px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  background-color: rgba(255, 255, 255, 0.9);
}
.content-image-wrapper.large-image {
  max-width: 70%;
  margin-left: auto;
  margin-right: auto;
}
.content-image {
  width: 100%;
  height: auto;
  display: block;
}

/* --- 8. [核心] 覆盖子菜单样式 --- */
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