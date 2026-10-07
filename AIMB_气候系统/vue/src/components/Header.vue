<template>
  <div class="admin-header-inner">
    <div class="header-left">
      <span :class="collapseBtnClass" class="collapse-btn" @click="collapse"></span>
      <el-breadcrumb separator="/" class="admin-breadcrumb">
        <el-breadcrumb-item :to="'/home'">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentPathName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-dropdown class="user-dropdown" placement="bottom-end">
      <div class="dropdown-trigger">
        <img :src="user.avatarUrl" alt="" class="user-avatar">
        <span class="user-name">{{ user.username }}</span>
        <i class="el-icon-arrow-down"></i>
      </div>
      <el-dropdown-menu slot="dropdown" class="dropdown-menu-dark">
        <el-dropdown-item>
          <router-link to="/person" class="dropdown-link">个人信息</router-link>
        </el-dropdown-item>
        <el-dropdown-item>
          <router-link to="/password" class="dropdown-link">修改密码</router-link>
        </el-dropdown-item>
        <el-dropdown-item divided>
          <span class="dropdown-link" @click="logout">退出</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  name: "Header",
  props: {
    collapseBtnClass: String,
    user: Object
  },
  computed: {
    currentPathName () {
      return this.$store.state.currentPathName;
    },
  },
  methods: {
    logout() {
      this.$store.commit("logout")
      this.$message.success("退出成功")
    },
    collapse() {
      this.$emit("asideCollapse")
    }
  }
}
</script>

<style scoped>
.admin-header-inner {
  line-height: 60px;
  display: flex;
  align-items: center;
  height: 60px;
}
.header-left {
  flex: 1;
  display: flex;
  align-items: center;
}
.collapse-btn {
  cursor: pointer;
  font-size: 18px;
  color: #cbd5e1;
  transition: color 0.3s ease;
}
.collapse-btn:hover {
  color: #67e8f9;
}
.admin-breadcrumb {
  display: inline-block;
  margin-left: 10px;
}
.user-dropdown {
  width: 150px;
  cursor: pointer;
  text-align: right;
}
.dropdown-trigger {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  color: #cbd5e1;
}
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 8px;
}
.user-name {
  color: #cbd5e1;
  transition: color 0.3s ease;
}
.dropdown-trigger:hover .user-name {
  color: #67e8f9;
}
.dropdown-trigger .el-icon-arrow-down {
  margin-left: 5px;
  color: #64748b;
  transition: color 0.3s ease;
}
.dropdown-trigger:hover .el-icon-arrow-down {
  color: #67e8f9;
}
.dropdown-link {
  text-decoration: none;
  color: #cbd5e1 !important;
  display: block;
}
.dropdown-link:hover {
  color: #67e8f9 !important;
}
.dropdown-menu-dark {
  background-color: #1e293b !important;
  border: 1px solid rgba(100, 116, 139, 0.5) !important;
}
.dropdown-menu-dark >>> .el-dropdown-menu__item {
  color: #cbd5e1 !important;
}
.dropdown-menu-dark >>> .el-dropdown-menu__item:hover {
  background-color: rgba(88, 166, 255, 0.1) !important;
}
.dropdown-menu-dark >>> .el-dropdown-menu__item--divided {
  border-top-color: rgba(100, 116, 139, 0.5) !important;
}
</style>
