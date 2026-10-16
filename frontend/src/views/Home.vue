<template>
  <div class="home-layout">
    <div class="sidebar">
      <div class="side-card">
        <div class="side-item" :class="{active: category===''}" @click="selectCategory('')">
          <el-icon :size="16" style="vertical-align:middle;margin-right:6px"><Odometer /></el-icon> 综合
        </div>
        <div
          v-for="c in categories" :key="c"
          class="side-item" :class="{active: category===c}"
          @click="selectCategory(c)"
        ><el-icon :size="16" style="vertical-align:middle;margin-right:6px"><component :is="iconMap[c]" /></el-icon> {{ c }}</div>
      </div>
    </div>
    <div class="main-content">
      <div class="tabs">
        <span :class="{on: tab==='recommend'}" @click="switchTab('recommend')">推荐</span>
        <span :class="{on: tab==='latest'}" @click="switchTab('latest')">最新</span>
        <span v-if="category===''" :class="{on: tab==='follow'}" @click="switchTab('follow')">关注</span>
      </div>
      <el-empty v-if="list.length===0 && tab==='follow'" description="你还没有关注任何人，去发现感兴趣的作者吧" />
      <el-empty v-else-if="list.length===0" description="暂无文章" />
      <div v-for="a in list" :key="a.id" class="feed-item" @click="$router.push('/article/'+a.id)">
        <div class="feed-info">
          <h3 class="feed-title">{{ a.title }}</h3>
          <p class="feed-summary">{{ a.summary || '' }}</p>
          <div class="feed-meta">
            <span class="author">{{ a.nickname || ('用户'+a.userId) }}</span>
            <span class="meta-item"><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg> {{ a.viewCount }}</span>
            <span class="meta-item"><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ a.likeCount }}</span>
            <el-tag v-if="a.category" size="small" style="margin-left:4px">{{ a.category }}</el-tag>
            <span v-for="t in (a.tags||'').split(',').filter(x=>x.trim())" :key="t" class="tag">#{{ t }}</span>
          </div>
        </div>
        <div v-if="a.coverUrl" class="feed-cover">
          <img :src="a.coverUrl" />
        </div>
      </div>
      <el-pagination v-if="total>size" :current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next" @current-change="handlePage" style="margin-top:20px" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const category = ref('')
const tab = ref('recommend')
const categories = ['后端开发','前端开发','AI应用','数据库','架构设计','运维部署','云原生','职业成长','开源项目','其他']
const iconMap = { '后端开发':'Cpu', '前端开发':'Brush', 'AI应用':'ChatDotRound', '数据库':'Coin', '架构设计':'OfficeBuilding', '运维部署':'Setting', '云原生':'Cloudy', '职业成长':'TrendCharts', '开源项目':'Box', '其他':'Folder' }

const selectCategory = (c) => { category.value = category.value===c ? '' : c; page.value=1; tab.value='recommend'; loadList() }
const switchTab = (t) => { tab.value=t; page.value=1; loadList() }

const loadList = async () => {
  const params = { page: page.value, size: size.value, category: category.value }
  if (tab.value === 'follow') {
    const res = await api.get('/article/follow/list', { params })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } else if (tab.value === 'latest') {
    const res = await api.get('/article/list', { params })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } else {
    const res = await api.get('/article/hot', { params })
    list.value = res.data.records || []
    total.value = res.data.total || list.value.length
  }
}
const handlePage = (p) => { page.value=p; loadList() }
onMounted(loadList)
</script>

<style scoped>
.home-layout { display: flex; gap: 16px; max-width: 1100px; margin: 20px auto; padding: 0 20px; }
.sidebar { width: 200px; flex-shrink: 0; }
.side-card { background: #fff; border-radius: 8px; padding: 12px 0; }
.side-title { padding: 8px 16px; font-weight: 600; font-size: 15px; }
.side-item { padding: 10px 16px; cursor: pointer; color: #555; display: flex; align-items: center; gap: 8px; }
.side-item .icon { width: 20px; text-align: center; }
.side-item:hover { background: #f5f7fa; }
.side-item.active { background: #e8f3ff; color: #1e80ff; }
.main-content { flex: 1; background: #fff; border-radius: 8px; padding: 20px; }
.tabs { display: flex; gap: 24px; border-bottom: 1px solid #eee; margin-bottom: 16px; padding-bottom: 12px; }
.tabs span { cursor: pointer; color: #666; font-size: 15px; }
.tabs span.on { color: #1e80ff; font-weight: 600; }
.feed-item { display: flex; gap: 16px; padding: 16px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.feed-item:last-child { border-bottom: none; }
.feed-info { flex: 1; }
.feed-title { margin: 0 0 6px; font-size: 18px; color: #222; }
.feed-summary { color: #888; font-size: 14px; margin: 0 0 10px; line-height: 1.5; }
.feed-meta { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #999; flex-wrap: wrap; }
.meta-item { display: inline-flex; align-items: center; gap: 3px; }
.author { color: #1e80ff; }
.tag { color: #1e80ff; }
.feed-cover { width: 200px; height: 120px; flex-shrink: 0; border-radius: 6px; overflow: hidden; }
.feed-cover img { width: 100%; height: 100%; object-fit: cover; }
</style>
