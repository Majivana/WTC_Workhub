const configuredBase = import.meta.env.VITE_API_BASE_URL?.trim() || '/api'
const API_BASE = configuredBase.replace(/\/$/, '')
const TOKEN_KEY = 'workhub.session'

export type ApiFailureKind = 'network' | 'unauthorized' | 'forbidden' | 'not-found' | 'conflict' | 'validation' | 'server'

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly kind: ApiFailureKind,
    readonly code?: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

let unauthorizedHandler: (() => void) | undefined
export function setUnauthorizedHandler(handler?: () => void) { unauthorizedHandler = handler }

export const sessionToken = {
  get: () => sessionStorage.getItem(TOKEN_KEY),
  set: (token: string) => sessionStorage.setItem(TOKEN_KEY, token),
  clear: () => sessionStorage.removeItem(TOKEN_KEY),
}

function failureKind(status: number): ApiFailureKind {
  if (status === 401) return 'unauthorized'
  if (status === 403) return 'forbidden'
  if (status === 404) return 'not-found'
  if (status === 409) return 'conflict'
  if (status >= 400 && status < 500) return 'validation'
  return 'server'
}

async function readError(response: Response): Promise<ApiError> {
  const kind = failureKind(response.status)
  let payload: { code?: string; message?: string } = {}
  try { payload = await response.json() as typeof payload } catch { /* body can be empty */ }
  const messages: Record<ApiFailureKind, string> = {
    network: 'The service could not be reached. Check your connection and try again.',
    unauthorized: 'Your session has expired. Sign in again to continue.',
    forbidden: 'Your account does not have permission to do that.',
    'not-found': 'The requested record could not be found.',
    conflict: 'This record has changed or cannot be updated in its current state.',
    validation: 'Check the details and try again.',
    server: 'The service could not complete the request. Try again shortly.',
  }
  return new ApiError(payload.message || messages[kind], response.status, kind, payload.code)
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  const token = sessionToken.get()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (init.body && !(init.body instanceof FormData) && !(init.body instanceof Blob)) {
    headers.set('Content-Type', 'application/json')
  }
  let response: Response
  try {
    response = await fetch(`${API_BASE}${path.startsWith('/') ? path : `/${path}`}`, {
      ...init,
      headers,
      credentials: 'same-origin',
    })
  } catch {
    throw new ApiError('The service could not be reached. Check that Workhub is running and try again.', 0, 'network')
  }
  if (response.status === 401) {
    sessionToken.clear()
    unauthorizedHandler?.()
  }
  if (!response.ok) throw await readError(response)
  if (response.status === 204) return undefined as T
  if (response.headers.get('content-type')?.includes('text/csv')) return await response.text() as T
  return await response.json() as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
  upload: <T>(path: string, body: Blob, mediaType: string) => request<T>(path, { method: 'PUT', headers: { 'Content-Type': mediaType }, body }),
  download: async (path: string) => {
    const headers = new Headers()
    const token = sessionToken.get()
    if (token) headers.set('Authorization', `Bearer ${token}`)
    const response = await fetch(`${API_BASE}${path}`, { headers, credentials: 'same-origin' })
    if (response.status === 401) { sessionToken.clear(); unauthorizedHandler?.() }
    if (!response.ok) throw await readError(response)
    return response.blob()
  },
}

