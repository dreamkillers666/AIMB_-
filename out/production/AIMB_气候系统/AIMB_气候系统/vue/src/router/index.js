import Vue from 'vue'
import VueRouter from 'vue-router'
import store from "@/store";

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: () => import('../views/Manage.vue'),
    redirect: "/login",
    children: [
      { path: 'home', name: '首页', component: () => import('../views/Home.vue')},
      { path: 'user', name: '账户管理', component: () => import('../views/User.vue')},
      { path: 'emp', name: '员工管理', component: () => import('../views/Emp.vue')},
      { path: 'person', name: '个人信息', component: () => import('../views/Person.vue')},
      { path: 'password', name: '修改密码', component: () => import('../views/Password.vue')},
      { path: 'file', name: '文件管理', component: () => import('../views/File.vue')},

      { path: 'feedback', name: '用户反馈', component: () => import('../views/Feedback.vue')},
      { path: 'building', name: '产业管理', component: () => import('../views/Building.vue')},
      {
        path: 'model',
        name: '模型数据',
        component: () => import('../views/Model'),
      },
      {
        path: 'grid_data',
        name: '气候数据展示',
        component: () => import('../views/Grid_data'),
      },
      {
        path: 'enso_data',
        name: 'enso数据',
        component: () => import('../views/Enso_data'),
      }
    ]
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register')
  },
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue')
  },


  {
    path: '/usermanage',
    name: 'User_Manage',
    component: () => import('../views/User_Manage'),
    children: [

      { path: 'person', name: '个人信息', component: () => import('../views/Person.vue')},
      { path: 'password', name: '修改密码', component: () => import('../views/Password.vue')},

      { path: 'video', name: '视频播放', component: () => import('../views/Video.vue')},
      { path: 'im', name: '聊天室', component: () => import("../views/Im.vue")},
      { path: 'ai', name: '智能AI', component: () => import("../views/AI.vue")},
      { path: 'videoDetail', name: 'VideoDetail', component: () => import('../views/VideoDetail.vue')},
      { path: 'feedback', name: '用户反馈', component: () => import('../views/User_FeedBack.vue')},
      { path: 'comment', name: '多级评论', component: () => import('../views/Comment.vue')},



      {
        path: 'introduce',
        name: '简介',
        component: () => import('../views/introducePage.vue'),
      },
      {
        path: 'enso_introduce',
        name: 'ENSO介绍',
        component: () => import('../views/ENSO_introduce.vue'),
      },
      {
        path: 'enso_forecast',
        name: 'ENSO预测',
        component: () => import('../views/ENSO/ENSO_forecast'),
      },
      {
        path: 'enso_data',
        name: 'ENSO数据',
        component: () => import('../views/ENSO/ENSO_data'),
      },
      {
        path: 'enso_resource',
        name: '更多ENSO信息',
        component: () => import('../views/ENSO/ENSO_resource'),
      },

      {
        path: 'mjo_introduce',
        name: 'MJO介绍',
        component: () => import('../views/MJO_introduce'),
      },
      {
        path: 'mjo_forecast',
        name: 'MJO预测',
        component: () => import('../views/MJO/MJO_forecast'),
      },
      {
        path: 'mjo_data',
        name: 'MJO数据',
        component: () => import('../views/MJO/MJO_data'),
      },
      {
        path: 'mjo_resource',
        name: '更多MJO信息',
        component: () => import('../views/MJO/MJO_resource'),
      },


      {
        path: 'iod_introduce',
        name: 'IOD介绍',
        component: () => import('../views/IOD_introduce'),
      },
      {
        path: 'iod_data',
        name: 'MJO数据',
        component: () => import('../views/IOD/IOD_data'),
      },
      {
        path: 'iod_resource',
        name: '更多IOD信息',
        component: () => import('../views/IOD/IOD_resource'),
      },

      {
        path: 'others_introduce',
        name: '其它现象',
        component: () => import('../views/Others_introduce'),
      },
      {
        path: 'enso_pre',
        name: 'ENSO预测产品',
        component: () => import('../views/enso_prediction.vue'),
      },
      {
        path: 'mjo_pre',
        name: 'MJO预测产品',
        component: () => import('../views/mjo_prediction'),
      },

      {
        path: 'mjo_pre',
        name: 'MJO预测产品',
        component: () => import('../views/mjo_prediction.vue'),
      },
      {
        path: 'rainfall_pre',
        name: 'rainfall预测产品',
        component: () => import('../views/rainfall_prediction.vue'),
      },
      {
        path: 'teamPage',
        name: '团队成果',
        component: () => import('../views/teamPage.vue'),
      },
      {
        path: 'user_model',
        name: '模型数据',
        component: () => import('../views/User_Model'),
      },
      {
        path: 'user_gridData',
        name: '气候数据',
        component: () => import('../views/User_GridData'),
      },
      {
        path: 'connect',
        name: '联系',
        component: () => import('../views/connect'),
      },
      {
        path: 'data_introduction',
        name: '数据集介绍',
        component: () => import('../views/data_introduction.vue'),
      },
      {
        path: 'data_download',
        name: '数据集下载',
        component: () => import('../views/User_downloadData'),
      },
      {
        path: 'seasonal_introduce',
        name: '降水介绍',
        component: () => import('../views/seasonal_introduce.vue'),
      },
      {
        path: 'rainfall_forecast',
        name: '降水预测',
        component: () => import('../views/rainfall forecast/rainfall_forecast.vue'),
      },
      {
        path: 'rainfall_data',
        name: '降水数据',
        component: () => import('../views/rainfall forecast/rainfall_data.vue'),
      },
      {
        path: 'rainfall_resource',
        name: '降水资源',
        component: () => import('../views/rainfall forecast/rainfall_resource.vue'),
      },
      {
        path: 'forecastenso',
        name: 'forecastenso',
        component: () => import('../views/forecast/forecastenso.vue'),
      },

    ]
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  localStorage.setItem("currentPathName", to.name)  // 设置当前的路由名称，为了在Header组件中去使用
  store.commit("setPath")  // 触发store的数据更新

  // 未找到路由的情况
  if (!to.matched.length) {
    next("/404")
  }
  // 其他的情况都放行
  next()
})

export default router

export class resetRouter {
}