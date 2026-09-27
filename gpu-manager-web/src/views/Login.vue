<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>GPU Manager 登录</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item
          label="密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            type="password"
            show-password
          />
        </el-form-item>

        <el-button
            type="primary"
            @click="handleLogin"
            style="width: 100%"
        >
          登录
        </el-button>

        <el-button
          style="width: 100%; margin-top: 10px; margin-left: 0"
          @click="router.push('/register')"
        >
          注册账号
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  login,
  getCurrentUser
} from '../api/user'
import { ref, reactive } from 'vue'

const router = useRouter()

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  const valid = await formRef.value.validate()
    .catch(() => false)

  if (!valid) {
    return
  }

  try {
    const res = await login(form)

    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data)

      const userRes = await getCurrentUser()

      if (userRes.data.code === 200) {
        localStorage.setItem(
          'user',
          JSON.stringify(userRes.data.data)
        )
      }

      ElMessage.success('登录成功')

      router.push('/servers')
    } else {
      ElMessage.error(res.data.message)
    }

  } catch (error) {
    console.error(error)
  }
}

const formRef = ref()

const rules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    }
  ],

  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    }
  ]
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
}
</style>