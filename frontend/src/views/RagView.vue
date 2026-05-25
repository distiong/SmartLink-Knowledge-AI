<template>
  <div class="rag-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="upload-card">
          <template #header>
            <span>文档上传</span>
          </template>
          
          <div class="upload-section">
            <input type="file" ref="fileInput" @change="handleFileChange" 
                   accept=".pdf,.doc,.docx,.txt,.xls,.xlsx,.csv" style="display:none" />
            <el-button type="primary" @click="triggerFileInput" :loading="uploading" style="width:100%">
              <el-icon><UploadFilled /></el-icon>
              选择文件上传
            </el-button>
            <div class="upload-tip">
              支持 PDF、Word、Excel、TXT 格式文件，最大10MB
            </div>
          </div>
          
          <el-divider />
          
          <div class="document-list">
            <div class="doc-header">
              <h4>已上传文档</h4>
              <el-button size="small" @click="fetchDocuments">刷新</el-button>
            </div>
            <el-table :data="documentList" size="small" max-height="400">
              <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
              <el-table-column prop="createTime" label="上传时间" width="150">
                <template #default="{ row }">
                  {{ formatTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60">
                <template #default="{ row }">
                  <el-button size="small" type="danger" text @click="deleteDocument(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="chat-card">
          <template #header>
            <span>RAG知识库问答</span>
          </template>
          
          <div class="chat-messages" ref="messagesRef">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
            >
              <div class="message-content">
                <div class="message-avatar">
                  <el-avatar :size="40">
                    {{ msg.role === 'user' ? '我' : 'AI' }}
                  </el-avatar>
                </div>
                <div class="message-text">
                  <div class="message-bubble">{{ msg.content }}</div>
                  <div v-if="msg.context" class="message-context">
                    <el-divider content-position="left">引用资料</el-divider>
                    <div class="context-content">{{ msg.context }}</div>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="loading" class="message ai-message">
              <div class="message-content">
                <div class="message-avatar">
                  <el-avatar :size="40">AI</el-avatar>
                </div>
                <div class="message-text">
                  <div class="message-bubble loading-bubble">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    正在检索知识库...
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="inputQuestion"
              placeholder="请输入您的问题，AI将基于知识库回答..."
              :rows="3"
              type="textarea"
              @keyup.enter.ctrl="sendQuestion"
            />
            <el-button
              type="primary"
              :loading="loading"
              @click="sendQuestion"
              class="send-btn"
            >
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Loading, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'
import axios from 'axios'

const messages = ref([])
const inputQuestion = ref('')
const loading = ref(false)
const uploading = ref(false)
const messagesRef = ref(null)
const fileInput = ref(null)
const documentList = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }
  
  uploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/rag/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('文档上传成功')
      fetchDocuments()
      // 清空文件输入
      event.target.value = ''
    } else {
      ElMessage.error(response.data.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传错误:', error)
    ElMessage.error(error.response?.data?.msg || '文档上传失败')
  } finally {
    uploading.value = false
  }
}

const fetchDocuments = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/document/list', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.code === 200) {
      documentList.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取文档列表失败:', error)
  }
}

const deleteDocument = async (doc) => {
  try {
    await ElMessageBox.confirm(`确定删除文档 ${doc.fileName}？`, '提示')
    
    const token = localStorage.getItem('token')
    await axios.delete(`/api/document/delete?id=${doc.id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('删除成功')
    fetchDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const sendQuestion = async () => {
  if (!inputQuestion.value.trim() || loading.value) return
  
  const question = inputQuestion.value.trim()
  messages.value.push({ role: 'user', content: question })
  inputQuestion.value = ''
  loading.value = true
  scrollToBottom()
  
  try {
    const res = await api.get('/rag/chat', {
      params: { question }
    })
    messages.value.push({
      role: 'ai',
      content: res.data.answer,
      context: res.data.context
    })
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，请求失败，请稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.rag-container {
  height: calc(100vh - 140px);
}

.upload-card {
  height: 100%;
}

.upload-section {
  text-align: center;
}

.upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.document-list {
  margin-top: 10px;
}

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.doc-header h4 {
  margin: 0;
  color: #606266;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.message {
  margin-bottom: 20px;
}

.message-content {
  display: flex;
  gap: 12px;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.message-text {
  max-width: 80%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.user-message .message-bubble {
  background-color: #409eff;
  color: white;
}

.ai-message .message-bubble {
  background-color: #f4f4f5;
  color: #303133;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-context {
  margin-top: 12px;
}

.context-content {
  background-color: #ebeef5;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  max-height: 150px;
  overflow-y: auto;
}

.chat-input {
  margin-top: 16px;
}

.send-btn {
  margin-top: 12px;
  width: 100%;
}
</style>
