<template>
  <div>
    <AppHeader />

    <div style="padding: 24px">
      <h2>我的预约</h2>

      <el-table :data="reservations">
        <el-table-column prop="id" label="预约ID" width="90" />
        <el-table-column prop="serverName" label="服务器名称" />
        <el-table-column prop="gpuModel" label="GPU型号" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="status" label="状态" />

        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              :disabled="scope.row.status !== 'ACTIVE'"
              @click="cancelReservation(scope.row.id)"
            >
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import AppHeader from '../components/AppHeader.vue'
import {
  getMyReservations,
  cancelReservation as cancelReservationApi
} from '../api/reservation'

const reservations = ref([])

const loadReservations = async () => {
  try {
    const res = await getMyReservations()

    if (res.data.code === 200) {
      reservations.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('加载预约记录失败')
    console.error(error)
  }
}

const cancelReservation = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这条预约吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await cancelReservationApi(id)

    if (res.data.code === 200) {
      ElMessage.success('取消成功')

      await loadReservations()
    } else {
      ElMessage.error(res.data.message)
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadReservations()
})
</script>