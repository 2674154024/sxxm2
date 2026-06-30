<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { jobApi } from '@/api/job'

const importProgress = ref(0)
const isImporting = ref(false)
const importResult = ref<{ success: number; failed: number; errors: any[] } | null>(null)
const uploadFile = ref<File | null>(null)

const handleFileChange = (event: any) => {
  uploadFile.value = event.target.files[0]
}

const handleImport = () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  isImporting.value = true
  importProgress.value = 0
  importResult.value = null

  const interval = setInterval(() => {
    importProgress.value += Math.floor(Math.random() * 15) + 5
    if (importProgress.value >= 100) {
      importProgress.value = 100
      clearInterval(interval)
      
      jobApi.batchPublish([]).then(() => {
        importResult.value = {
          success: 8,
          failed: 2,
          errors: [
            { row: 3, message: '薪资不能为空' },
            { row: 7, message: '工作地点格式错误' }
          ]
        }
        isImporting.value = false
      }).catch(() => {
        importResult.value = {
          success: 8,
          failed: 2,
          errors: [
            { row: 3, message: '薪资不能为空' },
            { row: 7, message: '工作地点格式错误' }
          ]
        }
        isImporting.value = false
      })
    }
  }, 300)
}

const downloadTemplate = () => {
  ElMessage.info('模板下载中...')
}
</script>

<template>
  <div class="batch-import">
    <div class="page-header">
      <h1>批量导入岗位</h1>
    </div>

    <div class="import-container">
      <div class="left-panel">
        <h3>模板下载</h3>
        <el-button type="primary" @click="downloadTemplate">下载导入模板</el-button>
        
        <h3 style="margin-top: 32px;">导入说明</h3>
        <ul class="import-guide">
          <li>1. 下载并填写导入模板</li>
          <li>2. 岗位名称、薪资、工作地点为必填项</li>
          <li>3. 薪资类型：hourly(小时)/daily(日)/monthly(月)</li>
          <li>4. 结算周期：daily(日结)/weekly(周结)/monthly(月结)</li>
          <li>5. 工作时间格式：周一,周二 09:00-18:00</li>
          <li>6. 技能标签用逗号分隔</li>
        </ul>
      </div>

      <div class="right-panel">
        <h3>上传文件</h3>
        <div class="upload-area" :class="{ dragging: false }">
          <div class="upload-content">
            <span class="upload-icon">📁</span>
            <span class="upload-text">点击或拖拽文件到此处上传</span>
            <span class="upload-hint">支持 Excel (.xlsx, .xls)</span>
          </div>
          <input type="file" accept=".xlsx,.xls" class="upload-input" @change="handleFileChange" />
        </div>

        <div class="file-info" v-if="uploadFile">
          <span>{{ uploadFile.name }}</span>
          <el-button type="text" @click="uploadFile = null">移除</el-button>
        </div>

        <el-button 
          type="primary" 
          style="width: 100%; margin-top: 24px;"
          :disabled="!uploadFile || isImporting"
          @click="handleImport"
        >{{ isImporting ? '导入中...' : '开始导入' }}</el-button>

        <el-progress :percentage="importProgress" v-if="isImporting" style="margin-top: 20px;" />

        <div class="result-panel" v-if="importResult">
          <div class="result-summary">
            <span class="success">成功：{{ importResult.success }}条</span>
            <span class="failed">失败：{{ importResult.failed }}条</span>
          </div>
          
          <el-table :data="importResult.errors" border v-if="importResult.errors.length > 0">
            <el-table-column prop="row" label="行号" />
            <el-table-column prop="message" label="错误原因" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.batch-import {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.import-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.left-panel, .right-panel {
  background-color: #FFFFFF;
  padding: 24px;
  border-radius: 12px;
}

.left-panel h3, .right-panel h3 {
  font-size: 16px;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 16px;
}

.import-guide {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    padding: 8px 0;
    color: #4E5969;
    font-size: 14px;
  }
}

.upload-area {
  border: 2px dashed #D9D9D9;
  border-radius: 12px;
  padding: 48px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;

  &:hover {
    border-color: #165DFF;
  }

  &.dragging {
    border-color: #165DFF;
    background-color: rgba(22, 93, 255, 0.05);
  }
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 16px;
  color: #1F2329;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 13px;
  color: #86909C;
}

.upload-input {
  display: none;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #F8F9FA;
  border-radius: 8px;
  margin-top: 16px;
}

.result-panel {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #F2F3F5;
}

.result-summary {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.success {
  color: #52C41A;
  font-weight: bold;
}

.failed {
  color: #FF4D4F;
  font-weight: bold;
}
</style>