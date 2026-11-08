import { createContext, useContext, useMemo, useState } from 'react'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('scoop-user')) } catch { return null }
  })

  const storeSession = (session) => {
    localStorage.setItem('scoop-token', session.accessToken)
    localStorage.setItem('scoop-user', JSON.stringify(session.user))
    setUser(session.user)
  }

  const loginWithGoogle = async (credential) => {
    const { data } = await api.post('/auth/google', { credential })
    storeSession(data)
  }

  const demoLogin = async () => {
    const { data } = await api.post('/auth/demo')
    storeSession(data)
  }

  const logout = () => {
    localStorage.removeItem('scoop-token')
    localStorage.removeItem('scoop-user')
    setUser(null)
  }

  const value = useMemo(() => ({ user, loginWithGoogle, demoLogin, logout }), [user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
