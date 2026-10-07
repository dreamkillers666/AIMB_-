<template>
  <div class="auth-wrapper">
    <WindCanvas />
    <div class="auth-card">
      <div class="auth-header">
        <img src="../assets/AIMB.jpg" alt="Logo" class="auth-logo">
        <div class="auth-title">人工智能气候预测系统</div>
        <div class="auth-subtitle">Artificial Intelligence &amp; Meteorological Big Data</div>
      </div>
      <el-form :model="user" :rules="rules" ref="userForm" class="auth-form">
        <el-form-item prop="username">
          <el-input prefix-icon="el-icon-user" v-model="user.username" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input prefix-icon="el-icon-lock" show-password v-model="user.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input prefix-icon="el-icon-lock" show-password v-model="user.confirmPassword" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item prop="workId">
          <el-input prefix-icon="el-icon-document" v-model="user.workId" placeholder="请输入工号"></el-input>
        </el-form-item>
        <el-form-item prop="type">
          <el-select v-model="user.type" placeholder="请选择身份" style="width: 100%">
            <el-option label="管理员" value="管理员"></el-option>
            <el-option label="用户" value="用户"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="auth-actions">
          <el-button type="primary" @click="register">注册</el-button>
          <el-button type="warning" @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import WindCanvas from "@/components/WindCanvas";

export default {
  name: "Register",
  components: { WindCanvas },
  data() {
    return {
      user: {},
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 10, message: '长度在 2 到 5 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        workId: [
          { required: true, message: '请输入工号', trigger: 'blur' },
          { min: 5, max: 20, message: '长度在 5 到 10 个字符', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择用户类型', trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    register() {
      this.$refs['userForm'].validate((valid) => {
        if (valid) {
          if (this.user.password !== this.user.confirmPassword) {
            this.$message.error("两次输入的密码不一致")
            return false
          }
          this.request.post("/user/register", this.user).then(res => {
            if(res.code === '200') {
              this.$message.success("注册成功")
              this.$router.push("/login")
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      });
    }
  }
}
</script>

<style scoped>
.auth-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a, #0c0c1e);
  position: relative;
  overflow: hidden;
}
/* 顶部径向光晕 */
.auth-wrapper::before {
  content: '';
  position: absolute;
  width: 700px;
  height: 700px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(88, 166, 255, 0.10) 0%, transparent 70%);
  top: -250px;
  left: -150px;
  z-index: 0;
}
.auth-wrapper::after {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(103, 232, 249, 0.08) 0%, transparent 70%);
  bottom: -200px;
  right: -100px;
  z-index: 0;
}
.auth-card {
  width: 400px;
  padding: 36px 36px 24px;
  border-radius: 16px;
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(100, 116, 139, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35);
  position: relative;
  z-index: 1;
}
.auth-header {
  text-align: center;
  margin-bottom: 24px;
}
.auth-logo {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  filter: drop-shadow(0 0 8px rgba(103, 232, 249, 0.4));
}
.auth-title {
  font-size: 1.25rem;
  font-weight: bold;
  background: linear-gradient(90deg, #58a6ff 0%, #67e8f9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-top: 12px;
}
.auth-subtitle {
  font-size: 0.75rem;
  color: #64748b;
  margin-top: 4px;
}
.auth-form >>> .el-input__inner {
  background-color: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(100, 116, 139, 0.5);
  color: #e0e7ff;
  height: 42px;
}
.auth-form >>> .el-input__inner::placeholder {
  color: #64748b;
}
.auth-form >>> .el-input__inner:focus {
  border-color: #58a6ff;
}
.auth-form >>> .el-input__prefix {
  color: #64748b;
}
.auth-form >>> .el-select .el-input__inner {
  color: #e0e7ff;
}
.auth-actions {
  text-align: right;
  margin-bottom: 0;
}
.auth-actions >>> .el-button {
  padding: 10px 20px;
  font-weight: 500;
}
</style>
