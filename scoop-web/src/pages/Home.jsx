import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

export default function Home() {
  const [stop, setStop] = useState(undefined)
  useEffect(() => {
    api.get('/stops/current').then(({ data }) => setStop(data)).catch(() => setStop(null))
  }, [])
  return (
    <section className="grid items-center gap-10 md:grid-cols-[1.1fr_.9fr]">
      <div>
        <p className="mb-3 inline-flex rounded-full bg-mint px-4 py-2 text-sm font-extrabold">Scoop happiness, served fresh</p>
        <h1 className="font-display text-5xl font-black leading-[1.05] sm:text-7xl">Your next<br /><span className="text-berry">happy place.</span></h1>
        <p className="mt-5 max-w-lg text-lg leading-8 text-ink/70">Small-batch scoops and sunny-day flavors, rolling through your neighborhood.</p>
        <Link to="/menu" className="mt-7 inline-flex min-h-12 items-center rounded-full bg-berry px-7 font-extrabold text-white shadow-soft hover:bg-berry/90">Explore the menu <span className="ml-2">→</span></Link>
      </div>
      <div className="rounded-[2rem] bg-[#f7d8e2] p-6 sm:p-10">
        <div className="flex aspect-square items-center justify-center rounded-full bg-[#fff4e7] text-8xl shadow-soft sm:text-9xl" aria-label="Ice cream cone">🍦</div>
        <div className="mt-6 rounded-2xl bg-white/80 p-5">
          <div className="flex items-center justify-between">
            <p className="font-extrabold">Truck status</p>
            <span className={`rounded-full px-3 py-1 text-xs font-black ${stop ? 'bg-mint text-emerald-900' : 'bg-stone-100 text-stone-600'}`}>{stop ? 'OPEN NOW' : 'CLOSED'}</span>
          </div>
          <p className="mt-2 text-ink/70">{stop ? `${stop.name} · come say hi!` : 'We’re between stops. Check back soon!'}</p>
        </div>
      </div>
    </section>
  )
}
