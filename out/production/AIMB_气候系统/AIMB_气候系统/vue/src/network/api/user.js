import {request} from "@/network/request"
export const login = (userObj) => {
  return request({
    method: 'POST',
    url: '/user/frontLogin',
    data: userObj
  })
}
export const changePassword = (userObj) => {
  return request({
    method: 'POST',
    url: '/frontUser/changePassword',
    data: userObj
  })
}
export const getPersonInfo = (userId) => {
  return request({
    method: 'GET',
    url: '/frontUser/getPersonInfo',
    params: {
      userId: userId
    }
  })
}
export const updatePersonInfo = (userObj) => {
  return request({
    method: 'POST',
    url: '/frontUser/updatePersonInfo',
    data: userObj
  })
}