const legacyRoutes = {
  introduction: '/usermanage/enso_introduce',
  results: '/usermanage/enso_forecast',
  data: '/usermanage/enso_data',
  resources: '/usermanage/enso_resource'
}

const modernRoutes = {
  introduction: '/enso/introduction',
  results: '/enso/results',
  data: '/enso/data',
  resources: '/enso/resources'
}

export default function ensoSectionRoute(currentPath, section) {
  const routes = currentPath.indexOf('/enso/') === 0 && currentPath.indexOf('/usermanage/') !== 0
    ? modernRoutes
    : legacyRoutes
  return routes[section]
}
