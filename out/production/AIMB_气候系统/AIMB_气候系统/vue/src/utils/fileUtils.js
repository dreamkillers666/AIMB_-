export function getFormData(params) {
    let formData = new FormData()
    formData.append('file', params.file)
    return formData
}
