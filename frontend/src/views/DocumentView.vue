<template>
  <div class="document-container">
    <el-card class="document-card">
      <template #header>
        <div class="card-header">
          <span>文档管理</span>
          <el-button type="primary" @click="refreshList">刷新列表</el-button>
        </div>
      </template>
      
      <el-table
        :data="documents"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名称" min-width="200" />
        <el-table-column prop="fileType" label="文件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getFileTypeTag(row.fileType)">
              {{ row.fileType?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              详情
            </el-button>
            <el-button type="danger" link @click="deleteDocument(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && documents.length === 0" description="暂无文档" />
    </el-card>
    
    <el-dialog
      v-model="detailVisible"
      :title="currentDoc?.fileName"
      width="600px"
    >
      <div class="doc-detail">
        <p><strong>文件类型：</strong>{{ currentDoc?.fileType }}</p>
        <p><strong>上传时间：</strong>{{ currentDoc?.createTime }}</p>
        <el-divider />
        <div class="doc-content">
          <h4>文档内容预览：</h4>
          <div class="content-text">{{ currentDoc?.content?.substring(0, 2000) }}...</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const documents = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentDoc = ref(null)

onMounted(() => {
  fetchDocuments()
})

const fetchDocuments = async () => {
  loading.value = true
  try {
    const res = await api.get('/document/list')
    documents.value = res.data || []
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  fetchDocuments()
}

const getFileTypeTag = (type) => {
  const map = {
    pdf: 'danger',
    docx: 'primary',
    txt: 'info'
  }
  return map[type?.toLowerCase()] || ''
}

const viewDetail = (doc) => {
  currentDoc.value = doc
  detailVisible.value = true
}

const deleteDocument = async (doc) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档 "${doc.fileName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.delete('/document/delete', {
      params: { id: doc.id }
    })
    
    ElMessage.success('文档删除成功')
    fetchDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('文档删除失败')
    }
  }
}
</script>

<style scoped>
.document-container {
  max-width: 1200px;
  margin: 0 auto;
}

.document-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doc-detail p {
  margin-bottom: 8px;
}

.doc-content {
  margin-top: 16px;
}

.doc-content h4 {
  margin-bottom: 12px;
  color: #303133;
}

.content-text {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.6;
  font-size: 14px;
}
</style>