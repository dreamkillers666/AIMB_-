<template>
  <!-- 1. 给最外层容器添加一个 class -->
  <div class="user-layout">
    <!-- 2. 头部区域：移除所有内联样式，改用 class -->
    <div class="user-header">
      <div class="logo-area">
        <div class="logo-img-wrapper">
          <img src="../assets/AIMB.jpg" alt="Logo" class="logo-img">
        </div>
        <div class="logo-title">人工智能气候预测系统</div>
      </div>

      <div class="menu-container">
        <!-- Element UI 菜单组件 -->
        <el-menu :default-active="'1'" class="el-menu-dark-theme" mode="horizontal" router>
          <el-menu-item index="/usermanage/introduce">简介</el-menu-item>
          <el-menu-item index="/usermanage/forecastenso">enso</el-menu-item>
          <el-submenu index="2">
            <template slot="title">气候现象</template>
            <el-menu-item index="/usermanage/enso_introduce">ENSO</el-menu-item>
            <el-menu-item index="/usermanage/mjo_introduce">MJO</el-menu-item>
            <el-menu-item index="/usermanage/iod_introduce">IOD</el-menu-item>
            <el-menu-item index="/usermanage/seasonal_introduce">次季节降水</el-menu-item>
            <el-menu-item index="/usermanage/others_introduce">其它</el-menu-item>
          </el-submenu>
          <el-menu-item index="/usermanage/enso_pre">预测模型</el-menu-item>
          <el-submenu index="4">
            <template slot="title">数据</template>
            <el-menu-item index="/usermanage/user_model">模型数据</el-menu-item>
            <el-menu-item index="/usermanage/user_gridData">气候数据展示</el-menu-item>
            <el-menu-item index="/usermanage/data_introduction">数据集介绍</el-menu-item>
          </el-submenu>
          <el-menu-item index="/usermanage/feedback">用户反馈</el-menu-item>
          <el-menu-item index="/usermanage/connect">联系方式</el-menu-item>
          <el-menu-item index="/usermanage/im"><i class="el-icon-chat-dot-round"></i>聊天室</el-menu-item>
          <el-menu-item index="/usermanage/ai"><i class="el-icon-help"></i>AI</el-menu-item>
        </el-menu>
      </div>

      <div class="user-info-area">
        <el-dropdown class="user-dropdown" placement="bottom-end">
          <div class="dropdown-trigger">
            <img :src="user.avatarUrl" alt="Avatar" class="user-avatar">
            <span>{{ user.username }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
          </div>
          <el-dropdown-menu slot="dropdown" class="dropdown-menu-dark">
            <el-dropdown-item><router-link to="/usermanage/person">个人信息</router-link></el-dropdown-item>
            <el-dropdown-item><router-link to="/usermanage/password">修改密码</router-link></el-dropdown-item>
            <el-dropdown-item divided><span @click="logout">退出</span></el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <router-view @refreshUser="getUser"/>
    </div>

    <!-- 页脚 -->
    <el-footer class="user-footer">
      <span>Copyright © 2024 Institute of Artificial Intelligence and Meteorological Big Data. All rights reserved.</span>
    </el-footer>
  </div>
</template>

<script>
// --- Script 部分保持不变 ---
export default {
  name: "User_Manage",
  // ... 其他所有 script 内容 ...
  data() {
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {}
    }
  },
  methods: {
    logout() {
      this.$router.push("/login")
      localStorage.removeItem("user")
      this.$message.success("退出成功")
    },
    getUser() {
      let workId = localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")).workId : ""
      if (workId) {
        this.request.get("/user/workId/" + workId).then(res => {
          this.user = res.data
        })
      }
    },
  }
}
</script>

<style scoped>
/* --- 全新的深色主题样式 --- */

/* 1. 整体布局 */
.user-layout {
  min-height: 100vh;
  background-color: transparent; /* 透出 App.vue 的背景 */
  display: flex;
  flex-direction: column;
}

/* 2. 头部容器 */
.user-header {
  display: flex;
  height: 60px;
  line-height: 60px;
  /* 应用与后台布局一致的“毛玻璃”效果 */
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 2px 12px rgba(88, 166, 255, 0.1);
  border-bottom: 1px solid rgba(100, 116, 139, 0.3);
  padding: 0 30px;
  flex-shrink: 0; /* 防止容器被压缩 */
}

/* 3. Logo 和标题区域 */
.logo-area {
  width: 300px;
  display: flex;
  align-items: center;
  font-size: 1.1rem;
  font-weight: bold;
  color: #e0e7ff; /* 明亮的文字颜色 */
}
.logo-img-wrapper {
  width: 50px;
}
.logo-img {
  width: 40px;
  vertical-align: middle;
}

/* 4. 菜单容器 */
.menu-container {
  flex: 1;
}

/* 5. 用户信息区域 */
.user-info-area {
  width: 200px;
  text-align: right;
}
.dropdown-trigger {
  display: inline-block;
  cursor: pointer;
  color: #cbd5e1;
}
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  vertical-align: middle;
  margin-right: 8px;
}

/* 6. 主内容区域 */
.main-content {
  width: 1200px; /* 您可以根据需要调整内容宽度 */
  margin: 24px auto; /* 上下边距，并水平居中 */
  flex: 1; /* 占据剩余所有空间 */
}

/* 7. 页脚 */
.user-footer {
  height: 40px !important;
  line-height: 40px;
  text-align: center;
  background-color: #0c0c1e; /* 使用更深的背景色 */
  color: #64748b; /* 较暗的文字颜色 */
  font-size: 12px;
  flex-shrink: 0;
}

/* --- 8. [核心] 覆盖 Element UI 子组件样式 --- */

/* 菜单主题 */
::v-deep .el-menu.el-menu-dark-theme {
  background-color: transparent !important;
  border-bottom: none !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item,
::v-deep .el-menu.el-menu-dark-theme .el-submenu__title {
  color: #cbd5e1 !important;
  background-color: transparent !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item:hover,
::v-deep .el-menu.el-menu-dark-theme .el-submenu__title:hover {
  background-color: rgba(88, 166, 255, 0.1) !important;
  color: #fff !important;
}
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active {
  color: #67e8f9 !important; /* 高亮青色 */
  border-bottom: 2px solid #67e8f9 !important;
}

/* 下拉菜单主题 */
::v-deep .el-dropdown-menu.dropdown-menu-dark {
  background-color: #1e293b !important;
  border: 1px solid rgba(100, 116, 139, 0.5) !important;
}
::v-deep .dropdown-menu-dark .el-dropdown-menu__item,
::v-deep .dropdown-menu-dark .el-dropdown-menu__item a,
::v-deep .dropdown-menu-dark .el-dropdown-menu__item span {
  color: #cbd5e1 !important;
  text-decoration: none;
}
::v-deep .dropdown-menu-dark .el-dropdown-menu__item:hover {
  background-color: rgba(88, 166, 255, 0.1) !important;
}
::v-deep .dropdown-menu-dark .el-dropdown-menu__item--divided {
  border-top-color: rgba(100, 116, 139, 0.5) !important;
}
</style>