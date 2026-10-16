<template>
  <div class="container">
    <div class="header">
      <h2>AI工具广场</h2>
      <el-button type="primary" @click="dialog = true">提交工具</el-button>
    </div>
    <div class="filter-bar">
      <el-radio-group v-model="category" @change="loadList">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="AI开发平台">AI开发平台</el-radio-button>
        <el-radio-button label="开发框架">开发框架</el-radio-button>
        <el-radio-button label="模型部署">模型部署</el-radio-button>
        <el-radio-button label="AI客户端">AI客户端</el-radio-button>
        <el-radio-button label="向量数据库">向量数据库</el-radio-button>
        <el-radio-button label="API管理">API管理</el-radio-button>
      </el-radio-group>
    </div>
    <el-empty v-if="list.length === 0" description="暂无AI工具，快来提交第一个吧" />
    <div class="tool-grid" v-else>
      <div v-for="t in list" :key="t.id" class="tool-card" @click="openUrl(t.url)">
        <h3>{{ t.name }}</h3>
        <p>{{ t.description }}</p>
        <el-tag size="small">{{ t.category }}</el-tag>
      </div>
    </div>

    <el-dialog v-model="dialog" title="提交AI工具" width="500px">
      <el-input v-model="form.name" placeholder="工具名称" style="margin-bottom:10px" />
      <el-input v-model="form.url" placeholder="官网地址 https://..." style="margin-bottom:10px" />
      <el-input v-model="form.category" placeholder="分类（AI开发平台/开发框架/模型部署/AI客户端/向量数据库/API管理）" style="margin-bottom:10px" />
      <el-input v-model="form.description" type="textarea" :rows="3" placeholder="工具简介" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const category = ref('')
const dialog = ref(false)
const form = reactive({ name: '', url: '', category: '', description: '' })

const loadList = async () => {
  const res = await api.get('/tool/list', { params: { page: 1, size: 50, category: category.value } })
  list.value = res.data.records
}
const openUrl = (url) => window.open(url, '_blank')
const submit = async () => {
  try {
    await api.post('/tool', form)
    ElMessage.success('提交成功')
    dialog.value = false
    form.name = form.url = form.category = form.description = ''
    loadList()
  } catch (e) {}
}
onMounted(loadList)
</script>

<style scoped>
.container { max-width: 1000px; margin: 20px auto; padding: 0 20px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin: 15px 0; }
.tool-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.tool-card {
  background: #fff; padding: 20px; border-radius: 8px; cursor: pointer;
  transition: box-shadow 0.2s;
}
.tool-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.tool-card h3 { margin: 0 0 8px; font-size: 16px; }
.tool-card p { color: #666; font-size: 13px; height: 40px; overflow: hidden; }
.likes { float: right; color: #999; font-size: 12px; }
</style>
