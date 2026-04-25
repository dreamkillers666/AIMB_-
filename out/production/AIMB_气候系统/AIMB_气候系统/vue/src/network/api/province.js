import { request } from '@/network/request'

export const getProvinces = () => {
  return request({
    method: 'GET',
    url: '/province/getAllProvinces'
  })
}
