<template>
  <div class="page" v-if="article">
    <div class="container">
      <h1 class="title">{{ article.title }}</h1>
      <div class="meta">
        <span class="author" @click="$router.push('/user/' + article.userId)">
          {{ article.nickname || ('用户' + article.userId) }}
        </span>
        <el-tag v-if="article.category" size="small">{{ article.category }}</el-tag>
        <span v-for="tag in tags" :key="tag" class="tag">#{{ tag }}</span>
        <span>{{ article.viewCount }} 浏览 · {{ article.likeCount }} 赞 · {{ article.createTime?.substring(0,10) }}</span>
      </div>
      <el-image v-if="article.coverUrl" :src="article.coverUrl" fit="cover" class="cover" />
      <div class="content" v-html="renderedContent"></div>
      <div class="actions">
        <el-button :type="liked ? 'primary' : 'default'" @click="doLike" :icon="Star">{{ liked ? '已点赞' : '点赞' }}</el-button>
        <el-button :type="favorited ? 'primary' : 'default'" @click="doFavorite" :icon="Collection">{{ favorited ? '已收藏' : '收藏' }}</el-button>
        <el-button v-if="isAuthor" type="warning" @click="$router.push('/publish/' + article.id)">编辑文章</el-button>
        <el-divider style="margin:10px 0" />
        <div class="reactions">
          <span class="reaction-label">觉得这篇文章：</span>
          <el-button size="small" :type="reactionMine.includes('LIKE') ? 'primary' : 'default'" @click="doReact('LIKE')">👍 赞 {{ reactionCounts.LIKE || 0 }}</el-button>
          <el-button size="small" :type="reactionMine.includes('AWESOME') ? 'primary' : 'default'" @click="doReact('AWESOME')">😲 哇 {{ reactionCounts.AWESOME || 0 }}</el-button>
          <el-button size="small" :type="reactionMine.includes('GOOD') ? 'primary' : 'default'" @click="doReact('GOOD')">💪 棒 {{ reactionCounts.GOOD || 0 }}</el-button>
          <el-button size="small" @click="share">分享</el-button>
          <el-button size="small" type="danger" plain @click="reportArticle">举报</el-button>
        </div>
      </div>
      <el-divider />
      <h3>评论 ({{ comments.length }})</h3>
      <div v-if="token" class="comment-input">
        <el-input v-model="commentText" type="textarea" :rows="2" :placeholder="replyTo ? '回复 ' + replyTo.nickname + '：' : '写下你的评论'" />
        <div style="margin-top:8px">
          <el-button v-if="replyTo" link @click="replyTo = null">取消回复</el-button>
          <el-button type="primary" @click="doComment">发表评论</el-button>
        </div>
      </div>
      <div v-for="c in rootComments" :key="c.id" class="comment">
        <div class="comment-head">
          <b @click="$router.push('/user/' + c.userId)" class="author">{{ c.nickname || ('用户' + c.userId) }}</b>
          <span class="comment-time">{{ c.createTime?.substring(0,16).replace('T',' ') }}</span>
        </div>
        <div class="comment-body">{{ c.content }}</div>
        <div class="comment-actions">
          <el-button link size="small" @click="doLikeComment(c)">{{ c.liked ? '已赞' : '赞' }} {{ c.likeCount || 0 }}</el-button>
          <el-button v-if="token" link size="small" @click="replyTo = c">回复</el-button>
          <el-button v-if="isMyComment(c)" link type="danger" size="small" @click="delComment(c.id)">删除</el-button>
        </div>
        <!-- 递归渲染子回复 -->
        <template v-for="r in childrenOf(c.id)" :key="r.id">
          <div class="reply" style="margin-left:20px">
            <b @click="$router.push('/user/' + r.userId)" class="author">{{ r.nickname }}</b>
            <template v-if="r.replyToNickname"> 回复 <b>{{ r.replyToNickname }}</b></template>
            ：{{ r.content }}
            <span class="comment-actions">
              <el-button v-if="token" link size="small" @click="replyTo = c">回复</el-button>
              <el-button v-if="isMyComment(r)" link type="danger" size="small" @click="delComment(r.id)">删除</el-button>
            </span>
          </div>
        </template>
      </div>
    </div>
    <div class="sidebar" v-if="related.length">
      <h4>相关推荐</h4>
      <div v-for="a in related" :key="a.id" class="related-item" @click="$router.push('/article/' + a.id)">
        {{ a.title }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import api from '../api'
import { Star, Collection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const md = new MarkdownIt()
const article = ref(null)
const comments = ref([])
const related = ref([])
const commentText = ref('')
const replyTo = ref(null)
const liked = ref(false)
const favorited = ref(false)
const reactionCounts = ref({})
const reactionMine = ref([])
const token = ref(localStorage.getItem('token'))
const myUserId = ref(localStorage.getItem('userId'))
const tags = computed(() => article.value?.tags ? article.value.tags.split(',') : [])
const renderedContent = computed(() => DOMPurify.sanitize(md.render(article.value?.content || '')))
const isAuthor = computed(() => article.value && myUserId.value == article.value.userId)

const rootComments = computed(() => comments.value.filter(c => !c.parentId || c.parentId === 0))
const childrenOf = (pid) => comments.value.filter(c => c.parentId === pid)

const loadDetail = async () => {
  const res = await api.get('/article/' + route.params.id)
  article.value = res.data.article
  comments.value = res.data.comments
  liked.value = res.data.liked || false
  favorited.value = res.data.favorited || false
  related.value = res.data.related || []
}
const doLike = async () => {
  try {
    const res = await api.post(`/article/${route.params.id}/like`)
    ElMessage.success(res.data)
    liked.value = !liked.value
    article.value.likeCount += liked.value ? 1 : -1
  } catch (e) {}
}
const doFavorite = async () => {
  try {
    const res = await api.post(`/article/${route.params.id}/favorite`)
    ElMessage.success(res.data)
    favorited.value = !favorited.value
  } catch (e) {}
}
const doComment = async () => {
  try {
    await api.post(`/article/${route.params.id}/comment`, {
      content: commentText.value,
      parentId: replyTo.value ? replyTo.value.id : 0,
      replyToUserId: replyTo.value ? replyTo.value.userId : null
    })
    commentText.value = ''
    replyTo.value = null
    loadDetail()
  } catch (e) {}
}
const doLikeComment = async (c) => {
  if (!token.value) { ElMessage.warning('请先登录'); return }
  try {
    const res = await api.post(`/article/comment/${c.id}/like`)
    ElMessage.success(res.data)
    c.liked = !c.liked
    c.likeCount += c.liked ? 1 : -1
  } catch (e) {}
}
const isMyComment = (c) => myUserId.value == c.userId
const delComment = async (cid) => {
  try {
    await ElMessageBox.confirm('确认删除这条评论？', '提示', { type: 'warning' })
    await api.delete(`/article/comment/${cid}`)
    ElMessage.success('已删除')
    loadDetail()
  } catch (e) {}
}
onMounted(loadDetail)

const doReact = async (type) => {
  if (!token.value) { ElMessage.warning('请先登录'); return }
  const res = await api.post(`/article/${route.params.id}/react/${type}`)
  reactionCounts.value = res.data
  reactionMine.value = res.data.mine || []
}
const share = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => ElMessage.success('链接已复制到剪贴板'))
}
const reportArticle = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入举报原因', '举报文章', { inputPlaceholder: '如：垃圾内容/广告/侵权' })
    await api.post('/report', { articleId: route.params.id, reason: value })
    ElMessage.success('举报已提交，管理员会尽快处理')
  } catch (e) {}
}
</script>

