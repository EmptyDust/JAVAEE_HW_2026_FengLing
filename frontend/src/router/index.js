import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import StudentList from '../views/StudentList.vue'
import ClassList from '../views/ClassList.vue'
import TeacherList from '../views/TeacherList.vue'
import CourseList from '../views/CourseList.vue'
import CourseSelection from '../views/CourseSelection.vue'
import MyCourses from '../views/MyCourses.vue'
import CourseCalendar from '../views/CourseCalendar.vue'
import WeeklySchedule from '../views/WeeklySchedule.vue'
import NotificationList from '../views/NotificationList.vue'
import NotificationManagement from '../views/NotificationManagement.vue'
import AttachmentManagement from '../views/AttachmentManagement.vue'
import Profile from '../views/Profile.vue'
import ChangePassword from '../views/ChangePassword.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true },
    redirect: '/home/profile',
    children: [
      {
        path: 'students',
        name: 'StudentList',
        component: StudentList,
        meta: { roles: ['admin', 'teacher'] }
      },
      {
        path: 'classes',
        name: 'ClassList',
        component: ClassList,
        meta: { roles: ['admin', 'teacher'] }
      },
      {
        path: 'teachers',
        name: 'TeacherList',
        component: TeacherList,
        meta: { roles: ['admin'] }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: CourseList,
        meta: { roles: ['admin', 'teacher'] }
      },
      {
        path: 'course-selection',
        name: 'CourseSelection',
        component: CourseSelection,
        meta: { roles: ['student'] }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: MyCourses,
        meta: { roles: ['student'] }
      },
      {
        path: 'course-calendar',
        name: 'CourseCalendar',
        component: CourseCalendar,
        meta: { roles: ['student', 'admin', 'teacher'] }
      },
      {
        path: 'weekly-schedule',
        name: 'WeeklySchedule',
        component: WeeklySchedule,
        meta: { roles: ['student', 'admin', 'teacher'] }
      },
      {
        path: 'notifications',
        name: 'NotificationList',
        component: NotificationList,
        meta: { roles: ['student', 'admin', 'teacher'] }
      },
      {
        path: 'notification-management',
        name: 'NotificationManagement',
        component: NotificationManagement,
        meta: { roles: ['admin', 'teacher'] }
      },
      {
        path: 'attachment-management',
        name: 'AttachmentManagement',
        component: AttachmentManagement,
        meta: { roles: ['admin', 'teacher'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { roles: ['student', 'admin', 'teacher'] }
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: ChangePassword,
        meta: { roles: ['student', 'admin', 'teacher'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(userStore.userType)) {
    // 权限不足，重定向到合适的页面
    if (userStore.userType === 'student') {
      next('/home/profile')
    } else {
      next('/home/students')
    }
  } else {
    next()
  }
})

export default router
