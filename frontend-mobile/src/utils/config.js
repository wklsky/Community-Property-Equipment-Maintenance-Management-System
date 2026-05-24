const ENV = {
  development: {
    BASE_URL: 'http://localhost:8080/api/v1',

  },
  production: {
    BASE_URL: 'http://your-production-server.com/api/v1',
  }
}

const getEnv = () => {

  return process.env.NODE_ENV || 'development'

  try {
    const systemInfo = uni.getSystemInfoSync()

    if (systemInfo.appVersion) {
      return 'production'
    }
  } catch (e) {

  }
  return 'development'

}

const currentEnv = getEnv()
const config = ENV[currentEnv] || ENV.development

export const BASE_URL = config.BASE_URL

export default config
