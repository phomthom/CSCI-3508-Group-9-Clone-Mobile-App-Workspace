import http from 'node:http'

export const fixture = {
  id: 1, title: 'Mock calculus textbook', price: 25, category: 'Textbooks',
  condition: 'Good', location: 'Mock campus', isbn: null,
  datetime: '2026-11-14T12:00:00', status: 'Active', user: 'mock_seller',
  image_url: null,
}
export function createMockBackend() {
  return http.createServer(async (req, res) => {
    const chunks = []
    for await (const chunk of req) chunks.push(chunk)
    let data
    try { data = JSON.parse(Buffer.concat(chunks).toString() || '{}') }
    catch { res.writeHead(400).end('{}'); return }
    const send = (status, value) => {
      res.writeHead(status, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify(value))
    }
    if (data.app_access !== 'mock-app-only') return send(401, { message: 'Not authorized.' })
    // Simulates the preserved adapter's service-account bootstrap; never uses a real account.
    if (req.url === '/LogIn' && req.method === 'POST' && data.email === 'service@mock.invalid' && data.password === 'mock-password-only')
      return send(201, { access_token: 'mock-service-token' })
    if (req.method !== 'GET') return send(405, { message: 'Mock marketplace is read-only.' })
    if (req.url === '/Admin' && data.access_token === 'mock-service-token') return send(200, { is_admin: true, level: 0 })
    if (data.admin_token !== 'mock-service-token') return send(401, { message: 'Not authorized.' })
    if (req.url === '/Listing') {
      const matches = (!data.category || data.category === fixture.category) && (!data.user_page || data.user_page === fixture.user) && (!data.isbn || data.isbn === fixture.isbn)
      // Production search ranks rather than excludes results. A one-item fixture retains it.
      return send(200, matches ? [fixture] : [])
    }
    if (req.url === '/Listing/1') return send(200, { ...fixture, description: 'Local mock response; no database.', first_name: 'Mock', last_name: 'Seller', email: 'private@mock.invalid', images_url: null })
    if (req.url.startsWith('/Listing/')) return send(400, 'Listing not found.')
    if (req.url === '/Category') return send(200, ['Textbooks', 'Electronics'])
    if (req.url === '/Condition') return send(200, ['New', 'Good'])
    return send(404, { message: 'Mock route not found.' })
  })
}
