import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Cart() {
  const { items, changeQuantity, clear, total } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const placeOrder = async () => {
    if (!user) return navigate('/login')
    setSubmitting(true)
    setError('')
    try {
      const { data } = await api.post('/orders', { items: items.map(({ id, quantity }) => ({ flavorId: id, quantity })) })
      clear()
      navigate(`/orders/${data.id}`)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'We could not place the order. Please try again.')
    } finally { setSubmitting(false) }
  }

  return (
    <section className="mx-auto max-w-3xl">
      <h1 className="font-display text-4xl font-black">Your little treat</h1>
      {error && <p role="alert" className="mt-4 rounded-xl bg-rose-100 p-4 text-rose-800">{error}</p>}
      {items.length === 0 ? <div className="mt-8 rounded-3xl bg-white p-8 text-center shadow-soft"><p>Your cart is waiting for something sweet.</p><Link className="mt-4 inline-block font-extrabold text-berry" to="/menu">Find a flavor →</Link></div> : (
        <div className="mt-7 space-y-3">
          {items.map((item) => <div key={item.id} className="flex items-center justify-between gap-4 rounded-2xl bg-white p-4 shadow-soft">
            <div><h2 className="font-extrabold">{item.name}</h2><p className="text-sm text-ink/60">${Number(item.price).toFixed(2)} each</p></div>
            <div className="flex items-center gap-3"><button aria-label={`Remove one ${item.name}`} onClick={() => changeQuantity(item.id, item.quantity - 1)} className="h-10 w-10 rounded-full bg-cream text-xl">−</button><span className="w-5 text-center font-bold">{item.quantity}</span><button aria-label={`Add one ${item.name}`} onClick={() => changeQuantity(item.id, item.quantity + 1)} className="h-10 w-10 rounded-full bg-mint text-xl">+</button></div>
            <strong className="w-16 text-right">${(Number(item.price) * item.quantity).toFixed(2)}</strong>
          </div>)}
          <div className="flex items-center justify-between rounded-2xl bg-white p-5 font-extrabold shadow-soft"><span>Total</span><span>${total.toFixed(2)}</span></div>
          <p className="text-sm text-ink/60">No payment needed — we’ll confirm your order with the truck.</p>
          <button onClick={placeOrder} disabled={submitting} className="min-h-14 w-full rounded-full bg-berry font-extrabold text-white disabled:opacity-60">{submitting ? 'Sending your order…' : user ? 'Place order' : 'Sign in to place your order'}</button>
        </div>
      )}
    </section>
  )
}
