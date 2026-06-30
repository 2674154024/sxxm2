<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
</script>

<template>
  <div class="about-page">
    <NavBar title="关于我们" show-back />
    <div class="about-content">
      <div class="logo-section">
        <div class="logo">
          <span class="logo-icon">🎓</span>
        </div>
        <h1 class="app-name">长沙大学生兼职平台</h1>
        <p class="version">版本 v1.0.0</p>
      </div>

      <div class="info-section">
        <div class="info-item">
          <span class="info-label">平台介绍</span>
          <span class="info-value">长沙大学生兼职平台是专注于长沙地区大学生的兼职服务平台，致力于为大学生提供安全、靠谱、免费的兼职信息服务。</span>
        </div>
      </div>

      <div class="feature-section">
        <h3 class="section-title">平台特色</h3>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-icon">🛡️</span>
            <span class="feature-text">严格审核 安全可靠</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💰</span>
            <span class="feature-text">薪资托管 担保发放</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🎯</span>
            <span class="feature-text">精准匹配 就近推荐</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💬</span>
            <span class="feature-text">实时沟通 快速响应</span>
          </div>
        </div>
      </div>

      <div class="contact-section">
        <h3 class="section-title">联系我们</h3>
        <div class="contact-item">
          <span class="contact-label">客服邮箱</span>
          <span class="contact-value">service@csparttime.com</span>
        </div>
        <div class="contact-item">
          <span class="contact-label">工作时间</span>
          <span class="contact-value">9:00 - 18:00（工作日）</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.about-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
}

.about-content {
  padding: var(--spacing-base);
}

.logo-section {
  text-align: center;
  padding: var(--spacing-xl) 0;
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  margin-bottom: var(--spacing-sm);
}

.logo {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #4080ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-base);
}

.logo-icon {
  font-size: 36px;
}

.app-name {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.version {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: 0;
}

.info-section {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 8px;
}

.info-value {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.feature-section {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 var(--spacing-base);
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-sm);
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-base) 0;
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.feature-text {
  font-size: var(--font-size-sm);
  color: var(--color-text);
}

.contact-section {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
}

.contact-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border);

  &:last-child {
    border-bottom: none;
  }
}

.contact-label {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.contact-value {
  font-size: var(--font-size-base);
  color: var(--color-text);
}
</style>
