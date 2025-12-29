<template>
  <div class="settings-page">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-desc">管理平台的基础配置和系统参数</p>
    </div>

    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 基础设置 -->
      <el-tab-pane label="基础设置" name="basic">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>平台信息</span>
            </div>
          </template>
          <el-form :model="basicForm" label-width="120px" class="settings-form">
            <el-form-item label="平台名称">
              <el-input v-model="basicForm.platformName" placeholder="请输入平台名称" />
            </el-form-item>
            <el-form-item label="平台Logo">
              <el-upload
                class="logo-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                @change="handleLogoChange"
              >
                <img v-if="basicForm.logoUrl" :src="basicForm.logoUrl" class="logo-preview" />
                <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="平台描述">
              <el-input v-model="basicForm.description" type="textarea" :rows="3" placeholder="请输入平台描述" />
            </el-form-item>
            <el-form-item label="版权信息">
              <el-input v-model="basicForm.copyright" placeholder="请输入版权信息" />
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="basicForm.icpNumber" placeholder="请输入ICP备案号" />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>联系方式</span>
            </div>
          </template>
          <el-form :model="basicForm" label-width="120px" class="settings-form">
            <el-form-item label="客服邮箱">
              <el-input v-model="basicForm.supportEmail" placeholder="请输入客服邮箱" />
            </el-form-item>
            <el-form-item label="客服电话">
              <el-input v-model="basicForm.supportPhone" placeholder="请输入客服电话" />
            </el-form-item>
            <el-form-item label="技术支持">
              <el-input v-model="basicForm.techSupport" placeholder="请输入技术支持联系方式" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>登录安全</span>
            </div>
          </template>
          <el-form :model="securityForm" label-width="140px" class="settings-form">
            <el-form-item label="登录失败锁定">
              <el-switch v-model="securityForm.loginLockEnabled" />
              <span class="form-tip">开启后，连续登录失败将锁定账号</span>
            </el-form-item>
            <el-form-item label="失败次数上限" v-if="securityForm.loginLockEnabled">
              <el-input-number v-model="securityForm.maxFailedAttempts" :min="3" :max="10" />
              <span class="form-tip">次</span>
            </el-form-item>
            <el-form-item label="锁定时长" v-if="securityForm.loginLockEnabled">
              <el-input-number v-model="securityForm.lockDuration" :min="5" :max="1440" />
              <span class="form-tip">分钟</span>
            </el-form-item>
            <el-form-item label="密码强度要求">
              <el-select v-model="securityForm.passwordStrength">
                <el-option label="低 - 6位以上" value="low" />
                <el-option label="中 - 8位以上，含字母和数字" value="medium" />
                <el-option label="高 - 10位以上，含大小写字母、数字和特殊字符" value="high" />
              </el-select>
            </el-form-item>
            <el-form-item label="密码有效期">
              <el-input-number v-model="securityForm.passwordExpireDays" :min="0" :max="365" />
              <span class="form-tip">天，0表示永不过期</span>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>会话管理</span>
            </div>
          </template>
          <el-form :model="securityForm" label-width="140px" class="settings-form">
            <el-form-item label="会话超时时间">
              <el-input-number v-model="securityForm.sessionTimeout" :min="5" :max="1440" />
              <span class="form-tip">分钟</span>
            </el-form-item>
            <el-form-item label="单点登录">
              <el-switch v-model="securityForm.singleSignOn" />
              <span class="form-tip">开启后，同一账号只能在一处登录</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- API 设置 -->
      <el-tab-pane label="API 设置" name="api">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>网关配置</span>
            </div>
          </template>
          <el-form :model="apiForm" label-width="140px" class="settings-form">
            <el-form-item label="网关地址">
              <el-input v-model="apiForm.gatewayUrl" placeholder="请输入网关地址" />
            </el-form-item>
            <el-form-item label="默认超时时间">
              <el-input-number v-model="apiForm.defaultTimeout" :min="1" :max="300" />
              <span class="form-tip">秒</span>
            </el-form-item>
            <el-form-item label="签名有效期">
              <el-input-number v-model="apiForm.signatureExpire" :min="60" :max="600" />
              <span class="form-tip">秒</span>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>限流配置</span>
            </div>
          </template>
          <el-form :model="apiForm" label-width="140px" class="settings-form">
            <el-form-item label="全局限流">
              <el-switch v-model="apiForm.globalRateLimitEnabled" />
            </el-form-item>
            <el-form-item label="每秒请求数" v-if="apiForm.globalRateLimitEnabled">
              <el-input-number v-model="apiForm.globalQps" :min="100" :max="100000" :step="100" />
              <span class="form-tip">QPS</span>
            </el-form-item>
            <el-form-item label="单IP限流">
              <el-switch v-model="apiForm.ipRateLimitEnabled" />
            </el-form-item>
            <el-form-item label="单IP每秒请求" v-if="apiForm.ipRateLimitEnabled">
              <el-input-number v-model="apiForm.ipQps" :min="10" :max="10000" :step="10" />
              <span class="form-tip">QPS</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 通知设置 -->
      <el-tab-pane label="通知设置" name="notification">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>邮件配置</span>
            </div>
          </template>
          <el-form :model="notificationForm" label-width="140px" class="settings-form">
            <el-form-item label="SMTP服务器">
              <el-input v-model="notificationForm.smtpHost" placeholder="smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="notificationForm.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="notificationForm.senderEmail" placeholder="noreply@example.com" />
            </el-form-item>
            <el-form-item label="SMTP用户名">
              <el-input v-model="notificationForm.smtpUsername" />
            </el-form-item>
            <el-form-item label="SMTP密码">
              <el-input v-model="notificationForm.smtpPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="启用SSL">
              <el-switch v-model="notificationForm.smtpSsl" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="testEmailConfig">测试邮件配置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>告警通知</span>
            </div>
          </template>
          <el-form :model="notificationForm" label-width="140px" class="settings-form">
            <el-form-item label="告警邮件通知">
              <el-switch v-model="notificationForm.alertEmailEnabled" />
            </el-form-item>
            <el-form-item label="告警接收邮箱" v-if="notificationForm.alertEmailEnabled">
              <el-input v-model="notificationForm.alertReceivers" placeholder="多个邮箱用逗号分隔" />
            </el-form-item>
            <el-form-item label="Webhook通知">
              <el-switch v-model="notificationForm.webhookEnabled" />
            </el-form-item>
            <el-form-item label="Webhook地址" v-if="notificationForm.webhookEnabled">
              <el-input v-model="notificationForm.webhookUrl" placeholder="https://..." />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 保存按钮 -->
    <div class="settings-footer">
      <el-button @click="resetForm">重置</el-button>
      <el-button type="primary" :loading="saving" @click="handleSaveSettings">保存设置</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllSettings, saveSettings } from '@/api/settings'

