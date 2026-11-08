import { createContext, useContext, useMemo, useState } from 'react'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const [items, setItems] = useState([])

  const add = (flavor) => setItems((current) => {
    const existing = current.find((item) => item.id === flavor.id)
    if (existing) return current.map((item) => item.id === flavor.id ? { ...item, quantity: item.quantity + 1 } : item)
    return [...current, { ...flavor, quantity: 1 }]
  })
  const changeQuantity = (id, quantity) => setItems((current) =>
    quantity <= 0 ? current.filter((item) => item.id !== id)
      : current.map((item) => item.id === id ? { ...item, quantity } : item))
  const clear = () => setItems([])
  const total = items.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)
  const value = useMemo(() => ({ items, add, changeQuantity, clear, total }), [items, total])
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export const useCart = () => useContext(CartContext)
