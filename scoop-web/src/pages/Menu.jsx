import { useEffect, useState } from 'react'
import api from '../api/client'
import { useCart } from '../context/CartContext'

const descriptions = ['A cozy classic with real vanilla bean.', 'Deep cocoa, a little extra fudge.', 'Bright berry sweetness in every bite.', 'Cool mint with tiny chocolate crunch.', 'Buttery caramel with a pinch of sea salt.', 'Cookie dough pieces and cookie magic.', 'A tropical, dairy-free mango swirl.', 'Toasty pistachio with a dreamy finish.']

export default function Menu() {
  const [flavors, setFlavors] = useState([])
  const [error, setError] = useState('')
  const { add } = useCart()
  useEffect(() => {
    api.get('/flavors').then(({ data }) => setFlavors(data)).catch(() => setError('The menu is taking a little break. Please try again.'))
  }, [])
  return (
    <section>
      <p className="font-extrabold uppercase tracking-widest text-berry">Made for sunny days</p>
      <h1 className="mt-2 font-display text-4xl font-black sm:text-5xl">Today’s scoops</h1>
      {error && <p role="alert" className="mt-5 rounded-xl bg-rose-100 p-4 text-rose-800">{error}</p>}
      {!flavors.length && !error && <p className="mt-6 text-ink/60">Loading the flavor board…</p>}
      <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {flavors.map((flavor, index) => (
          <article key={flavor.id} className="rounded-3xl bg-white p-5 shadow-soft">
            <div className={`flex h-36 items-center justify-center rounded-2xl text-6xl ${index % 2 ? 'bg-mint/50' : 'bg-[#f8dce5]'}`}>{['🍦', '🍫', '🍓', '🌿', '🍯', '🍪', '🥭', '💚'][index % 8]}</div>
            <div className="mt-4 flex items-start justify-between gap-2">
              <div><h2 className="font-display text-xl font-black">{flavor.name}</h2><p className="mt-1 text-sm text-ink/60">{descriptions[index % descriptions.length]}</p></div>
              <strong className="whitespace-nowrap">${Number(flavor.price).toFixed(2)}</strong>
            </div>
            <button disabled={!flavor.available} onClick={() => add(flavor)} className="mt-4 min-h-12 w-full rounded-full bg-berry px-4 font-extrabold text-white disabled:cursor-not-allowed disabled:bg-stone-300">
              {flavor.available ? 'Add to cart +' : 'Sold out today'}
            </button>
          </article>
        ))}
      </div>
    </section>
  )
}
