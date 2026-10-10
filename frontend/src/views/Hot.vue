<template>
  <div class="container">
    <h2>热门文章</h2>
    <ArticleCard v-for="a in list" :key="a.id" :article="a" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import ArticleCard from '../components/ArticleCard.vue'

const list = ref([])
onMounted(async () => {
  const res = await api.get('/article/hot', { params: { page: 1, size: 20 } })
  list.value = res.data.records
})
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
</style>
