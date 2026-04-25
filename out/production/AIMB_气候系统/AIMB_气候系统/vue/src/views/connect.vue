<template>
  <!-- 1. The main container is our standard dark theme card -->
  <el-card class="dark-theme-card">

    <!-- 2. A single content wrapper -->
    <div class="content-wrapper">

      <section class="content-section">
        <h2 class="content-title">联系方式</h2>
        <p class="main-text">
          亲爱的小伙伴你好！本网站致力于探索人工智能和气象大数据的交叉应用，分享气象科学和人工智能领域的前沿技术和最新研究成果，为读者提供气象预测、气候变化、灾害预警等方面的知识，并介绍人工智能和气象大数据在促进社会发展、加强生态环境保护等方面的应用案例。
        </p>
        <p class="main-text">
          有什么可以帮助你的，可以微信联系我！直接搜 <strong class="highlight-text">zxz_311</strong> 添加微信！
        </p>
        <p class="main-text">
          交流学习添加时请注明公司名（或学校名）+方向！商务合作添加时请注明商务合作！否则将不予通过，谢谢理解和配合！
        </p>

        <div class="content-image-wrapper">
          <img src="../assets/公众号.png" alt="公众号二维码" class="content-image" />
        </div>

        <!-- Styled call-to-action box -->
        <div class="call-to-action">
          如想了解更多研究内容，欢迎大家关注上方公众号！
        </div>
      </section>

    </div>
  </el-card>
</template>

<script>
export default {
  // A more appropriate name for the component
  name: "contactPage"
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

/* 2. 内容区域 */
.content-wrapper {
  line-height: 1.8;
  text-align: center; /* Center align content for a contact page */
}
.content-section {
  max-width: 600px; /* Constrain width for better readability */
  margin: 0 auto;
}
.content-title {
  color: #58a6ff;
  margin-bottom: 24px;
}

/* 3. 文本样式 */
.main-text {
  display: block;
  font-size: 16px;
  color: #e0e7ff;
  margin-bottom: 1.5em;
  text-align: justify;
}
.highlight-text {
  color: #67e8f9; /* High-contrast cyan for important info */
  font-weight: bold;
}

/* 4. 图片样式 */
.content-image-wrapper {
  margin: 24px auto;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(100, 116, 139, 0.2);
  background-color: rgba(255, 255, 255, 0.95); /* Light background for the QR code */
  max-width: 300px; /* Control size of the QR code */
}
.content-image {
  width: 100%;
  height: auto;
  display: block;
}

/* 5. [核心] 最后的呼吁/高亮框样式 */
.call-to-action {
  margin-top: 32px;
  padding: 16px 24px;
  background-color: rgba(88, 116, 255, 0.1);
  border-left: 4px solid #58a6ff;
  border-radius: 8px;
  color: #e0e7ff;
  font-size: 1em;
  font-weight: bold;
  text-align: center;
}
</style>