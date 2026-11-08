import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api from '../api/client'

const statusMessage = {
  PENDING: 'Your order is in the queue.',
  PREPARING: 'Our crew is making your scoops.',
  READY: 'It’s ready! Come pick it up.',
  SERVED: 'Enjoy every bite!',
  CANCELLED: 'This order was cancelled.',
}

export default function OrderStatus() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => {
    let active = true
    const load = () => api.get(`/orders/${id}`).then(({ data }) => { if (active) { setOrder(data); setError('') } })
      .catch((requestError) => { if (active) setError(requestError.response?.data?.message || 'We could not load this order.') })
    load()
    const interval = window.setInterval(load, 10000)
    return () => { active = false; window.clearInterval(interval) }
  }, [id])
  return (
    <section className="mx-auto max-w-xl rounded-3xl bg-white p-8 text-center shadow-soft">
      <div className="text-6xl">🍦</div><p className="mt-4 font-extrabold uppercase tracking-widest text-berry">Order #{id}</p>
      {error && <p role="alert" className="mt-4 text-rose-700">{error}</p>}
      {!order && !error && <p className="mt-5">Finding your order…</p>}
      {order && <>
        <h1 className="mt-2 font-display text-3xl font-black">{statusMessage[order.status] || order.status}</h1>
        <p className="mt-2 text-ink/60">Status updates automatically every 10 seconds.</p>
        <div className="my-6 rounded-2xl bg-cream p-4 text-left">{order.items.map((item) => <p key={item.flavorId} className="py-1">{item.quantity} × {item.flavorName}</p>)}<p className="mt-3 border-t border-ink/10 pt-3 font-extrabold">Total · ${Number(order.total).toFixed(2)}</p></div>
      </>}
      <Link to="/menu" className="font-extrabold text-berry">Back to the menu</Link>
    </section>
  )
}
