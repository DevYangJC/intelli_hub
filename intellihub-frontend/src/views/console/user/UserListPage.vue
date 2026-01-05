<template>
  <div class="user-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理</h2>
        <span class="page-desc">管理系统用户和权限分配</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建用户
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="用户名/昵称/邮箱" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 120px">
            <el-option label="正常" value="active" />
            <el-option label="已禁用" value="disabled" />
            <el-option label="已锁定" value="locked" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filterForm.roleId" placeholder="选择角色" clearable style="width: 150px">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="userList"
        style="width: 100%"
        v-loading="loading"
        :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600' }"
      >
        <el-table-column label="用户信息" min-width="240">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar">
                {{ row.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ row.nickname || row.username }}</div>
                <div class="user-username">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="邮箱" prop="email" width="200">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" prop="phone" width="140">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="160">
          <template #default="{ row }">
            <div class="role-tags">
              <el-tag
                v-for="role in (row.roleNames || [])"
                :key="role"
                size="small"
                :type="getRoleTagType(role)"
                class="role-tag"
              >
                {{ role }}
              </el-tag>
              <span v-if="!row.roleNames?.length" class="empty-text">未分配</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="55" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="180">
          <template #default="{ row }">
            <div v-if="row.lastLoginAt" class="login-info">
              <div class="login-time">{{ formatDate(row.lastLoginAt) }}</div>
              <div class="login-ip">IP: {{ row.lastLoginIp || '-' }}</div>
            </div>
            <span v-else class="empty-text">从未登录</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="primary" link size="small" @click="handleAssignRoles(row)">
              分配角色
            </el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
              <el-button type="primary" link size="small">
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="resetPassword">
                    <el-icon><Key /></el-icon>
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item command="disable" v-if="row.status === 'active'" divided>
                    <el-icon><CircleClose /></el-icon>
                    禁用用户
                  </el-dropdown-item>
                  <el-dropdown-item command="enable" v-if="row.status === 'disabled'">
                    <el-icon><CircleCheck /></el-icon>
                    启用用户
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除用户
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="fetchUserList"
          @size-change="fetchUserList"
        />
      </div>
    </el-card>

    <!-- 创建/编辑用户弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '创建用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
    >
      <div v-if="currentUser" class="role-dialog">
        <div class="user-header">
          <el-avatar :size="48" :src="currentUser.avatar">
            {{ currentUser.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div>
            <div class="user-name">{{ currentUser.nickname || currentUser.username }}</div>
            <div class="user-username">@{{ currentUser.username }}</div>
          </div>
        </div>

        <el-divider />

        <el-checkbox-group v-model="selectedRoles">
          <div v-for="role in roleList" :key="role.id" class="role-item">
            <el-checkbox :label="role.id">
              <div class="role-info">
                <span class="role-name">{{ role.name }}</span>
                <span class="role-desc">{{ role.description }}</span>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleLoading" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码结果弹窗 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="密码已重置"
      width="400px"
    >
      <div class="password-result">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <p>新密码已生成，请妥善保管：</p>
        <div class="password-box">
          <span>{{ newPassword }}</span>
          <el-button type="primary" link @click="copyPassword">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </div>
        <p class="warning-text">此密码仅显示一次，关闭后无法再次查看</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="passwordDialogVisible = false">我已记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Search,
  ArrowDown,
  Key,
  CircleClose,
  CircleCheck,
  Delete,
  CopyDocument,
} from '@element-plus/icons-vue'
import { userApi, type UserResponse } from '@/api/user'
import { roleApi, type RoleResponse } from '@/api/role'

// 筛选表单
const filterForm = reactive({
  keyword: '',
  status: '',
  roleId: '',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 状态
const loading = ref(false)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const roleLoading = ref(false)
const currentUser = ref<any>(null)
const newPassword = ref('')

// 用户列表
const userList = ref<UserResponse[]>([])

// 角色列表
const roleList = ref<RoleResponse[]>([])
const selectedRoles = ref<string[]>([])

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await userApi.list({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: filterForm.keyword || undefined,
      status: filterForm.status || undefined,
      roleId: filterForm.roleId || undefined,
    })
    userList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error: any) {
    console.error('获取用户列表失败:', error)
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoleList = async () => {
  try {
    const res = await roleApi.list()
    roleList.value = res.data || []
  } catch (error: any) {
    console.error('获取角色列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchUserList()
  fetchRoleList()
})

// 表单
const formRef = ref<FormInstance>()
const formData = reactive({
  id: '',
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    active: 'success',
    disabled: 'info',
    locked: 'danger',
  }
  return map[status] || 'info'
}

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  if (role.includes('超级') || role.includes('platform')) return 'danger'
  if (role.includes('管理') || role.includes('admin')) return 'warning'
  return ''
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    active: '正常',
    disabled: '已禁用',
    locked: '已锁定',
  }
  return map[status] || status
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  filterForm.keyword = ''
  filterForm.status = ''
  filterForm.roleId = ''
  pagination.page = 1
  fetchUserList()
}

// 创建用户
const handleCreate = () => {
  isEdit.value = false
  formData.id = ''
  formData.username = ''
  formData.password = ''
  formData.nickname = ''
  formData.email = ''
  formData.phone = ''
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: UserResponse) => {
  isEdit.value = true
  formData.id = row.id
  formData.username = row.username
  formData.nickname = row.nickname || ''
  formData.email = row.email || ''
  formData.phone = row.phone || ''
  dialogVisible.value = true
}

// 分配角色
const handleAssignRoles = async (row: UserResponse) => {
  currentUser.value = row
  selectedRoles.value = [] // 需要从后端获取用户当前角色
  roleDialogVisible.value = true
}

// 更多操作
const handleCommand = async (command: string, row: UserResponse) => {
  switch (command) {
    case 'resetPassword':
      try {
        await ElMessageBox.confirm('确定要重置该用户的密码吗？', '重置密码', {
          type: 'warning',
        })
        const res = await userApi.resetPassword(row.id)
        newPassword.value = res.data.newPassword
        passwordDialogVisible.value = true
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '重置密码失败')
        }
      }
      break
    case 'disable':
      try {
        await ElMessageBox.confirm('禁用后该用户将无法登录，确定要禁用吗？', '禁用确认', {
          type: 'warning',
        })
        await userApi.disable(row.id)
        ElMessage.success('用户已禁用')
        fetchUserList()
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '操作失败')
        }
      }
      break
    case 'enable':
      try {
        await userApi.enable(row.id)
        ElMessage.success('用户已启用')
        fetchUserList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('删除后无法恢复，确定要删除该用户吗？', '删除确认', {
          type: 'warning',
        })
        await userApi.delete(row.id)
        ElMessage.success('用户已删除')
        fetchUserList()
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '删除失败')
        }
      }
      break
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await userApi.update(formData.id, {
        nickname: formData.nickname,
        email: formData.email,
        phone: formData.phone,
      })
    } else {
      await userApi.create({
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
        email: formData.email,
        phone: formData.phone,
      })
    }

    ElMessage.success(isEdit.value ? '用户更新成功' : '用户创建成功')
    dialogVisible.value = false
    fetchUserList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 保存角色
