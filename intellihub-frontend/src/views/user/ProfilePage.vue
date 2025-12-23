<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card" shadow="never">
          <div class="user-header">
            <el-avatar :size="80" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-basic">
              <h2 class="user-name">{{ userInfo.nickname || userInfo.username }}</h2>
              <p class="user-username">@{{ userInfo.username }}</p>
              <el-tag :type="getRoleType(userInfo.role)" size="small">
                {{ getRoleName(userInfo.role) }}
              </el-tag>
            </div>
          </div>

          <el-divider />

          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.tenantName || '-' }}</div>
              <div class="stat-label">所属租户</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ formatDate(userInfo.lastLoginAt) }}</div>
              <div class="stat-label">最后登录</div>
            </div>
          </div>

          <el-divider />

          <div class="user-actions">
            <el-button type="primary" @click="activeTab = 'profile'">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
            <el-button @click="activeTab = 'password'">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：详细信息 -->
      <el-col :span="16">
        <el-card class="detail-card" shadow="never">
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="profile">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="100px"
                style="max-width: 500px;"
              >
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="头像">
                  <div class="avatar-upload">
                    <el-avatar :size="64" :src="profileForm.avatar">
                      {{ userInfo.username?.charAt(0)?.toUpperCase() }}
                    </el-avatar>
                    <el-button size="small" @click="handleAvatarChange">更换头像</el-button>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="profileLoading" @click="saveProfile">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                style="max-width: 500px;"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="passwordLoading" @click="changePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 账户安全 -->
            <el-tab-pane label="账户安全" name="security">
              <div class="security-list">
                <div class="security-item">
                  <div class="security-info">
                    <div class="security-title">登录密码</div>
                    <div class="security-desc">定期更换密码可以保护账户安全</div>
                  </div>
                  <el-button type="primary" link @click="activeTab = 'password'">修改</el-button>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <div class="security-title">绑定邮箱</div>
                    <div class="security-desc">
                      {{ userInfo.email ? `已绑定：${userInfo.email}` : '未绑定' }}
                    </div>
                  </div>
                  <el-button type="primary" link @click="activeTab = 'profile'">
                    {{ userInfo.email ? '修改' : '绑定' }}
                  </el-button>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <div class="security-title">绑定手机</div>
                    <div class="security-desc">
                      {{ userInfo.phone ? `已绑定：${maskPhone(userInfo.phone)}` : '未绑定' }}
                    </div>
                  </div>
                  <el-button type="primary" link @click="activeTab = 'profile'">
                    {{ userInfo.phone ? '修改' : '绑定' }}
                  </el-button>
                </div>
              </div>
            </el-tab-pane>

            <!-- 登录日志 -->
            <el-tab-pane label="登录日志" name="logs">
              <el-table :data="loginLogs" style="width: 100%">
                <el-table-column label="登录时间" prop="loginTime" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.loginTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="登录IP" prop="ip" width="150" />
                <el-table-column label="登录地点" prop="location" />
                <el-table-column label="设备" prop="device" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                      {{ row.status === 'success' ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Edit, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'

const authStore = useAuthStore()

// 当前标签
const activeTab = ref('profile')

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  role: '',
  tenantId: '',
  tenantName: '',
  lastLoginAt: '',
})

// 加载用户信息
const loadUserInfo = () => {
  const user = authStore.user
  if (user) {
    userInfo.id = user.id
    userInfo.username = user.username
    userInfo.nickname = user.nickname || ''
    userInfo.email = user.email || ''
    userInfo.phone = user.phone || ''
    userInfo.avatar = user.avatar || ''
    userInfo.role = user.role
    userInfo.tenantId = user.tenantId || ''
    userInfo.tenantName = user.tenantName || ''
    userInfo.lastLoginAt = user.lastLoginAt || ''
    
    // 同步到表单
    profileForm.nickname = userInfo.nickname
    profileForm.email = userInfo.email
    profileForm.phone = userInfo.phone
    profileForm.avatar = userInfo.avatar
  }
}

// 监听用户变化
watch(() => authStore.user, loadUserInfo, { immediate: true })

// 基本信息表单
const profileFormRef = ref<FormInstance>()
const profileLoading = ref(false)
const profileForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
})

const profileRules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

// 密码表单
const passwordFormRef = ref<FormInstance>()
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 登录日志
const loginLogs = ref([
  { loginTime: '2024-12-20 14:30:00', ip: '192.168.1.100', location: '北京市', device: 'Chrome/Windows', status: 'success' },
  { loginTime: '2024-12-19 09:15:00', ip: '192.168.1.100', location: '北京市', device: 'Chrome/Windows', status: 'success' },
  { loginTime: '2024-12-18 18:45:00', ip: '10.0.0.1', location: '上海市', device: 'Safari/macOS', status: 'success' },
  { loginTime: '2024-12-17 10:00:00', ip: '192.168.1.50', location: '北京市', device: 'Firefox/Linux', status: 'fail' },
])

// 获取角色类型
const getRoleType = (role: string) => {
  const map: Record<string, string> = {
    platform_admin: 'danger',
    tenant_admin: 'warning',
    api_developer: 'success',
    api_consumer: 'info',
    user: '',
  }
  return map[role] || ''
}

// 获取角色名称
const getRoleName = (role: string) => {
  const map: Record<string, string> = {
    platform_admin: '平台管理员',
    tenant_admin: '租户管理员',
    api_developer: 'API开发者',
    api_consumer: 'API调用方',
    user: '普通用户',
  }
  return map[role] || role
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 手机号脱敏
const maskPhone = (phone: string) => {
  if (!phone || phone.length < 7) return phone
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

// 更换头像
const handleAvatarChange = () => {
  ElMessage.info('头像上传功能开发中...')
}

// 保存基本信息
const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    profileLoading.value = true
    
    await userApi.update(userInfo.id, {
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: profileForm.avatar,
    })
    
    // 更新本地用户信息
    authStore.updateUser({
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: profileForm.avatar,
    })
    
    userInfo.nickname = profileForm.nickname
    userInfo.email = profileForm.email
    userInfo.phone = profileForm.phone
    userInfo.avatar = profileForm.avatar
    
    ElMessage.success('资料已更新')
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    await authApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
    })
    
    ElMessage.success('密码修改成功，请重新登录')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 退出登录
    setTimeout(() => {
      authStore.logout()
    }, 1500)
  } catch (error: any) {
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 用户卡片 */
.user-card {
  text-align: center;
}

.user-header {
  padding: 20px 0;
}

.user-basic {
  margin-top: 16px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.user-username {
  font-size: 14px;
  color: #999;
  margin: 0 0 8px 0;
}

/* 用户统计 */
.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 用户操作 */
.user-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
}

.user-actions .el-button {
  width: 100%;
}

/* 详细信息卡片 */
.detail-card {
  min-height: 500px;
}

/* 头像上传 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 安全设置 */
.security-list {
  padding: 0;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.security-item:last-child {
  border-bottom: none;
}

.security-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.security-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
