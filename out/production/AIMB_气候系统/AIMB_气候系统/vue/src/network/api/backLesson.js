import { request } from '@/network/request'

export const getAllLessons = () => {
  return request({
    method: 'GET',
    url: '/backLesson/getAllLessons'
  })
}
export const getLessonsByUserId = (pageRequest) => {
  return request({
    method: 'POST',
    url: '/backLesson/getLessonsByUserId',
    data: pageRequest
  })
}
export const getLessonsByTagId = (pageRequest) => {
  return request({
    method: 'POST',
    url: '/backLesson/getLessonsByTagId',
    data: pageRequest
  })
}
export const uploadFile = (formData) => {
  return request({
    method: 'POST',
    url: '/backLesson/uploadFile',
    data: formData
  })
}
export const uploadChapterFile = (formData) => {
  return request({
    method: 'POST',
    url: '/backLesson/uploadChapterFile',
    data: formData
  })
}
export const uploadLessonFile = (formData) => {
  return request({
    method: 'POST',
    url: '/backFile/uploadLessonFile',
    data: formData
  })
}
export const createDirectory = (path) => {
  return request({
    method: 'GET',
    url: '/backFile/createDirectory',
    params: {
      path: path
    }
  })
}
export const deleteLessonFile = (filePath, isFile) => {
  return request({
    method: 'GET',
    url: '/backFile/deleteLessonFile',
    params: {
      filePath: filePath,
      isFile: isFile
    }
  })
}
export const parseJupyter = (formData) => {
  return request({
    method: 'POST',
    url: '/backLesson/parseJupyter',
    data: formData
  })
}
export const addLesson = (lessonObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/addLesson',
    data: lessonObj
  })
}
export const updateLesson = (lessonObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/updateLesson',
    data: lessonObj
  })
}
export const getLessonDetail = (lessonId) => {
  return request({
    method: 'GET',
    url: '/backLesson/getLessonDetail',
    params: {
      lessonId: lessonId
    }
  })
}
export const getChapterInfo = (lessonId) => {
  return request({
    method: 'GET',
    url: '/backLesson/getChapterInfoByLessonId',
    params: {
      lessonId: lessonId
    }
  })
}
export const findLessonsByName = (pageRequest) => {
  return request({
    method: 'POST',
    url: '/backLesson/findLessonsByName',
    data: pageRequest
  })
}
export const addChapter = (chapterObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/addChapter',
    data: chapterObj
  })
}
export const updateChapter = (chapterObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/updateChapter',
    data: chapterObj
  })
}
export const delChapter = (lessonName, chapterId, chapterName) => {
  return request({
    method: 'GET',
    url: '/backLesson/deleteChapter',
    params: {
      lessonName: lessonName,
      chapterId: chapterId,
      chapterName: chapterName
    }
  })
}
export const delSonChapter = (lessonName, chapterName, sonId, sonName) => {
  return request({
    method: 'GET',
    url: '/backLesson/delSonChapter',
    params: {
      lessonName: lessonName,
      chapterName: chapterName,
      sonId: sonId,
      sonName: sonName
    }
  })
}
export const addSonChapter = (chapterObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/addSonChapter',
    data: chapterObj
  })
}
export const editSonChapter = (chapterObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/updateSonChapter',
    data: chapterObj
  })
}
export const addSonChapterBook = (sonChapterObj) => {
  return request({
    method: 'POST',
    url: '/backLesson/addSonChapterBook',
    data: sonChapterObj
  })
}
export const deleteLessonById = (lessonId, lessonName) => {
  return request({
    method: 'GET',
    url: '/backLesson/deleteLesson',
    params: {
      lessonId: lessonId,
      lessonName: lessonName
    }
  })
}
export const getEditSonChapterInfo = (sonId) => {
  return request({
    method: 'GET',
    url: '/backLesson/getEditSonChapterInfo',
    params: {
      sonId: sonId
    }
  })
}
export const getFileTrees = () => {
  return request({
    method: 'POST',
    url: '/backLesson/getFileTrees'
  })
}
