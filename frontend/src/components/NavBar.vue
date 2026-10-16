<template>
  <div class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="logo">
        <el-icon :size="24" color="#1e80ff"><Compass /></el-icon>
        <span>知微</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/tools" class="nav-link">AI工具</router-link>
      </div>
      <div class="nav-right">
        <div class="search-box">
          <el-input v-model="keyword" placeholder="搜索文章/标签/用户" style="width:220px"
            @keyup.enter="doSearch">
            <template #suffix>
              <el-icon style="cursor:pointer" @click="doSearch"><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <el-dropdown v-if="token" trigger="hover" class="publish-dropdown" @command="handleCommand">
          <span class="publish-btn">创作者中心 <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="publish"><el-icon style="vertical-align:middle;margin-right:6px"><EditPen /></el-icon>写文章</el-dropdown-item>
              <el-dropdown-item command="drafts"><el-icon style="vertical-align:middle;margin-right:6px"><Document /></el-icon>草稿箱</el-dropdown-item>
              <el-dropdown-item command="tool"><el-icon style="vertical-align:middle;margin-right:6px"><Tools /></el-icon>提交AI工具</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <template v-if="token">
          <el-dropdown trigger="hover" @command="handleCommand">
            <span class="username">
              <el-avatar :size="28" :src="avatarUrl || undefined" style="vertical-align:middle">{{ nickname[0] }}</el-avatar>
              {{ nickname }} <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon style="vertical-align:middle;margin-right:6px"><User /></el-icon>个人主页</el-dropdown-item>
                <el-dropdown-item command="favorites"><el-icon style="vertical-align:middle;margin-right:6px"><Star /></el-icon>我的收藏</el-dropdown-item>
                <el-dropdown-item command="notifications">
                  <el-icon style="vertical-align:middle;margin-right:6px"><Bell /></el-icon>消息通知
                  <el-badge v-if="unread > 0" :value="unread" :max="99" class="nav-badge" />
                </el-dropdown-item>
                <el-dropdown-item v-if="role==='ADMIN'" command="admin"><el-icon style="vertical-align:middle;margin-right:6px"><Setting /></el-icon>管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout"><el-icon style="vertical-align:middle;margin-right:6px"><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <div v-else class="login-btn-group">
          <router-link to="/login" class="login-btn">登录</router-link>
          <span class="divider">|</span>
          <router-link to="/login?register=1" class="register-btn">注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Search, Bell, EditPen, Document, User, Star, Setting, SwitchButton, Tools } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const keyword = ref('')
const token = ref(localStorage.getItem('token'))
const nickname = ref(localStorage.getItem('nickname') || '用户')
const userId = ref(localStorage.getItem('userId'))
const role = ref(localStorage.getItem('role') || 'USER')
const avatarUrl = ref(localStorage.getItem('avatar') || '')
const unread = ref(0)
const suggestions = ref([])
const hotWords = ref([])
const searchPop = ref(false)

const doSearch = () => {
  if (keyword.value) {
    searchPop.value = false
    router.push('/search?keyword=' + encodeURIComponent(keyword.value))
  }
}
const pickSuggest = (w) => {
  keyword.value = w
  searchPop.value = false
  router.push('/search?keyword=' + encodeURIComponent(w))
}
const onSearchInput = async () => {
  if (!keyword.value || keyword.value.length < 1) { suggestions.value = []; return }
  try {
    const res = await api.get('/search/suggest', { params: { keyword: keyword.value } })
    suggestions.value = res.data || []
  } catch (e) {}
}
const onSearchFocus = async () => {
  searchPop.value = true
  if (!hotWords.value.length) {
    try { const res = await api.get('/search/hot'); hotWords.value = (res.data || []).filter(w => w.length >= 3).slice(0, 10) } catch (e) {}
  }
}
const onSearchBlur = () => {
  setTimeout(() => { searchPop.value = false }, 200)
}
const fetchUnread = async () => {
  if (!token.value) return
  try { const res = await api.get('/notification/unread'); unread.value = res.data } catch (e) {}
}
onMounted(fetchUnread)
const logout = () => {
  localStorage.clear()
  window.location.href = '/'
}
const handleCommand = (cmd) => {
  if (cmd === 'publish') router.push('/publish')
  else if (cmd === 'drafts') router.push('/drafts')
  else if (cmd === 'tool') {
    if (!localStorage.getItem('token')) {
      ElMessage.warning('请先登录')
      setTimeout(() => router.push('/login'), 500)
      return
    }
    if (window.location.pathname === '/tools') {
      window.dispatchEvent(new Event('open-tool-dialog'))
    } else {
      router.push('/tools?submit=1')
    }
  }
  else if (cmd === 'profile') router.push('/user/' + userId.value)
  else if (cmd === 'favorites') router.push('/favorites')
  else if (cmd === 'notifications') router.push('/notifications')
  else if (cmd === 'admin') router.push('/admin')
  else if (cmd === 'logout') logout()
}
</script>

<style scoped>
.nav-badge :deep(.el-badge__content) { top: -4px; right: -8px; transform: scale(0.7); }
.navbar { background: #fff; border-bottom: 1px solid #e8e8e8; position: sticky; top: 0; z-index: 100; }
.navbar-inner { max-width: 1000px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; height: 56px; gap: 20px; }
.logo { display: flex; align-items: center; gap: 6px; font-size: 20px; font-weight: bold; color: #1e80ff; text-decoration: none; }
.nav-links { display: flex; gap: 15px; }
.nav-link { color: #555; text-decoration: none; font-size: 14px; }
.nav-link:hover { color: #1e80ff; }
.nav-right { margin-left: auto; display: flex; align-items: center; gap: 15px; }
.publish-btn { background: #1e80ff; color: #fff; padding: 6px 16px; border-radius: 4px; text-decoration: none; font-size: 14px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; outline: none; }
.publish-dropdown .el-tooltip__trigger, .publish-dropdown .el-tooltip__trigger:focus { outline: none; border: none; box-shadow: none; }
.publish-btn .el-icon { transition: transform 0.2s; }
.publish-dropdown:hover .publish-btn .el-icon { transform: rotate(180deg); }
.login-btn-group { background: #1e80ff; border-radius: 6px; padding: 6px 16px; display: flex; gap: 8px; align-items: center; }
.login-btn { color: #fff; text-decoration: none; font-size: 14px; }
.register-btn { color: #fff; text-decoration: none; font-size: 14px; }
.divider { color: rgba(255,255,255,0.5); }
.username { cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 5px; outline: none; }
.username .el-icon { transition: transform 0.2s; }
.username:hover .el-icon { transform: rotate(180deg); }
.el-tooltip__trigger { outline: none !important; }
.el-tooltip__trigger:focus { outline: none !important; box-shadow: none !important; }
.bell { font-size: 18px; }
.search-panel { padding: 8px 0; }
.search-box { position: relative; }
.search-dropdown { position: absolute; top: 40px; left: 0; width: 280px; background: #fff; border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); z-index: 200; padding: 8px 0; }
.section { padding: 4px 12px; }
.section-title { font-size: 12px; color: #999; margin-bottom: 6px; }
.suggest-item { padding: 6px 0; cursor: pointer; font-size: 14px; color: #333; }
.suggest-item:hover { color: #1e80ff; }
.hot-words { display: flex; flex-wrap: wrap; gap: 6px; }
.hot-tag { cursor: pointer; }
</style>
