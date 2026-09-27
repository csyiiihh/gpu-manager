<template>
  <div class="header">
    <div class="title">
      GPU Manager
    </div>

    <div class="nav">
      <el-button
        text
        @click="router.push('/servers')"
      >
        GPU服务器
      </el-button>

      <el-button
        text
        @click="router.push('/reservations/my')"
      >
        我的预约
      </el-button>

      <el-button
        type="danger"
        plain
        @click="logout"
      >
        退出登录
      </el-button>

      <el-button
        v-if="isAdmin"
        text
        @click="router.push('/admin/reservations')"
      >
        预约管理
      </el-button>      
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const router = useRouter()

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')

  router.push('/login')
}

const user = JSON.parse(
  localStorage.getItem('user') || '{}'
)

const isAdmin = computed(() => {
  return user.role === 'ADMIN'
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.nav {
  display: flex;
  gap: 10px;
}
</style>