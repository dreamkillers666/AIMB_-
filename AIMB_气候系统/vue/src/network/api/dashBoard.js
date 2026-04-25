import { request } from '@/network/request'

export const getDashBoardInfo = (roleId, userId) => {
  return request({
    method: 'GET',
    url: '/back/getDashBoardInfo',
    params: {
      roleId: roleId,
      userId: userId
    }
  })
}
