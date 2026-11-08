import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function SiteLayout({ children }) {
  const { user, logout } = useAuth()
  const { items } = useCart()
  const count = items.reduce((sum, item) => sum + item.quantity, 0)
  return (
    <div className="min-h-screen bg-cream text-ink">
      <header className="sticky top-0 z-10 border-b border-pink-100 bg-cream/95 backdrop-blur">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link className="font-display text-2xl font-black tracking-tight text-berry" to="/">scoop <span aria-hidden>🍦</span></Link>
          <div className="flex items-center gap-3 text-sm font-bold sm:gap-6">
            <NavLink to="/menu" className="hover:text-berry">Menu</NavLink>
            <NavLink to="/cart" className="hover:text-berry">Cart{count > 0 && ` (${count})`}</NavLink>
            {user?.role === 'OPERATOR' && <NavLink to="/operator" className="hover:text-berry">Operator</NavLink>}
            {user ? <button onClick={logout} className="text-berry">Sign out</button> : <NavLink to="/login" className="text-berry">Sign in</NavLink>}
          </div>
        </nav>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:py-12">{children}</main>
      <footer className="px-4 pb-8 text-center text-sm text-ink/60">Small-batch scoops, wherever you are.</footer>
    </div>
  )
}
