import {request} from "@/network/request"

export const getToolsByName = (toolName) => {
  return request({
    method: 'GET',
    url: '/backTool/getToolsByName',
    params: {
      toolName: toolName
    }
  })
}
export const getAllTools = (currentPage, pageSize) => {
  return request({
    method: 'GET',
    url: '/backTool/getAllTools',
    params: {
      currentPage: currentPage,
      pageSize: pageSize
    }
  })
}
