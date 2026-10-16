import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/article/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue') },
  { path: '/publish', name: 'Publish', component: () => import('../views/Publish.vue'), meta: { requiresAuth: true } },
  { path: '/publish/:id', name: 'Edit', component: () => import('../views/Publish.vue'), meta: { requiresAuth: true } },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/user/:id', name: 'Profile', component: () => import('../views/Profile.vue') },
  { path: '/tools', name: 'Tools', component: () => import('../views/Tools.vue') },
  { path: '/search', name: 'Search', component: () => import('../views/Search.vue') },
  { path: '/favorites', name: 'Favorites', component: () => import('../views/Favorites.vue'), meta: { requiresAuth: true } },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { requiresAuth: true } },
  { path: '/drafts', name: 'Drafts', component: () => import('../views/Drafts.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue'), meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    next('/login')
  } else {
    next()
  }
})

export default router
