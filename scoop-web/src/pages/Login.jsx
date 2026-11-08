import { GoogleLogin } from '@react-oauth/google'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { loginWithGoogle, demoLogin } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const googleSuccess = async (result) => {
    if (!result.credential) return setError('Google did not return a sign-in credential.')
    setBusy(true)
    try { await loginWithGoogle(result.credential); navigate('/') }
    catch (requestError) { setError(requestError.response?.data?.message || 'Sign in did not work. Please try again.') }
    finally { setBusy(false) }
  }
  const demo = async () => {
    setBusy(true)
    try { await demoLogin(); navigate('/operator') }
    catch (requestError) { setError(requestError.response?.data?.message || 'Demo login is unavailable.') }
    finally { setBusy(false) }
  }
  return (
    <section className="mx-auto max-w-md rounded-3xl bg-white p-7 text-center shadow-soft sm:p-10">
      <div className="text-6xl">🍨</div><h1 className="mt-3 font-display text-3xl font-black">Welcome to the scoop club</h1>
      <p className="my-5 text-ink/60">Sign in to place an order and keep an eye on your scoops.</p>
      {error && <p role="alert" className="mb-4 rounded-xl bg-rose-100 p-3 text-sm text-rose-800">{error}</p>}
      {busy ? <p>Signing you in…</p> : <div className="flex justify-center"><GoogleLogin onSuccess={googleSuccess} onError={() => setError('Google sign-in failed. Please try again.')} /></div>}
      {import.meta.env.DEV && <button onClick={demo} disabled={busy} className="mt-5 min-h-12 w-full rounded-full bg-mint font-extrabold text-emerald-950">Continue as demo operator</button>}
    </section>
  )
}