const activeTab = ref('basic')
const saving = ref(false)
const loading = ref(false)

// 基础设置表单
const basicForm = reactive({
  platformName: 'IntelliHub',
  logoUrl: '',
  description: '智能API管理平台，提供API全生命周期管理能力',
  copyright: '© 2024 IntelliHub. All rights reserved.',
  icpNumber: '',
  supportEmail: 'support@intellihub.com',
  supportPhone: '',
  techSupport: ''
})

// 安全设置表单
const securityForm = reactive({
  loginLockEnabled: true,
  maxFailedAttempts: 5,
  lockDuration: 30,
  passwordStrength: 'medium',
  passwordExpireDays: 90,
  sessionTimeout: 30,
  singleSignOn: false
})

// API设置表单
const apiForm = reactive({
  gatewayUrl: 'https://api.intellihub.com',
  defaultTimeout: 30,
  signatureExpire: 300,
  globalRateLimitEnabled: true,
  globalQps: 10000,
  ipRateLimitEnabled: true,
  ipQps: 100
})

// 通知设置表单
const notificationForm = reactive({
  smtpHost: '',
  smtpPort: 465,
  senderEmail: '',
  smtpUsername: '',
  smtpPassword: '',
  smtpSsl: true,
  alertEmailEnabled: false,
  alertReceivers: '',
  webhookEnabled: false,
  webhookUrl: ''
})

