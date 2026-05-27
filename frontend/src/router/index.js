import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    meta: { requiresAuth: true, permission: 'ai:chat' }
  },
  {
    path: '/rag',
    name: 'Rag',
    redirect: '/rag/chat',
    meta: { requiresAuth: true, permission: 'rag:manage' }
  },
  {
    path: '/rag/chat',
    name: 'RagChat',
    component: () => import('../views/RagView.vue'),
    meta: { requiresAuth: true, permission: 'rag:chat' }
  },
  {
    path: '/rag/document',
    name: 'Document',
    component: () => import('../views/DocumentView.vue'),
    meta: { requiresAuth: true, permission: 'rag:document' }
  },
  {
    path: '/rag/category',
    name: 'Category',
    component: () => import('../views/CategoryView.vue'),
    meta: { requiresAuth: true, permission: 'rag:category' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/SearchView.vue'),
    meta: { requiresAuth: true, permission: 'search:query' }
  },
  {
    path: '/graph',
    name: 'Graph',
    component: () => import('../views/GraphView.vue'),
    meta: { requiresAuth: true, permission: 'graph:manage' }
  },
  {
    path: '/system',
    name: 'System',
    redirect: '/system/user',
    meta: { requiresAuth: true, permission: 'system:manage' }
  },
  {
    path: '/system/user',
    name: 'User',
    component: () => import('../views/UserView.vue'),
    meta: { requiresAuth: true, permission: 'system:user' }
  },
  {
    path: '/system/role',
    name: 'Role',
    component: () => import('../views/RoleView.vue'),
    meta: { requiresAuth: true, permission: 'system:role' }
  },
  {
    path: '/system/log',
    name: 'Log',
    component: () => import('../views/LogView.vue'),
    meta: { requiresAuth: true, permission: 'system:log' }
  },
  {
    path: '/system/model',
    name: 'Model',
    component: () => import('../views/ModelView.vue'),
    meta: { requiresAuth: true, permission: 'system:model' }
  },
  {
    path: '/system/config',
    name: 'Config',
    component: () => import('../views/ConfigView.vue'),
    meta: { requiresAuth: true, permission: 'system:config' }
  },
  {
    path: '/system/user/:id/permission',
    name: 'UserPermission',
    component: () => import('../views/UserPermissionView.vue'),
    meta: { requiresAuth: true, permission: 'system:user:permission' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