<style scoped>
.page { max-width: 1100px; margin: 20px auto; padding: 0 20px; display: flex; gap: 30px; }
.container { flex: 1; padding: 30px; background: #fff; border-radius: 8px; }
.sidebar { width: 260px; }
.sidebar h4 { color: #555; }
.related-item { padding: 8px 0; color: #333; cursor: pointer; font-size: 14px; border-bottom: 1px solid #eee; }
.related-item:hover { color: #1e80ff; }
.title { font-size: 26px; }
.meta { color: #999; font-size: 13px; margin: 10px 0 20px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.author { color: #1e80ff; cursor: pointer; font-weight: 500; }
.author:hover { text-decoration: underline; }
.tag { color: #1e80ff; }
.cover { width: 100%; max-height: 300px; border-radius: 8px; margin-bottom: 20px; }
.content { line-height: 1.8; font-size: 15px; }
.actions { margin: 20px 0; }
.reactions { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.reaction-label { color: #999; font-size: 13px; }
.comment { padding: 12px 0; border-bottom: 1px solid #eee; }
.comment-head { display: flex; gap: 10px; align-items: center; }
.comment-time { color: #aaa; font-size: 12px; }
.comment-body { margin: 6px 0; }
.comment-actions { display: inline-flex; gap: 8px; }
.reply { margin: 8px 0 8px 20px; padding: 6px 10px; background: #f8f8f8; border-radius: 4px; font-size: 14px; }
</style>
