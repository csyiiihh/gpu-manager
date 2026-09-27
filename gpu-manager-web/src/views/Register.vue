<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>注册账号</h2>

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
          label="姓名"
          prop="name"
        >
          <el-input v-model="form.name" />
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
          style="width: 100%"
          @click="handleRegister"
        >
          注册
        </el-button>

        <el-button
          style="width: 100%; margin-top: 10px; margin-left: 0"
          @click="router.push('/login')"
        >
          返回登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  register
} from '../api/user'

const router = useRouter()

const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  name: ''
})

const rules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    }
  ],

  name: [
    {
      required: true,
      message: '请输入姓名',
      trigger: 'blur'
    }
  ],

  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      min: 6,
      message: '密码长度不能少于6位',
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value
    .validate()
    .catch(() => false)

  if (!valid) {
    return
  }

  try {
    const res = await register(form)

    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录')

      router.push('/login')
    } else {
      ElMessage.error(res.data.message)
    }

  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-card {
  width: 400px;
}
</style>