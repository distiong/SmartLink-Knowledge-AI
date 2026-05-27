<template>
  <div class="config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>系统参数配置</span>
          <el-button type="primary" @click="saveAllConfigs" :loading="saving">
            保存所有配置
          </el-button>
        </div>
      </template>

      <el-form v-loading="loading" label-width="150px">
        <el-divider content-position="left">大模型参数</el-divider>

        <el-form-item label="Temperature">
          <el-slider
            v-model="configs.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
          />
          <div class="form-tip">控制生成文本的随机性，值越高越随机</div>
        </el-form-item>

        <el-form-item label="Max Tokens">
          <el-input-number
            v-model="configs.max_tokens"
            :min="100"
            :max="8000"
            :step="100"
          />
          <div class="form-tip">单次生成的最大token数量</div>
        </el-form-item>

        <el-divider content-position="left">RAG检索参数</el-divider>

        <el-form-item label="Top K">
          <el-input-number
            v-model="configs.top_k"
            :min="1"
            :max="20"
            :step="1"
          />
          <div class="form-tip">向量检索返回的最相似文档数量</div>
        </el-form-item>

        <el-form-item label="相似度阈值">
          <el-slider
            v-model="configs.similarity_threshold"
            :min="0"
            :max="1"
            :step="0.05"
            show-input
          />
          <div class="form-tip">低于此阈值的检索结果将被过滤</div>
        </el-form-item>

        <el-divider content-position="left">文档分块参数</el-divider>

        <el-form-item label="分块大小">
          <el-input-number
            v-model="configs.chunk_size"
            :min="100"
            :max="2000"
            :step="50"
          />
          <div class="form-tip">文档分块的字符数</div>
        </el-form-item>

        <el-form-item label="分块重叠长度">
          <el-input-number
            v-model="configs.chunk_overlap"
            :min="0"
            :max="500"
            :step="10"
          />
          <div class="form-tip">相邻分块重叠的字符数</div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const loading = ref(false)
const saving = ref(false)

const configs = reactive({
  temperature: 0.7,
  max_tokens: 2000,
  top_k: 5,
  similarity_threshold: 0.7,
  chunk_size: 500,
  chunk_overlap: 50
})

onMounted(() => {
  fetchConfigs()
})

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await api.get('/system/config/list')
    const configList = res.data || []

    configList.forEach(item => {
      if (configs.hasOwnProperty(item.configKey)) {
        const value = item.configValue
        if (['temperature', 'similarity_threshold'].includes(item.configKey)) {
          configs[item.configKey] = parseFloat(value)
        } else if (['max_tokens', 'top_k', 'chunk_size', 'chunk_overlap'].includes(item.configKey)) {
          configs[item.configKey] = parseInt(value)
        } else {
          configs[item.configKey] = value
        }
      }
    })
  } catch (error) {
    console.error('获取配置失败:', error)
  } finally {
    loading.value = false
  }
}

const saveAllConfigs = async () => {
  saving.value = true
  try {
    const configData = {}
    Object.keys(configs).forEach(key => {
      configData[key] = String(configs[key])
    })

    await api.put('/system/config/batch', configData)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.config-container {
  max-width: 800px;
  margin: 0 auto;
}

.config-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
