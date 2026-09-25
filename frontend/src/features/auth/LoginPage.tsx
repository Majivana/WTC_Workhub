import { useState, type FormEvent } from 'react'
import { useLocation } from 'react-router-dom'
import { KeyRound } from 'lucide-react'
import { useAuth } from '../../app/auth'
import { ApiError } from '../../api/client'

export function LoginPage() {
  const { signIn } = useAuth()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const expired = Boolean((location.state as { expired?: boolean } | null)?.expired)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try { await signIn(username.trim(), password) }
    catch (cause) { setError(cause instanceof ApiError && cause.status === 401 ? 'The username or password is incorrect.' : cause instanceof Error ? cause.message : 'Sign in could not be completed.') }
    finally { setBusy(false) }
  }

  return <main className="login-page">
    <section className="login-panel" aria-labelledby="login-title">
      <div className="login-brand"><div className="brand-mark">W</div><span>WeThinkCode_ <b>/</b> Workhub</span></div>
      <div className="login-heading"><span className="eyebrow">INSTITUTIONAL WORKSPACE</span><h1 id="login-title">Sign in to Workhub</h1><p>Use your Workhub account to continue.</p></div>
      {expired && <div className="alert alert-neutral" role="status">Your session expired. Sign in again to continue.</div>}
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      <form className="form-stack" onSubmit={submit}>
        <label className="field"><span>Username</span><input autoComplete="username" autoFocus required value={username} onChange={event => setUsername(event.target.value)} /></label>
        <label className="field"><span>Password</span><input type="password" autoComplete="current-password" required value={password} onChange={event => setPassword(event.target.value)} /></label>
        <button className="button button-primary button-wide" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'} <KeyRound size={16} /></button>
      </form>
      <p className="login-note">Access is managed by your institution. Contact your supervisor or administrator if you need an account.</p>
    </section>
    <aside className="login-aside"><div className="aside-rule" /><p>ATTENDANCE · WORK · VERIFICATION</p><h2>A clear record of the work behind every hour.</h2><span>Record your attendance and work. Keep evidence with each submission. Follow review outcomes in one place.</span><small>WTC WORKHUB · INTERNAL SERVICE</small></aside>
  </main>
}
