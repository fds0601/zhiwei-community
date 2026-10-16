<template>
  <div class="container">
    <h2>热门标签</h2>
    <el-empty v-if="tags.length===0" description="暂无标签" />
    <div class="tag-grid">
      <div v-for="t in tags" :key="t.tag" class="tag-card" @click="$router.push('/search?keyword=' + encodeURIComponent(t.tag))">
        <div class="tag-name">#{{ t.tag }}</div>
        <div class="tag-count">{{ t.count }} 篇文章</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
const tags = ref([])
onMounted(async () => {
  const res = await api.get('/tags/hot')
  tags.value = (res.data || []).sort((a,b) => b.count - a.count)
})
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
.tag-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 20px; }
.tag-card { background: #fff; padding: 16px; border-radius: 8px; cursor: pointer; transition: box-shadow 0.2s; }
.tag-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.tag-name { font-size: 15px; font-weight: 500; color: #1e80ff; margin-bottom: 6px; }
.tag-count { font-size: 12px; color: #999; }
</style>
