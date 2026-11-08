import { useCallback, useEffect, useState } from 'react'
import api from '../api/client'
import AiPanel from '../components/AiPanel'

const nextStatus = { PENDING: ['PREPARING', 'Start'], PREPARING: ['READY', 'Ready'], READY: ['SERVED', 'Served'] }
const tabs = ['Queue', 'Walk-up', 'Today']

export default function Operator() {
  const [tab, setTab] = useState('Queue')
  const [queue, setQueue] = useState([])
  const [flavors, setFlavors] = useState([])
  const [summary, setSummary] = useState(null)
  const [stop, setStop] = useState(undefined)
  const [quantities, setQuantities] = useState({})
  const [stopName, setStopName] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadQueue = useCallback(async () => {
    try { const { data } = await api.get('/operator/orders'); setQueue(data); setError('') }
    catch (requestError) { setError(requestError.response?.data?.message || 'Could not load the order queue.') }
  }, [])

  useEffect(() => {
    loadQueue()
    const timer = window.setInterval(loadQueue, 10000)
    api.get('/flavors').then(({ data }) => setFlavors(data)).catch(() => setError('Could not load flavors.'))
    api.get('/operator/summary/daily').then(({ data }) => setSummary(data)).catch(() => {})
    api.get('/stops/current').then(({ data }) => { setStop(data); if (data) setStopName(data.name) }).catch(() => {})
    return () => window.clearInterval(timer)
  }, [loadQueue])

  const updateStatus = async (id, status) => {
    setError('')
    try { await api.patch(`/operator/orders/${id}`, { status }); await loadQueue() }
    catch (requestError) { setError(requestError.response?.data?.message || 'Could not update this order.') }
  }

  const createWalkup = async (event) => {
    event.preventDefault()
    const items = Object.entries(quantities).filter(([, quantity]) => quantity > 0)
      .map(([flavorId, quantity]) => ({ flavorId: Number(flavorId), quantity: Number(quantity) }))
    if (!items.length) return setError('Add at least one scoop to the walk-up order.')
    setError('')
    try {
      await api.post('/operator/orders', { items })
      setQuantities({})
      setMessage('Walk-up order added to the queue.')
      await loadQueue()
    } catch (requestError) { setError(requestError.response?.data?.message || 'Could not create the walk-up order.') }
  }

  const startStop = async (suggestion) => {
    setError('')
    try {
      const location = suggestion || await new Promise((resolve, reject) => {
        if (!navigator.geolocation) return reject(new Error('Geolocation is not available in this browser.'))
        navigator.geolocation.getCurrentPosition(({ coords }) => resolve({ latitude: coords.latitude, longitude: coords.longitude }),
          () => reject(new Error('Location permission was denied.')))
      })
      const name = suggestion?.stopName || stopName.trim()
      if (!name) return setError('Enter a stop name first.')
      const { data } = await api.post('/operator/stops', { name, latitude: location.latitude, longitude: location.longitude })
      setStop(data)
      setStopName(data.name)
      setMessage(`Now serving at ${data.name}.`)
    } catch (requestError) { setError(requestError.response?.data?.message || requestError.message || 'Could not start this stop.') }
  }

  const endStop = async () => {
    setError('')
    try { await api.post('/operator/stops/end'); setStop(null); setMessage('Stop ended.') }
    catch (requestError) { setError(requestError.response?.data?.message || 'Could not end the stop.') }
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div><p className="font-extrabold uppercase tracking-widest text-berry">Truck control</p><h1 className="font-display text-4xl font-black">Hey, operator.</h1></div>
        <div className={`rounded-full px-4 py-2 text-sm font-black ${stop ? 'bg-mint text-emerald-950' : 'bg-stone-200 text-stone-600'}`}>{stop ? `OPEN · ${stop.name}` : 'CLOSED · between stops'}</div>
      </div>
      {(error || message) && <p role={error ? 'alert' : 'status'} className={`mt-5 rounded-xl p-3 ${error ? 'bg-rose-100 text-rose-800' : 'bg-mint/60 text-emerald-950'}`}>{error || message}</p>}
      <div className="mt-6 rounded-3xl bg-white p-5 shadow-soft sm:p-7">
        <h2 className="font-display text-xl font-black">Stop controls</h2>
        {stop ? <div className="mt-4 flex flex-wrap items-center justify-between gap-4"><p>Currently serving at <strong>{stop.name}</strong></p><button onClick={endStop} className="min-h-12 rounded-full bg-ink px-6 font-extrabold text-white">End stop</button></div> :
          <div className="mt-4 flex flex-col gap-3 sm:flex-row"><input value={stopName} onChange={(event) => setStopName(event.target.value)} placeholder="Stop name" className="min-h-12 flex-1 rounded-xl border border-ink/15 px-4" /><button onClick={() => startStop()} className="min-h-12 rounded-full bg-berry px-6 font-extrabold text-white">Start stop · use my location</button></div>}
      </div>

      <div className="mt-7 flex gap-2 border-b border-ink/10">{tabs.map((name) => <button key={name} onClick={() => setTab(name)} className={`min-h-12 px-4 font-extrabold ${tab === name ? 'border-b-4 border-berry text-berry' : 'text-ink/55'}`}>{name}</button>)}</div>
      <div className="mt-6 grid items-start gap-6 lg:grid-cols-[1.2fr_.8fr]">
        <section>
          {tab === 'Queue' && <div className="space-y-4">
            <div className="flex items-center justify-between"><h2 className="font-display text-2xl font-black">Live order queue</h2><button onClick={loadQueue} className="rounded-full bg-white px-4 py-2 text-sm font-bold shadow">Refresh</button></div>
            {queue.length === 0 && <p className="rounded-2xl bg-white p-6 text-ink/60 shadow-soft">No orders waiting. Time for a tiny dance.</p>}
            {queue.map((order) => <article key={order.id} className="rounded-3xl bg-white p-5 shadow-soft"><div className="flex flex-wrap justify-between gap-2"><h3 className="text-lg font-black">Order #{order.id}</h3><span className="rounded-full bg-[#f7d8e2] px-3 py-1 text-xs font-black">{order.status}</span></div><p className="mt-2 text-ink/70">{order.items.map((item) => `${item.quantity} × ${item.flavorName}`).join(' · ')}</p><div className="mt-4 flex items-center justify-between"><strong>${Number(order.total).toFixed(2)}</strong>{nextStatus[order.status] && <button onClick={() => updateStatus(order.id, nextStatus[order.status][0])} className="min-h-12 rounded-full bg-mint px-6 font-black text-emerald-950">{nextStatus[order.status][1]} →</button>}</div></article>)}
          </div>}
          {tab === 'Walk-up' && <form onSubmit={createWalkup} className="rounded-3xl bg-white p-5 shadow-soft sm:p-7"><h2 className="font-display text-2xl font-black">Quick walk-up order</h2><div className="mt-4 space-y-2">{flavors.map((flavor) => <label key={flavor.id} className="flex items-center justify-between gap-3 rounded-xl bg-cream p-3"><span className="font-bold">{flavor.name} · ${Number(flavor.price).toFixed(2)} {!flavor.available && <small className="text-rose-700">Sold out</small>}</span><input aria-label={`${flavor.name} quantity`} type="number" min="0" max="30" disabled={!flavor.available} value={quantities[flavor.id] || ''} onChange={(event) => setQuantities((current) => ({ ...current, [flavor.id]: Number(event.target.value) }))} className="w-20 rounded-lg border border-ink/15 p-2 text-center" /></label>)}</div><button className="mt-5 min-h-12 w-full rounded-full bg-berry font-extrabold text-white">Add to queue</button></form>}
          {tab === 'Today' && <div className="rounded-3xl bg-white p-5 shadow-soft sm:p-7"><h2 className="font-display text-2xl font-black">Today at a glance</h2>{summary ? <><div className="mt-5 grid grid-cols-2 gap-3"><div className="rounded-2xl bg-cream p-4"><p className="text-sm text-ink/60">Orders served</p><p className="text-3xl font-black">{summary.ordersServed}</p></div><div className="rounded-2xl bg-mint/60 p-4"><p className="text-sm text-ink/60">Order value</p><p className="text-3xl font-black">${Number(summary.totalValue).toFixed(2)}</p></div></div><h3 className="mt-6 font-extrabold">Top flavors</h3>{summary.topFlavors.length ? summary.topFlavors.map((item) => <p key={item.flavorId} className="mt-2 flex justify-between rounded-xl bg-cream p-3"><span>{item.name}</span><strong>{item.quantity} scoops</strong></p>) : <p className="mt-2 text-ink/60">No scoops served yet today.</p>}<h3 className="mt-6 font-extrabold">Orders by hour</h3><div className="mt-3 space-y-2">{summary.ordersPerHour.filter((item) => item.orders > 0).map((item) => <div key={item.hour} className="flex items-center gap-3 text-sm"><span className="w-14">{String(item.hour).padStart(2, '0')}:00</span><div className="h-3 flex-1 rounded-full bg-cream"><div className="h-3 rounded-full bg-berry" style={{ width: `${Math.min(100, item.orders / Math.max(1, summary.ordersServed) * 100)}%` }} /></div><strong>{item.orders}</strong></div>)}</div></> : <p className="mt-4 text-ink/60">Loading today’s numbers…</p>}</div>}
        </section>
        <AiPanel onStartStop={startStop} />
      </div>
    </div>
  )
}
