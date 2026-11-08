import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, operator = false }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (operator && user.role !== 'OPERATOR') return <Navigate to="/" replace />
  return children
}
