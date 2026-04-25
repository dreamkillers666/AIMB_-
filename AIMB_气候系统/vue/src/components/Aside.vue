<template>
  <el-menu :default-openeds="['1', '3']" style="min-height: 100%; overflow-x: hidden"
           background-color="rgb(48, 65, 86)"
           text-color="#fff"
           active-text-color="#ffd04b"
           :collapse-transition="false"
           :collapse="isCollapse"
           router
  >
    <div style="height: 60px; line-height: 60px; text-align: center">
      <img src="../assets/AIMB.jpg" alt="" style="width: 30%; position: relative; top: 5px; right: 5px">

    </div>
    <div style="height: 60px; line-height: 60px; text-align: center">
        <b style="color: white" v-show="logoTextShow">人工智能气候预测系统</b>
    </div>
    <el-menu-item index="/home">
        <i class="el-icon-house"></i>
        <span slot="title">主页</span>
    </el-menu-item>

    <el-submenu index="1">
      <template slot="title">
        <i class="el-icon-menu"></i>
        <span slot="title">用户管理模块</span>
      </template>
      <el-menu-item index="/user">
        <i class="el-icon-s-custom"></i>
        <span slot="title">个人账户管理</span>
      </el-menu-item>

    </el-submenu>

    <el-submenu index="2">
      <template slot="title">
        <i class="el-icon-menu"></i>
        <span slot="title">数据管理模块</span>
      </template>
      <el-menu-item index="/model">
        <i class="el-icon-date"></i>
        <span slot="title">模型数据管理</span>
      </el-menu-item>

      <el-menu-item index="/grid_data">
        <i class="el-icon-document"></i>
        <span slot="title">上传数据管理</span>
      </el-menu-item>
      <el-menu-item index="/enso_data">
        <i class="el-icon-document"></i>
        <span slot="title">气候数据集管理</span>
      </el-menu-item>

    </el-submenu>

    <el-submenu index="3">
      <template slot="title">
        <i class="el-icon-menu"></i>
        <span slot="title">辅助模块</span>
      </template>
      <el-menu-item index="/feedback">
        <i class="el-icon-s-ticket"></i>
        <span slot="title">用户反馈</span>
      </el-menu-item>

      <el-menu-item index="/announcement">
        <i class="el-icon-notebook-1"></i>
        <span slot="title">公告管理</span>
      </el-menu-item>
      <el-menu-item index="/file">
        <i class="el-icon-notebook-1"></i>
        <span slot="title">文件管理</span>
      </el-menu-item>

    </el-submenu>

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

</style>