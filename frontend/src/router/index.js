import { createRouter, createWebHistory } from 'vue-router'
import { useSessionStore } from '../stores/session'
import LoginView from '../views/LoginView.vue'
import StudentLayout from '../views/student/StudentLayout.vue'
import StudentDashboardView from '../views/student/StudentDashboardView.vue'
import MyReservationsView from '../views/student/MyReservationsView.vue'
import CreditView from '../views/student/CreditView.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminResourcesView from '../views/admin/AdminResourcesView.vue'
import AdminReservationsView from '../views/admin/AdminReservationsView.vue'
import AdminBlacklistsView from '../views/admin/AdminBlacklistsView.vue'
import AdminStudentsView from '../views/admin/AdminStudentsView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true },
  },
  {
    path: '/',
    redirect: '/student',
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'student-home',
        component: StudentDashboardView,
      },
      {
        path: 'reservations',
        name: 'student-reservations',
        component: MyReservationsView,
      },
      {
        path: 'credit',
        name: 'student-credit',
        component: CreditView,
      },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'admin-resources',
        component: AdminResourcesView,
      },
      {
        path: 'reservations',
        name: 'admin-reservations',
        component: AdminReservationsView,
      },
      {
        path: 'blacklists',
        name: 'admin-blacklists',
        component: AdminBlacklistsView,
      },
      {
        path: 'students',
        name: 'admin-students',
        component: AdminStudentsView,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const session = useSessionStore()

  if (!session.state.initialized) {
    await session.initSession()
  }

  const user = session.user.value

  if (to.meta.guestOnly && user) {
    return user.role === 1 ? '/admin' : '/student'
  }

  if (to.meta.requiresAuth && !user) {
    return '/login'
  }

  if (to.meta.requiresAdmin && user?.role !== 1) {
    return '/student'
  }

  if (to.path === '/student' && user?.role === 1) {
    return '/admin'
  }

  return true
})

export default router
