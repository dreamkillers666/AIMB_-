import { request } from '@/network/request'

export const getMenuTree = (userId) => {
  return request({
    method: 'GET',
    url: '/permission/getMenu',
    params: {
      userId: userId
    }
  })
}

export const getUserPermission = (userId) => {
  return request({
    method: 'GET',
    url: '/permission/getUserPermission',
    params: {
      userId: userId
    }
  })
}