const saveRoles = async () => {
  if (!currentUser.value) return

  roleLoading.value = true
  try {
    await userApi.assignRoles(currentUser.value.id, {
      roleIds: selectedRoles.value,
    })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchUserList()
  } catch (error: any) {
    ElMessage.error(error.message || '分配角色失败')
  } finally {
    roleLoading.value = false
  }
}

// 复制密码
const copyPassword = () => {
  navigator.clipboard.writeText(newPassword.value)
  ElMessage.success('密码已复制到剪贴板')
}
</script>

<style scoped>
.user-list-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 16px;
}

/* 表格卡片 */
.table-card {
  margin-bottom: 16px;
}

/* 表格列间距 */
:deep(.el-table .cell) {
  padding-left: 12px;
  padding-right: 12px;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.user-username {
  font-size: 12px;
  color: #999;
}

.sub-text {
  font-size: 12px;
  color: #999;
}

/* 角色标签 */
.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  border-radius: 4px;
}

/* 登录信息 */
.login-info {
  line-height: 1.5;
}

.login-time {
  font-size: 13px;
  color: #606266;
}

.login-ip {
  font-size: 12px;
  color: #909399;
}

/* 空文本 */
.empty-text {
  color: #c0c4cc;
  font-size: 13px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

/* 角色弹窗 */
.role-dialog .user-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.role-item:last-child {
  border-bottom: none;
}

.role-info {
  display: flex;
  flex-direction: column;
  margin-left: 8px;
}

.role-name {
  font-size: 14px;
  color: #1a1a1a;
}

.role-desc {
  font-size: 12px;
  color: #999;
}

/* 密码结果弹窗 */
.password-result {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 16px;
}

.password-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 18px;
  margin: 16px 0;
}

.warning-text {
  font-size: 12px;
  color: #e6a23c;
}
</style>
