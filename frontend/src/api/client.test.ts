import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, api, sessionToken } from './client'

describe('central API client', () => {
  afterEach(() => sessionToken.clear())

  it('sends the session bearer token and parses the backend response', async () => {
    sessionToken.set('unit-test-token')
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ id: 'current-user' }), {
      status: 200,
      headers: { 'content-type': 'application/json' },
    }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(api.get<{ id: string }>('/me')).resolves.toEqual({ id: 'current-user' })
    expect(fetchMock).toHaveBeenCalledWith('/api/me', expect.objectContaining({
      headers: expect.any(Headers),
      credentials: 'same-origin',
    }))
    const headers = fetchMock.mock.calls[0][1].headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer unit-test-token')
  })

  it('normalizes forbidden responses and preserves the backend message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({
      code: 'ACCESS_DENIED', message: 'This record is outside your assignment.',
    }), { status: 403, headers: { 'content-type': 'application/json' } })))

    await expect(api.get('/private')).rejects.toMatchObject<ApiError>({
      status: 403,
      kind: 'forbidden',
      message: 'This record is outside your assignment.',
      code: 'ACCESS_DENIED',
    })
  })
})
