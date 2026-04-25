import { request } from '@/network/request'

export const getAllBanners = (currentPage, pageSize) => {
  return request({
    method: 'GET',
    url: '/backBanner/getAllBanner',
    params: {
      currentPage: currentPage,
      pageSize: pageSize
    }
  })
}

export const updateBanner = (bannerObj) => {
  return request({
    method: 'POST',
    url: '/backBanner/updateBanner',
    data: bannerObj
  })
}
