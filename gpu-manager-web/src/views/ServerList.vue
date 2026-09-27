<template>
  <div>
    <AppHeader />

    <div class="page">
      <div class="page-header">
        <h2>GPU服务器列表</h2>

        <el-button
          v-if="isAdmin"
          type="primary"
          @click="openAddDialog"
        >
          新增服务器
        </el-button>
      </div>

      <el-table :data="servers" border>
        <el-table-column
          prop="id"
          label="ID"
          width="70"
        />

        <el-table-column
          prop="serverName"
          label="服务器名称"
        />

        <el-table-column
          prop="ipAddress"
          label="IP地址"
        />

        <el-table-column
          prop="gpuModel"
          label="GPU型号"
        />

        <el-table-column
          prop="gpuCount"
          label="GPU数量"
          width="100"
        />

        <el-table-column
          prop="status"
          label="状态"
          width="120"
        />

        <el-table-column
          prop="description"
          label="描述"
        />

        <el-table-column
          label="操作"
          width="260"
        >
          <template #default="scope">

            <!-- 普通用户也可以预约 -->
            <el-button
              type="success"
              size="small"
              @click="openReservation(scope.row)"
            >
              预约
            </el-button>

            <!-- 只有管理员能看到 -->
            <el-button
              v-if="isAdmin"
              type="primary"
              size="small"
              @click="openEditDialog(scope.row)"
            >
              修改
            </el-button>

            <el-button
              v-if="isAdmin"
              type="danger"
              size="small"
              @click="deleteServer(scope.row)"
            >
              删除
            </el-button>

          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ======================= -->
    <!-- 新增 / 修改服务器弹窗 -->
    <!-- ======================= -->

    <el-dialog
      v-model="serverDialogVisible"
      :title="editingServerId ? '修改服务器' : '新增服务器'"
      width="500px"
    >

      <el-form
        ref="serverFormRef"
        :model="serverForm"
        :rules="serverRules"
        label-width="100px"
      >

        <el-form-item
          label="服务器名称"
          prop="serverName"
        >
          <el-input v-model="serverForm.serverName" />
        </el-form-item>

        <el-form-item
          label="GPU型号"
          prop="gpuModel"
        >
          <el-input v-model="serverForm.gpuModel" />
        </el-form-item>

        <el-form-item
          label="GPU数量"
          prop="gpuCount"
        >
          <el-input-number
            v-model="serverForm.gpuCount"
            :min="1"
          />
        </el-form-item>

        <el-form-item
          label="状态"
          prop="status"
        >
          <el-select
            v-model="serverForm.status"
            style="width: 100%"
          >
            <el-option
              label="可用"
              value="AVAILABLE"
            />

            <el-option
              label="忙碌"
              value="BUSY"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="serverForm.description"
            type="textarea"
          />
        </el-form-item>

      </el-form>

      <template #footer>

        <el-button
          @click="serverDialogVisible = false"
        >
          取消
        </el-button>

        <el-button
          type="primary"
          @click="saveServer"
        >
          保存
        </el-button>

      </template>
    </el-dialog>


    <!-- ======================= -->
    <!-- 预约弹窗 -->
    <!-- ======================= -->

    <el-dialog
      v-model="reservationDialogVisible"
      title="预约 GPU"
      width="500px"
    >

      <el-form
        ref="reservationFormRef"
        :model="reservationForm"
        :rules="reservationRules"
        label-width="100px"
      >

        <el-form-item label="服务器">

          <el-input
            :model-value="
              selectedServer?.serverName
            "
            disabled
          />

        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">

          <el-date-picker
            v-model="reservationForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
          />

        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">

          <el-date-picker
            v-model="reservationForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
          />

        </el-form-item>

      </el-form>

      <template #footer>

        <el-button
          @click="reservationDialogVisible = false"
        >
          取消
        </el-button>

        <el-button
          type="primary"
          @click="submitReservation"
        >
          提交预约
        </el-button>

      </template>

    </el-dialog>

  </div>
</template>

<script setup>

import {
  ref,
  reactive,
  computed,
  onMounted
} from 'vue'

import {
  ElMessage,
  ElMessageBox
} from 'element-plus'

import AppHeader from '../components/AppHeader.vue'

import {
  getServers,
  addServer,
  updateServer,
  deleteServer as removeServer
} from '../api/server'

import {
  createReservation
} from '../api/reservation'


const serverFormRef = ref()

const serverRules = {
  serverName: [
    {
      required: true,
      message: '请输入服务器名称',
      trigger: 'blur'
    }
  ],

  gpuModel: [
    {
      required: true,
      message: '请输入GPU型号',
      trigger: 'blur'
    }
  ],

  gpuCount: [
    {
      required: true,
      message: '请输入GPU数量',
      trigger: 'change'
    }
  ],

  status: [
    {
      required: true,
      message: '请选择服务器状态',
      trigger: 'change'
    }
  ]
}


const reservationFormRef = ref()

