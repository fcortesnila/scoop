import { Navigate, Route, Routes } from 'react-router-dom'
import SiteLayout from './components/SiteLayout'
import ProtectedRoute from './components/ProtectedRoute'
import Cart from './pages/Cart'
import Home from './pages/Home'
import Login from './pages/Login'
import Menu from './pages/Menu'
import Operator from './pages/Operator'
import OrderStatus from './pages/OrderStatus'

export default function App() {
  return (
    <SiteLayout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/menu" element={<Menu />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/login" element={<Login />} />
        <Route path="/orders/:id" element={<ProtectedRoute><OrderStatus /></ProtectedRoute>} />
        <Route path="/operator" element={<ProtectedRoute operator><Operator /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </SiteLayout>
  )
}
