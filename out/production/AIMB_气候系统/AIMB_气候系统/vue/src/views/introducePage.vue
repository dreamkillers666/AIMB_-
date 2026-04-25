<template>
  <!-- The page-container can remain for structure, but its background is now controlled globally -->
  <div class="page-container">
    <!-- Apply our reusable dark theme card class -->
    <el-card class="dark-theme-card">
      <!-- Main content area -->
      <div class="content-wrapper">

        <h2 class="content-title">概要</h2>
        <span class="main-text">
          气候预测是指通过收集、分析和解释历史气象资料和其他相关数据，利用气象、海洋、大气、水文和地球物理等学科知识，运用数值模型和统计方法，预测和估计未来气候变化的科学活动。气候预测主要分为短期预测和长期预测两种类型。
          <br/><br/>
          短期预测通常指对未来数天到几周的气候变化进行预测。其中，ENSO预测是一种重要的短期预测。ENSO现象是指热带太平洋东部海域海温和大气压力变化的相互作用，具有周期性和不规则性。ENSO预测主要是通过分析海洋温度、海洋表层高度、大气压力、风速、降水等多个因素的变化，来确定ENSO现象的强度、发展趋势和可能的影响。ENSO预测可提供重要的气候信息，对于相关领域的决策和规划具有重要的参考价值。
          <br/><br/>
          长期气候预测主要是针对几个月到几年的时间尺度进行预测。长期预测依赖于全球气候模式，以及大气、海洋、陆面和冰雪等系统的相互作用，通过对这些因素的分析和模拟，预测未来气候的趋势和变化。长期气候预测对于应对气候变化和环境保护等方面具有重要的意义。
        </span>

        <h2 class="content-title">研究主题</h2>
        <div class="topics-container">

          <div class="theme-item">
            <h3>ENSO预测</h3>
            <div class="image-wrapper">
              <img src="../assets/ENSO/elnino-banner.png" alt="ENSO预测" class="theme-image" />
            </div>
          </div>

          <div class="theme-item">
            <h3>MJO预测</h3>
            <div class="image-wrapper">
              <img src="../assets/MJO/mjo.png" alt="MJO预测" class="theme-image" />
            </div>
          </div>

          <div class="theme-item">
            <h3>IOD预测</h3>
            <div class="image-wrapper" style="height:75px">
              <img src="../assets/IOD/Sstanom_199711_krig.jpg" alt="IOD预测" class="theme-image" />
            </div>
          </div>

        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "introducePage"
}
</script>

<style scoped>
/* --- 统一的深色主题样式 --- */

/* 1. 页面容器 (可选，主要用于提供外边距) */
.page-container {
  /* 移除了所有背景样式，现在由 App.vue 控制 */
  padding: 0; /* 内边距由卡片自己控制 */
}

/* 2. 主卡片容器 */
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
  padding: 32px 40px;
}


/* 3. 内容区域 */
.content-wrapper {
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

/* 5. 文本样式 */
.main-text {
  display: block;
  font-size: 18px;
  color: #e0e7ff;
  margin-bottom: 1em;
}

/* 6. 研究主题区域 */
.topics-container {
  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  margin-top: 20px;
  gap: 20px; /* 使用 gap 属性创建间距 */
}

.theme-item {
  text-align: center;
  width: 33.33%;
}
.theme-item h3 {
  color: #58a6ff;
  margin-bottom: 12px;
}

/* 7. 图片样式 */
.image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.3);
}

.theme-image {
  width: 100%;
  height: auto;
  display: block;
}
</style>