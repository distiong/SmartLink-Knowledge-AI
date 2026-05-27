<template>
  <div class="log-container">
    <el-card class="log-card">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <div class="header-actions">
            <el-select
              v-model="filters.operationType"
              placeholder="操作类型"
              clearable
              style="width: 120px; margin-right: 10px"
            >
              <el-option label="登录" value="登录" />
              <el-option label="登出" value="登出" />
              <el-option label="上传" value="上传" />
              <el-option label="删除" value="删除" />
              <el-option label="权限变更" value="权限变更" />
            </el-select>
            <el-date-picker
              v-model="filters.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 350px; margin-right: 10px"
            />
            <el-button type="primary" @click="fetchLogs">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="logs"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作账号" width="120" />
        <el-table-column prop="operation" label="操作内容" min-width="200" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <el-pagination
        v-if="total > 0"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />

      <el-empty v-if="!loading && logs.length === 0" description="暂无日志记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const logs = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const filters = ref({
  operationType: '',
  dateRange: null
})

onMounted(() => {
  fetchLogs()
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    if (filters.value.operationType) {
      params.operationType = filters.value.operationType
    }

    if (filters.value.dateRange && filters.value.dateRange.length === 2) {
      params.startTime = filters.value.dateRange[0]
      params.endTime = filters.value.dateRange[1]
    }

    const res = await api.get('/system/log/list', { params })
    const data = res.data
    logs.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    operationType: '',
    dateRange: null
  }
  currentPage.value = 1
  fetchLogs()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchLogs()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchLogs()
}

const getOperationTypeTag = (type) => {
  const map = {
    '登录': 'success',
    '登出': 'info',
    '上传': 'primary',
    '删除': 'danger',
    '权限变更': 'warning'
  }
  return map[type] || ''
}
</script>

<style scoped>
.log-container {
  max-width: 1200px;
  margin: 0 auto;
}

.log-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>
