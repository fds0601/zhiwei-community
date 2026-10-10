<template>
  <div class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="logo">
        <el-icon :size="24" color="#1e80ff"><Compass /></el-icon>
        <span>知微</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/hot" class="nav-link">热门</router-link>
        <router-link to="/tools" class="nav-link">AI工具</router-link>
      </div>
      <div class="nav-right">
        <el-input v-model="keyword" placeholder="搜索文章" style="width:220px" @keyup.enter="doSearch">
          <template #prefix>
            <el-icon @click="doSearch" style="cursor:pointer"><Search /></el-icon>
          </template>
        </el-input>
        <router-link to="/publish" v-if="token" class="publish-btn">写文章</router-link>
        <template v-if="token">
          <el-badge :value="unread" :hidden="unread===0" class="bell">
            <el-icon :size="20" @click="$router.push('/notifications')" style="cursor:pointer"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="username">
              {{ nickname }} <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <router-link to="/login" v-else class="login-btn">登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Search, Bell } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const keyword = ref('')
const token = ref(localStorage.getItem('token'))
const nickname = ref(localStorage.getItem('nickname') || '用户')
const userId = ref(localStorage.getItem('userId'))
const unread = ref(0)

const doSearch = () => {
  if (keyword.value) router.push('/search?keyword=' + encodeURIComponent(keyword.value))
}

const fetchUnread = async () => {
  if (!token.value) return
  try {
    const res = await api.get('/notification/unread')
    unread.value = res.data
  } catch (e) {}
}

onMounted(fetchUnread)

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userId')
  window.location.href = '/'
}

const handleCommand = (cmd) => {
  if (cmd === 'profile') router.push('/user/' + userId.value)
  else if (cmd === 'favorites') router.push('/favorites')
  else if (cmd === 'logout') logout()
}
</script>

<style scoped>
.navbar { background: #fff; border-bottom: 1px solid #e8e8e8; position: sticky; top: 0; z-index: 100; }
.navbar-inner { max-width: 1000px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; height: 56px; gap: 20px; }
.logo { display: flex; align-items: center; gap: 6px; font-size: 20px; font-weight: bold; color: #1e80ff; text-decoration: none; }
.nav-links { display: flex; gap: 15px; }
.nav-link { color: #555; text-decoration: none; font-size: 14px; }
.nav-link:hover { color: #1e80ff; }
.nav-right { margin-left: auto; display: flex; align-items: center; gap: 15px; }
.publish-btn { background: #1e80ff; color: #fff; padding: 6px 16px; border-radius: 4px; text-decoration: none; font-size: 14px; }
.login-btn { color: #1e80ff; text-decoration: none; font-size: 14px; }
.username { cursor: pointer; font-size: 14px; display: flex; align-items: center; }
.bell { font-size: 18px; }
</style>
