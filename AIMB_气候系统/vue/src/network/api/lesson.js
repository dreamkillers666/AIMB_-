import {request} from "@/network/request"

export const getAllLessons = (activeName) => {
  return request({
    method: 'GET',
    url: '/frontLesson/getAllLessons',
    params: {
      activeName: activeName
    }
  })
}
export const getLessonsByUserIdAndTagId = (pageRequest) => {
  return request({
    method: 'POST',
    url: '/frontLesson/getLessons',
    data: pageRequest
  })
}
export const getLessonsByUserId = (userId) => {
  return request({
    method: 'GET',
    url: '/frontLesson/getLessonsByUserId',
    params: {
      userId: userId
    }
  })
}
export const getLessonsByName = (lessonName) => {
  return request({
    method: 'GET',
    url: '/frontLesson/getLessonsByName',
    params: {
      lessonName: lessonName
    }
  })
}
export const getLessonInfo = (lessonId) => {
  return request({
    method: 'GET',
    url: '/frontLesson/getLessonInfo',
    params: {
      lessonId: lessonId
    }
  })
}
export const getChapterInfo = (lessonId) => {
  return request({
    method: 'GET',
    url: '/frontLesson/getChapterInfoByLessonId',
    params: {
      lessonId: lessonId
    }
  })
}
export const getGuideBook = (sonId) => {
  return request({
    method: 'GET',
    url: '/frontLesson/getGuideBook',
    params: {
      sonId: sonId
    }
  })
}
export const onlineIDE = (codeInput) => {
  return request({
    method: 'GET',
    url: '/IDE/OnlineIDESub',
    params: {
      codeInput: codeInput
    }
  })
}
export const getUserJupyter = (sonChapterObj) => {
  return request({
    method: 'POST',
    url: '/frontLesson/getUserJupyter',
    data: sonChapterObj
  })
}
export const downloadChapterFile = (downloadUrl) => {
  return request({
    method: 'GET',
    url: '/frontLesson/downloadChapterFile',
    params: {
      downloadUrl: downloadUrl
    }
  })
}