<template>
  <!-- 1. 给最外层容器添加一个 class -->
  <div class="user-layout">
    <!-- 2. 头部区域：移除所有内联样式，改用 class -->
    <div class="user-header">
      <div class="logo-area" @click="$router.push('/usermanage/introduce')">
        <div class="logo-img-wrapper">
          <img src="../assets/AIMB.jpg" alt="Logo" class="logo-img">
        </div>
        <div class="logo-title">人工智能气候预测系统</div>
      </div>

      <div class="menu-container">
        <!-- Element UI 菜单组件 -->
        <el-menu :default-active="activeMenu" class="el-menu-dark-theme" mode="horizontal" router>
          <el-menu-item index="/usermanage/introduce"><i class="el-icon-house"></i>简介</el-menu-item>
          <el-menu-item index="/usermanage/forecastenso"><i class="el-icon-data-line"></i>ENSO监测</el-menu-item>
          <el-submenu index="2">
            <template slot="title"><i class="el-icon-cloudy"></i>气候现象</template>
            <el-menu-item index="/usermanage/enso_introduce">ENSO</el-menu-item>
            <el-menu-item index="/usermanage/mjo_introduce">MJO</el-menu-item>
            <el-menu-item index="/usermanage/iod_introduce">IOD</el-menu-item>
            <el-menu-item index="/usermanage/seasonal_introduce">次季节降水</el-menu-item>
            <el-menu-item index="/usermanage/others_introduce">其它</el-menu-item>
          </el-submenu>
          <el-menu-item index="/usermanage/enso_pre"><i class="el-icon-s-data"></i>预测模型</el-menu-item>
          <el-submenu index="4">
            <template slot="title"><i class="el-icon-folder-opened"></i>数据</template>
            <el-menu-item index="/usermanage/user_model">模型数据</el-menu-item>
            <el-menu-item index="/usermanage/user_gridData">气候数据展示</el-menu-item>
            <el-menu-item index="/usermanage/data_introduction">数据集介绍</el-menu-item>
          </el-submenu>
          <el-menu-item index="/usermanage/feedback"><i class="el-icon-chat-line-square"></i>用户反馈</el-menu-item>
          <el-menu-item index="/usermanage/connect"><i class="el-icon-phone-outline"></i>联系方式</el-menu-item>
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

    <!-- 悬浮气泡 -->
    <div ref="fabContainer" class="fab-container" :class="{ open: fabOpen }">
      <transition name="fab-panel">
        <div v-show="fabOpen" class="fab-panel" @click.stop>
          <div class="fab-item" @click="navigateTo('/usermanage/ai')">
            <i class="el-icon-help"></i>
            <span>AI 助手</span>
          </div>
          <div class="fab-item" @click="navigateTo('/usermanage/im')">
            <i class="el-icon-chat-dot-round"></i>
            <span>聊天室</span>
          </div>
        </div>
      </transition>
      <div class="fab-bubble" @click.stop="toggleFab">
        <i :class="fabOpen ? 'el-icon-close' : 'el-icon-chat-dot-round'"></i>
      </div>
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
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      fabOpen: false,
      outsideClickHandler: null,
    }
  },
  computed: {
    activeMenu() {
      const path = this.$route.path;
      if (path === "/usermanage/introduce") return "/usermanage/introduce";
      if (path === "/usermanage/forecastenso") return "/usermanage/forecastenso";
      if ([
        "/usermanage/enso_introduce",
        "/usermanage/mjo_introduce",
        "/usermanage/iod_introduce",
        "/usermanage/seasonal_introduce",
        "/usermanage/others_introduce",
      ].includes(path)) return "2";
      if ([
        "/usermanage/enso_pre",
        "/usermanage/mjo_pre",
        "/usermanage/rainfall_pre",
        "/usermanage/convlstm_pre",
      ].includes(path)) return "/usermanage/enso_pre";
      if ([
        "/usermanage/user_model",
        "/usermanage/user_gridData",
        "/usermanage/data_introduction",
      ].includes(path)) return "4";
      if (path === "/usermanage/feedback") return "/usermanage/feedback";
      if (path === "/usermanage/connect") return "/usermanage/connect";
      return path;
    },
  },
  mounted() {
    this.bindOutsideClick();
  },
  beforeDestroy() {
    this.unbindOutsideClick();
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
    toggleFab() {
      this.fabOpen = !this.fabOpen;
    },
    navigateTo(path) {
      this.fabOpen = false;
      if (this.$route.path !== path) {
        this.$router.push(path);
      }
    },
    bindOutsideClick() {
      this.outsideClickHandler = (e) => {
        const fab = this.$refs.fabContainer;
        if (fab && !fab.contains(e.target)) {
          this.fabOpen = false;
        }
      };
      document.addEventListener("click", this.outsideClickHandler);
    },
    unbindOutsideClick() {
      if (this.outsideClickHandler) {
        document.removeEventListener("click", this.outsideClickHandler);
        this.outsideClickHandler = null;
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
  /* 吸顶常驻：滚动时始终可见 */
  position: sticky;
  top: 0;
  z-index: 1000;
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
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.3s ease;
}
.logo-area:hover {
  opacity: 0.9;
}
.logo-img-wrapper {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.logo-area:hover .logo-img-wrapper {
  transform: scale(1.08);
  box-shadow: 0 0 18px rgba(103, 232, 249, 0.45);
}
.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  vertical-align: middle;
  filter: drop-shadow(0 0 6px rgba(103, 232, 249, 0.4));
  transition: filter 0.3s ease;
}
.logo-area:hover .logo-img {
  filter: drop-shadow(0 0 10px rgba(103, 232, 249, 0.65));
}
.logo-title {
  font-size: 1.1rem;
  font-weight: bold;
  background: linear-gradient(90deg, #58a6ff 0%, #67e8f9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: transform 0.3s ease, filter 0.3s ease;
}
.logo-area:hover .logo-title {
  transform: translateX(2px);
  filter: drop-shadow(0 0 8px rgba(88, 166, 255, 0.4));
}

/* 4. 菜单容器 */
.menu-container {
  flex: 1;
  overflow-x: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.menu-container::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}

/* 菜单项图标间距 */
::v-deep .el-menu-dark-theme .el-menu-item [class^="el-icon-"],
::v-deep .el-menu-dark-theme .el-submenu__title [class^="el-icon-"] {
  margin-right: 5px;
  font-size: 15px;
  vertical-align: middle;
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
  width: 100%;
  max-width: 1200px;
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

/* 悬浮气泡 */
.fab-container {
  position: fixed;
  left: 24px;
  bottom: 80px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.fab-bubble {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #ffffff;
  font-size: 22px;
  background: rgba(20, 30, 50, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(88, 166, 255, 0.4);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.35),
    0 0 16px rgba(88, 166, 255, 0.25),
    inset 0 0 12px rgba(103, 232, 249, 0.08);
  transition: all 0.3s ease;
  animation: fabPulse 2.4s ease-in-out infinite;
}

.fab-bubble:hover {
  transform: scale(1.1);
  box-shadow:
    0 6px 28px rgba(0, 0, 0, 0.4),
    0 0 28px rgba(103, 232, 249, 0.45),
    inset 0 0 16px rgba(103, 232, 249, 0.12);
  border-color: rgba(103, 232, 249, 0.65);
}

.fab-container.open .fab-bubble {
  animation: none;
  transform: rotate(90deg);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.35),
    0 0 20px rgba(103, 232, 249, 0.35);
}

.fab-container.open .fab-bubble:hover {
  transform: rotate(90deg) scale(1.1);
}

@keyframes fabPulse {
  0%, 100% {
    box-shadow:
      0 4px 20px rgba(0, 0, 0, 0.35),
      0 0 16px rgba(88, 166, 255, 0.25),
      inset 0 0 12px rgba(103, 232, 249, 0.08);
  }
  50% {
    box-shadow:
      0 4px 24px rgba(0, 0, 0, 0.38),
      0 0 28px rgba(103, 232, 249, 0.45),
      inset 0 0 16px rgba(103, 232, 249, 0.14);
  }
}

.fab-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 8px;
}

.fab-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  background: rgba(20, 30, 50, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(88, 166, 255, 0.35);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  color: #e0e7ff;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s ease;
}

.fab-item:hover {
  background: rgba(30, 45, 75, 0.9);
  border-color: rgba(103, 232, 249, 0.65);
  color: #ffffff;
  transform: translateX(4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.35), 0 0 14px rgba(103, 232, 249, 0.25);
}

.fab-item i {
  font-size: 18px;
  color: #67e8f9;
}

/* 展开面板过渡动画 */
.fab-panel-enter-active,
.fab-panel-leave-active {
  transition: all 0.25s ease;
}

.fab-panel-enter,
.fab-panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.92);
}

/* 小屏响应式 */
@media (max-width: 1199px) {
  .user-header {
    padding: 0 16px;
  }
  .logo-area {
    width: 220px;
    flex-shrink: 0;
  }
  .logo-title {
    font-size: 1rem;
  }
  .menu-container {
    overflow-x: auto;
  }
  ::v-deep .el-menu.el-menu-dark-theme {
    flex-wrap: nowrap !important;
    min-width: max-content;
  }
  .fab-container {
    left: 16px;
    bottom: 72px;
  }
}
</style>
