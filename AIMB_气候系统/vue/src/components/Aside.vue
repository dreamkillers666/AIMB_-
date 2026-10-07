<template>
  <el-menu :default-openeds="['1', '3']" class="admin-aside-menu"
           :collapse-transition="false"
           :collapse="isCollapse"
           router
  >
    <div class="logo-section">
      <div class="logo-img-wrapper">
        <img src="../assets/AIMB.jpg" alt="Logo" class="logo-img">
      </div>
      <div class="logo-text" v-show="logoTextShow">人工智能气候预测系统</div>
    </div>

    <el-menu-item index="/enso/overview"><i class="el-icon-house"></i><span slot="title">ENSO 总览</span></el-menu-item>
    <el-menu-item index="/enso/forecast"><i class="el-icon-data-line"></i><span slot="title">预测产品</span></el-menu-item>
    <el-menu-item index="/enso/replay"><i class="el-icon-time"></i><span slot="title">历史回放</span></el-menu-item>
    <el-menu-item index="/enso/validation"><i class="el-icon-s-check"></i><span slot="title">提前期验证</span></el-menu-item>
    <el-menu-item index="/enso/explainability"><i class="el-icon-view"></i><span slot="title">物理解释</span></el-menu-item>
    <el-menu-item index="/enso/data-model"><i class="el-icon-folder-opened"></i><span slot="title">数据与模型</span></el-menu-item>

  </el-menu>
</template>

<script>
export default {
  name: "Aside",
  props: {
    isCollapse: Boolean,
    logoTextShow: Boolean
  }
}
</script>

<style scoped>
.logo-section {
  padding: 16px 0;
  text-align: center;
}
.logo-img-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
}
.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  filter: drop-shadow(0 0 6px rgba(103, 232, 249, 0.4));
  transition: filter 0.3s ease;
}
.logo-text {
  color: #cbd5e1;
  font-size: 13px;
  font-weight: bold;
  margin-top: 8px;
  padding: 0 8px;
  line-height: 1.4;
  background: linear-gradient(90deg, #58a6ff 0%, #67e8f9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 菜单透明背景，文字浅色 */
.admin-aside-menu {
  min-height: 100%;
  overflow-x: hidden;
  border-right: none !important;
  background-color: transparent !important;
}
.admin-aside-menu >>> .el-menu-item,
.admin-aside-menu >>> .el-submenu__title {
  color: #cbd5e1 !important;
}
.admin-aside-menu >>> .el-menu-item:hover,
.admin-aside-menu >>> .el-submenu__title:hover {
  background-color: rgba(88, 166, 255, 0.1) !important;
  color: #fff !important;
}
.admin-aside-menu >>> .el-menu-item.is-active {
  color: #67e8f9 !important;
  background-color: rgba(103, 232, 249, 0.08) !important;
  border-right: 3px solid #67e8f9;
}
.admin-aside-menu >>> .el-submenu__title i,
.admin-aside-menu >>> .el-menu-item i {
  color: inherit !important;
}
/* 折叠时隐藏图标后的文字，居中图标 */
.admin-aside-menu >>> .el-menu--collapse {
  width: 64px;
}
.admin-aside-menu >>> .el-menu--collapse .logo-section {
  padding: 16px 0;
}
</style>
