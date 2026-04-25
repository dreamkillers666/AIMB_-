import {request} from "@/network/request"

export const getTags = () => {
  return request({
    method: 'GET',
    url: '/backTag/getTags'
  })
}
