import { useEffect, useState } from 'react'
import api from '../api/client'

export default function AiPanel({ onStartStop }) {
  const [inventory, setInventory] = useState(null)
  const [routes, setRoutes] = useState(null)
  const [loading, setLoading] = useState(true)
  const [errors, setErrors] = useState([])

  useEffect(() => {
    let active = true
    Promise.allSettled([api.get('/ai/inventory-alerts'), api.get('/ai/route-suggestions')]).then((results) => {
      if (!active) return
      setInventory(results[0].status === 'fulfilled' ? results[0].value.data : null)
      setRoutes(results[1].status === 'fulfilled' ? results[1].value.data : null)
      setErrors(results.filter((result) => result.status === 'rejected').map(() => 'Some AI insights are unavailable right now.'))
      setLoading(false)
    })
    return () => { active = false }
  }, [])

  const urgency = { CRITICAL: 'bg-rose-100 text-rose-800', HIGH: 'bg-orange-100 text-orange-800', MEDIUM: 'bg-amber-100 text-amber-900' }
  return (
    <section className="rounded-3xl bg-[#302a38] p-5 text-white sm:p-7">
      <div className="flex items-center justify-between gap-3"><div><p className="text-sm font-bold uppercase tracking-widest text-mint">Smart scoop</p><h2 className="mt-1 font-display text-2xl font-black">AI insights</h2></div><span className="text-3xl">✨</span></div>
      {loading && <p className="mt-5 text-white/70">Gathering a little insight…</p>}
      {!loading && errors.length > 0 && <p className="mt-4 rounded-xl bg-white/10 p-3 text-sm text-white/80">{errors[0]}</p>}
      {!loading && inventory && <>
        <h3 className="mt-6 font-extrabold">Inventory watch</h3>
        {inventory.alerts?.length ? <div className="mt-3 space-y-2">{inventory.alerts.map((alert) => <div key={alert.ingredientId} className="flex flex-wrap items-center justify-between gap-2 rounded-xl bg-white/10 p-3"><span className="font-bold">{alert.name} <small className="font-normal text-white/60">{alert.stock} {alert.unit} left</small></span><span className={`rounded-full px-2 py-1 text-xs font-black ${urgency[alert.urgency]}`}>{alert.urgency}</span></div>)}</div> : <p className="mt-2 text-sm text-white/65">Everything is stocked up.</p>}
        {inventory.recommendation && <p className="mt-3 rounded-xl bg-white/10 p-3 text-sm text-white/80">{inventory.recommendation}</p>}
      </>}
      {!loading && routes && <>
        <h3 className="mt-6 font-extrabold">Route ideas</h3>
        {!routes.enoughHistory && <p className="mt-2 text-xs text-white/60">Still learning — these are the best averages so far.</p>}
        {routes.suggestions?.length ? <div className="mt-3 space-y-3">{routes.suggestions.map((suggestion) => <article key={`${suggestion.stopName}-${suggestion.latitude}-${suggestion.longitude}`} className="rounded-2xl bg-white/10 p-4"><h4 className="font-extrabold">{suggestion.stopName}</h4><p className="mt-1 text-sm text-white/65">{suggestion.reason}</p><button onClick={() => onStartStop(suggestion)} className="mt-3 min-h-10 rounded-full bg-mint px-4 text-sm font-black text-emerald-950">Go here →</button></article>)}</div> : <p className="mt-2 text-sm text-white/65">More stop history is needed before we can suggest a route.</p>}
      </>}
    </section>
  )
}
