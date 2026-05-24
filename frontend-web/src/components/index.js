import ProTable from './ProTable/index.vue'
import ProForm from './ProForm/index.vue'
import PageHeader from './PageHeader/index.vue'
import AuthButton from './AuthButton/index.vue'

const components = {
  ProTable,
  ProForm,
  PageHeader,
  AuthButton
}

export function setupComponents(app) {
  Object.keys(components).forEach(key => {
    app.component(key, components[key])
  })
}

export {
  ProTable,
  ProForm,
  PageHeader,
  AuthButton
}

export default components
