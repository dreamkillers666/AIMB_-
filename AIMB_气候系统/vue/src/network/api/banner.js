import {request} from "@/network/request"

export const getAllBanners = () => {
  return request({
    method: 'GET',
    url: '/frontBanner/getAllBanners'
  })
}
