<script setup lang="ts">
import { ref } from 'vue'

const agreements = ref([
  { id: '1', name: '学生兼职协议', type: 'student', count: 15, status: 'active' },
  { id: '2', name: '企业入驻协议', type: 'enterprise', count: 1, status: 'active' },
  { id: '3', name: '保密协议', type: 'secret', count: 8, status: 'active' },
  { id: '4', name: '服务协议', type: 'service', count: 20, status: 'active' }
])

const selectedAgreement = ref(null as any)

const viewDetail = (agreement: any) => {
  selectedAgreement.value = agreement
}
</script>

<template>
  <div class="agreement">
    <div class="page-header">
      <h1>协议管理</h1>
    </div>

    <div class="agreement-list">
      <div 
        class="agreement-card" 
        v-for="agreement in agreements" 
        :key="agreement.id"
        @click="viewDetail(agreement)"
      >
        <div class="card-header">
          <span class="agreement-name">{{ agreement.name }}</span>
          <el-tag type="success">生效中</el-tag>
        </div>
        <div class="card-body">
          <div class="stat">签署数量: <span>{{ agreement.count }}</span></div>
        </div>
      </div>
    </div>

    <el-dialog :title="selectedAgreement?.name" v-model="selectedAgreement" width="800px">
      <div class="agreement-detail">
        <h3>协议内容预览</h3>
        <div class="preview-content">
          <p>甲方：企业名称</p>
          <p>乙方：学生姓名</p>
          <p>根据《中华人民共和国劳动合同法》及相关法律法规，甲乙双方经友好协商，就兼职工作事宜达成如下协议：</p>
          <p>一、工作内容：乙方同意按照甲方安排从事兼职工作...</p>
          <p>二、工作时间：根据双方约定执行...</p>
          <p>三、劳动报酬：甲方按约定支付乙方劳动报酬...</p>
          <p>四、双方权利义务...</p>
          <p>五、违约责任...</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.agreement {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.agreement-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.agreement-card {
  background-color: #FFFFFF;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.agreement-name {
  font-size: 16px;
  font-weight: bold;
  color: #1F2329;
}

.card-body {
  padding-top: 12px;
  border-top: 1px solid #F2F3F5;
}

.stat {
  font-size: 14px;
  color: #86909C;

  span {
    font-weight: bold;
    color: #1F2329;
  }
}

.agreement-detail {
  padding: 20px;
}

.preview-content {
  padding: 20px;
  background-color: #F8F9FA;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.8;
  color: #4E5969;
}
</style>