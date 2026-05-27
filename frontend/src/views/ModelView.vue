<template>
  <div class="model-container">
    <el-card class="model-card">
      <template #header>
        <div class="card-header">
          <span>模型管理</span>
          <el-button type="primary" @click="showAddDialog">新增模型</el-button>
        </div>
      </template>

      <el-table
        :data="models"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="modelName" label="模型名称" min-width="150" />
        <el-table-column prop="apiUrl" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="API Key" min-width="150">
          <template #default="{ row }">
            <span>{{ maskApiKey(row.apiKey) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button
              :type="row.enabled === 1 ? 'warning' : 'success'"
              link
              @click="toggleModel(row)"
            >
              {{ row.enabled === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="deleteModel(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && models.length === 0" description="暂无模型配置" />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模型' : '新增模型'"
      width="500px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="模型名称" required>
          <el-input v-model="formData.modelName" placeholder="例如: GPT-4、通义千问" />
        </el-form-item>
        <el-form-item label="API地址" required>
          <el-input v-model="formData.apiUrl" placeholder="例如: https://api.openai.com/v1" />
        </el-form-item>
        <el-form-item label="API Key" required>
          <el-input v-model="formData.apiKey" placeholder="请输入API Key" show-password />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="formData.enabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const models = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editingId = ref(null)

const formData = ref({
  modelName: '',
  apiUrl: '',
  apiKey: '',
  enabled: 1
})

onMounted(() => {
  fetchModels()
})

const fetchModels = async () => {
  loading.value = true
  try {
    const res = await api.get('/system/model/list')
    models.value = res.data || []
  } catch (error) {
    ElMessage.error('获取模型列表失败')
  } finally {
    loading.value = false
  }
}

const maskApiKey = (apiKey) => {
  if (!apiKey) return '未配置'
  if (apiKey.length <= 8) return '****'
  return apiKey.substring(0, 4) + '****' + apiKey.substring(apiKey.length - 4)
}

const showAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  formData.value = {
    modelName: '',
    apiUrl: '',
    apiKey: '',
    enabled: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editingId.value = row.id
  formData.value = {
    modelName: row.modelName,
    apiUrl: row.apiUrl,
    apiKey: row.apiKey,
    enabled: row.enabled
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.value.modelName.trim()) {
    ElMessage.warning('请输入模型名称')
    return
  }
  if (!formData.value.apiUrl.trim()) {
    ElMessage.warning('请输入API地址')
    return
  }
  if (!formData.value.apiKey.trim()) {
    ElMessage.warning('请输入API Key')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await api.put(`/system/model/${editingId.value}`, formData.value)
      ElMessage.success('模型配置更新成功')
    } else {
      await api.post('/system/model', formData.value)
      ElMessage.success('模型配置创建成功')
    }
    dialogVisible.value = false
    fetchModels()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleModel = async (row) => {
  try {
    await api.put(`/system/model/${row.id}/toggle`)
    ElMessage.success(row.enabled === 1 ? '模型已禁用' : '模型已启用')
    fetchModels()
  } catch (error) {
    ElMessage.error('切换模型状态失败')
  }
}

const deleteModel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模型 "${row.modelName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.delete(`/system/model/${row.id}`)
    ElMessage.success('模型配置删除成功')
    fetchModels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.model-container {
  max-width: 1200px;
  margin: 0 auto;
}

.model-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