// Logo上传处理
const handleLogoChange = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    basicForm.logoUrl = e.target?.result as string
  }
  reader.readAsDataURL(file.raw)
}

// 测试邮件配置
const testEmailConfig = async () => {
  if (!notificationForm.smtpHost || !notificationForm.senderEmail) {
    ElMessage.warning('请先填写SMTP配置')
    return
  }
  ElMessage.success('测试邮件已发送，请检查收件箱')
}

// 加载设置
const loadSettings = async () => {
  loading.value = true
  try {
    const res = await getAllSettings()
    if (res.code === 200 && res.data) {
      const data = res.data
      // 基础设置
      if (data.platformName) basicForm.platformName = data.platformName
      if (data.logoUrl) basicForm.logoUrl = data.logoUrl
      if (data.description) basicForm.description = data.description
      if (data.copyright) basicForm.copyright = data.copyright
      if (data.icpNumber) basicForm.icpNumber = data.icpNumber
      if (data.supportEmail) basicForm.supportEmail = data.supportEmail
      if (data.supportPhone) basicForm.supportPhone = data.supportPhone
      if (data.techSupport) basicForm.techSupport = data.techSupport
      
      // 安全设置
      if (data.loginLockEnabled !== undefined) securityForm.loginLockEnabled = data.loginLockEnabled
      if (data.maxFailedAttempts) securityForm.maxFailedAttempts = data.maxFailedAttempts
      if (data.lockDuration) securityForm.lockDuration = data.lockDuration
      if (data.passwordStrength) securityForm.passwordStrength = data.passwordStrength
      if (data.passwordExpireDays) securityForm.passwordExpireDays = data.passwordExpireDays
      if (data.sessionTimeout) securityForm.sessionTimeout = data.sessionTimeout
      if (data.singleSignOn !== undefined) securityForm.singleSignOn = data.singleSignOn
      
      // API设置
      if (data.gatewayUrl) apiForm.gatewayUrl = data.gatewayUrl
      if (data.defaultTimeout) apiForm.defaultTimeout = data.defaultTimeout
      if (data.signatureExpire) apiForm.signatureExpire = data.signatureExpire
      if (data.globalRateLimitEnabled !== undefined) apiForm.globalRateLimitEnabled = data.globalRateLimitEnabled
      if (data.globalQps) apiForm.globalQps = data.globalQps
      if (data.ipRateLimitEnabled !== undefined) apiForm.ipRateLimitEnabled = data.ipRateLimitEnabled
      if (data.ipQps) apiForm.ipQps = data.ipQps
      
      // 通知设置
      if (data.smtpHost) notificationForm.smtpHost = data.smtpHost
      if (data.smtpPort) notificationForm.smtpPort = data.smtpPort
      if (data.senderEmail) notificationForm.senderEmail = data.senderEmail
      if (data.smtpUsername) notificationForm.smtpUsername = data.smtpUsername
      if (data.smtpSsl !== undefined) notificationForm.smtpSsl = data.smtpSsl
      if (data.alertEmailEnabled !== undefined) notificationForm.alertEmailEnabled = data.alertEmailEnabled
      if (data.alertReceivers) notificationForm.alertReceivers = data.alertReceivers
      if (data.webhookEnabled !== undefined) notificationForm.webhookEnabled = data.webhookEnabled
      if (data.webhookUrl) notificationForm.webhookUrl = data.webhookUrl
    }
  } catch (error) {
    console.error('加载设置失败', error)
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  loadSettings()
  ElMessage.info('已重置为已保存的值')
}

// 保存设置
const handleSaveSettings = async () => {
  saving.value = true
  try {
    const allConfigs = {
      ...basicForm,
      ...securityForm,
      ...apiForm,
      ...notificationForm
    }
    await saveSettings(allConfigs)
    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.settings-tabs {
  margin-bottom: 24px;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.settings-form {
  max-width: 600px;
}

.form-tip {
  margin-left: 8px;
  font-size: 13px;
  color: #999;
}

/* Logo上传 */
.logo-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.logo-preview {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 底部按钮 */
.settings-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
  border-top: 1px solid #e8e8e8;
}
</style>
