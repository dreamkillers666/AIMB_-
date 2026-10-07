<template>
  <el-container class="admin-layout admin-dark">

    <el-aside :width="sideWidth + 'px'" class="admin-aside">
      <Aside :isCollapse="isCollapse" :logoTextShow="logoTextShow"/>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <Header :collapseBtnClass="collapseBtnClass" @asideCollapse="collapse" :user="user" />
      </el-header>

      <el-main class="admin-main">
        <router-view @refreshUser="getUser"/>
      </el-main>

    </el-container>
  </el-container>
</template>

<script>

import Aside from "@/components/Aside";
import Header from "@/components/Header";

export default {
  name: 'Home',
  data() {
    return {
      collapseBtnClass: 'el-icon-s-fold',
      isCollapse: false,
      sideWidth: 200,
      logoTextShow: true,
      user: {}
    }
  },
  components: {
    Aside,
    Header
  },
  created() {
    // 从后台获取最新的User数据
    this.getUser()
  },
  methods: {
    collapse() {
      this.isCollapse = !this.isCollapse
      if (this.isCollapse) {
        this.sideWidth = 64
        this.collapseBtnClass = 'el-icon-s-unfold'
        this.logoTextShow = false
      } else {
        this.sideWidth = 200
        this.collapseBtnClass = 'el-icon-s-fold'
        this.logoTextShow = true
      }
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
.admin-layout {
  min-height: 100vh;
}
.admin-aside {
  background-color: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-right: 1px solid rgba(100, 116, 139, 0.3);
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.3);
  transition: width 0.3s ease;
}
.admin-header {
  border-bottom: 1px solid rgba(100, 116, 139, 0.3) !important;
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.admin-main {
  padding: 20px;
}
</style>
