<template>
  <div class="container">
    <div class="header">
      <h2>热门文章</h2>
    </div>
    <div v-for="(a, i) in list" :key="a.id" class="hot-card" @click="$router.push('/article/'+a.id)">
      <div class="rank" :class="{top: (i+1+(page-1)*size)<=3}">{{ i+1+(page-1)*size }}</div>
      <div class="info">
        <h3>{{ a.title }}</h3>
        <p>{{ a.summary || '' }}</p>
        <div class="meta">
          <span class="author">{{ a.nickname || ('用户'+a.userId) }}</span>
          <el-tag size="small" v-if="a.category">{{ a.category }}</el-tag>
          <span class="count">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            {{ a.viewCount }}
          </span>
          <span class="count">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
            {{ a.likeCount }}
          </span>
          <span class="date">{{ a.createTime?.substring(0,10) }}</span>
        </div>
      </div>
    </div>
    <el-pagination v-if="total>size" :current-page="page" :page-size="size" :total="total"
      layout="prev, pager, next" @current-change="loadList" style="margin-top:20px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadList = async (p) => {
  if (p) page.value = p
  const res = await api.get('/article/hot', { params: { page: page.value, size: size.value } })
  list.value = res.data.records || []
  total.value = res.data.total || 0
}
onMounted(() => loadList())
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
.header { display: flex; align-items: baseline; gap: 10px; margin-bottom: 20px; }
.header h2 { margin: 0; font-size: 22px; }
.hot-card { display: flex; gap: 15px; background: #fff; padding: 18px 20px; border-radius: 8px; margin-bottom: 12px; cursor: pointer; transition: box-shadow 0.2s; }
.hot-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.rank { font-size: 20px; font-weight: bold; color: #ccc; width: 28px; text-align: center; line-height: 1.4; }
.rank.top { color: #ff6b6b; }
.info { flex: 1; }
.info h3 { margin: 0 0 6px; font-size: 17px; }
.info p { color: #666; font-size: 14px; margin: 0 0 10px; }
.meta { display: flex; align-items: center; gap: 10px; font-size: 12px; color: #999; }
.author { color: #1e80ff; }
.count { display: inline-flex; align-items: center; gap: 3px; }
</style>