const reservationRules = {
  startTime: [
    {
      required: true,
      message: '请选择开始时间',
      trigger: 'change'
    }
  ],

  endTime: [
    {
      required: true,
      message: '请选择结束时间',
      trigger: 'change'
    }
  ]
}

// =========================
// 当前用户
// =========================

const user = JSON.parse(
  localStorage.getItem('user') || '{}'
)

const isAdmin = computed(() => {
  return user.role === 'ADMIN'
})


// =========================
// 服务器列表
// =========================

const servers = ref([])

const loadServers = async () => {

  try {

    const res = await getServers()

    if (res.data.code === 200) {

      servers.value =
        res.data.data

    } else {

      ElMessage.error(
        res.data.message
      )

    }

  } catch (error) {

    ElMessage.error(
      '加载服务器失败'
    )

    console.error(error)

  }

}


// =========================
// 新增 / 修改服务器
// =========================

const serverDialogVisible =
  ref(false)

const editingServerId =
  ref(null)

const serverForm =
  reactive({

    serverName: '',

    ipAddress: '',

    gpuModel: '',

    gpuCount: 1,

    status: 'AVAILABLE',

    description: ''

  })


const resetServerForm = () => {

  serverForm.serverName = ''

  serverForm.ipAddress = ''

  serverForm.gpuModel = ''

  serverForm.gpuCount = 1

  serverForm.status =
    'AVAILABLE'

  serverForm.description = ''

}


const openAddDialog = () => {

  editingServerId.value =
    null

  resetServerForm()

  serverDialogVisible.value =
    true

}


const openEditDialog = (server) => {

  editingServerId.value =
    server.id

  serverForm.serverName =
    server.serverName

  serverForm.ipAddress =
    server.ipAddress

  serverForm.gpuModel =
    server.gpuModel

  serverForm.gpuCount =
    server.gpuCount

  serverForm.status =
    server.status

  serverForm.description =
    server.description

  serverDialogVisible.value =
    true

}


const saveServer = async () => {
  const valid = await serverFormRef.value
    .validate()
    .catch(() => false)

  if (!valid) {
    return
  }

  try {
    let res

    if (editingServerId.value) {
      res = await updateServer(
        editingServerId.value,
        serverForm
      )
    } else {
      res = await addServer(serverForm)
    }

    if (res.data.code === 200) {
      ElMessage.success(
        editingServerId.value
          ? '修改成功'
          : '新增成功'
      )

      serverDialogVisible.value = false
      await loadServers()

    } else {
      ElMessage.error(res.data.message)
    }

  } catch (error) {
    ElMessage.error(
      error.response?.data?.message
      || '保存失败'
    )
  }
}

// =========================
// 删除服务器
// =========================

const deleteServer = async (server) => {
  try {
    await ElMessageBox.confirm(
      `确定删除服务器 ${server.serverName} 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await removeServer(server.id)

    if (res.data.code === 200) {
      ElMessage.success('删除成功')

      await loadServers()
    } else {
      ElMessage.error(res.data.message)
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// =========================
// GPU预约
// =========================

const reservationDialogVisible =
  ref(false)

const selectedServer =
  ref(null)

const reservationForm =
  reactive({

    startTime: null,

    endTime: null

  })


const openReservation =
  (server) => {

    selectedServer.value =
      server

    reservationForm.startTime =
      null

    reservationForm.endTime =
      null

    reservationDialogVisible.value =
      true

  }


const formatDateTime =
  (date) => {

    const pad =
      n =>
        String(n).padStart(
          2,
          '0'
        )

    return (
      date.getFullYear()
      + '-'
      + pad(
        date.getMonth() + 1
      )
      + '-'
      + pad(
        date.getDate()
      )
      + 'T'
      + pad(
        date.getHours()
      )
      + ':'
      + pad(
        date.getMinutes()
      )
      + ':'
      + pad(
        date.getSeconds()
      )
    )

  }


const submitReservation = async () => {
  const valid = await reservationFormRef.value
    .validate()
    .catch(() => false)

  if (!valid) {
    return
  }

  const startTime =
    reservationForm.startTime

  const endTime =
    reservationForm.endTime

  const now = new Date()

  // 不能预约过去时间
  if (startTime < now) {
    ElMessage.warning(
      '开始时间不能早于当前时间'
    )

    return
  }

  // 结束时间必须晚于开始时间
  if (endTime <= startTime) {
    ElMessage.warning(
      '结束时间必须晚于开始时间'
    )

    return
  }

  try {
    const res = await createReservation({
      serverId: selectedServer.value.id,

      startTime:
        formatDateTime(startTime),

      endTime:
        formatDateTime(endTime)
    })

    if (res.data.code === 200) {
      ElMessage.success('预约成功')

      reservationDialogVisible.value =
        false

    } else {
      ElMessage.error(
        res.data.message
      )
    }

  } catch (error) {
    ElMessage.error(
      error.response?.data?.message
      || '预约失败'
    )

    console.error(error)
  }
}


onMounted(() => {

  loadServers()

})

</script>


<style scoped>

.page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content:
    space-between;
  align-items: center;
  margin-bottom: 20px;
}

</style>