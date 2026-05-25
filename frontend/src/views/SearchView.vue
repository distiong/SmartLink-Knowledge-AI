<template>
  <div class="search-container">
    <el-card class="search-card">
      <template #header>
        <span>向量语义检索</span>
      </template>
      
      <div class="search-input">
        <el-input
          v-model="searchQuery"
          placeholder="输入检索内容..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :loading="loading" @click="handleSearch">
              <el-icon><Search /></el-icon>
              检索
            </el-button>
          </template>
        </el-input>
        
        <div class="search-options">
          <span>返回数量：</span>
          <el-input-number v-model="topK" :min="1" :max="20" size="small" />
        </div>
      </div>
      
      <div class="search-results" v-if="results.length > 0">
        <el-divider content-position="left">检索结果</el-divider>
        
        <div v-for="(result, index) in results" :key="index" class="result-item">
          <div class="result-header">
            <el-tag type="info">片段 {{ index + 1 }}</el-tag>
            <el-tag type="success">相似度: {{ (result.score * 100).toFixed(2) }}%</el-tag>
            <el-tag>文档ID: {{ result.documentId }}</el-tag>
          </div>
          <div class="result-content">
            {{ result.text }}
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="searched" description="未找到相关结果" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const searchQuery = ref('')
const topK = ref(5)
const results = ref([])
const loading = ref(false)
const searched = ref(false)

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入检索内容')
    return
  }
  
  loading.value = true
  searched.value = true
  
  try {
    const res = await api.get('/rag/search', {
      params: {
        query: searchQuery.value.trim(),
        topK: topK.value
      }
    })
    results.value = res.data || []
  } catch (error) {
    ElMessage.error('检索失败')
    results.value = []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.search-container {
  max-width: 1000px;
  margin: 0 auto;
}

.search-card {
  min-height: calc(100vh - 140px);
}

.search-input {
  margin-bottom: 20px;
}

.search-options {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.search-results {
  margin-top: 20px;
}

.result-item {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.result-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.result-content {
  color: #303133;
  line-height: 1.6;
  font-size: 14px;
}
</style>