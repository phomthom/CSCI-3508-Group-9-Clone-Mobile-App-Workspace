import { once } from 'node:events'
import { createWebsiteServer } from './app.mjs'
import { createMockBackend } from './mock-backend.mjs'

const mock = createMockBackend()
mock.listen(0, '127.0.0.1')
await once(mock, 'listening')
const adapter = createWebsiteServer({
  backend: `http://127.0.0.1:${mock.address().port}/`,
  appAccess: 'mock-app-only', user: 'service@mock.invalid', userPass: 'mock-password-only',
  production: false,
})
adapter.listen(3100, '127.0.0.1')
await once(adapter, 'listening')
console.log('Mock-only development API: http://127.0.0.1:3100/api/listings')
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => {
  adapter.closeAllConnections(); mock.closeAllConnections()
  adapter.close(); mock.close()
})
