import { request } from '@/network/request'

export const getClazzList = (pageRequest) => {
  return request({
    method: 'POST',
    url: '/backClazz/getClazzList',
    data: pageRequest
  })
}

export const addClazz = (clazzObj) => {
  return request({
    method: 'POST',
    url: '/backClazz/addClazz',
    data: clazzObj
  })
}

export const updateClazz = (clazzObj) => {
  return request({
    method: 'POST',
    url: '/backClazz/updateClazz',
    data: clazzObj
  })
}

export const deleteClazz = (clazzId) => {
  return request({
    method: 'GET',
    url: '/backClazz/deleteClazz',
    params: {
      clazzId: clazzId
    }
  })
}

export const getStudentsByClazzId = (clazzId, currentPage, pageSize) => {
  return request({
    method: 'GET',
    url: '/backClazz/getStudents',
    params: {
      clazzId: clazzId,
      currentPage: currentPage,
      pageSize: pageSize
    }
  })
}
