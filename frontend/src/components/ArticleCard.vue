<template>
  <div class="article-card" @click="$router.push('/article/' + article.id)">
    <div class="card-body">
      <h3 class="title">{{ article.title }}</h3>
      <p class="summary">{{ article.summary || '暂无摘要' }}</p>
      <div class="meta">
        <span class="author" @click.stop="$router.push('/user/' + article.userId)">{{ article.nickname || '用户' + article.userId }}</span>
        <el-tag v-if="article.category" size="small">{{ article.category }}</el-tag>
        <span v-for="tag in tags" :key="tag" class="tag">#{{ tag }}</span>
        <span class="right">
          浏览 {{ article.viewCount }} · 赞 {{ article.likeCount }} · {{ formatTime(article.createTime) }}
        </span>
      </div>
    </div>
    <el-image v-if="article.coverUrl" :src="article.coverUrl" fit="cover" class="cover">
      <template #error><div style="width:120px"></div></template>
    </el-image>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ article: Object })
const tags = computed(() => {
  if (!props.article.tags) return []
  return props.article.tags.split(',').filter(t => t.trim())
})
const formatTime = (t) => {
  if (!t) return ''
  return t.substring(0, 10)
}
</script>

<style scoped>
.article-card {
  background: #fff; border-radius: 8px; margin-bottom: 12px;
  cursor: pointer; transition: box-shadow 0.2s; display: flex; overflow: hidden;
}
.article-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.card-body { padding: 20px; flex: 1; }
.cover { width: 120px; height: 100%; min-height: 80px; }
.title { margin: 0 0 8px; font-size: 18px; color: #222; }
.summary { color: #666; font-size: 14px; margin: 0 0 12px; }
.meta { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #999; flex-wrap: wrap; }
.author { color: #1e80ff; cursor: pointer; font-weight: 500; }
.author:hover { text-decoration: underline; }
.tag { color: #1e80ff; }
.right { margin-left: auto; }
</style>
