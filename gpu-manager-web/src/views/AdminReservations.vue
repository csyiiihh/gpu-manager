<template>
  <div>
    <AppHeader />

    <div class="page">
      <div class="page-header">
        <h2>预约管理</h2>
      </div>

      <!-- 查询条件 -->
      <el-form
        :inline="true"
        :model="query"
        class="search-form"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="query.username"
            placeholder="输入用户名"
            clearable
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            placeholder="全部状态"
            clearable
            style="width: 160px"
          >
            <el-option
              label="进行中"
              value="ACTIVE"
            />

            <el-option
              label="已取消"
              value="CANCELLED"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
          >
            查询
          </el-button>

          <el-button
            @click="handleReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        :data="records"
        border
        v-loading="loading"
      >
        <el-table-column
          prop="id"
          label="预约ID"
          width="90"
        />

        <el-table-column
          prop="username"
          label="用户名"
        />

        <el-table-column
          prop="name"
          label="姓名"
        />

        <el-table-column
          prop="serverName"
          label="服务器"
        />

        <el-table-column
          prop="gpuModel"
          label="GPU型号"
        />

        <el-table-column
          prop="startTime"
          label="开始时间"
          width="180"
        />

        <el-table-column
          prop="endTime"
          label="结束时间"
          width="180"
        />

        <el-table-column
          prop="status"
          label="状态"
          width="110"
        >
          <template #default="scope">
            <el-tag
              v-if="scope.row.status === 'ACTIVE'"
              type="success"
            >
              ACTIVE
            </el-tag>

            <el-tag
              v-else
              type="info"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadReservations"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  reactive,
  ref,
  onMounted
} from 'vue'

import { ElMessage } from 'element-plus'

import AppHeader from '../components/AppHeader.vue'
import {
  getAdminReservations
} from '../api/reservation'

const records = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  page: 1,
  size: 10,
  username: '',
  status: ''
})

const loadReservations = async () => {
  loading.value = true

  try {
    const params = {
      page: query.page,
      size: query.size
    }

    if (query.username) {
      params.username = query.username
    }

    if (query.status) {
      params.status = query.status
    }

    const res = await getAdminReservations(params)

    if (res.data.code === 200) {
      const data = res.data.data

      records.value = data.records
      total.value = data.total
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error(
      error.response?.data?.message
      || '加载预约失败'
    )

    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadReservations()
}

const handleReset = () => {
  query.username = ''
  query.status = ''
  query.page = 1

  loadReservations()
}

const handleSizeChange = () => {
  query.page = 1
  loadReservations()
}

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.page {
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>