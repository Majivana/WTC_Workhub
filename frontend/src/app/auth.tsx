import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/resources'
import { sessionToken, setUnauthorizedHandler } from '../api/client'
import type { CurrentUser } from '../types/domain'

interface AuthContextValue {
  user: CurrentUser | null
  loading: boolean
  signIn: (username: string, password: string) => Promise<void>
  signOut: () => void
  has: (...permissions: string[]) => boolean
  hasRole: (...roles: string[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null)
      navigate('/login', { replace: true, state: { expired: true } })
    })
    if (!sessionToken.get()) { setLoading(false); return }
    authApi.me().then(setUser).catch(() => { sessionToken.clear(); setUser(null) }).finally(() => setLoading(false))
    return () => setUnauthorizedHandler(undefined)
  }, [navigate])

  async function signIn(username: string, password: string) {
    const session = await authApi.login(username, password)
    sessionToken.set(session.token)
    try {
      const profile = await authApi.me()
      setUser(profile)
      navigate('/app', { replace: true })
    } catch (error) {
      sessionToken.clear()
      throw error
    }
  }

  function signOut() {
    sessionToken.clear()
    setUser(null)
    navigate('/login', { replace: true })
  }

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    signIn,
    signOut,
    has: (...permissions) => permissions.some(permission => user?.permissions.includes(permission)),
    hasRole: (...roles) => roles.includes(user?.systemRole ?? ''),
  }), [user, loading])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used within AuthProvider')
  return value
}
